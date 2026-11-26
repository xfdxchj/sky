package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Safe operations on the Core logical SkyBlock inventory. */
public final class InventoryAPI {
    public static final int API_VERSION = 1;
    private InventoryAPI() {}

    public static int getCount(ServerPlayerEntity player, Identifier itemId) {
        if (player == null || itemId == null) return 0;
        return ProfileManager.get(player).getInventory().getCount(itemId.toString());
    }

    public static boolean add(ServerPlayerEntity player, Identifier itemId, int count) {
        if (player == null || itemId == null) return false;
        boolean changed = ProfileManager.get(player).getInventory().add(itemId.toString(), count);
        if (changed) ProfileManager.markChanged(player);
        return changed;
    }

    public static boolean remove(ServerPlayerEntity player, Identifier itemId, int count) {
        if (player == null || itemId == null) return false;
        boolean changed = ProfileManager.get(player).getInventory().remove(itemId.toString(), count);
        if (changed) ProfileManager.markChanged(player);
        return changed;
    }
}
