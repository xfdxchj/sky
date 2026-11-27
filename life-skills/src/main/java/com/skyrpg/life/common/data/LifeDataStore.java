package com.skyrpg.life.common.data;

import com.skyrpg.skyblock.api.PlayerDataAPI;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Per-skill persistent statistics stored through the Core PlayerDataAPI.
 * Skill XP and levels are NOT stored here; Core owns them.
 */
public final class LifeDataStore {
    private LifeDataStore() {}

    /** Defensive copy, or an empty compound when the player has no data yet. */
    public static NbtCompound get(ServerPlayerEntity player, Identifier skillId) {
        return PlayerDataAPI.get(player, skillId).orElseGet(NbtCompound::new);
    }

    public static void set(ServerPlayerEntity player, Identifier skillId, NbtCompound data) {
        PlayerDataAPI.set(player, skillId, data);
    }

    public static void incrementLong(ServerPlayerEntity player, Identifier skillId, String key, long delta) {
        NbtCompound data = get(player, skillId);
        data.putLong(key, data.getLong(key) + delta);
        set(player, skillId, data);
    }
}
