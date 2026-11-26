package com.skyrpg.skyblock.skill;

/** XP curve abstraction. P1 uses a deliberately provisional curve until current Hypixel data is imported. */
public final class SkillDefinition {
    private SkillDefinition() {}

    // Temporary development curve: each level requires 100 * level XP.
    // Replace with versioned current-Hypixel XP tables before gameplay balancing.
    public static double xpToNextLevel(int currentLevel) {
        if (currentLevel < 1) currentLevel = 1;
        return 100.0 * currentLevel;
    }

    public static int levelFromTotalXp(double totalXp) {
        if (!Double.isFinite(totalXp) || totalXp <= 0) return 0;
        int level = 0;
        double remaining = totalXp;
        while (level < 60) {
            double required = xpToNextLevel(level + 1);
            if (remaining < required) break;
            remaining -= required;
            level++;
        }
        return level;
    }

    public static double progressInLevel(double totalXp) {
        if (!Double.isFinite(totalXp) || totalXp <= 0) return 0;
        int level = levelFromTotalXp(totalXp);
        double consumed = 0;
        for (int i = 1; i <= level; i++) consumed += xpToNextLevel(i);
        return (totalXp - consumed) / xpToNextLevel(level + 1);
    }
}
