package com.example.skyblocklife;

import com.skyrpg.skyblock.api.CoreApi;
import com.skyrpg.skyblock.api.SkillAPI;
import com.skyrpg.skyblock.skill.SkillRegistration;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal compile/runtime smoke-test extension.
 * The XP curve below is test data, not verified Hypixel progression data.
 */
public final class LifeSkillsApiSmoke implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("SkyBlock Life Skills API Smoke");

    @Override
    public void onInitialize() {
        if (CoreApi.API_VERSION < 2) {
            throw new IllegalStateException("SkyBlock RPG Core API v2 or newer is required");
        }

        register("mining");
        register("fishing");
        register("farming");
        register("foraging");
        LOGGER.info("Life Skills API smoke-test initialized against Core API v{}.", CoreApi.API_VERSION);
    }

    private static void register(String path) {
        Identifier id = Identifier.of("skyblock_life", path);
        // Deliberately simple test curve: 100 XP per level, 50 levels.
        boolean registered = SkillAPI.registerSkill(
                SkillRegistration.linear(id, capitalize(path), 50, 100.0)
        );
        if (!registered) {
            throw new IllegalStateException("Could not register smoke-test skill " + id
                    + " (duplicate ID or registration lifecycle issue)");
        }
        LOGGER.info("Registered test skill {}", id);
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
