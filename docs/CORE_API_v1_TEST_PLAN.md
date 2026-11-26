# Core Module API v1 Test Plan

## A. Skill registration

1. Start a world with Core only.
2. Confirm all built-in skills are registered.
3. Run `/skyblock skill mining` and `/skyblock skill combat`.
4. Register an extension skill with ID `skyblock_life:mining` from a dependent test mod.
5. Confirm `/skyblock skill skyblock_life:mining` resolves it.
6. Award XP through `SkillAPI.addExperience`; verify XP and level.
7. Attempt duplicate registration; verify it returns false and does not replace the original curve.
8. Attempt to award XP to an unregistered ID; verify it is rejected.

## B. Legacy profile migration

1. Use a copy of a pre-v8 world save.
2. Load the world with the new Core.
3. Confirm old bare skill keys (e.g. `mining`, `combat`) are loaded as `skyblockrpg:mining` and `skyblockrpg:combat`.
4. Confirm purse, bank, inventory, stats, quests, region discovery, powers and tuning remain unchanged.
5. Save, stop, and restart.
6. Confirm built-in XP values remain unchanged after the first and second reload.

## C. Extension data persistence

1. Write `skyblock_life:mining` with a compound containing a counter and nested compound.
2. Mutate the compound returned by `get` without calling `set`; verify persisted data is unchanged.
3. Call `set`, stop the world, and restart; verify the data is present.
4. Remove the optional extension from the mods folder and restart.
5. Save and stop; reinstall the extension and verify its data was retained.
6. Verify one extension cannot overwrite another extension's different Identifier key.

## D. Regression

- Run `/skyblock skill <built-in-id>` for each built-in skill.
- Run `/skyblock skill <built-in-id> add <amount>` with permission.
- Verify invalid IDs and invalid amounts are rejected.
- Verify Core-only startup works without optional extensions.
- Verify the extension is loaded only when Core is installed.
