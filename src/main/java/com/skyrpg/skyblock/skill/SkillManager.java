package com.skyrpg.skyblock.skill;

import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class SkillManager {
    private SkillManager() {}

    public static double getExperience(ServerPlayerEntity player, SkillType skill) {
        return skill == null ? 0.0 : getExperience(player, skill.identifier());
    }

    public static double getExperience(ServerPlayerEntity player, Identifier skillId) {
        return ProfileManager.get(player).getSkillExperience(skillId);
    }

    public static int getLevel(ServerPlayerEntity player, SkillType skill) {
        return skill == null ? 0 : getLevel(player, skill.identifier());
    }

    public static int getLevel(ServerPlayerEntity player, Identifier skillId) {
        SkillRegistration definition = SkillRegistry.get(skillId).orElse(null);
        return definition == null ? 0 : definition.levelFromTotalXp(getExperience(player, skillId));
    }

    public static boolean addExperience(ServerPlayerEntity player, SkillType skill, double amount) {
        return skill != null && addExperience(player, skill.identifier(), amount);
    }

    public static boolean addExperience(ServerPlayerEntity player, Identifier skillId, double amount) {
        if (SkillRegistry.get(skillId).isEmpty()) return false;
        PlayerProfile profile = ProfileManager.get(player);
        boolean changed = profile.addSkillExperience(skillId, amount);
        if (changed) ProfileManager.markChanged(player);
        return changed;
    }
}
