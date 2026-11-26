package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.economy.BankManager;
import com.skyrpg.skyblock.economy.CurrencyManager;
import net.minecraft.server.network.ServerPlayerEntity;

/** Server-authoritative access to the Core Purse and Bank. */
public final class CurrencyAPI {
    public static final int API_VERSION = 1;
    private CurrencyAPI() {}

    public static long getPurse(ServerPlayerEntity player) {
        return player == null ? 0L : CurrencyManager.getCoins(player);
    }

    public static long getBank(ServerPlayerEntity player) {
        return player == null ? 0L : BankManager.getBalance(player);
    }

    public static boolean addPurse(ServerPlayerEntity player, long amount) {
        return player != null && CurrencyManager.addCoins(player, amount);
    }

    public static boolean removePurse(ServerPlayerEntity player, long amount) {
        return player != null && CurrencyManager.removeCoins(player, amount);
    }

    public static boolean deposit(ServerPlayerEntity player, long amount) {
        return player != null && BankManager.deposit(player, amount);
    }

    public static boolean withdraw(ServerPlayerEntity player, long amount) {
        return player != null && BankManager.withdraw(player, amount);
    }
}
