# Map assembly test plan

## Build and registration

1. Run .\gradlew.bat build -PincludeCommunityMaps=true.
2. Launch a disposable Minecraft 1.21.1 Fabric world with the map-enabled JAR.
3. Run /skyblock map list; verify Hub and Gold Mine are registered.
4. Run /skyblock map info hub; verify 384x157x336 and all slices are present.
5. Check latest.log for map/structure registry errors.

## Safe generation

1. Use a new creative test world.
2. Choose an isolated empty target cuboid. Keep the production Hub origin unassigned until
   the original NBT coordinate frame is verified.
3. Run /skyblock map generate hub <x> <y> <z>.
4. Poll /skyblock map status hub.
5. Verify PREFLIGHT advances without placing blocks, then PLACING advances one slice at a
   time, and finally COMPLETE is recorded.
6. Verify all source blocks appear at origin + source-local coordinates.
7. Verify no pre-existing blocks were overwritten.

## Failure and recovery

- Put a single obstruction anywhere inside the target cuboid. Preflight must fail and the
  world must remain unchanged by the map assembler.
- Save/restart during PREFLIGHT. Preflight should safely restart from the beginning.
- Save/restart between slices. Placement should continue from the next unplaced slice.
- Interrupt during a slice if practical. The record must become INTERRUPTED and must not
  blindly place that slice again.
- Restart after COMPLETE. The map must not generate a second time.
- Try an invalid dimension, out-of-height origin, world-border overflow, missing template,
  and a template-size mismatch; each must fail before any placement.

## Not yet validated

The command and generation logic still require a local Gradle build and in-game execution.
CI build success is not a substitute for Minecraft world-generation acceptance.
