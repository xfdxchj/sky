package com.skyrpg.skyblock.island;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

/** Creates and manages one persistent private island per player. */
public final class PrivateIslandManager {
    public record CreationResult(boolean success, boolean created, String message, PrivateIslandRecord island) {}

    private static final int BASE_X = 8192;
    private static final int BASE_Z = 8192;
    private static final int GRID_COLUMNS = 64;
    private static final int ISLAND_SPACING = 512;
    private static final int SURFACE_Y = 270;
    private static final int RADIUS = 32;
    private static final int THICKNESS = 20;
    private static final int MAX_ISLANDS = 100_000;

    private PrivateIslandManager() {}

    public static CreationResult create(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) {
            return new CreationResult(false, false, "A server player is required.", null);
        }
        PrivateIslandWorldData data = PrivateIslandWorldData.get(player.getServer());
        PrivateIslandRecord existing = data.get(player.getUuid());
        if (existing != null) {
            return new CreationResult(true, false, "Your private island already exists.", existing);
        }

        int slot = data.getNextSlot();
        if (slot >= MAX_ISLANDS) {
            return new CreationResult(false, false, "Private island allocation limit reached.", null);
        }

        int column = slot % GRID_COLUMNS;
        int row = slot / GRID_COLUMNS;
        int centerX = BASE_X + column * ISLAND_SPACING;
        int centerZ = BASE_Z + row * ISLAND_SPACING;
        ServerWorld world = player.getServer().getOverworld();
        if (SURFACE_Y + 8 >= world.getTopY() || SURFACE_Y - THICKNESS - 2 < world.getBottomY()) {
            return new CreationResult(false, false, "The configured private island height is outside this world's build range.", null);
        }

        if (!volumeIsClear(world, centerX, centerZ)) {
            data.skipToSlot(slot + 1);
            return new CreationResult(false, false,
                    "The reserved sky volume is not clear. That allocation slot was skipped; run the create command again.", null);
        }

        // The main island is offset to the west; the secondary island hosts the cave,
        // starter chest, Jerry, decorative Hub portal and a visual Minion pedestal.
        double spawnX = centerX - 14.5;
        double spawnY = SURFACE_Y + 6.0;
        double spawnZ = centerZ + 0.5;

        PrivateIslandStarterLayout.Result generated =
                PrivateIslandStarterLayout.generate(world, centerX, centerZ, SURFACE_Y);
        if (!generated.success() || generated.placedBlocks() == 0) {
            data.skipToSlot(slot + 1);
            return new CreationResult(false, false, "Private island generation failed: " + generated.message()
                    + " The allocation slot was skipped; run the create command again.", null);
        }

        PrivateIslandRecord record = new PrivateIslandRecord(
                player.getUuid(), world.getRegistryKey().getValue().toString(),
                centerX, centerZ, SURFACE_Y, RADIUS, THICKNESS,
                spawnX, spawnY, spawnZ, System.currentTimeMillis()
        );
        data.add(record, slot + 1);
        return new CreationResult(true, true,
                "Private island created. " + generated.message() + " Placed " + generated.placedBlocks() + " blocks.",
                record);
    }

    public static boolean visitHome(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) return false;
        PrivateIslandRecord island = PrivateIslandWorldData.get(player.getServer()).get(player.getUuid());
        if (island == null) return false;
        Identifier dimensionId = Identifier.tryParse(island.dimension());
        if (dimensionId == null) return false;
        var key = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, dimensionId);
        ServerWorld target = player.getServer().getWorld(key);
        if (target == null || island.spawnY() < target.getBottomY() || island.spawnY() >= target.getTopY()) return false;
        BlockPos spawn = BlockPos.ofFloored(island.spawnX(), island.spawnY(), island.spawnZ());
        if (!target.getWorldBorder().contains(spawn)) return false;
        player.teleport(target, island.spawnX(), island.spawnY(), island.spawnZ(), Set.of(), player.getYaw(), player.getPitch());
        return true;
    }

    public static PrivateIslandRecord get(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) return null;
        return PrivateIslandWorldData.get(player.getServer()).get(player.getUuid());
    }

    /** Returns true when a position belongs to another player's protected island. */
    public static boolean isProtectedFrom(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (player == null || world == null || pos == null) return false;
        for (PrivateIslandRecord island : PrivateIslandWorldData.get(world.getServer()).all()) {
            if (!island.dimension().equals(world.getRegistryKey().getValue().toString())) continue;
            if (island.containsProtectedPosition(pos.getX(), pos.getY(), pos.getZ())) {
                return !island.ownerId().equals(player.getUuid());
            }
        }
        return false;
    }

    private static boolean volumeIsClear(ServerWorld world, int centerX, int centerZ) {
        int minY = SURFACE_Y - THICKNESS - 2;
        int maxY = SURFACE_Y + 8;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int dx = -RADIUS - 2; dx <= RADIUS + 2; dx++) {
            for (int dz = -RADIUS - 2; dz <= RADIUS + 2; dz++) {
                double nx = dx / (double) (RADIUS + 2);
                double nz = dz / (double) (RADIUS + 2);
                if (nx * nx + nz * nz > 1.0) continue;
                for (int y = minY; y <= maxY; y++) {
                    pos.set(centerX + dx, y, centerZ + dz);
                    if (!world.getWorldBorder().contains(pos) || !world.getBlockState(pos).isAir()) return false;
                }
            }
        }
        return true;
    }
}
