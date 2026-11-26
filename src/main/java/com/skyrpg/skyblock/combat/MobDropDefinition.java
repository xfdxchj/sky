package com.skyrpg.skyblock.combat;

/** A drop entry. Unknown probabilities are explicitly distinguished from zero chance. */
public record MobDropDefinition(
        String itemId, int minCount, int maxCount, double chance,
        boolean chanceKnown, String dropType) {
    public MobDropDefinition {
        itemId = itemId == null ? "" : itemId;
        minCount = Math.max(0, minCount);
        maxCount = Math.max(minCount, maxCount);
        chance = Double.isFinite(chance) ? Math.max(0.0, Math.min(1.0, chance)) : 0.0;
        dropType = dropType == null ? "item" : dropType;
    }
}