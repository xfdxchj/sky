package com.skyrpg.skyblock.profile;

import net.minecraft.server.network.ServerPlayerEntity;

public final class ProfileManager {
    private ProfileManager() {}

    public static PlayerProfile get(ServerPlayerEntity player) {
        return SkyBlockWorldData.get(player.getServer()).getOrCreate(player.getUuid(), player.getName().getString());
    }

    public static void markChanged(ServerPlayerEntity player) {
        SkyBlockWorldData.get(player.getServer()).markChanged();
    }
}
