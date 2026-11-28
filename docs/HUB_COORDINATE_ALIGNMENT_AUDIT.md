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

## Coordinate alignment: not yet established

A vanilla structure NBT stores block positions relative to its own structure origin. This asset does not carry the original world selection's absolute X/Y/Z anchor. The community-wiki Hub POI catalogue contains world-reference coordinates, but those points cannot be directly equated with the NBT-relative coordinates.

Therefore:

- Do not use `(0, 80, 0)` as a production Hub origin. It remains an old planning placeholder only.
- Do not assume the Village POI coordinate `(-3, 70, -85)` is the structure origin or the player's safe spawn.
- Do not enable first-join teleport or automatic production assembly yet.
- Do not infer a source-to-destination transform from the NBT dimensions alone.

## Required alignment procedure

Before production placement, establish a reproducible coordinate transform:

1. Open the exact source world/version from which this NBT was exported, or obtain a trustworthy coordinate-bearing reference for the same map revision.
2. Identify at least three non-collinear landmarks that are unambiguous both in the source world and in the NBT (for example, distinctive building corners or fixed road intersections).
3. Record each landmark's NBT-relative block coordinate and its source-world coordinate.
4. Derive the rotation/mirror and translation from those correspondences. Validate the transform against at least one additional landmark not used to derive it.
5. Identify a safe standing block and player-facing direction at the Village spawn, separately from the Village POI reference point.
6. Check the complete transformed Hub bounds against world height, world border, the private-island allocation region, and all other reserved areas.
7. Only then write the production map origin, spawn anchor, and first-join routing configuration.

If the exact source world is unavailable, the alignment remains unresolved and must be treated as a blocking input rather than guessed.

## Runtime restoration requirements

The NBT audit reports no block-entity or entity payloads. Therefore the structure cannot restore chest inventories, signs or other block-entity data, nor NPC entities. Those must be supplied by explicit Core runtime definitions after the terrain is installed. This is consistent with the current map importer, which handles block structure slices and does not recreate missing entity payloads.

## CI status

The dedicated SkyBlock world preset and the current map resource packaging were verified by
[Fabric Build run 40](https://github.com/xfdxchj/sky/actions/runs/46156057646),
commit `6934a9a4605cd6d27ee3e3cdfbfcbc53070ea361` (**success**). That CI run verifies
Gradle build and JAR resource presence; it does not validate the Hub coordinate transform
or in-game player spawn.
