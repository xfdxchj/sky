package com.skyrpg.life.foraging;

import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * A tree type: the log blocks it owns, its drop item and base Foraging XP.
 *
 * <p>Values are placeholder development data, not verified Hypixel values.</p>
 */
public record TreeDefinition(Identifier treeId, String displayName, Set<Identifier> logBlocks,
                             Identifier dropItem, double baseXp) {
    public TreeDefinition {
        if (treeId == null) {
            throw new IllegalArgumentException("treeId is required");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName is required");
        }
        if (logBlocks == null || logBlocks.isEmpty()) {
            throw new IllegalArgumentException("logBlocks is required");
        }
        if (dropItem == null) {
            throw new IllegalArgumentException("dropItem is required");
        }
        logBlocks = Set.copyOf(logBlocks);
        baseXp = Double.isFinite(baseXp) ? Math.max(0.0, baseXp) : 0.0;
    }
}
