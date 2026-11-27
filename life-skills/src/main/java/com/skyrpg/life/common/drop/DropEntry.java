package com.skyrpg.life.common.drop;

import net.minecraft.util.Identifier;

/** One weighted/chance drop entry. Unknown chances are distinguished from zero chances. */
public record DropEntry(Identifier itemId, int minCount, int maxCount, double chance, boolean chanceKnown) {
    public DropEntry {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId is required");
        }
        minCount = Math.max(0, minCount);
        maxCount = Math.max(minCount, maxCount);
        chance = Double.isFinite(chance) ? Math.max(0.0, Math.min(1.0, chance)) : 0.0;
    }
}
