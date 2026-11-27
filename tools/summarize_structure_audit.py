#!/usr/bin/env python3
"""Summarize inspect_structure_nbt.py JSON output for map intake decisions.

This tool never invents source coordinates or target placement coordinates. It
only derives structural facts present in the audit report.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any


def fmt_size(value: Any) -> str:
    if not isinstance(value, list) or len(value) != 3:
        return "unknown"
    return " × ".join(str(v) for v in value)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path)
    parser.add_argument("--markdown", type=Path, help="Write a Markdown summary")
    args = parser.parse_args()

    data = json.loads(args.report.read_text(encoding="utf-8"))
    structures = data.get("structures", [])
    lines = [
        "# Structure Audit Summary",
        "",
        f"- Files: {data.get('file_count', len(structures))}",
        f"- Parsed: {data.get('successful', 0)}",
        f"- Failed: {data.get('failed', 0)}",
        "",
        "| File | Size (X × Y × Z) | Volume | Blocks | Block entities | Entities | Palette errors | Out-of-bounds |",
        "|---|---:|---:|---:|---:|---:|---:|---:|",
    ]

    warnings: list[str] = []
    for item in structures:
        name = Path(item.get("file", "<unknown>")).name
        if not item.get("ok"):
            lines.append(f"| {name} | — | — | — | — | — | — | — |")
            warnings.append(f"- **{name}**: parse failed: {item.get('error', 'unknown error')}")
            continue

        size = fmt_size(item.get("size"))
        volume_value = item.get("bounding_box_volume")\n        volume = f"{volume_value:,}" if isinstance(volume_value, int) else "unknown"
        blocks = item.get("block_entries", 0)
        block_entities = item.get("block_entities", 0)
        entities = item.get("entities", 0)
        palette_errors = item.get("invalid_state_indices", 0)
        oob = item.get("out_of_bounds_block_positions", 0)
        lines.append(
            f"| {name} | {size} | {volume} | {blocks:,} | {block_entities:,} | "
            f"{entities:,} | {palette_errors:,} | {oob:,} |"
        )

        if not item.get("size"):
            warnings.append(f"- **{name}**: missing or malformed size tag.")
        if palette_errors:
            warnings.append(f"- **{name}**: {palette_errors} block entries have invalid state indexes in the primary palette.")
        if oob:
            warnings.append(f"- **{name}**: {oob} block positions fall outside the declared size.")
        if block_entities:
            warnings.append(f"- **{name}**: contains {block_entities} block entities; inspect their data before import.")
        if entities:
            warnings.append(f"- **{name}**: contains {entities} entities; current Core importer ignores structure entities.")

        variants = item.get("palette_variants", [])
        if len(variants) > 1:
            invalid = [v.get("invalid_state_indices", 0) for v in variants]
            if len(set(invalid)) > 1:
                warnings.append(f"- **{name}**: alternate palettes have different invalid-index counts: {invalid}.")

    lines.extend(["", "## Review flags", ""])
    lines.extend(warnings or ["- No structural audit flags were detected. This does not prove game compatibility or redistribution permission."])
    lines.extend([
        "",
        "## Import decision notes",
        "",
        "- These measurements describe NBT structure payloads, not complete world saves.",
        "- Source coordinates and destination coordinates must be established separately.",
        "- A low bounding-box volume does not guarantee low runtime cost; block entities and entities need review.",
        "- Current Core structure placement requires the entire destination bounding box to be air and ignores structure entities.",
        "- Do not create production manifests from guessed origins.",
        "",
    ])

    rendered = "\n".join(lines)
    if args.markdown:
        args.markdown.parent.mkdir(parents=True, exist_ok=True)
        args.markdown.write_text(rendered, encoding="utf-8")
        print(f"Wrote {args.markdown}")
    else:
        print(rendered)
    return 0 if data.get("failed", 0) == 0 else 2


if __name__ == "__main__":
    raise SystemExit(main())
