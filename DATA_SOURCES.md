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
  - Private Island range: 120–849 Health, 35–194 Damage, 5–30 Combat XP, 1–6 Coins. Current test mapping uses the minimum XP/coin values; exact lowest-level row should be confirmed before treating it as a production balance value.
- Mob-level/reward formulas: https://hypixelskyblock.minecraft.wiki/w/Mobs
  - The wiki documents formulas deriving mob level, Combat XP and coins for most mobs. Special mobs can use manually defined values.

## Project-only placeholders

- `training_sword` is a test item, not a Hypixel item.
- Vanilla Zombie/Skeleton/Spider entities are temporary stand-ins. Their Minecraft health is not scaled to the Hypixel mob values above.
- Mob Defense is currently 0 because no validated Defense value is being assigned to these test stand-ins.
- Unregistered mobs receive no coins or Combat XP. This prevents accidental farming rewards from villagers, animals and unrelated entities.

## Data policy for future stages

1. Prefer official Hypixel API data when an endpoint exists and retain its `lastUpdated` / `version` metadata.
2. Use community wiki pages for mechanics and mob values only with an explicit `community-wiki` provenance tag and retrieval date.
3. Keep derived values separate from directly listed values.
4. Do not convert an absent value into zero unless zero is a deliberate project default; mark unknown values as unknown.
5. Store source URL, retrieval timestamp, source version / game update and confidence alongside imported data.
