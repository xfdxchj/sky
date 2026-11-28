# Hub NPC Services

This document records the current service integrations for the Hub NPC runtime.

## Banker

The Hub Banker definition uses `action: "bank"`. Interacting with the placed NPC opens the server-authoritative Bank menu.

The menu displays the current purse and bank balances and supports:

- Deposit 100 coins
- Deposit 1,000 coins
- Deposit the entire purse
- Withdraw 100 coins
- Withdraw 1,000 coins
- Withdraw the maximum amount that fits in the purse

All transfers call `BankManager`, which delegates validation to the player profile and marks the profile dirty after a successful transfer. The same menu is available from the SkyBlock menu.

## Blacksmith

The Hub Blacksmith definition uses `action: "reforge"`. Interacting opens a menu listing the five equipment slots. Each slot displays its equipped item, current reforge, compatible reforge count, and current cost range.

- Empty slots and non-reforgeable items cannot be selected.
- Clicking an eligible slot invokes the existing `ReforgeManager.roll` service.
- The selected reforge is random from the compatible registry pool; it replaces the current reforge.
- The menu refreshes after each attempt and reports the result.

**Content limitation:** the current repository's bundled reforge JSON entries are marked `test_fixture: true` (for example, `test_fierce` and `test_titanic`). The UI explicitly labels a pool made entirely of test fixtures. This connects the NPC to the real reforge code path, but does not mean the final Hypixel reforge catalogue is complete.

## Placement

After the Hub map is assembled and the operator is standing at the intended map-local NPC position:

```text
/skyblock hub place-npc banker_npc hub_banker
/skyblock hub place-npc blacksmith_npc hub_blacksmith
```

The POI catalogue's reference coordinates are not used as spawn positions. Place each NPC manually after visually inspecting the imported map.

## Current scope

- Banker: functional deposit/withdraw GUI backed by the existing bank service.
- Blacksmith: functional reforge GUI backed by the existing reforge service; current data pool remains test-fixture-only.
- Bartender, Bazaar Agent, Maddox, Angler Angus, Scoop: entity identity and interaction shell exist, but their dedicated service systems are not yet connected.
- NPC entities are persisted by Minecraft with their chunks. The Hub placement system does not yet have a global POI-to-entity registry or administrative removal/repair commands.

## Verification

GitHub Actions build validates Java compilation and resource packaging. It does not replace launching the mod in Minecraft and testing NPC interaction, menu clicks, balance persistence, and reforge stat recalculation in-game.
