# SkyBlock RPG

A single-player, Hypixel SkyBlock-inspired Fabric mod foundation.

## Development documentation

- [Development status v0.4](docs/DEV_STATUS_v0.4.md): what is implemented and what has actually been verified.
- [Development task list v0.4](docs/DEV_TASKS_v0.4.md): remaining work and acceptance criteria.
- [First playable slice quick start](docs/QUICK_START_PLAYABLE_SLICE.md): new-player → Jerry → starter sword → Graveyard combat quest.
- [VS-3 / VS-4 combat quest test plan](docs/VS3_COMBAT_QUEST_SLICE_TEST_PLAN.md): build and in-game acceptance steps.
- [World map reconstruction plan](docs/VS_WORLD_MAP_IMPLEMENTATION_PLAN.md): Private Island, Hub and later islands.
- [Hub map intake and slicing](docs/HUB_MAP_INTAKE_AND_SLICING.md): direct 1.22 map compatibility audit and structure slicing workflow.
- [Core API v3](docs/CORE_API_v3.md): supported integration contract for optional gameplay modules.
- [Life Skills specification](docs/LIFE_SKILLS_MOD_SPEC.md): Mining, Fishing, Farming and Foraging extension requirements.
- [Core API v3 test plan](docs/CORE_API_v3_TEST_PLAN.md): integration and regression checklist.
- [External module setup](docs/EXTERNAL_MODULE_SETUP.md): how another AI/developer can build an independent Fabric extension against Core.
- [API smoke-test project](examples/life-skills-api-smoke/README.md): minimal independent mod that registers the four life skills.
- Development may proceed directly on `main`; feature branches are optional and used only when they materially help isolate risk or parallel work.

## Environment
- Minecraft Java Edition 1.21.1
- Java 21
- Fabric Loader 0.16.x
- Fabric API 0.116.x

## Current milestone: Playable Vertical Slice (VS-1 through VS-4 implementation in progress; build/test pending)
P3.1 connects the P2 equipment/stat model to server-authoritative melee combat against vanilla living entities.

### Implemented
- P1 UUID profiles, coins, skill experience and world persistence
- P2 logical inventory, equipment slots and item stat modifiers
- `DAMAGE` stat and a test `training_sword`
- Server-side attack interception through a Mixin on `PlayerEntity.attack(Entity)`; client attack input and vanilla fallback remain intact
- SkyBlock-style melee damage formula with the documented +5 base damage, Strength, conditional Crit Damage and target Defense multipliers
- Baseline combat stats: 30 Crit Chance and 50 Crit Damage (community-wiki sourced)
- Crit Chance clamp and finite/range guards for damage calculations
- Starter-mob reward mapping for Zombie, Skeleton and Spider; unregistered entities receive no coins or Combat XP
- Mob definitions loaded from datapack JSON resources, including level, reference HP/Damage, Defense, rewards and provenance fields
- Registered mobs use persistent SkyBlock HP pools independent of vanilla max health; vanilla health is synchronized proportionally for the visible health bar
- Mob records include family, category, mob types, area, structured drops and source provenance
- Configured mob Damage is applied when a mapped mob hits a player; vanilla player-side mitigation still applies
- Custom mob HP is stored by entity UUID in dimension-scoped world PersistentState; unregistered entities retain vanilla health behavior
- Item definitions loaded from datapack JSON resources, replacing the Java static item catalogue
- `/skyblock mob list`, `/skyblock mob info <mobId>` and `/skyblock item info <itemId>`
- Kill rewards persisted through the existing player profile
- `/skyblock combat` displays equipped weapon and combat stats

### Current simplified melee formula
`floor((5 + WeaponDamage) * (1 + Strength / 100) * (critical ? 1 + CritDamage / 100 : 1) * 100 / (100 + MobDefense))`

This is the simplified melee core only. It does not yet include additive damage multipliers, enchantments, Combat-level bonuses, weapon/armor abilities, Ferocity, or other situational modifiers. Minecraft armor/shield handling still applies after the calculated damage; the mapped vanilla test mobs have no SkyBlock Defense data, so their current Defense is 0.

