# Hub NPC placement runtime

## Why placement is manual

The Hub structure NBT contains no entity records. The Hub POI catalogue stores published reference-world coordinates, while the imported NBT has its own local coordinate origin. Until a verified transform and in-map spawn anchors exist, those reference coordinates must not be used to spawn NPCs.

## Current implementation

- `/skyblock hub place-npc <poiId> <npcId>` is operator-only.
- The command requires the Hub map assembly to be `COMPLETE`.
- The operator must stand at the intended NPC position inside the assembled Hub map, on a solid floor with two clear blocks above.
- The POI must exist and have category `npc`; the NPC definition must exist.
- The spawned entity receives both its normal SkyBlock NPC tag and a Hub POI tag. Minecraft persists the entity with its saved chunk.
- A duplicate with the same POI tag in the nearby loaded area is rejected.
- The `banker_npc` POI can use `hub_banker`, which opens the bank deposit/withdraw interaction.\n- The `blacksmith_npc` POI can use `hub_blacksmith`, which opens the reforge selection GUI. The current reforge data remains test-fixture-only; this is a connected service path, not a complete Hypixel catalogue.

## Initial NPC definitions

| POI ID | NPC definition | Current interaction |
|---|---|---|
| `banker_npc` | `hub_banker` | Existing bank balance interaction |
| `bartender_npc` | `hub_bartender` | Display/interact only |
| `bazaar_agent_npc` | `hub_bazaar_agent` | Display/interact only; Bazaar system is not implemented yet |
| `blacksmith_npc` | `hub_blacksmith` | Reforge GUI; current reforge pool is test-fixture-only |
| `maddox_npc` | `hub_maddox` | Display/interact only; Slayer service integration is pending |
| `angler_angus_npc` | `hub_angler_angus` | Display/interact only; Fishing extension integration is pending |
| `scoop_npc` | `hub_scoop` | Display/interact only |

## Example

After importing Hub and standing on the exact floor tile where Banker should stand:

```mcfunction
/skyblock hub place-npc banker_npc hub_banker
```

Do not use the Wiki POI XYZ values as the placement coordinates. They are reference metadata, not local NBT positions. NPCs must be placed after inspecting the imported Hub map in-game.

## Current service status (2026-11-28)

| POI | NPC | Interaction |
|---|---|---|
| `banker_npc` | `hub_banker` | Functional purse/bank deposit and withdrawal menu |
| `blacksmith_npc` | `hub_blacksmith` | Functional reforge menu; only test fixture reforge entries are currently bundled |
| `bartender_npc` | `hub_bartender` | Display/interact shell |
| `bazaar_agent_npc` | `hub_bazaar_agent` | Display/interact shell; Bazaar service pending |
| `maddox_npc` | `hub_maddox` | Display/interact shell; Slayer service pending |
| `angler_angus_npc` | `hub_angler_angus` | Display/interact shell; Fishing extension integration pending |
| `scoop_npc` | `hub_scoop` | Display/interact shell |

The world-persistent placement registry prevents duplicate placement for a POI even when its entity chunk is unloaded or the server restarts. `/skyblock hub npc list` shows registered UUIDs; `/skyblock hub npc forget <poiId>` clears a stale record only after the old entity has been manually removed. The registry stores absolute world coordinates (not map-local coordinates).


## Persistent placement registry (2026-11-28)

Hub NPC placement now records one placement per POI in world-persistent state `skyblockrpg_hub_npcs`. Each record stores the POI ID, NPC definition ID, entity UUID, dimension and map-local position. A second placement is rejected even when the original entity's chunk is unloaded or the world has been restarted.

Admin commands:

- `/skyblock hub npc list` — list registered placements and UUIDs.
- `/skyblock hub npc forget <poiId>` — remove a stale placement record after the old entity has been manually removed.

The registry intentionally does not silently respawn missing entities: an unloaded chunk and a deleted entity cannot safely be distinguished without loading/altering world chunks. Administrators must remove the old entity first, then clear its record before replacing it. This closes the duplicate-across-unloaded-chunks hole, but automatic entity reconciliation and a global removal workflow remain future work.


## Measured runtime POI anchors

Static POI XYZ values remain reference metadata and are never treated as coordinates in the imported NBT. The project now stores measured anchors separately in world-persistent state `skyblockrpg_hub_poi_anchors`.

After the Hub map is assembled, an operator can stand at the actual in-map location (on a solid floor with two clear blocks) and run:

```mcfunction
/skyblock hub anchor village
/skyblock hub anchor banker_npc
/skyblock hub anchor clear banker_npc
```

The command rejects positions outside the assembled Hub bounds, in another dimension, or before map assembly is COMPLETE. Anchors store dimension, exact position and facing. They can be inspected with `/skyblock hub info <poiId>`, listed with `/skyblock hub list`, and used by `/skyblock hub locate <poiId>` and `/skyblock hub nearest`. Anchors are project-local measurements; they do not claim to be derived from Wiki coordinates.

The anchor workflow is implemented but the actual Hub map still needs in-game inspection and manual measurement. No production Village, NPC or service coordinates are bundled as guessed values.


## Placing NPCs at measured POI anchors

After recording a runtime anchor for an NPC POI, operators can place its configured NPC directly at that saved position without standing there again:

```mcfunction
/skyblock hub place-npc-at-anchor banker_npc hub_banker
```

The command requires the player to be in the anchor's dimension, verifies the anchor remains inside the completed Hub map bounds, checks the floor and two-block clearance, and uses the recorded facing. It shares the same persistent duplicate guard as manual placement. The existing `/skyblock hub place-npc <poiId> <npcId>` command remains available for manual placement when no anchor has been recorded.
