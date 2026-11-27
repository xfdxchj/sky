# P1-01 Stat Pipeline Test Plan

**Status:** Source changes submitted; local Gradle build and in-game verification pending.

## 1. Build and startup

1. Pull the latest `main`.
2. Run `gradlew build` with Java 21.
3. Start a fresh 1.21.1 Fabric world with Core only.
4. Confirm startup and existing profile loading complete without errors.

## 2. Base stat compatibility

1. Query baseline stats on a new profile.
2. Verify existing baseline values remain unchanged (including the current Critical Chance and Critical Damage defaults).
3. Set and reload existing base-stat NBT values using the current profile persistence path.
4. Confirm base stats are not counted as an equipment/accessory modifier.

## 3. Equipment source

1. Equip an item with one or more stat entries.
2. Query the final stat and confirm the equipment contribution is applied once.
3. Apply a compatible Reforge.
4. Confirm Reforge bonuses are also classified as EQUIPMENT and applied once.
5. Unequip the item and confirm both item and Reforge contributions disappear.

## 4. Accessory source

1. Add accessories with stat entries to the accessory bag.
2. Confirm only effective accessories contribute (duplicate/upgrade-line resolution remains unchanged).
3. Select a Power and confirm its bonus is classified as ACCESSORY.
4. Allocate Tuning points and confirm Tuning is classified as ACCESSORY.
5. Remove an accessory or change Power/Tuning and verify the derived modifier snapshot updates immediately.

## 5. Source filtering

Using `PlayerProfile.getFinalStat(type, includedSources)`:

- Include only EQUIPMENT and verify accessory contributions are excluded.
- Include only ACCESSORY and verify equipment/Reforge contributions are excluded.
- Include both and verify the result equals the ordinary `getFinalStat(type)`.
- Pass an empty source set and verify only the base value remains.
- Pass null as the source set and verify backwards-compatible inclusion of all modifiers.

## 6. Regression

- Existing four-argument `StatModifier` construction remains source-compatible.
- Legacy source IDs beginning with `equipment:`, `reforge:`, `accessory:`, `accessory_power:` and `accessory_tuning:` infer the expected categories.
- Unknown legacy source IDs classify as OTHER.
- Existing equipment, accessory, Power, Tuning, Reforge, stats commands and profile save/load continue to work.
- Verify no stat contribution is duplicated after save/reload.

## 7. Known scope limits

This stage does not yet implement Skill, Buff, Temporary or Other modifier registration/removal lifecycles. It does not add modifier expiration, stacking policy, stat clamps, or dynamic StatType registration. Those remain separate P1 work.
