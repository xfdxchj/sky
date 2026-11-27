package com.skyrpg.skyblock.quest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.npc.NpcRegistry;
import com.skyrpg.skyblock.combat.MobRegistry;
import com.skyrpg.skyblock.skill.SkillType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class QuestRegistry {
    private static final Map<String, QuestDefinition> DEFINITIONS = new LinkedHashMap<>();
    private QuestRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, QuestDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/quests",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String skillId = optional(json, "reward_skill", "");
                SkillType skill = skillId.isBlank() ? null : SkillType.fromId(skillId)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown reward_skill: " + skillId));
                QuestDefinition quest = new QuestDefinition(
                        required(json, "id"), optional(json, "title", ""),
                        optional(json, "description", ""), required(json, "objective_type"),
                        optional(json, "target_npc_id", ""), optional(json, "target_mob_id", ""),
                        integer(json, "required_count", 1),
                        longNumber(json, "reward_coins", 0), skill, number(json, "reward_skill_xp", 0));
                if ("talk_to_npc".equals(quest.objectiveType()) && NpcRegistry.get(quest.targetNpcId()).isEmpty())
                    throw new IllegalArgumentException("Unknown target NPC: " + quest.targetNpcId());
                if ("kill_mob".equals(quest.objectiveType()) && MobRegistry.getById(quest.targetMobId()).isEmpty())
                    throw new IllegalArgumentException("Unknown target mob: " + quest.targetMobId());
                if (quest.rewardSkillXp() > 0 && quest.rewardSkill() == null)
                    throw new IllegalArgumentException("reward_skill is required when reward_skill_xp > 0");
                String key = quest.id().toLowerCase(Locale.ROOT);
                if (loaded.putIfAbsent(key, quest) != null)
                    throw new IllegalArgumentException("Duplicate quest id: " + quest.id());
            } catch (Exception exception) {
                SkyBlockRPG.LOGGER.error("Failed to load quest data from {}", entry.getKey(), exception);
            }
        }
        DEFINITIONS.clear();
        DEFINITIONS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock quest definitions.", DEFINITIONS.size());
    }

    public static Optional<QuestDefinition> get(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }
    public static Collection<QuestDefinition> all() { return List.copyOf(DEFINITIONS.values()); }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsString();
    }
    private static String optional(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }
    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }
    private static long longNumber(JsonObject json, String key, long fallback) {
        return json.has(key) ? json.get(key).getAsLong() : fallback;
    }
    private static double number(JsonObject json, String key, double fallback) {
        double value = json.has(key) ? json.get(key).getAsDouble() : fallback;
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite " + key);
        return value;
    }
}
