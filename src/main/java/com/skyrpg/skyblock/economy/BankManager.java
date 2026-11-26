package com.skyrpg.skyblock.economy;

import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.server.network.ServerPlayerEntity;

/** Server-authoritative transfers between a player's purse and bank. */
public final class BankManager {
    private BankManager() {}
    public static long getBalance(ServerPlayerEntity player) { return ProfileManager.get(player).getBankCoins(); }
    public static boolean deposit(ServerPlayerEntity player, long amount) {
        PlayerProfile profile = ProfileManager.get(player);
        boolean changed = profile.depositCoins(amount);
        if (changed) ProfileManager.markChanged(player);
        return changed;
    }
    public static boolean withdraw(ServerPlayerEntity player, long amount) {
        PlayerProfile profile = ProfileManager.get(player);
        boolean changed = profile.withdrawCoins(amount);
        if (changed) ProfileManager.markChanged(player);
        return changed;
    }
}
