package com.skyrpg.skyblock.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Common reward facade. Every operation is server-side and delegates persistence
 * to Core-owned managers; callers must check the returned success value.
 */
public final class RewardAPI {
    public static final int API_VERSION = 1;
    private RewardAPI() {}

    public static boolean giveItem(ServerPlayerEntity player, Identifier itemId, int count) {
        return InventoryAPI.add(player, itemId, count);
    }

    public static boolean giveCoins(ServerPlayerEntity player, long amount) {
        return CurrencyAPI.addPurse(player, amount);
    }

    public static boolean giveSkillExperience(ServerPlayerEntity player, Identifier skillId, double amount) {
        return SkillAPI.addExperience(player, skillId, amount);
    }
}
