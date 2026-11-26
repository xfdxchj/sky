package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Arrays;
import java.util.List;

/**
 * Read-only access to Core's shared stat pipeline.
 * Extensions contribute stats through registered Core item definitions and other Core-owned sources;
 * they must not mutate PlayerStats directly.
 */
public final class StatAPI {
    public static final int API_VERSION = 1;
    private StatAPI() {}

    public static double getBase(ServerPlayerEntity player, StatType stat) {
        if (player == null || stat == null) return 0.0;
        return ProfileManager.get(player).getStats().getBase(stat);
    }

    public static double getFinal(ServerPlayerEntity player, StatType stat) {
        if (player == null || stat == null) return 0.0;
        return ProfileManager.get(player).getFinalStat(stat);
    }

    public static List<StatType> getSupportedStats() {
        return List.copyOf(Arrays.asList(StatType.values()));
    }
}
