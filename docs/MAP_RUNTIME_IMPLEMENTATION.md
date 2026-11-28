# In-mod map runtime and assembly

## Delivery model

Maps are runtime resources in the Mod JAR. Players do not install a datapack or run
structure paste commands. Community assets remain opt-in for local/private builds until
redistribution permission is confirmed:

~~~powershell
.\gradlew.bat build -PincludeCommunityMaps=true
~~~

The build slices the Hub source into vanilla structure NBT resources and generates one
logical map manifest. Gold Mine Modern is also staged as an in-JAR structure resource.

## Runtime architecture

- StructureRegistry loads individual vanilla structure manifests.
- MapRegistry loads logical maps composed of multiple structure slices.
- MapWorldData stores world-persistent generation checkpoints.
- MapAssemblyManager preflights the entire destination cuboid before placing anything,
  then places no more than one slice per server tick.
- Server startup recovers safe preflight / between-slice checkpoints. A save interrupted
  during an individual slice is marked INTERRUPTED and is not blindly replayed.

The map registry rejects missing structure references, out-of-bounds slices, overlapping
slices, and incomplete map coverage. At generation start, the assembler checks dimension,
world height, world border, structure template availability, and exact template dimensions.

## Commands

These are operator/development commands, not a player installation workflow:

~~~text
/skyblock map list
/skyblock map info <mapId>
/skyblock map generate <mapId> <originX> <originY> <originZ>
/skyblock map status <mapId>
~~~

Generation remains explicit for development and map validation. Under the teleport-first
world architecture, each map receives a project-controlled destination origin inside the
void-based SkyBlock world. The original source world's absolute X/Z coordinates are not
required and must not be inferred from Wiki POIs. The Hub's safe player spawn is a separate
structure-relative anchor that must be identified by previewing the actual NBT before
first-join routing is enabled.

The preflight checks the complete map cuboid in bounded batches (32,768 block checks per
server tick). No structures are placed unless the whole cuboid is air. After preflight,
one structure slice is placed per tick. A failed placement is recorded and logged.

## Local test procedure

1. Build with -PincludeCommunityMaps=true in a disposable test environment.
2. Create a new creative test world and install the map-enabled local JAR.
3. Confirm /skyblock map list contains hub and gold_mine.
4. Run /skyblock map info hub and confirm the expected dimensions / slice count.
5. Pick an isolated, empty target cuboid with enough vertical clearance. Do not use the
   planned production Hub coordinates until the coordinate transform has been checked.
6. Run /skyblock map generate hub <x> <y> <z>.
7. Poll /skyblock map status hub; wait for PREFLIGHT, then PLACING, then COMPLETE.
8. Confirm the map has the expected footprint and that no existing blocks were replaced.
9. Restart the world and confirm COMPLETE remains recorded and generation does not repeat.
10. In a second disposable world, place one obstruction in the target cuboid. Generation
    must fail preflight without placing any map slices.
11. Test a save/restart during PREFLIGHT and between slices. Safe checkpoints should
    resume; a checkpoint inside a slice should become INTERRUPTED and require inspection.

## Important current limitations

- The Hub's project-controlled destination origin and safe spawn offset still need to be selected after preview; source-world absolute coordinate alignment is intentionally not required.
- Hub NPCs, containers, block entities, portals and other runtime interactions must be
  restored by Core systems; the audit found no serialized block-entity/entity payloads
  in the source structure.
- Automatic first-world map assembly and first-join Hub routing are deferred until the
  destination area and safe spawn offset are configured and tested.
- Map generation has not yet been tested in Minecraft. CI only verifies the build and
  resource packaging.
- Publicly redistributing community map assets is not authorized by this project until
  explicit permission or a compatible license is established.
