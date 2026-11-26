# P5 Test Plan

## Build

```bash
git fetch origin
git checkout p5-island-map-foundation
gradlew build
```

## Registry

1. Start a test world and confirm one island definition loads.
2. Run `/skyblock map` and `/skyblock island list`.
3. Run `/skyblock island info starter_island_fixture`; check dimension, footprint, spawn, region and warp IDs.
4. Confirm malformed JSON, unknown block IDs and broken region/warp references are logged and skipped.

## Generation

1. Use a fresh test world or a large empty area in the Overworld.
2. Run `/skyblock island generate starter_island_fixture` as an operator.
3. Confirm an elliptical floating island is generated around X=0, Z=0 at approximately Y=96.
4. Confirm top/soil/core palette is grass_block/dirt/stone and the underside tapers.
5. Confirm existing non-air blocks are not overwritten.
6. Confirm running generation a second time does not replace existing blocks and reports skipped blocks.
7. Confirm the command refuses non-Overworld dimensions, out-of-height definitions and oversized footprints.

## Visit and region

1. Run `/skyblock island visit starter_island_fixture`.
2. Confirm the player is teleported to the configured spawn point.
3. Walk around the island and confirm the region actionbar reports first discovery once.
4. Confirm `/skyblock warp starter_island_spawn` reaches the same spawn.

## Regression

- Existing region, warp, NPC, quest, GUI, inventory, accessory, reforge, combat and persistence systems remain available.
- Opening a pre-existing world must not generate or overwrite map terrain automatically.
- The fixture must remain clearly identified as project test content, not Hypixel map data.