### Data provenance
- Hypixel's official public API documents SkyBlock resources such as items, collections and skills. It does not provide a complete public mob-stat / mob-reward endpoint.
- The P3.1 combat formula and baseline Crit Chance/Crit Damage are community-wiki references. See `DATA_SOURCES.md`.
- P3.2/P3.4 mob values are tagged community-wiki sourced, not official API values. Private Island Zombie/Skeleton/Spider include all 15 listed variants each; additional sourced examples cover Graveyard, Crypts, Ruins, The Park and Deep Caverns. Defense 0 remains a project placeholder where the Wiki does not publish a value.
- The Wiki's current standard Mob category derives most Mob Levels and rewards from HP/Damage and Mob Level; the formulas and exceptions are recorded in `DATA_SOURCES.md` for a future reward-calculation milestone.
- The training sword's item stats remain project test values, not a Hypixel item.

### Commands
- `/skyblock status`
- `/skyblock profile`
- `/skyblock stats`
- `/skyblock combat`
- `/skyblock coins`
- `/skyblock coins add <amount>` (permission level 2)
- `/skyblock coins remove <amount>` (permission level 2)
- `/skyblock bank` (show Purse and Bank balances)
- `/skyblock bank deposit <amount>`
- `/skyblock bank withdraw <amount>`
- `/skyblock npc list`
- `/skyblock npc info <npcId>`
- `/skyblock npc spawn <npcId>` (permission level 2; development/test spawn)
- `/skyblock quest list`
- `/skyblock quest info <questId>`
- `/skyblock quest accept <questId>`
- `/skyblock quest abandon <questId>`
- `/skyblock menu` (opens the SkyBlock GUI)
- `/skyblock accessory list|add <itemId>|remove <itemId>`
- `/skyblock region list`
- `/skyblock region here`
- `/skyblock warp list`
- `/skyblock warp <warpId>`
- `/skyblock skill <skill>`
- `/skyblock skill <skill> add <amount>` (permission level 2)
- `/skyblock shop` (list offers)
- `/skyblock shop buy <itemId> [count]`
- `/skyblock shop sell <itemId> [count]`
- `/skyblock items`
- `/skyblock item give <itemId> [count]` (permission level 2)
- `/skyblock item info <itemId>`
- `/skyblock mob list`
- `/skyblock mob info <mobId>`
- `/skyblock mob spawn <mobId>` (permission level 2; development/test spawn)
- `/skyblock inventory`
- `/skyblock equipment`
- `/skyblock equip <itemId>`
- `/skyblock unequip <slot>`

### P3.5 core mob content
- Design: `docs/P3.5_DESIGN.md`
- Test checklist: `docs/P3.5_TEST_PLAN.md`
- Adds 12 sourced Spider's Den and The End definitions, increasing the catalogue from 58 to 70 records.
- Adds `spawn_entity_type` to all 70 records and per-entity command-tag identity so multiple SkyBlock mob definitions can share one vanilla stand-in without colliding in the entity-type fallback map. This includes explicit spawning of the 15-level Private Island Zombie/Skeleton/Spider variants and the 12 new records.
- `/skyblock mob spawn <mobId>` creates a tagged stand-in with a `[LvX] Name` label and routes attacks/HP through the selected definition.
- This is explicit test spawning only; natural region spawns, loot rolls and unique AI are not implemented yet.

### P3.4 mob catalogue and incoming damage
- Design: `docs/P3.4_DESIGN.md`
- Test checklist: `docs/P3.4_TEST_PLAN.md`
- 58 sourced mob records are currently included: 15 level variants each for Private Island Zombie, Skeleton and Spider, plus 13 Graveyard/Crypts/Ruins/The Park/Deep Caverns examples. Five vanilla entity mappings are active; additional records can now be explicitly spawned through tagged per-instance identities.
- The incoming damage bridge replaces vanilla raw damage with the configured mob Damage before vanilla player mitigation.

### P3.3 mob HP
- Design: `docs/P3.3_DESIGN.md`
- Test checklist: `docs/P3.3_TEST_PLAN.md`
- Registered mob HP and max HP are independent of vanilla entity max health; the existing native damage pipeline still handles mitigation and invulnerability frames.

### P3.2 data files
- Mob data: `src/main/resources/data/skyblockrpg/skyblock/mobs/*.json`
- Item data: `src/main/resources/data/skyblockrpg/skyblock/items/*.json`
- Schema: `docs/P3.2_DATA_SCHEMA.md`
- Test checklist: `docs/P3.2_TEST_PLAN.md`

