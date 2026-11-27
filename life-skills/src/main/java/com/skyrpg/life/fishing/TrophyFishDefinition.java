package com.skyrpg.life.fishing;

import net.minecraft.util.Identifier;

/** A rare trophy fish with four quality tiers. */
public record TrophyFishDefinition(Identifier id, String displayName, double chance,
                                   Identifier bronzeItem, Identifier silverItem,
                                   Identifier goldItem, Identifier diamondItem) {
    public TrophyFishDefinition {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName is required");
        }
        chance = Double.isFinite(chance) ? Math.max(0.0, Math.min(1.0, chance)) : 0.0;
    }

    public Identifier itemForTier(int catches) {
        if (catches >= 600) {
            return diamondItem;
        }
        if (catches >= 100) {
            return goldItem;
        }
        double roll = Math.random();
        if (roll < 0.002) {
            return diamondItem;
        }
        if (roll < 0.022) {
            return goldItem;
        }
        if (roll < 0.272) {
            return silverItem;
        }
        return bronzeItem;
    }
}
