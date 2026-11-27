package com.skyrpg.life.mining;

import com.skyrpg.skyblock.stats.StatType;

/**
 * Fortune categories for a mineable block.
 *
 * <p>Core does not yet expose sub-category fortune stats (ore, block, dwarven...),
 * so only baseFortune is used; specificFortune stays null until Core adds them.</p>
 */
public record MiningBlockClassification(StatType baseFortune, StatType specificFortune) {
    public static final MiningBlockClassification NONE = new MiningBlockClassification(null, null);
    public static final MiningBlockClassification MINING = new MiningBlockClassification(StatType.MINING_FORTUNE, null);
    public static final MiningBlockClassification ORE = MINING;
    public static final MiningBlockClassification BLOCK = MINING;

    public static MiningBlockClassification of(StatType baseFortune) {
        return new MiningBlockClassification(baseFortune, null);
    }
}
