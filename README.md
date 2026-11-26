# SkyBlock RPG

A single-player, Hypixel SkyBlock-inspired Fabric mod foundation.

## Development documentation

- [Development status v0.4](docs/DEV_STATUS_v0.4.md): what is implemented and what has actually been verified.
- [Development task list v0.4](docs/DEV_TASKS_v0.4.md): remaining work and acceptance criteria.
- Development may proceed directly on `main`; feature branches are optional and used only when they materially help isolate risk or parallel work.

## Environment
- Minecraft Java Edition 1.21.1
- Java 21
- Fabric Loader 0.16.x
- Fabric API 0.116.x

## Current milestone: P3.1
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
- Kill rewards persisted through the existing player profile
- `/skyblock combat` displays equipped weapon and combat stats

### Current simplified melee formula
`floor((5 + WeaponDamage) * (1 + Strength / 100) * (critical ? 1 + CritDamage / 100 : 1) * 100 / (100 + MobDefense))`

This is the simplified melee core only. It does not yet include additive damage multipliers, enchantments, Combat-level bonuses, weapon/armor abilities, Ferocity, or other situational modifiers. Minecraft armor/shield handling still applies after the calculated damage; the mapped vanilla test mobs have no SkyBlock Defense data, so their current Defense is 0.

### Data provenance
- Hypixel's official public API documents SkyBlock resources such as items, collections and skills. It does not provide a complete public mob-stat / mob-reward endpoint.
- Combat formula, baseline Crit Chance/Crit Damage and starter-mob reward references currently come from the community-maintained Hypixel SkyBlock Wiki. See `DATA_SOURCES.md`.
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
- `/skyblock inventory`
- `/skyblock equipment`
- `/skyblock equip <itemId>`
- `/skyblock unequip <slot>`

### P3 limitations
- Combat interception currently applies to melee attacks against living non-player entities while a registered SkyBlock weapon is equipped.
- Vanilla entity health is not scaled to Hypixel mob health. The current Zombie/Skeleton/Spider mapping is only a combat/reward test bridge.
- Mob Defense is set to 0 until a defensible data source / custom mob data model exists.
- No custom mob entity types, AI, spawn tables, boss phases, abilities, ranged weapons, attack-speed timing, Ferocity, enchantments, reforges or elemental damage yet.
- The logical SkyBlock inventory still has no custom GUI and is not rendered as vanilla item stacks.
- Full Fabric Gradle build and in-game behavior have not been verified in this environment.

## Build
Open the project in IntelliJ IDEA with Gradle JVM set to Java 21, then run `gradle build` or the IDE Gradle `build` task.
