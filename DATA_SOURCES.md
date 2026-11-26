# SkyBlock Data Sources and Confidence

Research snapshot: 2026-11-26.

This file separates official Hypixel data interfaces from community-maintained game documentation and project placeholders.

## Official Hypixel sources

### Hypixel Public API v2
- Documentation: https://api.hypixel.net/
- Official SkyBlock items resource: https://api.hypixel.net/v2/resources/skyblock/items
- Official SkyBlock collections resource: https://api.hypixel.net/v2/resources/skyblock/collections
- Official SkyBlock skills resource: https://api.hypixel.net/v2/resources/skyblock/skills

The API documentation describes these endpoints as resources for SkyBlock items, collections and skills. The public resource list does not expose a complete mob catalogue with mob health, mob Defense, Combat XP and coin rewards. Do not infer missing mob values from item/collection endpoints.

## Community-maintained Hypixel SkyBlock Wiki references

These are useful current references, but they are not official Hypixel API data and can be edited by the community.

- Melee damage calculation: https://hypixelskyblock.minecraft.wiki/w/Damage_Calculation
  - Simplified melee core: `(5 + WeaponDamage) * (1 + Strength / 100) * (1 + CritDamage / 100)` on a critical hit, before additional multipliers and target mitigation.
  - Defense damage-taken multiplier: `100 / (100 + Defense)`.
- Combat stat baseline: https://hypixelskyblock.minecraft.wiki/w/Stats
  - Base Crit Chance: 30%.
  - Base Crit Damage: 50%.
- Zombie: https://hypixelskyblock.minecraft.wiki/w/Zombie
  - Private Island Lv1 entry: 100 Health, 20 Damage, 5 Combat XP, 1 Coin.
- Skeleton: https://hypixelskyblock.minecraft.wiki/w/Skeleton
  - Private Island Lv1 entry: 100 Health, 15 Damage, 5 Combat XP, 1 Coin.
- Spider: https://hypixelskyblock.minecraft.wiki/w/Spider
  - Private Island Lv3 entry: 120 Health, 35 Damage, 5 Combat XP, 1 Coin.
- Graveyard: https://hypixelskyblock.minecraft.wiki/w/Graveyard
  - Graveyard Zombie: Lv1, 100 Health, 20 Damage, 5 Combat XP, 1 Coin.
  - Zombie Villager: Lv2, 120 Health, 24 Damage, 5 Combat XP, 1 Coin.
- Crypt Ghoul: https://hypixelskyblock.minecraft.wiki/w/Crypt_Ghoul
  - Lv33, 2,000 Health, 350 Damage, 49 Combat XP, 11 Coins.
- Crypts: https://hypixelskyblock.minecraft.wiki/w/Crypts
  - Golden Ghoul: Lv65, 20,000 Health, 500 Damage, 97 Combat XP, 21 Coins.
- Ruins: https://hypixelskyblock.minecraft.wiki/w/Ruins
  - Wolf: Lv11, 500 Health, 75 Damage, 16 Combat XP, 3 Coins.
  - Old Wolf: Lv67, 15,000 Health, 750 Damage, 100 Combat XP, 22 Coins.
- Mob-level/reward formulas: https://hypixelskyblock.minecraft.wiki/w/Mobs
  - The wiki documents formulas deriving mob level, Combat XP and coins for most mobs. Special mobs can use manually defined values.

## Project-only placeholders

- `training_sword` is a test item, not a Hypixel item.
- Vanilla Zombie/Skeleton/Spider entities are temporary stand-ins. P3.3 adds a persistent SkyBlock HP pool for registered mobs, with vanilla health synchronized proportionally.
- P3.2 stores starter mob reference values in `src/main/resources/data/skyblockrpg/skyblock/mobs/`. Zombie and Skeleton use their listed Private Island Lv1 values; Spider uses the minimum of the documented range pending exact row verification.
- Mob Defense is 0 as an explicit project placeholder; it is not presented as a verified Hypixel stat.
- Unregistered mobs receive no coins or Combat XP. This prevents accidental farming rewards from villagers, animals and unrelated entities.

## Data policy for future stages

1. Prefer official Hypixel API data when an endpoint exists and retain its `lastUpdated` / `version` metadata.
2. Use community wiki pages for mechanics and mob values only with an explicit `community-wiki` provenance tag and retrieval date.
3. Keep derived values separate from directly listed values.
4. Do not convert an absent value into zero unless zero is a deliberate project default; mark unknown values as unknown.
5. Store source URL, retrieval timestamp, source version / game update and confidence alongside imported data.
6. A missing drop probability is represented with `chance_known: false`; never guess it or treat it as a guaranteed drop.
7. Wiki pages may change without an API version. Retain the retrieval date and, where useful, relevant changelog references.
