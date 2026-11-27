package com.skyrpg.life.mining;

import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.life.common.data.LifeDataStore;
import com.skyrpg.life.common.drop.DropTable;
import com.skyrpg.life.skill.LifeSkills;
import com.skyrpg.skyblock.api.RewardAPI;
import com.skyrpg.skyblock.api.SkillAPI;
import com.skyrpg.skyblock.api.StatAPI;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Server-side Mining processing. Driven by Fabric block-break events; Core never sees it. */
public final class MiningService {
    private MiningService() {}

    /** Called from the BEFORE event; returning false blocks the vanilla break. */
    public static boolean canBreak(ServerPlayerEntity player, BlockState state) {
        MineableBlock block = MiningRegistry.get(Registries.BLOCK.getId(state.getBlock())).orElse(null);
        if (block == null) {
            return true;
        }
        MiningToolType tool = MiningTool.typeOf(player.getMainHandStack());
        if (!block.handler().canToolBreak(tool)) {
            return false;
        }
        return MiningTool.breakingPowerOf(player.getMainHandStack()) >= block.handler().miningPowerRequirement();
    }

    public static void onBlockBroken(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state) {
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        MineableBlock block = MiningRegistry.get(blockId).orElse(null);
        if (block == null) {
            return;
        }

        int bonus = fortuneBonus(block, player);
        for (DropTable.DropResult drop : block.drops().roll(world.getRandom())) {
            RewardAPI.giveItem(player, drop.itemId(), drop.count() * (1 + bonus));
        }

        LifeDataStore.incrementLong(player, LifeSkills.MINING, "blocks_mined", 1);
        if (block.baseXp() > 0) {
            SkillAPI.addExperience(player, LifeSkills.MINING, block.baseXp());
        }
        SkyBlockLife.LOGGER.debug("Mined {} ({}).", blockId, block.handler().handlerName());
    }

    // Placeholder formula: every 100 base/specific fortune grants +1 bonus drop. Not Hypixel data.
    private static int fortuneBonus(MineableBlock block, ServerPlayerEntity player) {
        double fortune = 0.0;
        if (block.classification().baseFortune() != null) {
            fortune += StatAPI.getFinal(player, block.classification().baseFortune());
        }
        if (block.classification().specificFortune() != null) {
            fortune += StatAPI.getFinal(player, block.classification().specificFortune());
        }
        return (int) Math.max(0, Math.floor(fortune / 100.0));
    }

    /** Custom break time in ticks, used by the player breaking-speed mixin. */
    public static double breakTicks(ServerPlayerEntity player, MineableBlock block) {
        double strength = block.handler().strength();
        if (strength <= 0.0) {
            return 1.0;
        }
        double miningSpeed = StatAPI.getFinal(player, StatType.MINING_SPEED);
        double speedMultiplier = 1.0 + Math.max(0.0, miningSpeed) / 100.0;
        double seconds = strength / speedMultiplier;
        return Math.max(1.0, seconds * 20.0);
    }
}