### P3 limitations
- Player melee interception applies to living non-player entities while a registered SkyBlock weapon is equipped.
- Vanilla entities remain temporary stand-ins for SkyBlock mobs. P3.3 adds independent persistent HP pools, but does not add custom entity classes or AI.
- Mob Defense remains unknown/project-default for the sourced entries because the cited Wiki pages do not publish a Defense value. The custom HP pool is authoritative for the SkyBlock weapon melee pipeline only; environmental damage and attacks from other vanilla entities still use vanilla health and can bypass the custom pool.
- No custom mob entity types, unique AI, natural spawn tables, boss phases, abilities, ranged weapons, attack-speed timing, Ferocity, enchantments, reforges or elemental damage yet. Mob drop JSON is catalogue data only; drops are not yet granted on kill.
- The logical SkyBlock inventory still has no custom GUI and is not rendered as vanilla item stacks.
- Full Fabric Gradle build and in-game behavior for P3.4/P3.5 must be verified in the local project environment.

## Build
Open the project in IntelliJ IDEA with Gradle JVM set to Java 21, then run `gradle build` or the IDE Gradle `build` task.

### P5.0 Island map and floating-island foundation
- Design: `docs/P5_ISLAND_MAP_FOUNDATION.md`
- Test checklist: `docs/P5_ISLAND_MAP_TEST_PLAN.md`
- Adds JSON island manifests under `data/skyblockrpg/skyblock/islands/*.json`, linked to existing region and warp definitions.
- Adds an operator-triggered procedural floating-island generator with configurable footprint, height, thickness and block palette. It preserves existing non-air blocks and checks world border, build height and a generation-size safety cap.
- Adds `/skyblock map`, `/skyblock island list|info <id>|generate <id>|visit <id>`.
- The included `starter_island_fixture` is only a development fixture; it is not Hypixel Private Island or Hub geometry. No terrain is generated automatically when a world opens.
- Native structure NBT import is implemented in P5.1. P5.2 adds JSON-driven bridge and portal route definitions. Island protection, actual Hypixel map assets and map GUI remain deferred.
- Local Java 21 Gradle build and in-game verification are pending.

### P4.8 Basic Reforge foundation
- Design: `docs/P4.8_REFORGE_SYSTEM_DESIGN.md`
- Test checklist: `docs/P4.8_REFORGE_SYSTEM_TEST_PLAN.md`
- Loads JSON reforge definitions from `data/skyblockrpg/skyblock/reforges/*.json`.
- Adds a server-side basic reforge roll for equipped weapon and armor slots; compatible registered reforges are selected with equal probability and charge their configured coin cost (500 coins in the supplied fixtures).
- Reforge state is stored per equipped slot in PlayerInventory NBT and contributes additive modifiers through the existing final-stat pipeline.
- Replacing or unequipping an item clears that slot's reforge; P4.8 does not yet have unique item-instance identity, so reforges do not follow an item into the inventory.
- Commands: `/skyblock reforge`, `/skyblock reforge roll <slot>`.
- `test_fierce` and `test_titanic` are project test fixtures, not verified Hypixel reforge tables. Rarity scaling, full compatibility pools, Reforge Stones, special bonuses and GUI are deferred.
- Local Java 21 Gradle build and in-game verification are pending.

### P4.7 Accessory Power, Powers, Power Stones and Tuning
- Design: `docs/P4.7_ACCESSORY_POWER_SYSTEM_DESIGN.md`
- Test checklist: `docs/P4.7_ACCESSORY_POWER_SYSTEM_TEST_PLAN.md`
- Uses current Accessory Power terminology and rarity values.
- Accessory Bag capacity is 18 by default, reflecting the additional default slots described in the 0.27.3 release-candidate notes.
- Item definitions support accessory upgrade lines; only the highest-ranked accessory in a line contributes stats and Accessory Power.
- Power definitions are JSON-driven. Fortuitous and Pretty are starter Powers; Forceful is included as a Power Stone unlock fixture.
- Nine matching Power Stones unlock a non-starter Power.
- Selected Power stats scale with the Accessory Power multiplier.
- Tuning Points are granted at one per 10 Accessory Power and allocations are persisted.
- Commands: `/skyblock accessory power`, `/skyblock accessory power select <powerId>`, `/skyblock accessory power unlock <powerId>`, `/skyblock accessory tuning`, `/skyblock accessory tuning set <stat> <points>`.
- Tuning point values and full current-game parity still require in-game verification; Enrichments and dungeon runtime integration are deferred.
- Profile/world data schema advances to 7. Local Java 21 Gradle build and in-game verification are pending.

