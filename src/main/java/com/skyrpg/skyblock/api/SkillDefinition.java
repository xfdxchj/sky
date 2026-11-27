package com.skyrpg.skyblock.api;

import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Objects;

/** Stable API-level skill definition. XP entries are requirements for levels 1..maxLevel. */
public record SkillDefinition(Identifier id, String displayName, int maxLevel, List<Double> xpToNextLevel) {
    public SkillDefinition {
        Objects.requireNonNull(id, "id");
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName is required");
        if (maxLevel < 1 || maxLevel > 1000) throw new IllegalArgumentException("maxLevel must be between 1 and 1000");
        xpToNextLevel = List.copyOf(xpToNextLevel);
        if (xpToNextLevel.size() != maxLevel) throw new IllegalArgumentException("XP curve size must equal maxLevel");
        for (double xp : xpToNextLevel) {
            if (!Double.isFinite(xp) || xp <= 0) throw new IllegalArgumentException("XP requirements must be finite and positive");
        }
    }

    public static SkillDefinition linear(Identifier id, String displayName, int maxLevel, double multiplier) {
        if (maxLevel < 1 || maxLevel > 1000 || !Double.isFinite(multiplier) || multiplier <= 0)
            throw new IllegalArgumentException("Invalid maxLevel or multiplier");
        List<Double> curve = java.util.stream.IntStream.rangeClosed(1, maxLevel)
                .mapToObj(level -> multiplier * level).toList();
        return new SkillDefinition(id, displayName, maxLevel, curve);
    }
}
