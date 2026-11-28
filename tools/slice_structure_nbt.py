#!/usr/bin/env python3
"""Lossless vanilla structure-NBT slicer.

Splits a structure into bounded cuboids without changing palette/state indices.
Source NBT is read-only. Output slices are gzip-compressed vanilla structure NBT.
"""
from __future__ import annotations

import argparse
import copy
import gzip
import json
import math
import struct
import sys
from pathlib import Path
from typing import Any

class NBTError(ValueError):
    pass

class Reader:
    def __init__(self, data: bytes):
        self.data, self.pos = data, 0
    def take(self, n: int) -> bytes:
        if n < 0 or self.pos + n > len(self.data):
            raise NBTError("Unexpected end of NBT")
        out = self.data[self.pos:self.pos+n]
        self.pos += n
        return out
    def unpack(self, fmt: str) -> Any:
        return struct.unpack(fmt, self.take(struct.calcsize(fmt)))[0]
    def string(self) -> str:
        n = self.unpack(">H")
        return self.take(n).decode("utf-8")
    def payload(self, tag: int, depth: int = 0) -> Any:
        if depth > 128: raise NBTError("NBT nesting too deep")
        if tag == 1: return self.unpack(">b")
        if tag == 2: return self.unpack(">h")
        if tag == 3: return self.unpack(">i")
        if tag == 4: return self.unpack(">q")
        if tag == 5: return self.unpack(">f")
        if tag == 6: return self.unpack(">d")
        if tag == 7:
            n = self.unpack(">i")
            if n < 0: raise NBTError("Negative byte-array length")
            return self.take(n)
        if tag == 8: return self.string()
        if tag == 9:
            child, n = self.unpack(">B"), self.unpack(">i")
            if n < 0 or n > 20_000_000: raise NBTError("Invalid list length")
            return (child, [self.payload(child, depth+1) for _ in range(n)])
        if tag == 10:
            out = {}
            while True:
                child = self.unpack(">B")
                if child == 0: return out
                name = self.string()
                out[name] = (child, self.payload(child, depth+1))
        if tag == 11:
            n = self.unpack(">i")
            if n < 0: raise NBTError("Negative int-array length")
            return [self.unpack(">i") for _ in range(n)]
        if tag == 12:
            n = self.unpack(">i")
            if n < 0: raise NBTError("Negative long-array length")
            return [self.unpack(">q") for _ in range(n)]
        raise NBTError(f"Unsupported NBT tag {tag}")

def read_nbt(path: Path):
    raw = path.read_bytes()
    if raw[:2] == b"\x1f\x8b": raw = gzip.decompress(raw)
    r = Reader(raw)
    tag = r.unpack(">B")
    if tag != 10: raise NBTError("Root is not a compound")
    name = r.string()
    root = r.payload(10)
    if r.pos != len(raw): raise NBTError("Trailing bytes after root compound")
    return name, root

def pack_string(value: str) -> bytes:
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw

def write_payload(tag: int, value: Any) -> bytes:
    if tag == 1: return struct.pack(">b", value)
    if tag == 2: return struct.pack(">h", value)
    if tag == 3: return struct.pack(">i", value)
    if tag == 4: return struct.pack(">q", value)
    if tag == 5: return struct.pack(">f", value)
    if tag == 6: return struct.pack(">d", value)
    if tag == 7: return struct.pack(">i", len(value)) + value
    if tag == 8: return pack_string(value)
    if tag == 9:
        child, values = value
        return struct.pack(">Bi", child, len(values)) + b"".join(write_payload(child, v) for v in values)
    if tag == 10:
        out = bytearray()
        for name, (child, payload) in value.items():
            out += struct.pack(">B", child) + pack_string(name) + write_payload(child, payload)
        return bytes(out) + b"\x00"
    if tag == 11: return struct.pack(">i", len(value)) + b"".join(struct.pack(">i", v) for v in value)
    if tag == 12: return struct.pack(">i", len(value)) + b"".join(struct.pack(">q", v) for v in value)
    raise NBTError(f"Unsupported output tag {tag}")

