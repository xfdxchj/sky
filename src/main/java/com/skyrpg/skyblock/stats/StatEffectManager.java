package com.skyrpg.skyblock.stats;

import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Server-authoritative lifecycle for non-equipment stat effects.
 *
 * Reusing an effect ID refreshes/replaces that effect instead of stacking a
 * second copy. Different IDs may coexist and therefore stack normally.
 */
public final class StatEffectManager {
    private StatEffectManager() {}

    /**
     * Applies or refreshes an effect.
     *
     * @param durationTicks positive duration in Overworld game ticks, or -1 for permanent
     */
    public static boolean apply(ServerPlayerEntity player, String effectId, String sourceId,
                                StatSource source, StatType stat, double amount,
                                StatModifier.Operation operation, long durationTicks) {
        if (player == null || player.getServer() == null || effectId == null || effectId.isBlank()
                || sourceId == null || sourceId.isBlank() || source == null || stat == null
                || operation == null || !Double.isFinite(amount)
                || durationTicks == 0L || durationTicks < -1L) {
            return false;
        }
        if (source != StatSource.BUFF && source != StatSource.TEMPORARY && source != StatSource.OTHER) {
            return false;
        }

        long expiry = -1L;
        if (durationTicks > 0L) {
            long now = player.getServer().getOverworld().getTime();
            if (now > Long.MAX_VALUE - durationTicks) return false;
            expiry = now + durationTicks;
        }

        try {
            StatModifier modifier = new StatModifier(sourceId, stat, amount, operation, source);
            PlayerProfile profile = ProfileManager.get(player);
            profile.putStatEffect(new StatEffect(effectId, modifier, expiry));
            ProfileManager.markChanged(player);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static boolean remove(ServerPlayerEntity player, String effectId) {
        if (player == null || effectId == null || effectId.isBlank()) return false;
        PlayerProfile profile = ProfileManager.get(player);
        boolean removed = profile.removeStatEffect(effectId);
        if (removed) ProfileManager.markChanged(player);
        return removed;
    }

    /** Expires effects against the Overworld clock; offline worlds do not consume duration. */
    public static void tick(MinecraftServer server) {
        if (server == null) return;
        long gameTime = server.getOverworld().getTime();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerProfile profile = ProfileManager.get(player);
            if (profile.removeExpiredStatEffects(gameTime)) {
                ProfileManager.markChanged(player);
            }
        }
    }
}
