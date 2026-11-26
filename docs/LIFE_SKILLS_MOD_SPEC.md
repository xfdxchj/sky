# SkyBlock RPG Life Skills — Development Specification

**Mod ID:** `skyblock_life`  
**Required dependency:** `skyblockrpg`  
**Target:** Minecraft 1.21.1 / Fabric / Java 21  
**Core API contract:** [CORE_API_v2.md](CORE_API_v2.md)

## 1. Positioning

Life Skills is an optional gameplay extension of SkyBlock RPG Core, not a standalone game or peer product. Core remains the primary mod and owns shared game systems. Life Skills supplies Mining, Fishing, Farming and Foraging.

Installing Core alone must remain fully functional. Installing Life Skills adds these four gameplay families.

## 2. Non-negotiable boundaries

Life Skills owns concrete life-skill mechanics, resource definitions, tools, drop tables, special events, area-specific rules, collections/statistics and life-skill content.

Core owns player UUID/profile, persistence, Purse/Bank, shared stats, skill XP/levels, item/equipment foundations, reward operations, quest framework and GUI foundations.

Life Skills must not:
- Modify Core source or copy Core managers.
- Create a second profile, economy or skill progression system.
- Write directly into Core profile internals.
- Depend on Core internal implementation classes where a public API exists.
- Assume an API exists unless it is listed as available in `CORE_API_v2.md`.

## 3. Required Core APIs

- `SkillAPI`: register four namespaced skills and award/query XP/levels.
- `PlayerDataAPI`: persist collection counters, unlocks and module statistics.
- `StatAPI`: read Core's final Mining Speed/Fortune, Farming Fortune, Foraging Speed/Fortune, Fishing Speed, Sea Creature Chance and Wisdom stats.
- `ItemAPI`: query definitions loaded from Core and Life Skills datapack resources.
- `InventoryAPI`: query/add/remove items in the logical SkyBlock inventory.
- `CurrencyAPI`: query and mutate Purse/Bank using Core managers.
- `RewardAPI`: grant items, coins and skill XP.

Example skill IDs:
- `skyblock_life:mining`
- `skyblock_life:fishing`
- `skyblock_life:farming`
- `skyblock_life:foraging`

Do not duplicate skill XP/level in Life Skills data. Core is authoritative.

## 4. Item definitions

Place extension item JSON under:

```text
src/main/resources/data/skyblock_life/skyblock/items/<path>.json
```

The item definition ID must use the namespace `skyblock_life:`. Core scans all namespaces for `skyblock/items/*.json` when the server starts. Query definitions through ItemAPI after server resource loading; do not assume they are available during `onInitialize`.

Supported item stats are the current Core `StatType` enum values. Life Skills cannot add arbitrary StatType values or write transient stat modifiers in API v2. New shared stats or modifier sources must be requested from the Core maintainer.

## 5. Player data

Use one PlayerDataAPI key per skill family:
- `skyblock_life:mining`
- `skyblock_life:fishing`
- `skyblock_life:farming`
- `skyblock_life:foraging`

Store counters, collection progress, unlocks and module-specific statistics. Core owns persistence and retains these keys while the optional module is absent.

## 6. Mining

### First playable scope
- Mining XP events.
- Ore/resource definitions and registry.
- Drop tables and Fortune calculation.
- Mining tool definitions.
- Mining Speed and Breaking Power rules.
- Minimum tool requirements.
- Resource collection statistics.
- Save/load through PlayerDataAPI.

### Later scope
- Multiple mining areas and progression gates.
- Veins and special ore nodes.
- Rare resources and mining events.
- Area NPCs, quests and collection rewards.
- Tool abilities, upgrades and enchantment integration.

Validate target and tool, resolve mining completion, calculate base drops and Fortune, grant rewards through RewardAPI, award Mining XP, update module data and prevent duplicate processing.

## 7. Fishing

### First playable scope
- Fishing XP and fishing state.
- Fish definitions and weighted drop tables.
- Rod and bait definitions.
- Fishing Speed and Sea Creature Chance.
- Sea Creature registration/spawn integration.
- Fishing statistics and persistence.

### Later scope
- Biome/region fishing tables.
- Weather/time conditions.
- Rare fish, special creatures and bestiary.
- Fishing events, NPCs, quests and equipment progression.

Use an independent SkyBlock-style catch resolver, not merely vanilla fishing loot. Special creatures should reuse Core combat/mob systems where supported.

## 8. Farming

### First playable scope
- Farming XP.
- Crop definitions and registry.
- Growth/harvest state.
- Farming Fortune and drop calculation.
- Farming tool definitions.
- Crop collection statistics.
- Save/load through PlayerDataAPI.

### Later scope
- Dedicated farm areas and layouts.
- Crop-specific tools and bonuses.
- Orders, contests, farm NPCs and special crops.

Distinguish player harvest from automation, prevent duplicate rewards and support efficient continuous harvesting.

## 9. Foraging

### First playable scope
- Foraging XP.
- Tree/log type definitions.
- Wood drop tables and Fortune.
- Foraging tool definitions.
- Foraging Speed.
- Collection statistics and persistence.

### Later scope
- Special tree types and foraging areas.
- Tree-specific tool abilities.
- Foraging collections, NPCs, quests and events.

Do not award XP indiscriminately for every vanilla log broken by machines or unrelated mods.

## 10. Shared architecture and event handling

Use data-driven JSON definitions for resources, fish, crops, trees, tools and drop tables. Keep Mining Fortune, Farming Fortune and Foraging Fortune separate. Fishing uses its own stats such as Fishing Speed and Sea Creature Chance.

For block breaking, harvesting and vanilla fishing hooks, use Fabric server-side callbacks and validate action/world state. API v2 does not publish a generic Core gameplay event bus or quest-objective callback; maintain a Life Skills-local event model until that contract is released.

## 11. Data sources

Mark each dataset as Official, Community Wiki, Community-maintained, Project fixture, Project custom design or Unverified hypothesis. Record source URL, retrieval date and confidence. Never present guessed values as verified Hypixel data.

## 12. Delivery phases

- **L0 — Project shell:** Fabric project, hard Core dependency, independent build and test plan.
- **L1 — Core integration:** register four skills; use PlayerDataAPI, StatAPI, ItemAPI, InventoryAPI, CurrencyAPI and RewardAPI; verify Core-only and Core-plus-Life-Skills.
- **L2 — Shared foundation:** drop tables, Fortune resolver, JSON validation and common reward flow.
- **L3 — Mining vertical slice:** one resource area, ores, pickaxe, requirements, drops, XP and persistence.
- **L4 — Fishing vertical slice:** one location, rod, bait, fish, one special creature, rewards and persistence.
- **L5 — Farming vertical slice:** crop growth, harvest, Fortune, XP and duplicate-reward protection.
- **L6 — Foraging vertical slice:** tree types, tools, drops, XP and persistence.
- **L7 — Content expansion:** more areas, resources, gear, collections, NPCs, quests and events.
- **L8 — Integration:** compatibility, save migration, optional-mod removal/reinstall, server authority, performance and exploit tests.

## 13. Acceptance criteria

- Life Skills compiles independently and hard-depends on Core.
- All four namespaced skills register without modifying Core.
- Skill XP/levels use SkillAPI.
- All extension data uses PlayerDataAPI and survives Core saves.
- Extension item JSON definitions load from the Life Skills namespace and resolve through ItemAPI.
- Inventory, currency and rewards use the published Core APIs.
- No direct mutation of Core profile internals.
- Each playable skill passes action validation, XP/drop/fortune, duplicate-reward, save/reload and capacity tests.
