package com.skyrpg.skyblock.skill;

import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/** Immutable namespaced skill definition. XP entries are requirements for levels 1..maxLevel. */
public record SkillRegistration(Identifier id, String displayName, int maxLevel, List<Double> xpToNextLevel) {
    public SkillRegistration {
        Objects.requireNonNull(id, "id");
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName is required");
        if (maxLevel < 1 || maxLevel > 1000) throw new IllegalArgumentException("maxLevel must be between 1 and 1000");
        xpToNextLevel = List.copyOf(xpToNextLevel);
        if (xpToNextLevel.size() != maxLevel) throw new IllegalArgumentException("XP curve size must equal maxLevel");
        double cumulativeXp = 0.0;
        for (double xp : xpToNextLevel) {
            if (!Double.isFinite(xp) || xp <= 0) throw new IllegalArgumentException("XP requirements must be finite and positive");
            cumulativeXp += xp;
            if (!Double.isFinite(cumulativeXp)) throw new IllegalArgumentException("Cumulative XP curve must remain finite");
        }
    }

    public static SkillRegistration linear(Identifier id, String displayName, int maxLevel, double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier <= 0) throw new IllegalArgumentException("multiplier must be positive");
        List<Double> curve = IntStream.rangeClosed(1, maxLevel).mapToObj(level -> multiplier * level).toList();
        return new SkillRegistration(id, displayName, maxLevel, curve);
    }

    public int levelFromTotalXp(double totalXp) {
        if (!Double.isFinite(totalXp) || totalXp <= 0) return 0;
        double remaining = totalXp;
        int level = 0;
        while (level < maxLevel) {
            double required = xpToNextLevel.get(level);
            if (remaining < required) break;
            remaining -= required;
            level++;
        }
        return level;
    }

    public double progressInLevel(double totalXp) {
        if (!Double.isFinite(totalXp) || totalXp <= 0) return 0.0;
        int level = levelFromTotalXp(totalXp);
        if (level >= maxLevel) return 1.0;
        double consumed = 0.0;
        for (int i = 0; i < level; i++) consumed += xpToNextLevel.get(i);
        return Math.max(0.0, Math.min(1.0, (totalXp - consumed) / xpToNextLevel.get(level)));
    }
}
