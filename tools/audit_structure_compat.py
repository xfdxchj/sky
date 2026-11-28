#!/usr/bin/env python3
"""Compare structure palettes with the Minecraft Java 1.21.1 block registry."""
from __future__ import annotations
import argparse, json
from pathlib import Path

TARGET_DATA_VERSION = 3955

def parse_state(rendered: str):
    if "[" not in rendered:
        return rendered, {}
    name, raw = rendered.split("[", 1)
    raw = raw[:-1] if raw.endswith("]") else raw
    props = {}
    for pair in raw.split(","):
        if "=" in pair:
            key, value = pair.split("=", 1)
            props[key] = value
    return name, props

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path)
    parser.add_argument("--registry", type=Path, required=True)
    parser.add_argument("--json", type=Path, required=True)
    parser.add_argument("--markdown", type=Path, required=True)
    args = parser.parse_args()

    report = json.loads(args.report.read_text(encoding="utf-8"))
    registry_data = json.loads(args.registry.read_text(encoding="utf-8"))
    registry = {f"minecraft:{b['name']}": b for b in registry_data if isinstance(b, dict) and b.get("name")}
    results, total_unknown, total_invalid = [], 0, 0

    for item in report.get("structures", []):
        if not item.get("ok"):
            continue
        unknown, invalid = [], []
        variants = item.get("palette_variants") or [{"block_state_counts": item.get("block_state_counts", {})}]
        for variant in variants:
            for rendered, count in variant.get("block_state_counts", {}).items():
                name, props = parse_state(rendered)
                block = registry.get(name)
                if block is None:
                    unknown.append({"state": rendered, "count": count})
                    continue
                allowed = {s.get("name"): set(map(str, s.get("values", []))) for s in block.get("states", [])}
                allowed_names = set(allowed)
                for key, value in props.items():
                    if key not in allowed_names:
                        invalid.append({"state": rendered, "property": key, "value": value, "reason": "unknown_property", "count": count})
                    elif allowed[key] and value not in allowed[key]:
                        invalid.append({"state": rendered, "property": key, "value": value, "reason": "unknown_value", "count": count})
                for key in allowed_names - set(props):
                    invalid.append({"state": rendered, "property": key, "value": None, "reason": "missing_property", "count": count})
        total_unknown += len(unknown)
        total_invalid += len(invalid)
        results.append({
            "file": Path(item.get("file", "")).name,
            "data_version": item.get("data_version"),
            "target_data_version": TARGET_DATA_VERSION,
            "older_than_target": isinstance(item.get("data_version"), int) and item["data_version"] < TARGET_DATA_VERSION,
            "unknown_block_id_entries": unknown,
            "invalid_block_state_entries": invalid,
            "unknown_block_id_kinds": len(unknown),
            "invalid_block_state_kinds": len(invalid),
            "block_entities": item.get("block_entities", 0),
            "block_entity_types": item.get("block_entity_types", {}),
            "entities": item.get("entities", 0),
            "entity_types": item.get("entity_types", {}),
        })

    output = {
        "target": "Minecraft Java 1.21.1",
        "target_data_version": TARGET_DATA_VERSION,
        "registry_source": "PrismarineJS/minecraft-data data/pc/1.21.1/blocks.json",
        "structure_count_checked": len(results),
        "unknown_block_id_kinds_total": total_unknown,
        "invalid_block_state_kinds_total": total_invalid,
        "structures": results,
        "limitations": [
            "Registry comparison validates block IDs and declared block-state properties, not full game loading.",
            "DataVersion older than target is a data-fixer/upgrade review flag, not by itself proof of incompatibility.",
            "Block-entity payload schemas and entity behavior require a real 1.21.1 load test.",
            "This report does not validate datapacks, dimensions, world metadata, or redistribution rights."
        ]
    }
    args.json.parent.mkdir(parents=True, exist_ok=True)
    args.json.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    lines = [
        "# Minecraft 1.21.1 Structure Compatibility Audit", "",
        f"- Structures checked: {len(results)}",
        f"- Unknown block ID kinds: {total_unknown}",
        f"- Invalid block-state kinds: {total_invalid}", "",
        "| Structure | DataVersion | Older than target | Unknown IDs | Invalid states | Block entities | Entities |",
        "|---|---:|---:|---:|---:|---:|---:|"
    ]
    for r in results:
        lines.append(f"| {r['file']} | {r['data_version']} | {'yes' if r['older_than_target'] else 'no/unknown'} | {r['unknown_block_id_kinds']} | {r['invalid_block_state_kinds']} | {r['block_entities']} | {r['entities']} |")
    lines += ["", "## Interpretation", "",
        "- Unknown IDs or invalid properties require investigation before import.",
        "- Older DataVersion is a review flag; Minecraft's data fixer may upgrade old data.",
        "- Non-zero block entities/entities require separate payload and behavior review.",
        "- A clean registry comparison does not replace opening and saving a copy in Minecraft 1.21.1.",
        "", "## Details", ""]
    for r in results:
        lines += [f"### {r['file']}", ""]
        if r["unknown_block_id_entries"]:
            lines.append("Unknown block IDs:")
            lines.extend(f"- {x['state']} ({x['count']})" for x in r["unknown_block_id_entries"])
        if r["invalid_block_state_entries"]:
            lines.append("Invalid block states:")
            lines.extend(f"- {x['state']}: {x['reason']} {x['property']}={x['value']}" for x in r["invalid_block_state_entries"])
        if not r["unknown_block_id_entries"] and not r["invalid_block_state_entries"]:
            lines.append("- No unknown block IDs or invalid property values detected.")
        if r["block_entity_types"]:
            lines.append(f"- Block entity types: {json.dumps(r['block_entity_types'], ensure_ascii=False)}")
        if r["entity_types"]:
            lines.append(f"- Entity types: {json.dumps(r['entity_types'], ensure_ascii=False)}")
        lines.append("")
    args.markdown.parent.mkdir(parents=True, exist_ok=True)
    args.markdown.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Compatibility audit: {len(results)} structures; unknown IDs={total_unknown}; invalid states={total_invalid}")
    # Keep the workflow green for review flags; parsing/network failures still fail the job.
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
