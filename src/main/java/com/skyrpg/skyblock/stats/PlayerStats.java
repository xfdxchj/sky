package com.skyrpg.skyblock.stats;

import java.util.*;

public final class PlayerStats {
    private final Map<StatType, Double> baseValues = new EnumMap<>(StatType.class);

    public PlayerStats() {
        // Current Hypixel SkyBlock baseline combat stats (community-wiki sourced).
        // Profile NBT values, when present, override these defaults during loading.
        baseValues.put(StatType.CRIT_CHANCE, 30.0);
        baseValues.put(StatType.CRIT_DAMAGE, 50.0);
    }

    public double getBase(StatType type) {
        return type == null ? 0.0 : baseValues.getOrDefault(type, 0.0);
    }

    public void setBase(StatType type, double value) {
        if (type == null) throw new IllegalArgumentException("Stat type is required");
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Stat value must be finite");
        baseValues.put(type, value);
    }

    public Map<StatType, Double> getBaseValues() { return Map.copyOf(baseValues); }

    public double getFinal(StatType type) { return getFinal(type, List.of()); }

    /**
     * Calculates the final value from base + additive contributions, then
     * multiplicative contributions, then the last override in iteration order.
     */
    public double getFinal(StatType type, Collection<StatModifier> modifiers) {
        return getFinal(type, modifiers, null);
    }

    /**
     * Source-filtered variant used by diagnostics and future source-specific
     * rules. A null source filter includes every modifier for backwards
     * compatibility with the original aggregation method.
     */
    public double getFinal(StatType type, Collection<StatModifier> modifiers, Set<StatSource> includedSources) {
        if (type == null) return 0.0;
        double value = getBase(type);
        double multiplier = 1.0;
        Double override = null;
        if (modifiers != null) {
            for (StatModifier modifier : modifiers) {
                if (modifier == null || modifier.stat() != type || !Double.isFinite(modifier.amount())) continue;
                if (includedSources != null && !includedSources.contains(modifier.source())) continue;
                switch (modifier.operation()) {
                    case ADDITIVE -> value += modifier.amount();
                    case MULTIPLICATIVE -> multiplier *= (1.0 + modifier.amount());
                    case OVERRIDE -> override = modifier.amount();
                }
            }
        }
        double result = override == null ? value * multiplier : override;
        return Double.isFinite(result) ? result : 0.0;
    }
}
