# Core Public API v1 (Historical)

**Status:** Historical API v1 contract; superseded by [Core Public API v2](CORE_API_v2.md)  
**Target:** Minecraft 1.21.1 / Fabric / Java 21  
**Core Mod ID:** `skyblockrpg`

This document defines the supported integration surface for dependent mods such as Life Skills and Dungeons. It does not claim that every planned API (items, stats, rewards, quests) is already implemented.

## 1. Dependency and ownership

- A gameplay extension must declare a hard dependency on `skyblockrpg`.
- Core owns player profiles, persistence, built-in skill progression and shared game systems.
- Extensions own their gameplay rules and namespaced content.
- Extensions must not access Core's internal profile maps or mutate Core NBT directly.
- Public API classes live under `com.skyrpg.skyblock.api`.
- Current API compatibility marker: `CoreApi.API_VERSION = 1`.

## 2. Skill API (available)

Classes:
- `com.skyrpg.skyblock.api.SkillAPI`
- `com.skyrpg.skyblock.skill.SkillRegistration`

Register a skill from the dependent mod initializer:

```java
Identifier miningId = Identifier.of("skyblock_life", "mining");
SkillAPI.registerSkill(
    SkillRegistration.linear(miningId, "Mining", 60, 100.0)
);
```

The linear curve is an integration example, not an approved Hypixel XP table. Supply an explicit per-level XP curve when reliable reference data is available.

Player operations:

```java
SkillAPI.getExperience(player, miningId);
SkillAPI.getLevel(player, miningId);
SkillAPI.addExperience(player, miningId, 25.0);
```

Rules:
- IDs must be namespaced.
- Duplicate registration returns `false`; it does not replace an existing definition.
- XP can only be awarded to a registered skill.
- Unknown skill IDs retained in player saves are not discarded if the extension is temporarily absent.
- Legacy Core skill IDs stored before API v1 are migrated to the `skyblockrpg` namespace.
- Legacy command forms such as `/skyblock skill mining` remain valid; namespaced IDs such as `/skyblock skill skyblock_life:mining` are also accepted.

## 3. Player module-data API (available)

Class: `com.skyrpg.skyblock.api.PlayerDataAPI`

Each extension stores its own compound under a namespaced key:

```java
Identifier dataKey = Identifier.of("skyblock_life", "mining");
NbtCompound data = PlayerDataAPI.get(player, dataKey).orElseGet(NbtCompound::new);
data.putLong("blocks_mined", data.getLong("blocks_mined") + 1L);
PlayerDataAPI.set(player, dataKey, data);
```

Rules:
- Use keys owned by the extension, e.g. `skyblock_life:mining`, `skyblock_life:fishing`.
- `get` returns a defensive copy. Mutating it does not save anything until `set` is called.
- `set` copies the supplied compound and marks the Core profile dirty.
- `remove` deletes one extension data key and marks the profile dirty when it existed.
- Core persists extension compounds inside the existing player profile. Extensions must not create a second player-profile store.
- Data belonging to an unloaded optional extension is retained by Core and written back unchanged.

## 4. Planned APIs (not yet available)

The following are future Core work and must not be assumed to exist yet:
- Stat registration / modifier source API.
- Item definition and item-instance registration API.
- Currency / transaction API.
- Reward API.
- Quest objective API.
- Shared gameplay event API.

Until these are delivered, dependent mods should not compile against guessed class names. Submit API requests to the Core maintainer.

## 5. Compatibility policy

- Do not change public method signatures within API v1 without a compatibility plan.
- Breaking changes require an API version increment and migration note.
- Core's internal classes are implementation details unless explicitly listed in this document.
- Extensions must be tested with Core-only and Core-plus-extension configurations.
