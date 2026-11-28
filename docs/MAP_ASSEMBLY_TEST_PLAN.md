# Map assembly test plan

## Build and registration

1. Run .\gradlew.bat build -PincludeCommunityMaps=true.
2. Launch a disposable Minecraft 1.21.1 Fabric world with the map-enabled JAR.
3. Run /skyblock map list; verify Hub and Gold Mine are registered.
4. Run /skyblock map info hub; verify 384x157x336 and all slices are present.
5. Check latest.log for map/structure registry errors.

## Safe generation

1. Create a new creative **Superflat** test world. The flat ground is below the test volume,
   leaving the upper build area empty.
2. Use origin 0,80,0 for this isolated assembly test only. This is not the production Hub
   coordinate transform and must not be copied into the final Hub POI / Warp data.
3. Run /skyblock map generate hub 0 80 0.
4. Poll /skyblock map status hub. After completion, teleport to the middle of the map
   (approximately 192,160,168) to inspect the assembled terrain.
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