### P4.6 equipment, accessories and stat aggregation
- Design: `docs/P4.6_EQUIPMENT_ACCESSORY_STATS_DESIGN.md`
- Test checklist: `docs/P4.6_EQUIPMENT_ACCESSORY_STATS_TEST_PLAN.md`
- Adds a persistent 9-entry project-foundation Accessory Bag, separate from equipment slots and the logical inventory.
- Accessory definitions use `type: ACCESSORY`; moving an accessory into or out of the bag updates the logical inventory transactionally.
- Duplicate accessory definition IDs are rejected; item-instance identity and Hypixel accessory power progression remain future work.
- Equipped weapons/armor and bagged accessories feed the shared additive stat-modifier pipeline used by combat.
- Adds Equipment and Accessories GUI pages; the Accessories page supports returning an item to inventory.
- Adds `/skyblock accessory list|add|remove` commands.
- The included `test_power_talisman` is explicitly a project test fixture, not a verified Hypixel item or stat line.
- Existing profiles migrate additively: missing Accessory Bag data loads empty; profile/world data schema advances to 6.
- Hypixel accessory power, tuning, reforges, enchantments, set bonuses, item abilities and verified item catalogue import are deferred.
- Local Java 21 Gradle build and in-game verification are pending.

### P4.5 GUI foundation
- Design: `docs/P4.5_GUI_FOUNDATION_DESIGN.md`
- Test checklist: `docs/P4.5_GUI_FOUNDATION_TEST_PLAN.md`
- Adds a reusable 27-slot server-authoritative menu ScreenHandler and client-side custom screen.
- Adds a common ScreenHandlerType registration and client screen entrypoint; client sources under `src/client/java` are included in the Java source set.
- Adds `/skyblock menu` with Profile, Bank, Quests and Shop pages.
- Quest page can accept an unaccepted quest; profile and bank pages display current values; shop page displays the offer catalogue.
- Menu actions are held per ScreenHandler instance; menu icons cannot be inserted or extracted as items.
- Bank transfers and shop transactions remain command-based; this milestone establishes the UI framework, not complete interactive versions of every screen.
- Local Java 21 Gradle build and in-game verification are pending.

### P4.4 dialogue and quest foundation
- Design: `docs/P4.4_DIALOGUE_QUEST_DESIGN.md`
- Test checklist: `docs/P4.4_DIALOGUE_QUEST_TEST_PLAN.md`
- Adds JSON-driven quest definitions with a first objective type: talk to a configured NPC.
- Adds persistent active/completed quest state and objective progress to player profiles; older saves migrate additively to schema version 5.
- Adds quest list, info, accept and abandon commands.
- NPC interactions advance matching active quests; completion grants configured Purse coins and optional skill XP exactly once.
- Starter guide acts as a quest board; the sample `meet_the_banker` quest asks the player to speak with the Banker.
- Dialogue is still plain chat text and quest interaction is command-driven. No branching dialogue GUI, clickable options, kill/item objectives, quest chains or tracking HUD yet.
- Local Java 21 Gradle build and in-game verification for P4.4 remain pending.

### P4.3 NPC interaction foundation
- Design: `docs/P4.3_NPC_DESIGN.md`
- Test checklist: `docs/P4.3_NPC_TEST_PLAN.md`
- Loads NPC identities, dialogue lines, region restrictions and action bindings from JSON resources.
- Adds per-instance NPC command tags so multiple NPC definitions can share a vanilla villager stand-in without sharing identity.
- Adds `/skyblock npc list`, `/skyblock npc info <npcId>` and permission-level-2 `/skyblock npc spawn <npcId>`.
- Server-side right-click interaction suppresses vanilla trading for tagged NPCs and supports guide dialogue plus existing Bank, Shop catalogue and Warp entry points.
- Includes three starter test NPC definitions; they are fixtures, not verified Hypixel NPC placements or dialogue.
- No custom NPC models/skins, GUI dialogue choices, quest engine, natural placement or schedules yet.
- Local Java 21 Gradle build and in-game verification for P4.3 remain pending.

