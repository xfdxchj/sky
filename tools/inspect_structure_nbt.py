#!/usr/bin/env python3
"""Inspect vanilla Minecraft structure NBT files without third-party packages.

Usage:
  python tools/inspect_structure_nbt.py worlds/structures
  python tools/inspect_structure_nbt.py worlds/structures --output reports/structure_audit.json

The script reads gzip/zlib/uncompressed NBT and reports dimensions, palette IDs,
block counts, block-entity counts, entity counts, and coordinate bounds.
It does not modify source files.
"""
from __future__ import annotations

import argparse
import gzip
import json
import math
import struct
import zlib
from collections import Counter
from pathlib import Path
from typing import Any


class NBTError(Exception):
    pass


class Reader:
    def __init__(self, data: bytes):
        self.data = data
        self.pos = 0

    def take(self, size: int) -> bytes:
        if size < 0 or self.pos + size > len(self.data):
            raise NBTError(f"Unexpected end of NBT at byte {self.pos} (need {size})")
        out = self.data[self.pos:self.pos + size]
        self.pos += size
        return out

    def unpack(self, fmt: str) -> Any:
        size = struct.calcsize(fmt)
        return struct.unpack(fmt, self.take(size))[0]

    def string(self) -> str:
        size = self.unpack(">H")
        return self.take(size).decode("utf-8", errors="replace")

    def payload(self, tag: int, depth: int = 0) -> Any:
        if depth > 128:
            raise NBTError("NBT nesting depth exceeds 128")
        if tag == 1:
            return self.unpack(">b")
        if tag == 2:
            return self.unpack(">h")
        if tag == 3:
            return self.unpack(">i")
        if tag == 4:
            return self.unpack(">q")
        if tag == 5:
            return self.unpack(">f")
        if tag == 6:
            return self.unpack(">d")
        if tag == 7:
            length = self.unpack(">i")
            if length < 0 or length > 128_000_000:
                raise NBTError(f"Invalid byte array length: {length}")
            return self.take(length)
        if tag == 8:
            return self.string()
        if tag == 9:
            child_tag = self.unpack(">B")
            length = self.unpack(">i")
            if length < 0 or length > 20_000_000:
                raise NBTError(f"Invalid list length: {length}")
            if child_tag == 0 and length:
                raise NBTError("Non-empty list cannot contain TAG_End")
            return [self.payload(child_tag, depth + 1) for _ in range(length)]
        if tag == 10:
            result = {}
            while True:
                child_tag = self.unpack(">B")
                if child_tag == 0:
                    return result
                name = self.string()
                result[name] = self.payload(child_tag, depth + 1)
        if tag == 11:
            length = self.unpack(">i")
            if length < 0 or length > 20_000_000:
                raise NBTError(f"Invalid int array length: {length}")
            return [self.unpack(">i") for _ in range(length)]
        if tag == 12:
            length = self.unpack(">i")
            if length < 0 or length > 20_000_000:
                raise NBTError(f"Invalid long array length: {length}")
            return [self.unpack(">q") for _ in range(length)]
        raise NBTError(f"Unknown NBT tag type: {tag}")


def decode_nbt(raw: bytes) -> dict:
    if raw.startswith(b"\x1f\x8b"):
        data = gzip.decompress(raw)
        compression = "gzip"
    elif len(raw) >= 2 and raw[0] == 0x78:
        try:
            data = zlib.decompress(raw)
            compression = "zlib"
        except zlib.error:
            data = raw
            compression = "none"
    else:
        data = raw
        compression = "none"

    reader = Reader(data)
    root_tag = reader.unpack(">B")
    if root_tag != 10:
        raise NBTError(f"Root tag must be TAG_Compound (10), got {root_tag}")
    root_name = reader.string()
    root = reader.payload(10)
    return {"root_name": root_name, "root": root, "compression": compression,
            "uncompressed_bytes": len(data)}


def int_list(value: Any) -> list[int] | None:
    if isinstance(value, list) and len(value) == 3 and all(isinstance(v, (int, float)) for v in value):
        return [int(v) for v in value]
    return None


def block_state_name(palette_entry: Any) -> str:
    if not isinstance(palette_entry, dict):
        return "<invalid-palette-entry>"
    name = str(palette_entry.get("Name", "<missing-name>"))
    props = palette_entry.get("Properties")
    if isinstance(props, dict) and props:
        rendered = ",".join(f"{k}={props[k]}" for k in sorted(props))
        return f"{name}[{rendered}]"
    return name


