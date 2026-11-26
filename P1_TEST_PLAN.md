# P1 Test Plan

## Build gate
1. Open in IntelliJ with Gradle JVM set to JDK 21.
2. Run `gradle build`.
3. Confirm a remapped JAR appears in `build/libs/`.

## In-game checks
1. Start a new single-player world and join.
2. `/skyblock profile` should show the auto-created profile.
3. `/skyblock coins` should initially show 0.
4. `/skyblock coins add 1000` should set balance to 1000 (requires permission level 2).
5. `/skyblock coins remove 250` should set balance to 750.
6. `/skyblock skill combat` should initially show level 0.
7. `/skyblock skill combat add 250` should reach level 2 using the temporary P1 curve.
8. `/skyblock stats` should run and show any non-zero base stats.
9. Save, quit and reopen the world; verify coins and XP persist.
10. If testing a second player in LAN, confirm UUID-keyed profiles are independent.

## Caveats
The XP curve is provisional and must not be represented as current Hypixel balance. Stat modifiers and UI are outside P1.
