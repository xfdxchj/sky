package com.skyrpg.skyblock.economy;

import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CurrencyManager {
    private CurrencyManager() {}

    public static long getCoins(ServerPlayerEntity player) {
        return ProfileManager.get(player).getCoins();
    }

    public static boolean addCoins(ServerPlayerEntity player, long amount) {
        boolean changed = ProfileManager.get(player).addCoins(amount);
        if (changed) ProfileManager.markChanged(player);
        return changed;
    }

    public static boolean removeCoins(ServerPlayerEntity player, long amount) {
        boolean changed = ProfileManager.get(player).removeCoins(amount);
        if (changed) ProfileManager.markChanged(player);
        return changed;
    }
}