def inspect_file(path: Path) -> dict:
    raw = path.read_bytes()
    decoded = decode_nbt(raw)
    root = decoded["root"]
    size = int_list(root.get("size"))

    # Vanilla structure NBT can contain either one palette or several
    # alternative palettes. Block.state indexes are interpreted against
    # each palette independently.
    raw_palettes = root.get("palettes")
    if isinstance(raw_palettes, list) and raw_palettes:
        palette_variants = [
            palette if isinstance(palette, list) else []
            for palette in raw_palettes
        ]
        palette_format = "palettes"
    else:
        palette = root.get("palette")
        palette_variants = [palette if isinstance(palette, list) else []]
        palette_format = "palette"

    blocks = root.get("blocks") if isinstance(root.get("blocks"), list) else []
    entities = root.get("entities") if isinstance(root.get("entities"), list) else []

    variant_counts: list[Counter[str]] = [Counter() for _ in palette_variants]
    invalid_by_variant = [0 for _ in palette_variants]
    block_entity_count = 0
    min_pos = [None, None, None]
    max_pos = [None, None, None]
    out_of_bounds_positions = 0

    for block in blocks:
        if not isinstance(block, dict):
            continue
        state_idx = block.get("state")
        if isinstance(state_idx, int):
            for index, palette in enumerate(palette_variants):
                if 0 <= state_idx < len(palette):
                    variant_counts[index][block_state_name(palette[state_idx])] += 1
                else:
                    invalid_by_variant[index] += 1
        else:
            for index in range(len(palette_variants)):
                invalid_by_variant[index] += 1

        if isinstance(block.get("nbt"), dict):
            block_entity_count += 1
        pos = int_list(block.get("pos"))
        if pos:
            for axis in range(3):
                min_pos[axis] = pos[axis] if min_pos[axis] is None else min(min_pos[axis], pos[axis])
                max_pos[axis] = pos[axis] if max_pos[axis] is None else max(max_pos[axis], pos[axis])
            if size and any(pos[axis] < 0 or pos[axis] >= size[axis] for axis in range(3)):
                out_of_bounds_positions += 1

    entity_types = Counter()
    for entity in entities:
        if not isinstance(entity, dict):
            continue
        nbt = entity.get("nbt")
        entity_id = nbt.get("id", "<unknown>") if isinstance(nbt, dict) else "<missing-nbt>"
        entity_types[str(entity_id)] += 1

    volume = math.prod(size) if size else None
    primary_counts = variant_counts[0] if variant_counts else Counter()
    block_id_counts: Counter[str] = Counter()
    for state, count in primary_counts.items():
        block_id_counts[state.split("[", 1)[0]] += count

    palette_reports = []
    for index, (palette, counts) in enumerate(zip(palette_variants, variant_counts)):
        palette_reports.append({
            "index": index,
            "palette_entries": len(palette),
            "invalid_state_indices": invalid_by_variant[index],
            "block_state_counts": dict(sorted(counts.items())),
        })

    return {
        "file": str(path),
        "file_bytes": len(raw),
        "compression": decoded["compression"],
        "uncompressed_bytes": decoded["uncompressed_bytes"],
        "root_name": decoded["root_name"],
        "data_version": root.get("DataVersion"),
        "size": size,
        "bounding_box_volume": volume,
        "palette_format": palette_format,
        "palette_entries": len(palette_variants[0]) if palette_variants else 0,
        "alternate_palette_count": max(0, len(palette_variants) - 1),
        "palette_variants": palette_reports,
        "block_entries": len(blocks),
        "block_entities": block_entity_count,
        "entities": len(entities),
        "entity_types": dict(sorted(entity_types.items())),
        "block_position_min": min_pos if all(v is not None for v in min_pos) else None,
        "block_position_max": max_pos if all(v is not None for v is not None for v in max_pos) else None,
        "out_of_bounds_block_positions": out_of_bounds_positions,
        "invalid_state_indices": invalid_by_variant[0] if invalid_by_variant else len(blocks),
        "block_state_counts": dict(sorted(primary_counts.items())),
        "top_block_ids": dict(block_id_counts.most_common(30)),
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path, help="NBT file or directory containing .nbt files")
    parser.add_argument("--output", type=Path, help="Optional JSON report output path")
    args = parser.parse_args()

    files = [args.input] if args.input.is_file() else sorted(args.input.rglob("*.nbt"))
    if not files:
        parser.error(f"No .nbt files found under {args.input}")

    results = []
    for path in files:
        try:
            results.append({"ok": True, **inspect_file(path)})
        except Exception as exc:
            results.append({"ok": False, "file": str(path), "error": f"{type(exc).__name__}: {exc}"})

    report = {
        "tool": "inspect_structure_nbt.py",
        "file_count": len(files),
        "successful": sum(1 for item in results if item.get("ok")),
        "failed": sum(1 for item in results if not item.get("ok")),
        "structures": results,
    }
    rendered = json.dumps(report, ensure_ascii=False, indent=2)
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered + "\n", encoding="utf-8")
        print(f"Wrote {args.output} ({report['successful']}/{report['file_count']} parsed)")
    else:
        print(rendered)
    return 0 if report["failed"] == 0 else 2


if __name__ == "__main__":
    raise SystemExit(main())
