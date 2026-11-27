package com.skyrpg.life.fishing;

import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

/**
 * A fishable sea creature: the entity to spawn and its rewards.
 *
 * <p>Values are placeholder development data, not verified Hypixel values.</p>
 */
public record SeaCreatureDefinition(Identifier id, String displayName, EntityType<?> entityType,
                                    int requiredFishingLevel, double skillXp, Identifier dropItem) {
    public SeaCreatureDefinition {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName is required");
        }
        if (entityType == null) {
            throw new IllegalArgumentException("entityType is required");
        }
        requiredFishingLevel = Math.max(0, requiredFishingLevel);
        skillXp = Double.isFinite(skillXp) ? Math.max(0.0, skillXp) : 0.0;
    }
}
