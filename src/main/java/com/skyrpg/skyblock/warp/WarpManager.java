package com.skyrpg.skyblock.warp;

import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public final class WarpManager {
    public record WarpResult(boolean success, String message) {}
    private WarpManager() {}

    public static WarpResult teleport(ServerPlayerEntity player, String warpId) {
        WarpDefinition warp = WarpRegistry.get(warpId).orElse(null);
        if (warp == null) return fail("Unknown warp: " + warpId);
        if (!warp.requiredRegionId().isBlank()
                && !ProfileManager.get(player).hasDiscoveredRegion(warp.requiredRegionId()))
            return fail("Warp locked. Discover region '" + warp.requiredRegionId() + "' first.");

        Identifier dimensionId = Identifier.tryParse(warp.dimension());
        if (dimensionId == null) return fail("Warp has an invalid dimension: " + warp.dimension());
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimensionId);
        ServerWorld targetWorld = player.getServer().getWorld(worldKey);
        if (targetWorld == null) return fail("Warp dimension is not loaded: " + warp.dimension());

        if (warp.y() < targetWorld.getBottomY() || warp.y() >= targetWorld.getTopY())
            return fail("Warp Y coordinate is outside the target world's build height.");
        BlockPos targetPos = BlockPos.ofFloored(warp.x(), warp.y(), warp.z());
        if (!targetWorld.getWorldBorder().contains(targetPos))
            return fail("Warp destination is outside the world border.");

        player.teleport(targetWorld, warp.x(), warp.y(), warp.z(), Set.of(), warp.yaw(), warp.pitch());
        return new WarpResult(true, "Warped to " + warp.displayName()
                + (warp.description().isBlank() ? "." : ": " + warp.description()));
    }

    private static WarpResult fail(String message) {
        return new WarpResult(false, message);
    }
}
