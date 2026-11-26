package com.skyrpg.skyblock.skill;

import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SkillManager {
    private SkillManager() {}

    public static double getExperience(ServerPlayerEntity player, SkillType skill) {
        return ProfileManager.get(player).getSkillExperience(skill);
    }

    public static int getLevel(ServerPlayerEntity player, SkillType skill) {
        return SkillDefinition.levelFromTotalXp(getExperience(player, skill));
    }

    public static boolean addExperience(ServerPlayerEntity player, SkillType skill, double amount) {
        PlayerProfile profile = ProfileManager.get(player);
        boolean changed = profile.addSkillExperience(skill, amount);
        if (changed) ProfileManager.markChanged(player);
        return changed;
    }
}
