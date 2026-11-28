# WT-1 Hub runtime and player entry flow

## Runtime gate

Hub routing is implemented but deliberately fail-closed. A player is sent to Hub on join/rejoin only when all of these are true:

1. The local/private build includes the community Hub resources (`-PincludeCommunityMaps=true`).
2. The logical map registry has loaded `hub`.
3. `MapWorldData` records the Hub assembly as `COMPLETE`.
4. An operator has configured a safe Hub landing point with `/skyblock hub setspawn`.
5. The saved landing point remains inside the assembled Hub bounds, inside the world border, and has a solid floor plus two air blocks.

Until all checks pass, players remain at the dedicated SkyBlock world's safe preset spawn. The mod does not use the Wiki Village reference coordinate as a teleport target and does not create a private island automatically.

## Local setup and test

Use a disposable local/private instance because the community map redistribution permission is not verified.

```powershell
.\gradlew.bat build -PincludeCommunityMaps=true
```

1. Install the built JAR and create a new world with the **SkyBlock RPG** world preset.
2. Confirm `/skyblock map list` contains `hub`.
3. In the disposable test world, choose a clear origin with enough build-height headroom. The origin is the lower corner of the imported map, not the player spawn.
4. Run `/skyblock map generate hub <x> <y> <z>` and wait for `/skyblock map status hub` to report `COMPLETE`.
5. Preview the imported terrain and physically stand at the intended Village landing point, facing the intended direction.
6. As an operator, run `/skyblock hub setspawn`.
7. Run `/skyblock hub status`, then `/skyblock hub visit`.
8. Disconnect and rejoin. The player should enter Hub, not the private island.
9. Open `/skyblock menu`; use **Create Private Island** to create and enter the island. Use **Private Island Home** to return later and **Hub** to return to the public Hub.
10. Rejoin again; the player should start in Hub while the private island remains persistent.

## Commands

- `/skyblock hub status` — reports map resource, assembly state and spawn configuration.
- `/skyblock hub setspawn` — operator-only; saves the current safe standing position as the Hub spawn.
- `/skyblock hub visit` — teleports to the validated Hub spawn.
- `/skyblock map list|info|generate|status` — map assembly tools.

## Scope and limitations

- The first-join/rejoin route is runtime-gated; it does not invent a spawn offset.
- Hub map POI catalogue coordinates are sourced reference points and are not assumed to match the NBT structure's local coordinate system.
- Hub NBT contains no entities or block entities. NPCs, containers and other interactive content still require explicit runtime definitions.
- CI verifies compilation, NBT slicing and in-JAR resource packaging; only a local Minecraft run can verify the actual map appearance and selected spawn.