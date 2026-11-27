package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.skill.SkillManager;
import com.skyrpg.skyblock.skill.SkillRegistration;
import com.skyrpg.skyblock.skill.SkillRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.Collection;
import java.util.Optional;

/** Stable public entry point for skills contributed by dependent mods. */
public final class SkillAPI {
    public static final int API_VERSION = 1;
    private SkillAPI() {}
    public static boolean registerSkill(SkillRegistration definition) { return SkillRegistry.register(definition); }
    public static Optional<SkillRegistration> getSkill(Identifier id) { return SkillRegistry.get(id); }
    public static Collection<SkillRegistration> getSkills() { return SkillRegistry.all(); }
    public static double getExperience(ServerPlayerEntity player, Identifier skillId) { return player == null || skillId == null ? 0.0 : SkillManager.getExperience(player, skillId); }
    public static int getLevel(ServerPlayerEntity player, Identifier skillId) { return player == null || skillId == null ? 0 : SkillManager.getLevel(player, skillId); }
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
    public static boolean addExperience(ServerPlayerEntity player, Identifier skillId, double amount) {
        return player != null && skillId != null && SkillManager.addExperience(player, skillId, amount);
    }
}
