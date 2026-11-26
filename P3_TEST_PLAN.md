# P3.1 Test Plan

## Preparation
1. Back up the world and the P2 project.
2. Build and launch with Minecraft 1.21.1, Java 21, Fabric Loader 0.16.x and Fabric API 0.116.x.
3. Enter a test world with cheats enabled.

## Weapon and stat setup
1. Run `/skyblock item give training_sword`.
2. Run `/skyblock equip training_sword`.
3. Run `/skyblock combat`.
4. Expected with a fresh profile: Damage 10, Strength 10, Crit Chance 55 (30 base + 25 item), Crit Damage 100 (50 base + 50 item).

## Combat interception
1. Spawn a vanilla zombie with `/summon minecraft:zombie`.
2. Attack it with the normal melee input while the SkyBlock training sword is equipped.
3. Expected: the server-side `PlayerEntity.attack(Entity)` mixin cancels vanilla damage and runs the SkyBlock formula once.
4. With zero mob Defense, the formula produces 16 normal damage or 33 critical damage before Minecraft's own armor / shield processing.
5. Action-bar feedback should report the actual health loss and remaining target HP.
6. Repeat until the target dies. The current starter mapping grants 1 coin and 5 Combat XP for Zombie/Skeleton/Spider test analogues.

## Critical hit test
1. Attack a sufficiently large sample of mobs.
2. Expected: approximately 55% critical hits on a fresh profile with the test sword (30% base Crit Chance + 25 item Crit Chance).
3. Critical multiplier is 2.0 on a fresh profile with the test sword (50% base Crit Damage + 50 item Crit Damage).

## Fallback and safety tests
1. Unequip the SkyBlock weapon. Attack a zombie. Expected: vanilla combat remains unchanged.
2. Equip the sword and attack another player in a LAN test. Expected: PvP remains vanilla and is not intercepted.
3. Attack a non-living entity. Expected: vanilla behavior remains unchanged.
4. Attack an unregistered living entity (e.g. cow). Expected: SkyBlock melee calculation applies if using the SkyBlock weapon, but no coins or Combat XP are awarded.
5. Kill a mapped zombie, leave and re-enter the world. Expected: coins and Combat XP remain saved.

## Scope and data caveats
- This stage intercepts melee attacks against living non-player entities while a registered SkyBlock weapon is equipped.
- The starter-mob mapping is a test bridge from vanilla entities to the lowest-level Hypixel Private Island mob entries. Vanilla entity health is not yet scaled to Hypixel values.
- Hypixel mob Defense values are not published in the public SkyBlock API and are not asserted here; mapped test mobs currently use Defense 0.
- Combat XP / coin values are community-wiki-derived for the listed starter variants, not a complete official mob database.
- Additive damage bonuses, enchantments, Combat level, abilities, Ferocity, Attack Speed, ranged weapons, boss phases and custom mobs are not implemented.
- Full Gradle build and in-game behavior must be verified in the user's development environment.
