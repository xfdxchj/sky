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
| `blacksmith_npc` | `hub_blacksmith` | Display/interact only; Hub Blacksmith services are not implemented yet |
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

The placement duplicate guard currently checks only the nearby loaded area. It is not a global POI registry and does not provide cross-chunk duplicate prevention or an admin remove/repair workflow.
