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

    public double getBase(StatType type) { return baseValues.getOrDefault(type, 0.0); }

    public void setBase(StatType type, double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Stat value must be finite");
        baseValues.put(type, value);
    }

    public Map<StatType, Double> getBaseValues() { return Map.copyOf(baseValues); }

    public double getFinal(StatType type) { return getFinal(type, List.of()); }

    /** Calculation order: base + additive, then multiplicative modifiers, then the last override. */
    public double getFinal(StatType type, Collection<StatModifier> modifiers) {
        double value = getBase(type);
        double multiplier = 1.0;
        Double override = null;
        for (StatModifier modifier : modifiers) {
            if (modifier.stat() != type || !Double.isFinite(modifier.amount())) continue;
            switch (modifier.operation()) {
                case ADDITIVE -> value += modifier.amount();
                case MULTIPLICATIVE -> multiplier *= (1.0 + modifier.amount());
                case OVERRIDE -> override = modifier.amount();
            }
        }
        double result = override == null ? value * multiplier : override;
        return Double.isFinite(result) ? result : 0.0;
    }
}
