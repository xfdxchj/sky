package com.skyrpg.skyblock.profile;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies the first-pass SkyBlock natural Health Regen rule on the server.
 *
 * Uses world game time, so paused worlds do not accumulate healing time.
 * Offline healing and combat-state modifiers are intentionally not included.
 */
public final class PlayerHealthRegenManager {
    private static final long REGEN_INTERVAL_TICKS = 40L;
    private static final double MAX_REGEN_STAT = 1_000_000.0;
    private static final double MAX_HEAL_PER_PULSE = 1_000_000.0;
    private static final Map<UUID, RegenState> STATES = new HashMap<>();

    private PlayerHealthRegenManager() {}

    /** Start a fresh interval on login; do not grant an immediate login heal. */
    public static void onJoin(ServerPlayerEntity player) {
        reset(player);
    }

    /** Respawn also starts a fresh interval after the full-health respawn rule. */
    public static void onRespawn(ServerPlayerEntity player) {
        reset(player);
    }

    public static void tick(ServerPlayerEntity player) {
        if (player == null || !player.isAlive() || player.isSpectator()) return;

        ServerWorld world = player.getServerWorld();
        long gameTime = world.getTime();
        UUID playerId = player.getUuid();
        RegenState state = STATES.get(playerId);

        if (state == null || state.world != world || gameTime < state.lastGameTime) {
            STATES.put(playerId, new RegenState(world, gameTime));
            return;
        }

        if (gameTime - state.lastGameTime < REGEN_INTERVAL_TICKS) return;

        // Consume one pulse only. Never award burst healing for delayed processing.
        state.lastGameTime = gameTime;

        PlayerProfile profile = ProfileManager.get(player);
        double currentHealth = profile.getCurrentHealth();
        double maxHealth = profile.getMaxHealth();
        if (currentHealth <= 0.0 || currentHealth >= maxHealth) return;

        double regen = profile.getFinalStat(com.skyrpg.skyblock.stats.StatType.HEALTH_REGEN);
        if (!Double.isFinite(regen) || regen <= 0.0) return;
        regen = Math.min(regen, MAX_REGEN_STAT);

        // Hypixel community-wiki reference formula:
        // (MaxHealth / 100 + 1.5) * HealthRegen / 100
        double heal = (maxHealth / 100.0 + 1.5) * regen / 100.0;
        if (!Double.isFinite(heal) || heal <= 0.0) return;
        heal = Math.min(heal, MAX_HEAL_PER_PULSE);

        double nextHealth = Math.min(maxHealth, currentHealth + heal);
        if (nextHealth <= currentHealth) return;

        if (profile.setCurrentHealth(nextHealth)) {
            ProfileManager.markChanged(player);
            PlayerHealthManager.syncFromProfile(player, profile);
        }
    }

    public static void remove(ServerPlayerEntity player) {
        if (player != null) STATES.remove(player.getUuid());
    }

    public static void clear() {
        STATES.clear();
    }

    private static void reset(ServerPlayerEntity player) {
        if (player == null) return;
        STATES.put(player.getUuid(), new RegenState(player.getServerWorld(), player.getServerWorld().getTime()));
    }

    private static final class RegenState {
        private final ServerWorld world;
        private long lastGameTime;

        private RegenState(ServerWorld world, long lastGameTime) {
            this.world = world;
            this.lastGameTime = lastGameTime;
        }
    }
}
