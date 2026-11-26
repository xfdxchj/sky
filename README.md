# SkyBlock RPG

A single-player, Hypixel SkyBlock-inspired Fabric mod foundation.

## Environment
- Minecraft Java Edition 1.21.1
- Java 21
- Fabric Loader 0.16.x
- Fabric API 0.116.x

## Current milestone: P3.4 (implementation branch; build/test pending)
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
- The training sword's item stats remain project test values, not a Hypixel item.

### Commands
- `/skyblock status`
- `/skyblock profile`
- `/skyblock stats`
- `/skyblock combat`
- `/skyblock coins`
- `/skyblock coins add <amount>` (permission level 2)
- `/skyblock coins remove <amount>` (permission level 2)
- `/skyblock skill <skill>`
- `/skyblock skill <skill> add <amount>` (permission level 2)
- `/skyblock items`
- `/skyblock item give <itemId> [count]` (permission level 2)
- `/skyblock item info <itemId>`
- `/skyblock mob list`
- `/skyblock mob info <mobId>`
- `/skyblock inventory`
- `/skyblock equipment`
- `/skyblock equip <itemId>`
- `/skyblock unequip <slot>`

### P3.4 mob catalogue and incoming damage
- Design: `docs/P3.4_DESIGN.md`
- Test checklist: `docs/P3.4_TEST_PLAN.md`
- 58 sourced mob records are currently included: 15 level variants each for Private Island Zombie, Skeleton and Spider, plus 13 Graveyard/Crypts/Ruins/The Park/Deep Caverns examples. Five vanilla entity mappings are active; other entries are catalogue-only until variant-aware spawn assignment is implemented.
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
- No custom mob entity types, AI, spawn tables, variant-aware natural spawning, boss phases, abilities, ranged weapons, attack-speed timing, Ferocity, enchantments, reforges or elemental damage yet.
- The logical SkyBlock inventory still has no custom GUI and is not rendered as vanilla item stacks.
- Full Fabric Gradle build and in-game behavior for P3.4 must be verified in the local project environment.

## Build
Open the project in IntelliJ IDEA with Gradle JVM set to Java 21, then run `gradle build` or the IDE Gradle `build` task.
