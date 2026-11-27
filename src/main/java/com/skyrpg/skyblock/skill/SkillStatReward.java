package com.skyrpg.skyblock.skill;

import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.util.Identifier;

/** A stat contribution unlocked when a skill reaches a specific level. */
public record SkillStatReward(Identifier skillId, int requiredLevel, StatType stat,
                              double amount, StatModifier.Operation operation) {
    public SkillStatReward {
        if (skillId == null || stat == null || operation == null) {
            throw new IllegalArgumentException("Skill, stat and operation are required");
        }
        if (requiredLevel < 1 || requiredLevel > 1000) {
            throw new IllegalArgumentException("requiredLevel must be between 1 and 1000");
        }
        if (!Double.isFinite(amount)) throw new IllegalArgumentException("Reward amount must be finite");
    }

    public String sourceId() {
        return "skill:" + skillId + ":level:" + requiredLevel + ":" + stat.id();
    }
}
