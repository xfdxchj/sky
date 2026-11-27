package com.skyrpg.life.common.reward;

import com.skyrpg.skyblock.api.RewardAPI;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Thin facade over Core's reward API. Every call checks the returned success value. */
public final class LifeRewardService {
    private LifeRewardService() {}

    public static boolean giveItem(ServerPlayerEntity player, Identifier itemId, int count) {
        return RewardAPI.giveItem(player, itemId, count);
    }

    public static boolean giveCoins(ServerPlayerEntity player, long amount) {
        return RewardAPI.giveCoins(player, amount);
    }

    public static boolean giveSkillExperience(ServerPlayerEntity player, Identifier skillId, double amount) {
        return RewardAPI.giveSkillExperience(player, skillId, amount);
    }
}
