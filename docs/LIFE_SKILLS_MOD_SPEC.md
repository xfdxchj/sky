# SkyBlock RPG Life Skills — Development Specification

**Mod ID:** `skyblock_life`  
**Required dependency:** `skyblockrpg`  
**Target:** Minecraft 1.21.1 / Fabric / Java 21

## 1. Positioning

Life Skills is an optional gameplay extension of SkyBlock RPG Core, not a standalone game or a peer to Core. Core remains the primary mod and owns the shared game systems. Life Skills supplies four concrete gameplay families:

1. Mining
2. Fishing
3. Farming
4. Foraging

Installing Core alone must remain fully functional. Installing Life Skills adds these four gameplay families.

## 2. Non-negotiable boundaries

Life Skills owns concrete life-skill mechanics, resource definitions, tools, drop tables, special events, area-specific rules, collections/statistics and life-skill content.

Core owns player UUID/profile, persistence container, Purse/Bank, common stats, common skill XP/levels, item and equipment foundations, rewards, quest framework, GUI foundations and shared event infrastructure.

Life Skills must not:
- Modify Core source.
- Copy Core managers or create a second profile/economy/skill system.
- Write directly into Core profile internals.
- Depend on Core internal implementation classes where a public API exists.
- Assume planned APIs exist before they are released.

When a required API is missing, submit an API request and use a temporary adapter only inside Life Skills. Do not patch Core independently.

## 3. Skill identifiers and player data

Register these namespaced skills through Core API:

| Skill | ID |
|---|---|
| Mining | `skyblock_life:mining` |
| Fishing | `skyblock_life:fishing` |
| Farming | `skyblock_life:farming` |
| Foraging | `skyblock_life:foraging` |

Use `SkillAPI` for registration, XP, level and progress calculations. Skill definitions must contain an explicit XP curve. Do not label placeholder curves as Hypixel data.

Use `PlayerDataAPI` for extension data. Recommended keys:
- `skyblock_life:mining`
- `skyblock_life:fishing`
- `skyblock_life:farming`
- `skyblock_life:foraging`

Store counters, collection progress, unlocks and module-specific statistics under these keys. Do not store a duplicate skill XP/level value; Core is authoritative for that.

## 4. Mining

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

A mining event should validate the target and tool, resolve mining completion, calculate base drops and Fortune, issue rewards through Core when its reward/item APIs are available, award Mining XP, update module data and emit a module event. Prevent duplicate reward processing.

## 5. Fishing

### First playable scope
- Fishing XP events and fishing state.
- Fish definitions and weighted drop tables.
- Rod and bait definitions.
- Fishing Speed.
- Sea Creature Chance and special catch outcomes.
- Sea Creature registration/spawn integration.
- Fishing statistics and persistence.

### Later scope
- Biome/region fishing tables.
- Weather and time conditions.
- Rare fish, special creatures and bestiary.
- Fishing events, NPCs, quests and equipment progression.

Do not merely award XP from vanilla fishing loot. Establish a SkyBlock-style independent catch resolver. Special creatures should reuse Core combat/mob systems when supported rather than implementing a second generic combat engine.

## 6. Farming

### First playable scope
- Farming XP events.
- Crop definitions and registry.
- Growth/harvest state.
- Farming Fortune and drop calculation.
- Farming tool definitions.
- Crop collection statistics.
- Save/load through PlayerDataAPI.

### Later scope
- Dedicated farm areas and layouts.
- Crop-specific tools and bonuses.
- Orders, contests and farm NPCs.
- Special crops and automation integrations.

The harvest pipeline must distinguish player harvest from machine/automation changes, prevent duplicate rewards and avoid block-update duplication exploits. Support efficient continuous harvesting rather than treating every crop as an isolated vanilla break.

## 7. Foraging

### First playable scope
- Foraging XP events.
- Tree/log type definitions.
- Wood drop tables and Fortune.
- Foraging tool definitions.
- Foraging Speed.
- Collection statistics and persistence.

