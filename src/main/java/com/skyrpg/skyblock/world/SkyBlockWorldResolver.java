package com.skyrpg.skyblock.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Resolves the project's dedicated void-based SkyBlock overworld.
 *
 * The world preset does not introduce a custom dimension key: it is still
 * minecraft:overworld. We therefore validate the expected void biome before
 * allowing SkyBlock-only allocations or teleports.
 */
public final class SkyBlockWorldResolver {
    private static final BlockPos VALIDATION_POS = new BlockPos(0, 64, 0);

    private SkyBlockWorldResolver() {}

    public static ServerWorld resolve(MinecraftServer server) {
        if (server == null) return null;
        ServerWorld overworld = server.getOverworld();
        return isSkyBlockWorld(overworld) ? overworld : null;
    }

    public static boolean isSkyBlockWorld(ServerWorld world) {
        if (world == null) return false;
        RegistryKey<World> key = world.getRegistryKey();
        if (!World.OVERWORLD.equals(key)) return false;
        return world.getBiome(VALIDATION_POS).matchesKey(BiomeKeys.THE_VOID);
    }
}
