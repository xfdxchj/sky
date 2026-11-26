# Core API v2 Test Plan — Life Skills Integration

**Status:** Test plan written; source-level implementation submitted; local build/runtime tests pending.

## A. Skill and profile data

1. Start a world with Core only; confirm startup succeeds.
2. Load a dependent test mod and register `skyblock_life:mining`, `fishing`, `farming`, `foraging`.
3. Verify duplicate skill registration returns false.
4. Award XP using SkillAPI; query level, total XP, within-level progress and XP remaining to next level.
5. Write separate NBT compounds under all four Life Skills keys.
6. Stop/restart the world and verify values persist.
7. Remove Life Skills, load/save the world, reinstall Life Skills and verify its data remains.

## B. Stats

1. Query Mining Speed, Mining Fortune, Farming Fortune, Foraging Speed, Foraging Fortune, Fishing Speed and Sea Creature Chance through StatAPI.
2. Confirm values match Core's existing final-stat pipeline.
3. Equip an existing Core test item and confirm its supported modifiers are reflected.
4. Confirm the extension cannot mutate Core base stats through the public API.
5. Confirm unknown/custom StatType registration is not assumed to exist.

## C. Extension item datapack

1. Add a test definition at `data/skyblock_life/skyblock/items/test_material.json`.
2. Use ID `skyblock_life:test_material`.
3. Start a world and verify ItemAPI resolves the definition.
4. Verify ItemAPI can still resolve legacy Core IDs and namespaced Core IDs.
5. Add the item to the logical inventory through InventoryAPI.
6. Save/restart and verify the item count persists.
7. Verify duplicate IDs and invalid stat keys are logged/rejected without preventing unrelated definitions from loading.

## D. Economy and rewards

1. Add and remove Purse coins through CurrencyAPI.
2. Deposit and withdraw between Purse and Bank.
3. Verify insufficient funds, zero/negative values and overflow fail safely.
4. Grant an item, coins and skill XP through RewardAPI.
5. Fill the logical inventory and verify item reward returns false without losing other rewards.
6. Verify every successful mutation persists after restart.

## E. Regression / compatibility

- Build Core with Java 21.
- Start Core without optional mods.
- Start Core plus a minimal Life Skills test mod.
- Verify the extension can use only public API packages.
- Verify no Life Skills class is required by Core.
- Verify existing Core item IDs, equipment, shop and commands still resolve.
- Run all previous P5.4 and P5.3 regression checks.
