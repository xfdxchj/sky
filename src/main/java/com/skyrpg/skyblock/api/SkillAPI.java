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
    public static double getExperience(ServerPlayerEntity player, Identifier skillId) { return SkillManager.getExperience(player, skillId); }
    public static int getLevel(ServerPlayerEntity player, Identifier skillId) { return SkillManager.getLevel(player, skillId); }
    public static boolean addExperience(ServerPlayerEntity player, Identifier skillId, double amount) {
        return SkillManager.addExperience(player, skillId, amount);
    }
}
