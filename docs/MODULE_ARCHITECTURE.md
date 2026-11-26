# SkyBlock RPG Module Architecture

## 1. Product hierarchy

SkyBlock RPG Core is the primary game mod and owns the main game systems and overall experience. Optional modules extend Core; they are not peer products.

- **Core** (`skyblockrpg`): required base mod and primary game.
- **Life Skills** (`skyblock_life`): optional extension for Mining, Fishing, Farming and Foraging.
- **Dungeons** (`skyblock_dungeons`): optional extension for dungeon instances, classes, rooms, bosses and dungeon progression.

```text
Life Skills -----> Core <----- Dungeons
                    |
          Main game systems
```

Life Skills and Dungeons must not depend on one another. Core must not require either extension to load or run.

## 2. Ownership

Core owns player profiles and persistence, currency, common stats, common skill progression, item and equipment foundations, combat foundations, NPC and quest frameworks, GUI foundations, regions, warps, islands, Hub and all systems not explicitly delegated to an extension.

Life Skills owns the concrete mechanics and content for Mining, Fishing, Farming and Foraging. Dungeons owns dungeon instances, room generation, party/class rules, dungeon-specific enemies and bosses, dungeon scoring and dungeon-specific progression.

Extensions register content and use Core APIs. They must not duplicate Core systems or modify Core source files.

## 3. Current integration contract

Core API v2 is implemented in source (local build and runtime verification pending). Contract: [CORE_API_v2.md](CORE_API_v2.md).

Available:
- Namespaced skill registration/progression: `SkillAPI`.
- Namespaced extension player data: `PlayerDataAPI`.
- Read-only shared stat access: `StatAPI`.
- Extension datapack item definitions: `ItemAPI`.
- Logical SkyBlock inventory access: `InventoryAPI`.
- Purse and Bank operations: `CurrencyAPI`.
- Item, coin and skill-XP reward facade: `RewardAPI`.

Not included yet:
- Arbitrary runtime StatType registration or a public stat-modifier write API.
- Unique ItemInstance API.
- Atomic multi-reward transaction API.
- Quest objective API or generic Core gameplay event bus.

Life Skills should use Fabric server-side callbacks for Minecraft actions and its own internal event model until a Core event contract is published.

## 4. Project layout policy

The Core repository remains the main project. Life Skills can be developed in a separate repository or Gradle subproject, but must declare Core as a required dependency and must not fork/copy Core code. A multi-project Gradle build is not required for this milestone.

## 5. Compatibility and save safety

- Every extension-owned identifier must be namespaced by its Mod ID.
- Core owns the player profile container.
- Extension player data is stored under namespaced keys inside the Core profile.
- Core retains unknown extension data when an optional mod is absent.
- Extensions must not overwrite another extension's data keys.
- Breaking API changes require a compatibility-marker increment and migration notes.
