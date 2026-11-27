package com.skyrpg.life.skill;

import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.skyblock.api.SkillAPI;
import com.skyrpg.skyblock.api.SkillDefinition;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.util.Identifier;

/**
 * Registers the four life skills and their per-level stat rewards.
 *
 * <p>Reward values follow the reference server's skill configuration
 * (Mining Fortune +4/level, Farming Fortune +4/level, etc.).</p>
 */
public final class LifeSkillRegistry {
    private static final int MAX_LEVEL = 60;
    private static final double XP_PER_LEVEL = 100.0;

    private LifeSkillRegistry() {}

    public static void registerAll() {
        register(LifeSkills.MINING, "Mining");
        register(LifeSkills.FISHING, "Fishing");
        register(LifeSkills.FARMING, "Farming");
        register(LifeSkills.FORAGING, "Foraging");
        registerRewards();
    }

    private static void register(Identifier id, String displayName) {
        SkillDefinition definition = SkillDefinition.linear(id, displayName, MAX_LEVEL, XP_PER_LEVEL);
        if (SkillAPI.registerSkill(definition)) {
            SkyBlockLife.LOGGER.info("Registered life skill {} in the core skill system.", id);
        } else {
            SkyBlockLife.LOGGER.warn("Life skill {} was not registered (duplicate or invalid definition).", id);
        }
    }

    private static void registerRewards() {
        for (int level = 1; level <= MAX_LEVEL; level++) {
            reward(LifeSkills.MINING, level, StatType.MINING_FORTUNE, 4.0);
            reward(LifeSkills.MINING, level, StatType.DEFENSE, level <= 14 ? 1.0 : 2.0);
            reward(LifeSkills.FARMING, level, StatType.FARMING_FORTUNE, 4.0);
            reward(LifeSkills.FARMING, level, StatType.HEALTH, 2.0);
            reward(LifeSkills.FORAGING, level, StatType.FORAGING_FORTUNE, 4.0);
            reward(LifeSkills.FORAGING, level, StatType.STRENGTH, 1.0);
            reward(LifeSkills.FISHING, level, StatType.HEALTH, 2.0);
        }
    }

    private static void reward(Identifier skill, int level, StatType stat, double amount) {
        if (!SkillAPI.registerStatReward(skill, level, stat, amount, StatModifier.Operation.ADDITIVE)) {
            SkyBlockLife.LOGGER.warn("Could not register stat reward {} level {} {}+{}.", skill, level, stat.id(), amount);
        }
    }
}
