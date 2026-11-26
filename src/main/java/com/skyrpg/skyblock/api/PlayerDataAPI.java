package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.Optional;

/**
 * Namespaced extension-data API. Returned compounds are defensive copies;
 * callers must write them back with set() after mutation.
 */
public final class PlayerDataAPI {
    public static final int API_VERSION = 1;
    private PlayerDataAPI() {}

    public static Optional<NbtCompound> get(ServerPlayerEntity player, Identifier key) {
        if (player == null || key == null) return Optional.empty();
        return ProfileManager.get(player).getModuleData(key);
    }

    public static void set(ServerPlayerEntity player, Identifier key, NbtCompound value) {
        if (player == null) throw new IllegalArgumentException("player is required");
        PlayerProfile profile = ProfileManager.get(player);
        profile.setModuleData(key, value);
        ProfileManager.markChanged(player);
    }

    public static boolean remove(ServerPlayerEntity player, Identifier key) {
        if (player == null || key == null) return false;
        PlayerProfile profile = ProfileManager.get(player);
        boolean removed = profile.removeModuleData(key);
        if (removed) ProfileManager.markChanged(player);
        return removed;
    }
}
