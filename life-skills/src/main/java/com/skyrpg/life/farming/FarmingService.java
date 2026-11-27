package com.skyrpg.life.farming;

import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.life.common.data.LifeDataStore;
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

/** Server-side Farming processing. Only vanilla crop blocks are handled. */
public final class FarmingService {
    private FarmingService() {}

    /** Called from the BEFORE event; crops can always be harvested. */
    public static boolean canBreak(ServerPlayerEntity player, BlockState state) {
        return true;
    }

    public static void onBlockBroken(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state) {
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        CropDefinition crop = FarmingRegistry.byBlock(blockId).orElse(null);
        if (crop == null) {
            return;
        }

        int bonus = fortuneBonus(player);
        RewardAPI.giveItem(player, crop.dropItem(), 1 + bonus);

        LifeDataStore.incrementLong(player, LifeSkills.FARMING, "crops_harvested", 1);
        if (crop.baseXp() > 0) {
            SkillAPI.addExperience(player, LifeSkills.FARMING, crop.baseXp());
        }
        SkyBlockLife.LOGGER.debug("Harvested {} ({}).", blockId, crop.cropId());
    }

    // Placeholder formula: every 100 Farming Fortune grants one bonus crop. Not Hypixel data.
    private static int fortuneBonus(ServerPlayerEntity player) {
        double fortune = StatAPI.getFinal(player, StatType.FARMING_FORTUNE);
        return (int) Math.max(0, Math.floor(fortune / 100.0));
    }
}
