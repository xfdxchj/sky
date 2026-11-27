package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.skill.SkillManager;
import com.skyrpg.skyblock.skill.SkillRegistration;
import com.skyrpg.skyblock.skill.SkillRegistry;
import com.skyrpg.skyblock.skill.SkillStatRewardRegistry;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Stable public entry point for skills contributed by dependent mods. */
public final class SkillAPI {
    public static final int API_VERSION = 1;
    private SkillAPI() {}

    public static boolean registerSkill(SkillDefinition definition) {
        if (definition == null) return false;
        return SkillRegistry.register(new SkillRegistration(
                definition.id(), definition.displayName(), definition.maxLevel(), definition.xpToNextLevel()));
    }

    public static Optional<SkillDefinition> getSkill(Identifier id) {
        return SkillRegistry.get(id).map(SkillAPI::toApiDefinition);
    }

    public static Collection<SkillDefinition> getSkills() {
        return SkillRegistry.all().stream().map(SkillAPI::toApiDefinition).toList();
    }

    private static SkillDefinition toApiDefinition(SkillRegistration definition) {
        return new SkillDefinition(definition.id(), definition.displayName(),
                definition.maxLevel(), definition.xpToNextLevel());
    }

    public static double getExperience(ServerPlayerEntity player, Identifier skillId) {
        return player == null || skillId == null ? 0.0 : SkillManager.getExperience(player, skillId);
    }

    public static int getLevel(ServerPlayerEntity player, Identifier skillId) {
        return player == null || skillId == null ? 0 : SkillManager.getLevel(player, skillId);
    }

    /** Returns progress within the current level, clamped to [0, 1]. */
    public static double getProgress(ServerPlayerEntity player, Identifier skillId) {
        SkillRegistration definition = SkillRegistry.get(skillId).orElse(null);
        return definition == null || player == null ? 0.0
                : definition.progressInLevel(SkillManager.getExperience(player, skillId));
    }

    /** Returns the remaining XP required for the next level; zero at max level or for an unknown skill. */
    public static double getExperienceToNextLevel(ServerPlayerEntity player, Identifier skillId) {
        SkillRegistration definition = SkillRegistry.get(skillId).orElse(null);
        if (definition == null || player == null) return 0.0;
        double total = SkillManager.getExperience(player, skillId);
        int level = definition.levelFromTotalXp(total);
        if (level >= definition.maxLevel()) return 0.0;
        double consumed = 0.0;
        for (int i = 0; i < level; i++) consumed += definition.xpToNextLevel().get(i);
        return Math.max(0.0, definition.xpToNextLevel().get(level) - (total - consumed));
    }

    /**
     * Registers an exact stat reward for an extension-owned skill level.
     * Reward values are supplied by the extension and are never inferred from XP curves.
     */
    public static boolean registerStatReward(Identifier skillId, int requiredLevel,
                                             StatType stat, double amount,
                                             StatModifier.Operation operation) {
        if (skillId == null || "skyblockrpg".equals(skillId.getNamespace())) return false;
        return SkillStatRewardRegistry.register(skillId, requiredLevel, stat, amount, operation);
    }

    public static boolean addExperience(ServerPlayerEntity player, Identifier skillId, double amount) {
        return player != null && skillId != null && SkillManager.addExperience(player, skillId, amount);
    }
}
