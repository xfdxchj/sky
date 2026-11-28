# SkyBlock World Generation

## Current implementation

The mod provides a selectable Java Edition world preset:

- Preset ID: `skyblockrpg:skyblock`
- Display name: `SkyBlock RPG`
- World type: void Overworld using the vanilla flat chunk generator
- Overworld biome: `minecraft:the_void`
- Flat layers: one air layer; the vanilla Void biome feature supplies its small initial stone platform
- Random structure generation: disabled
- Nether and End: retain their vanilla generators for compatibility

This intentionally uses Minecraft's data-driven `WorldPreset` and `FlatChunkGenerator`. A custom Java `ChunkGenerator`, codec registration, and global Overworld mixin are unnecessary for a void world. The preset is isolated to worlds created with this world type; it does not change vanilla terrain generation in existing worlds.

## Creating a new SkyBlock world

1. Install the mod and launch Minecraft 1.21.1 with Fabric.
2. Create a **new** single-player world.
3. Open **More World Options** / **World** settings.
4. Cycle the **World Type** selector until **SkyBlock RPG** appears.
5. Create the world.

The preset is selectable; it is not silently forced onto every new world. Selecting Default instead still creates the normal vanilla Overworld.

## Important current limitations

This is the world-generation foundation, not the completed Hub opening flow:

- Existing worlds are not converted. World generation settings are fixed when a world is created.
- The vanilla Void biome start platform is only a safe initial spawn platform. It is not the Hypixel Hub.
- The real Hub NBT still needs its origin and spawn anchor audited before automatic assembly or player routing can be enabled.
- Hub auto-assembly and first-join teleport are not implemented by this change.
- Private Islands are not yet migrated to this dedicated void-world layout, and the hand-built starter island remains a temporary fixture.
- Map resources sourced from community downloads remain opt-in for local/private builds because redistribution permission is not verified.

## Next integration steps

1. Audit the Hub source NBT and establish its exact source origin, player spawn anchor, and vertical placement.
2. Auto-assemble the Hub into the new SkyBlock Overworld using the existing resumable map assembler.
3. Route first-time players to the verified Hub spawn only after assembly is complete.
4. Move private-island allocation into the same SkyBlock Overworld's reserved grid, retaining explicit player opt-in.
5. Replace the temporary hand-built starter island only when a trustworthy Private Island template and its usage rights are established.

## Validation

CI should validate that the preset and tag JSON parse and that the mod JAR contains both resources. A successful Gradle build does not replace the in-game check: create a fresh world with the SkyBlock RPG world type, confirm the Overworld is void apart from the vanilla start platform, and verify Nether/End still load normally.
