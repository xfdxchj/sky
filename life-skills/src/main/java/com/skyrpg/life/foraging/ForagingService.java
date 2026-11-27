package com.skyrpg.life.foraging;

import com.skyrpg.life.mining.MiningTool;
import com.skyrpg.life.mining.MiningToolType;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** Server-side Foraging entry point; tree handling lives in ForagingTreeManager. */
public final class ForagingService {
    private ForagingService() {}

    /** Called from the BEFORE event; returning false blocks the vanilla break. */
    public static boolean canBreak(ServerPlayerEntity player, BlockState state) {
        TreeDefinition tree = ForagingRegistry.byLog(Registries.BLOCK.getId(state.getBlock())).orElse(null);
        if (tree == null) {
            return true;
        }
        return MiningTool.typeOf(player.getMainHandStack()) == MiningToolType.AXE;
    }

    public static void onBlockBroken(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state) {
        TreeDefinition tree = ForagingRegistry.byLog(Registries.BLOCK.getId(state.getBlock())).orElse(null);
        if (tree == null) {
            return;
        }
        ForagingTreeManager.breakLogs(player, world, pos, tree);
    }
}