def write_nbt(path: Path, name: str, root: dict):
    raw = b"\x0a" + pack_string(name) + write_payload(10, root)
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("wb") as f:
        with gzip.GzipFile(fileobj=f, mode="wb", mtime=0) as gz: gz.write(raw)

def get(root: dict, key: str, expected: int | None = None):
    item = root.get(key)
    if item is None: return None
    if expected is not None and item[0] != expected:
        raise NBTError(f"Root {key!r} has tag {item[0]}, expected {expected}")
    return item[1]

def set_tag(root: dict, key: str, tag: int, value: Any):
    root[key] = (tag, value)

def xyz(value: Any, field: str):
    if not isinstance(value, list) or len(value) != 3:
        raise NBTError(f"{field} must be a 3-element int array")
    return tuple(int(v) for v in value)

def slice_structure(source: Path, out_dir: Path, sx: int, sy: int, sz: int):
    name, root = read_nbt(source)
    size = xyz(get(root, "size", 11), "size")
    if any(v <= 0 for v in size): raise NBTError(f"Invalid structure size: {size}")
    blocks = get(root, "blocks", 9)
    if blocks is None: raise NBTError("Structure has no blocks list")
    child_tag, block_values = blocks
    if child_tag != 10: raise NBTError("blocks list is not a compound list")
    entities_item = root.get("entities")
    entities = get(root, "entities", 9) if entities_item else []
    if entities_item and entities_item[1][0] != 10: raise NBTError("entities list is not a compound list")
    if sx * sy * sz > 4_000_000: raise NBTError("Slice bounding volume exceeds importer limit")

    nx, ny, nz = (math.ceil(size[0]/sx), math.ceil(size[1]/sy), math.ceil(size[2]/sz))
    buckets: dict[tuple[int,int,int], list] = {}
    entity_buckets: dict[tuple[int,int,int], list] = {}
    for block in block_values:
        pos = xyz(get(block, "pos", 11), "block.pos")
        if any(pos[i] < 0 or pos[i] >= size[i] for i in range(3)):
            raise NBTError(f"Block position outside source size: {pos}")
        key = (pos[0]//sx, pos[1]//sy, pos[2]//sz)
        buckets.setdefault(key, []).append(block)
    for entity in entities:
        bp = get(entity, "blockPos", 11)
        if bp is None:
            raise NBTError("Entity lacks blockPos; refusing lossy entity slicing")
        pos = xyz(bp, "entity.blockPos")
        if any(pos[i] < 0 or pos[i] >= size[i] for i in range(3)):
            raise NBTError(f"Entity blockPos outside source size: {pos}")
        key = (pos[0]//sx, pos[1]//sy, pos[2]//sz)
        entity_buckets.setdefault(key, []).append(entity)

    manifest = {
        "source": str(source.as_posix()), "source_size": list(size),
        "slice_size_limit": [sx, sy, sz], "slice_count": 0,
        "source_block_entries": len(block_values), "source_entities": len(entities),
        "slices": []
    }
    written_blocks = written_entities = 0
    for ix in range(nx):
        for iy in range(ny):
            for iz in range(nz):
                key = (ix, iy, iz)
                ox, oy, oz = ix*sx, iy*sy, iz*sz
                actual = (min(sx, size[0]-ox), min(sy, size[1]-oy), min(sz, size[2]-oz))
                local_blocks = []
                for block in buckets.get(key, []):
                    b = copy.deepcopy(block)
                    p = xyz(get(b, "pos", 11), "block.pos")
                    set_tag(b, "pos", 11, [p[0]-ox, p[1]-oy, p[2]-oz])
                    local_blocks.append(b)
                local_entities = []
                for entity in entity_buckets.get(key, []):
                    e = copy.deepcopy(entity)
                    bp = xyz(get(e, "blockPos", 11), "entity.blockPos")
                    set_tag(e, "blockPos", 11, [bp[0]-ox, bp[1]-oy, bp[2]-oz])
                    # Entity pos is a TAG_List of doubles. Translate all three coordinates.
                    pos_item = e.get("pos")
                    if pos_item and pos_item[0] == 9:
                        list_tag, coords = pos_item[1]
                        if list_tag == 6 and len(coords) == 3:
                            e["pos"] = (9, (6, [coords[0]-ox, coords[1]-oy, coords[2]-oz]))
                    local_entities.append(e)
                out_root = copy.deepcopy(root)
                set_tag(out_root, "size", 11, list(actual))
                set_tag(out_root, "blocks", 9, (10, local_blocks))
                if entities_item: set_tag(out_root, "entities", 9, (10, local_entities))
                filename = f"hub_x{ix:02d}_y{iy:02d}_z{iz:02d}.nbt"
                write_nbt(out_dir / filename, name, out_root)
                written_blocks += len(local_blocks)
                written_entities += len(local_entities)
                manifest["slices"].append({
                    "id": f"hub_x{ix:02d}_y{iy:02d}_z{iz:02d}",
                    "file": filename, "grid": list(key), "source_offset": [ox,oy,oz],
                    "size": list(actual), "bounding_box_volume": math.prod(actual),
                    "block_entries": len(local_blocks), "entities": len(local_entities)
                })
    if written_blocks != len(block_values) or written_entities != len(entities):
        raise NBTError("Conservation check failed: slice totals differ from source")
    manifest["slice_count"] = len(manifest["slices"])
    manifest["written_block_entries"] = written_blocks
    manifest["written_entities"] = written_entities
    out_dir.mkdir(parents=True, exist_ok=True)
    (out_dir / "hub_slices.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    return manifest

def self_test():
    # Construct a tiny typed NBT structure, round-trip it, split it, and verify
    # all block coordinates reconstruct exactly from local position + offset.
    import tempfile
    root = {
        "DataVersion": (3, 3955), "size": (11, [5,3,4]),
        "palette": (9, (10, [{"Name": (8,"minecraft:stone")}])),
        "blocks": (9, (10, [
            {"pos": (11,[0,0,0]), "state": (3,0)},
            {"pos": (11,[4,2,3]), "state": (3,0)},
            {"pos": (11,[2,1,1]), "state": (3,0)}
        ]))
    }
    with tempfile.TemporaryDirectory() as td:
        base = Path(td); src = base/"source.nbt"; write_nbt(src, "", root)
        out = base/"slices"; report = slice_structure(src,out,2,2,2)
        restored = []
        for entry in report["slices"]:
            _, part = read_nbt(out/entry["file"])
            for block in get(part,"blocks",9)[1]:
                p = xyz(get(block,"pos",11),"test pos")
                restored.append(tuple(p[i]+entry["source_offset"][i] for i in range(3)))
        expected = [(0,0,0),(4,2,3),(2,1,1)]
        if sorted(restored) != sorted(expected): raise AssertionError((restored, expected))
        if report["written_block_entries"] != 3: raise AssertionError(report)
    print("self-test passed: NBT round-trip, partition conservation, coordinate reconstruction")

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", nargs="?", type=Path)
    parser.add_argument("--output", type=Path)
    parser.add_argument("--slice-x", type=int, default=96)
    parser.add_argument("--slice-y", type=int, default=64)
    parser.add_argument("--slice-z", type=int, default=96)
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        self_test(); return 0
    if not args.source or not args.output: parser.error("source and --output are required")
    if min(args.slice_x,args.slice_y,args.slice_z) <= 0: parser.error("slice dimensions must be positive")
    result = slice_structure(args.source,args.output,args.slice_x,args.slice_y,args.slice_z)
    print(json.dumps({k:v for k,v in result.items() if k != "slices"}, indent=2))
    print(f"Manifest: {args.output/'hub_slices.json'}")
    return 0

if __name__ == "__main__":
    try: raise SystemExit(main())
    except Exception as exc:
        print(f"ERROR: {type(exc).__name__}: {exc}", file=sys.stderr)
        raise SystemExit(2)
