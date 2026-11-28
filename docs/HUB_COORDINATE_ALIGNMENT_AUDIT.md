# Hub Coordinate Alignment Audit

## Audit basis

This report uses the repository's Structure Audit artifact from workflow run
[46135562556](https://github.com/xfdxchj/sky/actions/runs/46135562556), commit
`44381b6e563b854af8efb5ecf4c9b6259e870227`, generated 2026-11-28.

The source asset is `worlds/structures/the_hub_incl_carnival.nbt`.

## Confirmed NBT facts

- Declared structure size: **384 × 157 × 336** (X × Y × Z).
- Bounding-box volume: **20,256,768** blocks.
- Serialized block entries: **1,184,911**.
- Serialized block entities: **0**.
- Serialized entities: **0**.
- Block position range: **(0, 0, 0) through (383, 156, 335)**.
- Palette state-index errors: **0**.
- Out-of-bounds block positions: **0**.
- DataVersion: **2584**; the audit marks this as older than the 1.21.1 target DataVersion 3955.

These facts confirm the NBT is structurally readable. They do not establish a world-coordinate origin, the player's spawn point, or the exact position of the Village within the structure.

## Placement decision: original-world alignment is not required

The project has adopted a teleport-first architecture: Hub, public islands and adventure areas are separate destinations inside the dedicated void-based SkyBlock world. They are not being reconstructed as one continuous map using Hypixel's original global X/Z coordinates.

Consequently, the original source world's absolute coordinate transform is **not a production blocker**. The NBT-relative coordinates are sufficient to place this structure at a project-controlled target origin. The old proposed Hub origin `(0, 80, 0)` remains an unverified historical placeholder and must not be treated as a chosen production origin.

What remains necessary before player routing:

1. Preview the actual NBT geometry in a disposable test world.
2. Identify a safe standing block and facing direction within the Village area.
3. Record that spawn relative to the NBT structure origin.
4. Choose a project-controlled destination origin in the SkyBlock void world and calculate the resulting spawn from the relative offset.
5. Reserve the complete Hub bounds so they do not overlap another public destination or the private-island allocation area.

Do not assume the Wiki Village POI coordinate `(-3, 70, -85)` is the structure origin or safe spawn. It is no longer needed to derive a source-world transform.

The NBT audit remains important for dimensions, block-state compatibility and content integrity. The safe spawn must still be measured from the actual map; teleport-first architecture does not make that a guessable value.

## Runtime restoration requirements

The NBT audit reports no block-entity or entity payloads. Therefore the structure cannot restore chest inventories, signs or other block-entity data, nor NPC entities. Those must be supplied by explicit Core runtime definitions after the terrain is installed. This is consistent with the current map importer, which handles block structure slices and does not recreate missing entity payloads.

## CI status

The dedicated SkyBlock world preset and the current map resource packaging were verified by
[Fabric Build run 40](https://github.com/xfdxchj/sky/actions/runs/46156057646),
commit `6934a9a4605cd6d27ee3e3cdfbfcbc53070ea361` (**success**). That CI run verifies
Gradle build and JAR resource presence; it does not validate the Hub coordinate transform
or in-game player spawn.
