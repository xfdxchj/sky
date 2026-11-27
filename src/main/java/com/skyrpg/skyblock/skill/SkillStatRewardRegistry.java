package com.skyrpg.skyblock.skill;

import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runtime registry of exact, data-authored skill level stat rewards. */
public final class SkillStatRewardRegistry {
    private static final Map<String, SkillStatReward> REWARDS = new LinkedHashMap<>();

    private SkillStatRewardRegistry() {}

    public static synchronized boolean register(Identifier skillId, int requiredLevel,
                                                 StatType stat, double amount,
                                                 StatModifier.Operation operation) {
        SkillRegistration skill = SkillRegistry.get(skillId).orElse(null);
        if (skill == null || requiredLevel < 1 || requiredLevel > skill.maxLevel()
                || stat == null || operation == null || !Double.isFinite(amount)) {
            return false;
        }
        SkillStatReward reward;
        try {
            reward = new SkillStatReward(skillId, requiredLevel, stat, amount, operation);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        String key = reward.sourceId();
        if (REWARDS.containsKey(key)) return false;
        REWARDS.put(key, reward);
        return true;
    }

    public static synchronized Collection<SkillStatReward> all() {
        return List.copyOf(REWARDS.values());
    }

    public static synchronized List<SkillStatReward> forSkill(Identifier skillId) {
        return REWARDS.values().stream().filter(reward -> reward.skillId().equals(skillId)).toList();
    }
}
