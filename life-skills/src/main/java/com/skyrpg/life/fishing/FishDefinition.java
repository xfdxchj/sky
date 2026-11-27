package com.skyrpg.life.fishing;

import net.minecraft.util.Identifier;

/**
 * A fishable catch entry with a weighted probability and base Fishing XP.
 *
 * <p>Weights and XP are placeholder development data, not verified Hypixel values.</p>
 */
public record FishDefinition(Identifier fishId, String displayName, Identifier itemId, int weight, double baseXp) {
    public FishDefinition {
        if (fishId == null) {
            throw new IllegalArgumentException("fishId is required");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName is required");
        }
        if (itemId == null) {
            throw new IllegalArgumentException("itemId is required");
        }
        weight = Math.max(1, weight);
        baseXp = Double.isFinite(baseXp) ? Math.max(0.0, baseXp) : 0.0;
    }
}
