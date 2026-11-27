package com.skyrpg.life.farming;

import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * A crop type: the crop blocks it owns, its drop item and base Farming XP.
 *
 * <p>Values are placeholder development data, not verified Hypixel values.</p>
 */
public record CropDefinition(Identifier cropId, String displayName, Set<Identifier> blocks,
                             Identifier dropItem, double baseXp) {
    public CropDefinition {
        if (cropId == null) {
            throw new IllegalArgumentException("cropId is required");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName is required");
        }
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("blocks is required");
        }
        if (dropItem == null) {
            throw new IllegalArgumentException("dropItem is required");
        }
        blocks = Set.copyOf(blocks);
        baseXp = Double.isFinite(baseXp) ? Math.max(0.0, baseXp) : 0.0;
    }
}
