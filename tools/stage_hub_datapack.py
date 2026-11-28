#!/usr/bin/env python3
"""Stage locally generated Hub slices as a Minecraft 1.21.1 datapack.

This does not modify the source NBT or publish community map assets.
"""
from __future__ import annotations
import argparse
import json
import shutil
from pathlib import Path

SOURCE_URL = "https://hypixel.net/threads/updated-hypixel-skyblock-world-downloads-for-every-single-island.5140617/"
LICENSE_NOTE = "Community asset; redistribution permission not verified. Local datapack only."

def stage(index_path: Path, output: Path, dimension: str, source_url: str, license_note: str) -> int:
    index = json.loads(index_path.read_text(encoding="utf-8"))
    slices = index.get("slices")
    if not isinstance(slices, list) or not slices:
        raise ValueError("hub_slices.json has no slices")
    source_dir = index_path.parent
    templates = output / "data/skyblockrpg/structures"
    definitions = output / "data/skyblockrpg/skyblock/structures"
    templates.mkdir(parents=True, exist_ok=True)
    definitions.mkdir(parents=True, exist_ok=True)
    commands = ["# Replace <X> <Y> <Z> with one overall target origin."]
    for part in slices:
        sid = part["id"]
        source = source_dir / part["file"]
        if not source.is_file():
            raise FileNotFoundError(source)
        size = part["size"]
        volume = int(part["bounding_box_volume"])
        if volume != int(size[0]) * int(size[1]) * int(size[2]) or volume > 4_000_000:
            raise ValueError(f"Invalid slice volume for {sid}: {volume}")
        shutil.copy2(source, templates / f"{sid}.nbt")
        definition = {
            "id": sid, "name": f"Hypixel Hub slice {sid}",
            "template": f"skyblockrpg:{sid}", "dimension": dimension,
            "origin_x": 0, "origin_y": 0, "origin_z": 0,
            "rotation": "none", "mirror": "none", "max_blocks": volume,
            "source": "Community Hypixel SkyBlock Hub world download",
            "source_url": source_url, "license": license_note, "test_fixture": False
        }
        (definitions / f"{sid}.json").write_text(
            json.dumps(definition, indent=2) + "\n", encoding="utf-8")
        dx, dy, dz = part["source_offset"]
        commands.append(
            f"/skyblock structure paste {sid} <X+({dx})> <Y+({dy})> <Z+({dz})>"
        )
    (output / "pack.mcmeta").write_text(json.dumps({
        "pack": {"pack_format": 48, "description": "Local Hypixel Hub slices (1.21.1)"}
    }, indent=2) + "\n", encoding="utf-8")
    (output / "paste_commands.txt").write_text("\n".join(commands) + "\n", encoding="utf-8")
    (output / "README.txt").write_text(
        "Install this folder in a disposable Minecraft 1.21.1 test world's datapacks directory, then run /reload.\n"
        "The per-slice manifests are registered by SkyBlock RPG Core.\n"
        "Use paste_commands.txt as a coordinate reference; replace the placeholders with an explicit overall origin.\n"
        "Every slice target bounding box must be entirely air. Paste slices in the listed order.\n"
        "This is a manual first-stage import. A server-side preflighted batch paste command is not implemented yet.\n"
        "Community redistribution permission is unverified: keep this datapack local and do not redistribute it.\n",
        encoding="utf-8")
    return len(slices)

def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("index", type=Path, help="path to hub_slices.json")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--dimension", default="minecraft:overworld")
    parser.add_argument("--source-url", default=SOURCE_URL)
    parser.add_argument("--license-note", default=LICENSE_NOTE)
    args = parser.parse_args()
    count = stage(args.index, args.output, args.dimension, args.source_url, args.license_note)
    print(f"Staged {count} Hub slice structures into {args.output}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
