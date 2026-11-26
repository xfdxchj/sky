# P5 — Island Map and Floating-Island Foundation

## Goal

Move from abstract region/warp coordinates to a map model that can describe and place floating islands in the existing Overworld. The project remains an Integrated Server mod and does not introduce a separate SkyBlock dimension.

## Implemented in P5.0

- JSON island manifests under `data/skyblockrpg/skyblock/islands/*.json`.
- Each island records a stable ID, display name, target dimension, center, surface height, elliptical footprint, thickness, spawn point, region/warp references, and block palette.
- Registry validation checks dimensions, block IDs, duplicate IDs, and referenced region/warp IDs.
- Server-side procedural island generator creates an elliptical floating landmass with a deterministic low-amplitude surface profile and tapered underside.
- Generation is an explicit operator action; it never runs automatically when an existing world is opened.
- Existing non-air blocks are preserved. World-border and build-height bounds are checked. A footprint safety cap prevents accidental huge generation.
- Island visit uses the configured spawn point and the normal server teleport path.
- The starter island included here is explicitly a project fixture, not a reconstruction of Hypixel Private Island or Hub.

## Implemented in P5.1

- Native Minecraft structure NBT templates loaded through StructureTemplateManager.
- JSON manifests define template ID, target dimension, origin, rotation, mirror, safety volume and provenance.
- Operator-only paste command with build-height/world-border checks and all-air preflight.
- Entity placement is disabled; a tiny project-authored NBT marker is included for testing.
- Direct .schem parsing and actual Hypixel map assets remain deferred.

## Implemented in P5.2

- JSON-driven bridge and portal connection definitions link existing source and destination warp nodes.
- Bridge geometry is validated against its warp endpoints; portal entrances are validated against the source warp and activation radius.
- Operator-triggered bridge deck construction performs complete all-air preflight and checks world height, world border and a strict block-count limit.
- Portal travel is proximity-gated and delegates destination checks and discovery requirements to WarpManager.
- Connection route queries expose inbound/outbound adjacency between warp nodes.
- Starter bridge, portal and outpost entries are project test fixtures, not Hypixel map coordinates. Physical portal blocks/effects and shortest-path navigation remain deferred.

## Commands

- `/skyblock map` — report map format version, loaded island count and current dimension.
- `/skyblock island list`
- `/skyblock island info <id>`
- `/skyblock island generate <id>` — permission level 2; generates only into air.
- `/skyblock island visit <id>`

## Map asset contract

P5 defines the metadata layer, not a bundled Hypixel map download. Future map packs should provide:
1. A versioned manifest with source project, target Minecraft version, coordinate origin and dimension.
2. Island definitions with stable IDs and explicit bounds.
3. Region and warp JSON whose IDs match the island manifest.
4. Structure/schematic assets kept separate from gameplay Java code.
5. A provenance/license record for every imported or converted map asset.

Do not label generated fixture terrain as Hypixel geometry. Actual Hub, Private Island, Spider's Den, The End and other area geometry should be imported only from a verified source and with redistribution rights checked.

## Deferred

- Direct Sponge/WorldEdit .schem parsing and bulk conversion tooling (native structure NBT placement is implemented in P5.1).
- Island protection and build permissions.
- Physical portal blocks, particles/sounds, island protection and map GUI.
- Actual Hypixel terrain/building assets and verified region coordinates.
- Per-world map version migration and asset pack update tooling.
