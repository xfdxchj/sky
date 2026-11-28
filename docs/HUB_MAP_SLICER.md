# Hub map slicing and local import

The Hub source is a community asset. Keep the original NBT read-only and do not
commit generated slices or bundle them into the mod JAR while redistribution
permission is unverified.

## Generate slices locally

From the repository root (Python 3.10+; no third-party packages):

```powershell
python tools/slice_structure_nbt.py worlds/structures/the_hub_incl_carnival.nbt --output build/hub-slices --slice-x 96 --slice-y 64 --slice-z 96
```

The current audited Hub dimensions are 384 × 157 × 336. With 96 × 64 × 96
slice limits this produces a 4 × 3 × 4 grid (48 files). Every slice bounding
volume is at most 589,824 blocks, below the Core structure importer's
4,000,000-block cap.

The script:
- preserves the complete palette(s), block state indices, DataVersion and
  per-block NBT payloads;
- partitions blocks by their source coordinates and translates each local
  position by the slice origin;
- partitions entities by `blockPos`; if an entity has no usable blockPos, it
  stops rather than silently dropping or misplacing that entity;
- writes gzip-compressed vanilla structure NBT and a `hub_slices.json`
  manifest containing source offsets, dimensions and counts;
- verifies that the total block/entity counts are conserved.

Run the built-in synthetic round-trip test:

```powershell
python tools/slice_structure_nbt.py --self-test
```

The JSON manifest is a slicing index, not yet a Core runtime assembly manifest.
The current importer pastes one structure at a time and checks that the complete
target bounding box is air. Do not paste 48 slices manually into a valuable
world: the next integration step is a server-side assembly job that preflights
all target boxes before placing any slice, then places slices in bounded batches.

## Coordinate rule

Choose the overall target origin explicitly. For a slice with
`source_offset = [dx, dy, dz]`, its target origin is:

`[target_x + dx, target_y + dy, target_z + dz]`

The slicer does not assume that source coordinates correspond to world
coordinates, and it does not select or hard-code a target origin.

## Known restoration work

The source audit reported no block-entity NBT payloads and no entities in the
Hub structure. Chest-like block states, if present, therefore do not imply
that container inventories are preserved. NPCs, interactive block entities,
warps, region definitions and quest triggers need a separate restoration pass.
