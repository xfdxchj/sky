package com.skyrpg.skyblock.stats;

/**
 * A named stat contribution.
 *
 * The four-argument constructor is retained for existing Core call sites and
 * infers a source category from the source ID. New code should pass StatSource
 * explicitly so source attribution does not depend on string conventions.
 */
public record StatModifier(String sourceId, StatType stat, double amount, Operation operation, StatSource source) {
    public enum Operation { ADDITIVE, MULTIPLICATIVE, OVERRIDE }

    public StatModifier(String sourceId, StatType stat, double amount, Operation operation) {
        this(sourceId, stat, amount, operation, StatSource.fromSourceId(sourceId));
    }

    public StatModifier {
        if (source == null) source = StatSource.fromSourceId(sourceId);
    }
}
