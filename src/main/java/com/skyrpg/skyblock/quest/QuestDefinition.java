package com.skyrpg.skyblock.quest;

import com.skyrpg.skyblock.skill.SkillType;

/** A small data-driven quest objective with one target and fixed rewards. */
public record QuestDefinition(
        String id, String title, String description, String objectiveType,
        String targetNpcId, String targetMobId, int requiredCount, long rewardCoins,
        SkillType rewardSkill, double rewardSkillXp) {
    public QuestDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Quest id cannot be blank");
        title = title == null || title.isBlank() ? id : title;
        description = description == null ? "" : description;
        if (!"talk_to_npc".equals(objectiveType) && !"kill_mob".equals(objectiveType))
            throw new IllegalArgumentException("Unsupported quest objective: " + objectiveType);
        targetNpcId = targetNpcId == null ? "" : targetNpcId;
        targetMobId = targetMobId == null ? "" : targetMobId;
        if ("talk_to_npc".equals(objectiveType) && targetNpcId.isBlank())
            throw new IllegalArgumentException("talk_to_npc objective requires target_npc_id");
        if ("kill_mob".equals(objectiveType) && targetMobId.isBlank())
            throw new IllegalArgumentException("kill_mob objective requires target_mob_id");
        if (requiredCount < 1 || requiredCount > 1000)
            throw new IllegalArgumentException("required_count must be between 1 and 1000");
        if (rewardCoins < 0) throw new IllegalArgumentException("reward_coins cannot be negative");
        if (!Double.isFinite(rewardSkillXp) || rewardSkillXp < 0)
            throw new IllegalArgumentException("reward_skill_xp must be finite and non-negative");
    }

    /** Compatibility constructor for existing talk-to-NPC definitions and tests. */
    public QuestDefinition(String id, String title, String description, String objectiveType,
                           String targetNpcId, int requiredCount, long rewardCoins,
                           SkillType rewardSkill, double rewardSkillXp) {
        this(id, title, description, objectiveType, targetNpcId, "", requiredCount,
                rewardCoins, rewardSkill, rewardSkillXp);
    }
}
