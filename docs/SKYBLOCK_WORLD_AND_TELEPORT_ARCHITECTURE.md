# SkyBlock World and Teleport Architecture

## Decision

The project will use a **single dedicated SkyBlock save/world preset with void terrain**, and model Hub, public islands, adventure areas, and private islands as separate playable destinations connected by the existing warp/portal systems.

This is a teleport-first game world, not one continuous overworld map. Players should not need to walk from Hub to Gold Mine, Spider's Den, The End, or every other destination. Each destination is installed as its own map area and entered through a warp, portal, NPC, menu, or quest unlock.

The existing RegionRegistry, WarpRegistry/WarpManager, ConnectionRegistry, StructureRegistry, MapRegistry, and NPC/quest systems remain the shared Core infrastructure. Do not create parallel managers for these features.

## World layout model

### One SkyBlock world, many separated map areas

- The selected `skyblockrpg:skyblock` world preset supplies the void-based SkyBlock world. It is not ordinary Overworld terrain.
- Hub and each public destination receive a project-controlled placement origin inside this void world.
- A map's placement origin is an internal destination anchor, not a recovered coordinate from Hypixel's source world.
- Players move between destinations through WarpManager. Physical roads/bridges are only built when they are part of the intended local map design, not as mandatory connections between every island.
- Public destination areas must have non-overlapping bounds and safe landing points.
- The private-island allocator reserves a separate coordinate area in this same SkyBlock world. Each player's island remains independently protected and persistent.
- Do not use the Nether or End as accidental storage dimensions for public maps; their vanilla behavior should not be part of the SkyBlock destination graph unless a later design explicitly requires it.

This keeps runtime simple: one server world, multiple map regions, and teleport destinations. It avoids dynamic per-player dimension registration and avoids having to reproduce Hypixel's original global coordinate layout.

### Destination graph

Initial logical destinations:

| Destination | Role | Entry |
|---|---|---|
| Hub | Public social/service center; first join destination | World spawn |
| Private Island | Per-player persistent build/home area | Hub menu/NPC/portal; explicit create action if absent |
| Gold Mine | Mining progression | Hub warp/portal |
| Deep Caverns | Mining progression | Gold Mine / warp |
| Farming Islands | Farming progression | Hub warp |
| The Park | Foraging progression | Hub warp |
| Spider's Den | Combat progression | Hub warp |
| The End | Combat progression | Hub warp |
| Dwarven Mines | Mining progression | Unlock + warp |
| Dungeon Hub | Dungeon entry lobby | Hub warp; dungeon extension owns dungeon gameplay |
| Other maps | Future content | Locked/hidden until implemented |

The table describes the navigation model, not a claim that all listed maps are already integrated.

## Player flow

1. A new player enters the SkyBlock world at the Hub spawn.
2. The player remains in Hub until choosing another destination.
3. The player can use Hub NPCs, portals, or the SkyBlock menu to travel.
4. Choosing Private Island:
   - If no island exists, show a clear Create Private Island action.
   - Only the explicit action creates the island.
   - On successful creation, teleport the player to its safe spawn.
   - If an island already exists, teleport to the saved island.
5. Rejoining the world returns the player to Hub, not automatically to the private island.
6. A Hub menu/NPC/portal always allows return to the existing private island.
7. Warp to an unavailable/unbuilt destination must show a locked/not-yet-available message; never teleport to an empty guessed coordinate.

## Map asset placement

The source-world absolute coordinates of a community NBT are not required to place it in this architecture.

For each map asset, the project only needs:
- verified source/version/provenance and redistribution permission status;
- NBT dimensions and compatibility audit;
- a project-controlled target origin in the SkyBlock void world;
- a safe landing block and facing direction identified from the actual structure preview;
- logical regions, warps, NPCs and other runtime interactions.

The source map's internal structure-relative coordinates remain useful. The old task of deriving a transform from the original world's absolute coordinates to the NBT is no longer a blocker unless a feature explicitly depends on original world coordinates.

The Hub's safe spawn still must be identified against the actual NBT geometry. Do not guess it from the Wiki POI coordinate. The NBT has no entity or block-entity payloads, so NPCs, containers and interactive block entities must be restored by Core definitions where needed.

Map import remains incremental and protected. Use the existing MapAssemblyManager preflight and slice placement. Each map manifest should declare its own destination origin/bounds; do not merge every map into one giant schematic or require a single contiguous footprint.

## Migration from the old coordinate plan

- Retire the idea that Hub must be anchored at its original Hypixel world coordinates.
- Retire the requirement to prove a transform between Hub NBT-relative coordinates and source-world absolute coordinates before placement.
- Keep the NBT structure audit; it is still needed for dimensions, block states, compatibility and content integrity.
- Replace the old shared-overworld coordinate collision check with a destination-area allocator/reservation registry for public maps and the private-island grid.
- Move PrivateIslandManager away from unconditional use of `server.getOverworld()` only after the SkyBlock world resolver is implemented and the world preset's runtime key is verified.
- Ensure StarterGraveyardManager no longer creates test content in an ordinary Overworld. It should become a clearly marked test destination or be removed from production startup.
- Keep first-join Hub routing and explicit island creation as settled requirements.

## Implementation sequence

### WT-0 — World resolver and destination model
- Add one Core resolver for the active SkyBlock world; fail clearly if the selected world is not the SkyBlock preset/world configuration.
- Define destination metadata: id, map id, target origin, safe spawn, facing, bounds, availability/lock state.
- Validate non-overlap between destination bounds and reserved private-island allocation space.
- Keep WarpManager as the only teleport execution path, adding safe-landing validation.

### WT-1 — Hub as the first destination
- Place Hub map into its reserved area in the SkyBlock void world.
- Preview the actual NBT and identify the Village spawn block/facing.
- Register Hub spawn and Hub regions.
- Recreate required NPCs/interactions from Core data.
- Route first join/world spawn to Hub. Do not auto-create a private island.

### WT-2 — Private Island
- Allocate and persist a per-player island inside the reserved private-island area of the SkyBlock world.
- Keep the explicit Create action and persistent island record.
- Add Hub return-home entry.
- Verify protection, spawn safety and persistence.

### WT-3 — Public destinations
- Register each imported map as a separate destination area.
- Add warps and unlock requirements.
- Integrate one destination at a time; do not require physical map continuity.
- Add map-specific region/NPC/mob/quest content after its terrain is placed.

### WT-4 — Playable navigation loop
- Hub menu lists only available destinations and shows locked states for future areas.
- Portals/NPCs and menu entries call the same WarpManager.
- Test all routes, return-to-Hub behavior, rejoin behavior, unavailable maps, and world-save persistence.

## Explicit non-goals

- Reconstructing Hypixel's original global X/Z coordinate system.
- Joining all public islands into one physically continuous terrain.
- Generating every map as one enormous NBT.
- Automatically teleporting a new player to a private island.
- Using vanilla Overworld terrain as a fallback SkyBlock surface.
