# P2 Test Plan

Use a backup copy of the world. Run commands as an operator where required.

## 1. Item catalogue and grant
1. Run `/skyblock items` and confirm the built-in IDs are listed.
2. Run `/skyblock item give training_sword`.
3. Run `/skyblock item give iron_helmet 2`.
4. Run `/skyblock inventory` and confirm `training_sword x1` and `iron_helmet x2`.

## 2. Equip and stat calculation
1. Run `/skyblock stats` and note Strength, Health and Defense.
2. Run `/skyblock equip training_sword`.
3. Run `/skyblock equip iron_helmet`.
4. Run `/skyblock equipment` and confirm weapon and helmet slots.
5. Run `/skyblock stats`; Strength should increase by 10, Health by 15 and Defense by 10 over their base values.
6. Run `/skyblock unequip weapon`; Strength should return to its previous value.
7. Equip a second helmet while one is equipped; the old helmet should return to inventory.

## 3. Inventory constraints
1. Give enough `enchanted_iron` to exceed one stack, e.g. `/skyblock item give enchanted_iron 65`.
2. Confirm inventory slot usage reflects two stacks.
3. Fill inventory with different item stacks and confirm a grant is rejected when the 36-slot capacity would be exceeded.
4. Confirm an item cannot be equipped if it is not in the SkyBlock inventory or is not equippable.

## 4. Persistence and backward compatibility
1. Equip items and leave some materials in inventory.
2. Save and quit the world completely.
3. Reopen the same world; run `/skyblock inventory`, `/skyblock equipment` and `/skyblock stats`.
4. Confirm items, equipment and equipment bonuses persist.
5. Test using a copy of a P1 world/profile: profiles without an `inventory` NBT compound should load with an empty inventory and no crash.

## 5. Build check
- Set Gradle JVM to Java 21.
- Run `gradle build`.
- This source package was assembled in an environment without a Gradle executable, so a successful Gradle compilation is not claimed here.
