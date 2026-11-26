package com.skyrpg.skyblock.island;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public final class IslandManager {
    public record GenerationResult(boolean success, String message, int placedBlocks, int skippedBlocks) {}
    private static final int MAX_COLUMNS = 300_000;
    private IslandManager() {}

    public static GenerationResult generate(ServerWorld world, IslandDefinition island) {
        if (!world.getRegistryKey().getValue().toString().equals(island.dimension()))
            return fail("Island dimension does not match the selected world.");
        long columns = (long) (island.radiusX() * 2 + 1) * (island.radiusZ() * 2L + 1L);
        if (columns > MAX_COLUMNS) return fail("Island footprint exceeds the generation safety limit.");
        if (island.surfaceY() >= world.getTopY() || island.surfaceY() - island.thickness() < world.getBottomY())
            return fail("Island vertical range is outside the world's build height.");

        BlockState top = blockState(island.topBlock()).orElse(null);
        BlockState soil = blockState(island.soilBlock()).orElse(null);
        BlockState core = blockState(island.coreBlock()).orElse(null);
        if (top == null || soil == null || core == null) return fail("Island block palette contains an unknown block.");

        int placed = 0;
        int skipped = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int dx = -island.radiusX(); dx <= island.radiusX(); dx++) {
            for (int dz = -island.radiusZ(); dz <= island.radiusZ(); dz++) {
                double nx = dx / (double) island.radiusX();
                double nz = dz / (double) island.radiusZ();
                double radial = 1.0 - nx * nx - nz * nz;
                if (radial <= 0.0) continue;
                double wave = Math.sin((island.centerX() + dx) * 0.17) * 1.2
                        + Math.cos((island.centerZ() + dz) * 0.13) * 1.0;
                int columnTop = island.surfaceY() + (int) Math.round(wave * Math.sqrt(radial));
                int depth = Math.max(2, (int) Math.ceil(island.thickness() * Math.sqrt(radial)));
                for (int y = columnTop - depth; y <= columnTop; y++) {
                    pos.set(island.centerX() + dx, y, island.centerZ() + dz);
                    if (!world.getWorldBorder().contains(pos)) {
                        skipped++;
                        continue;
                    }
                    if (!world.getBlockState(pos).isAir()) {
                        skipped++;
                        continue;
                    }
                    BlockState state = y == columnTop ? top
                            : y >= columnTop - 2 ? soil : core;
                    if (world.setBlockState(pos, state, Block.NOTIFY_ALL)) placed++;
                }
            }
        }
        return new GenerationResult(true, "Generated island '" + island.name() + "'. Existing blocks were preserved.",
                placed, skipped);
    }

    public static boolean visit(ServerPlayerEntity player, IslandDefinition island) {
        Identifier dimensionId = Identifier.tryParse(island.dimension());
        if (dimensionId == null) return false;
        var worldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, dimensionId);
        ServerWorld target = player.getServer().getWorld(worldKey);
        if (target == null || island.spawnY() < target.getBottomY() || island.spawnY() >= target.getTopY()) return false;
        BlockPos pos = BlockPos.ofFloored(island.spawnX(), island.spawnY(), island.spawnZ());
        if (!target.getWorldBorder().contains(pos)) return false;
        player.teleport(target, island.spawnX(), island.spawnY(), island.spawnZ(), java.util.Set.of(), player.getYaw(), player.getPitch());
        return true;
    }

    private static Optional<BlockState> blockState(String raw) {
        Identifier id = Identifier.tryParse(raw);
        if (id == null) return Optional.empty();
        return Registries.BLOCK.getOrEmpty(id).map(Block::getDefaultState);
    }
    private static GenerationResult fail(String message) { return new GenerationResult(false, message, 0, 0); }
}
