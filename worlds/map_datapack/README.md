# Local map datapack staging

This directory is the source for a **separate local datapack**, not mod-JAR resources.

Run from the repository root:

```powershell
.\gradlew.bat stageGoldMineMapDatapack
```

Gradle writes the datapack to:

`build/skyblockrpg-map-datapack`

Copy that directory into the datapacks folder of a disposable test world, for example:

`<minecraft-instance>/saves/<test-world>/datapacks/skyblockrpg-map`

Then enter the world and run `/reload`. The Core manifest registry should discover `gold_mine_modern`.

## Paste

The imported Gold Mine structure is 80 × 142 × 112 blocks (1,272,320 bounding-box blocks). Choose a completely empty target area with enough vertical clearance, then run as an operator:

```text
/skyblock structure info gold_mine_modern
/skyblock structure paste gold_mine_modern <x> <y> <z>
```

The coordinates are the target origin, not source-map coordinates. The importer checks the entire transformed bounding box and refuses to paste if **any target block is non-air**. It does not overwrite terrain. Entities are ignored. Use a disposable world and make a backup before testing.

## Scope

- Included first: Gold Mine Modern. The audit found no unknown 1.21.1 block IDs and its 1,272,320-block bounding volume is below the importer's 4,000,000 limit.
- Not included yet: Hub, Dungeon Hub, Floor VII, Spider's Den, Deep Caverns, The End, Farming Islands, Galatea. Their bounding volumes exceed the single-template limit and need chunk-safe slicing.
- Dwarven Mines and Jerry's Workshop assets in the current repository are only 16 × 160 × 16 and 16 × 88 × 16 respectively; they are not treated as complete island maps until their source files are checked.
- The NBT is not bundled into the mod JAR. The community map's redistribution permission remains unverified.
