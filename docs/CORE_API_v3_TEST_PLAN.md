# Core API v3 Test Plan — Life Skills Integration

**Status:** Source changes submitted; local Gradle build and runtime verification pending.

## A. Skill registration and progression

1. Start Core plus the independent smoke-test extension.
2. Confirm Core API version is 3 or newer.
3. Register the four extension skills and confirm duplicate registration fails.
4. Confirm the smoke fixture registers one Mining Fortune reward at Mining level 1.
5. Award XP and verify the level changes according to the registered XP curve.
6. Confirm reward is inactive below its threshold and active at/above the threshold.
7. Register a second reward at a higher level and confirm both threshold rewards are active after reaching the higher level.
8. Confirm duplicate reward key, unknown skill, invalid level, null stat/operation and non-finite amount are rejected.
9. Confirm extension API cannot register a reward for the reserved skyblockrpg skill namespace.
10. Confirm skill rewards use StatSource.SKILL and can be isolated with PlayerProfile.getFinalStat(stat, Set.of(StatSource.SKILL)) in an internal test.
11. Restart world and verify XP persists; reward definitions should be re-registered by the extension on each startup and apply to the saved XP.

## B. Temporary stat effect API

1. Use StatEffectAPI.apply with extension-owned matching namespaces for effectId/sourceId.
2. Confirm BUFF, TEMPORARY, OTHER are accepted; BASE, EQUIPMENT, ACCESSORY, SKILL are rejected.
3. Confirm Core namespace IDs, mismatched namespaces, null IDs and invalid durations are rejected.
4. Confirm same effect ID refreshes and distinct IDs coexist.
5. Confirm remove requires the extension's owner namespace and full namespaced effect ID; mismatched owner namespace is rejected.
6. Confirm timed effects persist and expire according to Overworld game time.

## C. Regression

1. Build Core with Java 21.
2. Copy the remapped Core jar to the smoke-test project's libs/ and build the smoke project.
3. Run Core-only and Core + smoke extension.
4. Verify extension data, items, inventory, currency and rewards still work.
5. Confirm Core starts without optional extensions.
6. Confirm no optional Life Skills class is referenced by Core.

## D. Local verification record

- Source implementation and documentation submitted to GitHub.
- No Gradle build has been run by the assistant.
- No runClient or in-game test has been performed.