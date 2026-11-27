package com.skyrpg.skyblock.profile;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bridges persistent RPG Health to Minecraft's native player health bar.
 *
 * RPG HP is normalized against the player's vanilla max-health attribute.
 * This class does not apply Defense or rewrite damage; it only reconciles
 * vanilla health changes with the persistent profile.
 */
public final class PlayerHealthManager {
    private static final double EPSILON = 0.001;
    private static final Map<UUID, SyncState> STATES = new HashMap<>();

    private PlayerHealthManager() {}

    public static void onJoin(ServerPlayerEntity player) {
        PlayerProfile profile = ProfileManager.get(player);
        applyProfileHealth(player, profile);
        // Persist any legacy initialization or max-health clamp performed during synchronization.
        ProfileManager.markChanged(player);
    }

    /** A respawn starts at full RPG Health; the vanilla respawn flow remains authoritative. */
    public static void onRespawn(ServerPlayerEntity player) {
        PlayerProfile profile = ProfileManager.get(player);
        profile.setCurrentHealth(profile.getMaxHealth());
        ProfileManager.markChanged(player);
        applyProfileHealth(player, profile);
    }

    /**
     * Reconcile changes once per server tick:
     * - max-health changes preserve absolute RPG HP and clamp to the new maximum;
     * - native damage/healing is converted back into RPG HP;
     * - explicit profile-side healing/damage is pushed to the native health bar.
     */
    public static void tick(ServerPlayerEntity player) {
        PlayerProfile profile = ProfileManager.get(player);
        double maxRpg = profile.getMaxHealth();
        double currentRpg = profile.getCurrentHealth();
        float maxVanilla = Math.max(1.0F, player.getMaxHealth());
        float observedVanilla = clampVanilla(player.getHealth(), maxVanilla);

        SyncState state = STATES.get(player.getUuid());
        if (state == null) {
            applyProfileHealth(player, profile);
            return;
        }

        boolean maxChanged = Math.abs(maxRpg - state.maxRpgHealth) > EPSILON
                || Math.abs(maxVanilla - state.maxVanillaHealth) > EPSILON;

        if (maxChanged) {
            // Preserve the absolute RPG HP amount; getCurrentHealth already clamps it.
            if (Math.abs(currentRpg - state.lastRpgHealth) > EPSILON) {
                ProfileManager.markChanged(player);
            }
            applyProfileHealth(player, profile);
            return;
        }

        if (Math.abs(observedVanilla - state.expectedVanillaHealth) > EPSILON) {
            double ratio = observedVanilla / maxVanilla;
            double nextRpg = Math.max(0.0, Math.min(maxRpg, ratio * maxRpg));
            if (Math.abs(nextRpg - currentRpg) > EPSILON) {
                profile.setCurrentHealth(nextRpg);
                ProfileManager.markChanged(player);
            }
            remember(player, profile);
            return;
        }

        if (Math.abs(currentRpg - state.lastRpgHealth) > EPSILON) {
            ProfileManager.markChanged(player);
            applyProfileHealth(player, profile);
        }
    }

    public static void remove(ServerPlayerEntity player) {
        if (player != null) STATES.remove(player.getUuid());
    }

    public static void clear() {
        STATES.clear();
    }

    private static void applyProfileHealth(ServerPlayerEntity player, PlayerProfile profile) {
        double maxRpg = profile.getMaxHealth();
        double currentRpg = profile.getCurrentHealth();
        float maxVanilla = Math.max(1.0F, player.getMaxHealth());
        float mapped = (float) (maxVanilla * currentRpg / maxRpg);
        player.setHealth(clampVanilla(mapped, maxVanilla));
        remember(player, profile);
    }

    private static float clampVanilla(float value, float maximum) {
        if (!Float.isFinite(value)) return maximum;
        return Math.max(0.0F, Math.min(maximum, value));
    }

    private static void remember(ServerPlayerEntity player, PlayerProfile profile) {
        STATES.put(player.getUuid(), new SyncState(
                profile.getMaxHealth(),
                profile.getCurrentHealth(),
                player.getMaxHealth(),
                player.getHealth()
        ));
    }

    private record SyncState(
            double maxRpgHealth,
            double lastRpgHealth,
            float maxVanillaHealth,
            float expectedVanillaHealth
    ) {}
}
