package com.skyrpg.life.common.fortune;

import com.skyrpg.skyblock.api.StatAPI;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Reads Core-owned fortune/speed stats.
 *
 * <p>The numeric effect of a fortune value is a Life Skills mechanic and must be
 * defined per skill; this class only exposes the Core-owned final stat values.</p>
 */
public final class FortuneSource {
    private FortuneSource() {}

    public static double get(ServerPlayerEntity player, StatType stat) {
        return StatAPI.getFinal(player, stat);
    }
}
