# SkyBlock RPG Module Architecture

## 1. Product hierarchy

SkyBlock RPG Core is the primary game mod and remains the owner of the main game systems and overall experience. Optional modules extend the Core; they are not peer products.

- **Core** (`skyblockrpg`): required base mod and primary game.
- **Life Skills** (`skyblock_life`): optional extension for Mining, Fishing, Farming and Foraging.
- **Dungeons** (`skyblock_dungeons`): optional extension for dungeon instances, classes, rooms, bosses and dungeon progression.

Dependency direction:

```text
Life Skills -----> Core <----- Dungeons
                    |
             Main game systems
```

Life Skills and Dungeons must not depend on one another. Core must not require either extension to load or run.

## 2. Ownership

Core owns player profiles and persistence, currency, common stats, common skill progression, item and equipment foundations, combat foundations, NPC and quest frameworks, GUI foundations, regions, warps, islands, Hub and all systems not explicitly delegated to an extension.

Life Skills owns the concrete mechanics and content for Mining, Fishing, Farming and Foraging.

Dungeons owns dungeon instances, room generation, party/class rules, dungeon-specific enemies and bosses, dungeon scoring and dungeon-specific progression.

Extensions register content and use Core APIs. They must not duplicate Core systems or modify Core source files.

## 3. Current integration contract

Available in Core API v1:
- Namespaced skill registration and progression through `SkillAPI`.
- Namespaced extension-owned player data through `PlayerDataAPI`.

Not yet released:
- Extensible Stat API.
- Public Item / ItemInstance API.
- Currency transaction API.
- Reward, Quest Objective and gameplay event APIs.

Extensions must use only APIs explicitly marked available in `CORE_API_v1.md`. Missing APIs must be requested rather than guessed.

## 4. Project layout policy

The Core repository remains the main project. The initial extension can be developed in a separate repository or Gradle subproject, but it must declare Core as a required dependency and must not fork/copy Core code.

A multi-project Gradle build is not required for this first integration milestone. Keep repository and build changes incremental until the API surface stabilizes.

## 5. Compatibility and save safety

- Every extension-owned identifier must be namespaced by its Mod ID.
- Core owns the player profile container.
- Extension player data is stored under namespaced keys inside the Core profile.
- Core must retain unknown extension data when an optional extension is absent.
- Extensions must not overwrite another extension's data keys.
- API breaking changes require an API version increment and migration notes.