### Later scope
- Special tree types and foraging areas.
- Tree-specific tool abilities.
- Foraging collections, NPCs, quests and events.

Do not award Foraging XP indiscriminately for every vanilla log broken by machines or unrelated mods. Define the eligible player action and tree validation rules.

## 8. Shared Life Skills architecture

Recommended package layout:

```text
com.example.skyblocklife
├── api
├── common
│   ├── registry
│   ├── drop
│   ├── fortune
│   └── event
├── mining
├── fishing
├── farming
└── foraging
```

Use data-driven definitions (JSON) for resources, fish, crops, trees, tools, drop tables and progression rewards wherever practical. Every definition needs a stable namespaced ID and schema validation.

Fortune is not one universal stat. Keep Mining Fortune, Farming Fortune and Foraging Fortune semantically separate. Fishing uses its own relevant stats such as Fishing Speed and Sea Creature Chance. Request Core Stat API support before trying to register custom stat types.

## 9. Data sources

Reference current Hypixel SkyBlock behavior. Mark every data set as one of:
- Official source.
- Community Wiki.
- Community-maintained data.
- Project fixture.
- Project custom design.
- Unverified hypothesis.

Record source URL, retrieval date and confidence where possible. Do not present guessed values as official. When reliable current data is unavailable, implement configurable mechanics and clearly mark the values provisional.

## 10. Delivery phases

### L0 — Project shell
- Fabric project and mod metadata.
- Hard dependency on `skyblockrpg`.
- Independent build.
- Package layout, README and test plan.
- No gameplay content required yet.

### L1 — Core integration
- Register four skills through SkillAPI.
- Use PlayerDataAPI for module data.
- Verify startup ordering and namespaced commands.
- Verify Core-only and Core-plus-Life-Skills loading.
- Submit missing API requirements; do not invent unavailable APIs.

### L2 — Shared gameplay foundation
- Drop table framework.
- Fortune resolver.
- Skill event/reward pipeline.
- JSON definition loader and validation.
- Shared data persistence helpers.

### L3 — Mining vertical slice
- One resource area, several ores, a basic pickaxe, tool requirements, XP and drops.
- Complete a playable end-to-end loop before expanding content.

### L4 — Fishing vertical slice
- One fishing location, rod, bait, several fish and one special creature.
- Complete catch, reward, XP and save/load loop.

### L5 — Farming vertical slice
- A small crop set, growth, harvest, Fortune and XP.
- Verify continuous harvest and duplicate-reward protection.

### L6 — Foraging vertical slice
- Several tree types, tools, drop and XP rules.
- Verify eligible player actions and persistence.

### L7 — Content expansion
- More areas, resources, gear, collections, NPC content, quests and events based on verified reference data.

### L8 — Integration and stability
- Core compatibility, save migration, optional-mod removal/reinstall, multiplayer/server authority, performance and exploit tests.

## 11. Acceptance criteria

Initial L1 acceptance:
- Life Skills compiles independently.
- Loading without Core fails with a clear dependency error.
- Loading with Core succeeds.
- All four skill IDs register without modifying Core.
- Skill XP and levels are read/written through SkillAPI.
- Module data persists through PlayerDataAPI.
- Core retains extension data while Life Skills is absent.

Each playable skill later requires:
- Valid action and tool checks.
- Correct XP and level progression.
- Correct base and bonus drops.
- Correct collection/statistics updates.
- No duplicate reward exploit.
- Save/reload correctness.
- No direct mutation of Core internals.

## 12. Required deliverables

- Complete source and Gradle project.
- Mod metadata and dependency declaration.
- JSON schemas/sample data.
- API integration notes.
- Design and test plans.
- Build/test results.
- Known limitations and outstanding Core API requests.

Core is the authoritative owner of shared game rules. Life Skills is responsible only for the four life-skill gameplay extensions.
