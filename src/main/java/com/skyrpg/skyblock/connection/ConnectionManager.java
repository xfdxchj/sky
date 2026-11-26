package com.skyrpg.skyblock.connection;

import com.skyrpg.skyblock.warp.WarpManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ConnectionManager {
    public record BuildResult(boolean success, String message, int placedBlocks) {}
    private static final int MAX_BRIDGE_BLOCKS = 4_096;
    private ConnectionManager() {}

    public static BuildResult buildBridge(ServerWorld world, ConnectionDefinition connection) {
        if (connection.type() != ConnectionDefinition.Type.BRIDGE)
            return fail("Only bridge connections can be built.");
        if (!world.getRegistryKey().getValue().toString().equals(connection.dimension()))
            return fail("Bridge dimension does not match the selected world.");

        Identifier blockId = Identifier.tryParse(connection.blockId());
        if (blockId == null) return fail("Invalid bridge block identifier.");
        BlockState deck = Registries.BLOCK.getOrEmpty(blockId).map(Block::getDefaultState).orElse(null);
        if (deck == null) return fail("Unknown bridge block: " + connection.blockId());

        double dx = connection.endX() - connection.startX();
        double dz = connection.endZ() - connection.startZ();
        double horizontalLength = Math.hypot(dx, dz);
        int steps = (int) Math.ceil(horizontalLength);
        double perpendicularX = -dz / horizontalLength;
        double perpendicularZ = dx / horizontalLength;
        Set<BlockPos> positions = new LinkedHashSet<>();
        int halfWidth = connection.width() / 2;

        for (int step = 0; step <= steps; step++) {
            double t = step / (double) steps;
            double centerX = connection.startX() + dx * t;
            double centerY = connection.startY() + (connection.endY() - connection.startY()) * t;
            double centerZ = connection.startZ() + dz * t;
            for (int offset = -halfWidth; offset <= halfWidth; offset++) {
                BlockPos pos = BlockPos.ofFloored(
                        centerX + perpendicularX * offset,
                        centerY,
                        centerZ + perpendicularZ * offset);
                positions.add(pos);
                if (positions.size() > MAX_BRIDGE_BLOCKS)
                    return fail("Bridge exceeds the " + MAX_BRIDGE_BLOCKS + "-block safety limit.");
            }
        }

        // Preflight every position before writing anything: bridge placement is all-or-nothing.
        for (BlockPos pos : positions) {
            if (pos.getY() < world.getBottomY() || pos.getY() >= world.getTopY())
                return fail("Bridge extends outside the world's build height.");
            if (!world.getWorldBorder().contains(pos))
                return fail("Bridge crosses the world border.");
            if (!world.getBlockState(pos).isAir())
                return fail("Bridge build cancelled: target is not empty at " + pos.toShortString() + ".");
        }

        int placed = 0;
        for (BlockPos pos : positions) {
            if (world.setBlockState(pos, deck, Block.NOTIFY_ALL)) placed++;
        }
        return new BuildResult(true, "Built bridge '" + connection.name() + "'.", placed);
    }

    public static WarpManager.WarpResult travel(ServerPlayerEntity player, ConnectionDefinition connection) {
        if (connection.type() != ConnectionDefinition.Type.PORTAL)
            return new WarpManager.WarpResult(false, "This connection is not a portal.");
        if (!player.getServerWorld().getRegistryKey().getValue().toString().equals(connection.dimension()))
            return new WarpManager.WarpResult(false, "You are in the wrong dimension for this portal.");
        double dx = player.getX() - connection.startX();
        double dy = player.getY() - connection.startY();
        double dz = player.getZ() - connection.startZ();
        if (dx * dx + dy * dy + dz * dz > connection.activationRadius() * connection.activationRadius())
            return new WarpManager.WarpResult(false, "Move closer to the portal entrance first.");
        return WarpManager.teleport(player, connection.targetWarpId());
    }

    private static BuildResult fail(String message) {
        return new BuildResult(false, message, 0);
    }
}
