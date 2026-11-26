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

## Community data repositories

### NotEnoughUpdates-REPO
- Repository: https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO
- License: MIT (check the repository's current LICENSE before reusing any assets or code).
- The repository includes a `mobs/` directory, but inspected entries such as `mobs/ent.json` describe entity render/model modifiers and player skins rather than a complete authoritative combat-stat table.
- Use NEU mob JSON as a reference for appearance/entity metadata and item repository formats; do not treat it as the source of HP, Damage, Defense, Combat XP or coin values unless an individual field is explicitly present.

## Community-maintained Hypixel SkyBlock Wiki references

These are useful current references, but they are not official Hypixel API data and can be edited by the community. Some pages explicitly warn that recently added or changing content may be incomplete or inaccurate; treat imported rows as sourced community data, not guaranteed ground truth.

- Melee damage calculation: https://hypixelskyblock.minecraft.wiki/w/Damage_Calculation
  - Simplified melee core: `(5 + WeaponDamage) * (1 + Strength / 100) * (1 + CritDamage / 100)` on a critical hit, before additional multipliers and target mitigation.
  - Defense damage-taken multiplier: `100 / (100 + Defense)`.
- Combat stat baseline: https://hypixelskyblock.minecraft.wiki/w/Stats
  - Base Crit Chance: 30%.
  - Base Crit Damage: 50%.
- Zombie: https://hypixelskyblock.minecraft.wiki/w/Zombie
  - All 15 Private Island variant rows are imported as individual JSON records (ZOMBIE_1 through ZOMBIE_15), preserving the listed level, HP, Damage, Combat XP and coin values.
- Skeleton: https://hypixelskyblock.minecraft.wiki/w/Skeleton
  - All 15 Private Island variant rows are imported as individual JSON records (SKELETON_1 through SKELETON_15).
- Spider: https://hypixelskyblock.minecraft.wiki/w/Spider
  - All 15 Private Island variant rows are imported as individual JSON records (SPIDER_1 through SPIDER_15).
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

- The Park: https://hypixelskyblock.minecraft.wiki/w/The_Park
  - Pack Spirit: Lv43, 6,000 Health, 300 Damage, 64 Combat XP, 14 Coins.
  - Howling Spirit: Lv51, 7,000 Health, 500 Damage, 76 Combat XP, 17 Coins.
  - Soul of the Alpha: Lv90, 31,150 Health, 1,425 Damage, 135 Combat XP, 30 Coins.
- Deep Caverns examples:
  - Sneaky Creeper: https://hypixelskyblock.minecraft.wiki/w/Sneaky_Creeper — Lv5, 120 Health, 80 Damage, 7 Combat XP, 1 Coin.
  - Lapis Zombie: https://hypixelskyblock.minecraft.wiki/w/Lapis_Zombie — Lv6, 200 Health, 50 Damage, 9 Combat XP, 2 Coins.
  - Redstone Pigman: https://hypixelskyblock.minecraft.wiki/w/Redstone_Pigman — Lv8, 250 Health, 75 Damage, 12 Combat XP, 2 Coins.
  - Emerald Slime: https://hypixelskyblock.minecraft.wiki/w/Cubic_%28Mob_Type%29 — Lv4, 80 Health, 70 Damage, 6 Combat XP, 1 Coin.
- Island coverage notes: the current Wiki's Mobs page states that the Gold Mine is the only public island without mobs. The Farming Islands do have passive animal families; these are better represented in a later farming/collection milestone rather than the current hostile-combat bridge.

## Community-wiki mob level and reward formulas

The current Mobs page documents that HP and Damage determine Mob Level, and that Mob Level generally determines skill XP, coins and experience orbs. For the standard `Mob` category, the page currently lists:

- Mob Level: `floor((log10(HP * Damage) - 2) ^ 2.6)`, minimum 1.
- Skill XP: `floor(Mob Level * 1.5)`, minimum +5 Combat XP (or +25 Fishing XP).
- Coins: `floor(Mob Level / 3)`, minimum 1 coin.
- Experience orbs: `floor(Mob Level * 1.1)`, minimum 1.

The Wiki notes that these formulas apply to the overwhelming majority of mobs, while special categories (Pests, Minibosses, Bosses, Slayer mobs and Sea Creatures) have separate formulas or manually set values. The imported starter JSON keeps the published per-mob values for auditability. A future reward-calculation milestone should model reward mode/category explicitly rather than assuming every mob follows the standard formula.

Source: https://hypixelskyblock.minecraft.wiki/w/Mobs (Mob Levels section; current page history lists the 2026-10-06 0.27.2 change).

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