### P4.2 region and warp foundation
- Design: `docs/P4.2_REGION_WARP_DESIGN.md`
- Test checklist: `docs/P4.2_REGION_WARP_TEST_PLAN.md`
- Adds JSON-driven cuboid region definitions with dimension and priority-aware lookup.
- Detects region entry server-side, reports first discovery and persists discovered region IDs in the existing player profile.
- Adds JSON-driven warp destinations, dimension-aware teleportation and optional region-discovery unlock requirements.
- Includes a clearly labelled project test region and test warp; these coordinates are not Hypixel map coordinates.
- Region/warp registries load from resources at server start. No Hypixel island terrain, verified region map data or map assets are included yet.
- Local Java 21 Gradle build and in-game verification are pending.

### P4.1 bank and purse separation
- Design: `docs/P4.1_BANK_DESIGN.md`
- Test checklist: `docs/P4.1_BANK_TEST_PLAN.md`
- Adds a persistent Bank balance beside the existing Purse balance in the same player profile.
- Existing profiles migrate additively: missing Bank defaults to 0; existing Purse, skills, stats and inventory are preserved.
- Deposit and withdrawal are atomic profile mutations with positive-amount, insufficient-funds and overflow checks.
- Shop transactions continue to use Purse only. No interest, fees, bank limits or bank GUI yet; the P4.3 Banker NPC is a command-driven interaction.
- Local Java 21 Gradle build and in-game verification are pending.

### P4.0 economy and shop
- Design: `docs/P4.0_ECONOMY_SHOP_DESIGN.md`
- Test checklist: `docs/P4.0_ECONOMY_SHOP_TEST_PLAN.md`
- Adds a JSON-driven General Store catalogue and server-side buy/sell transactions using the existing logical inventory and coin balance.
- Transactions validate quantity, funds, inventory capacity and arithmetic overflow; failed paired mutations are rolled back.
- Initial prices are explicitly marked as project test values, not verified Hypixel prices.
- This milestone is command-driven. A basic NPC interaction foundation is added in P4.3; NPC GUI, Bazaar and Auction House remain future work.
- Local Java 21 Gradle build and in-game verification are pending.


## P5.1 — Structure Asset Import

- Added manifest-driven structure registration under data/skyblockrpg/skyblock/structures.
- Added native Minecraft structure NBT placement using the 1.21.1 structure template manager.
- Supports configured origin, dimension, rotation and mirror.
- Operator-only paste command; entity placement disabled; full bounding-box preflight requires air and prevents partial overwrite.
- Includes a tiny project-authored marker fixture. No Hypixel map assets are bundled.
- See docs/P5.1_STRUCTURE_IMPORT.md and docs/P5.1_STRUCTURE_IMPORT_TEST_PLAN.md.


### P5.2 Island connections
- Design: `docs/P5.2_ISLAND_CONNECTIONS.md`
- Test checklist: `docs/P5.2_ISLAND_CONNECTIONS_TEST_PLAN.md`
- Adds JSON-driven bridge and portal connections under `data/skyblockrpg/skyblock/connections/*.json`, each linking existing source and target warp nodes.
- Adds a route adjacency query with `/skyblock connection routes <warpId>`.
- Adds operator-triggered bridge deck construction with dimension, build-height, world-border, block-count and all-air preflight checks.
- Adds proximity-gated portal travel delegated to the existing WarpManager, preserving destination warp discovery requirements.
- Includes clearly labelled starter bridge, portal and outpost fixtures. These are project test coordinates, not Hypixel map data.
- This is an adjacency graph only; shortest-path search, actual portal blocks/effects, supports/rails and verified Hypixel map coordinates remain future work.
- User reported Java 21 Gradle build success on 2026-11-27 (JAR about 261.8 KB); runClient and full in-game verification remain pending.


### P5.3 Hub location catalogue foundation
- Adds sourced Hub points of interest for Village, Bank, Auction House, Bazaar Alley, Mining District and selected NPC reference positions.
- Adds /skyblock hub, /skyblock hub list, /skyblock hub info <id>, /skyblock hub locate <id> and /skyblock hub nearest.
- Coordinates are community-wiki references, not official API data, region bounds or NPC spawn instructions.
- No Hub terrain or third-party schematic/NBT assets are included; map asset licensing and Minecraft-version compatibility remain open.
- Design: docs/P5.3_HUB_LOCATION_CATALOGUE.md
- Test checklist: docs/P5.3_HUB_LOCATION_CATALOGUE_TEST_PLAN.md
