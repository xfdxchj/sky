package com.skyrpg.skyblock.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.region.RegionRegistry;
import com.skyrpg.skyblock.warp.WarpRegistry;
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

public final class NpcRegistry {
    private static final Map<String, NpcDefinition> DEFINITIONS = new LinkedHashMap<>();
    private NpcRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, NpcDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/npcs",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String id = required(json, "id");
                List<String> dialogue = json.has("dialogue") && json.get("dialogue").isJsonArray()
                        ? json.getAsJsonArray("dialogue").asList().stream().map(element -> element.getAsString()).toList()
                        : List.of();
                NpcDefinition npc = new NpcDefinition(
                        id, optional(json, "name", id), optional(json, "entity_type", "minecraft:villager"),
                        optional(json, "required_region_id", ""),
                        number(json, "interaction_radius", 4.5), dialogue,
                        optional(json, "action", "none"), optional(json, "action_target", ""));
                if (Identifier.tryParse(npc.entityType()) == null)
                    throw new IllegalArgumentException("Invalid NPC entity_type: " + npc.entityType());
                if (!npc.requiredRegionId().isBlank()
                        && RegionRegistry.get(npc.requiredRegionId()).isEmpty())
                    throw new IllegalArgumentException("Unknown required region: " + npc.requiredRegionId());
                if (npc.action().equals("warp") && WarpRegistry.get(npc.actionTarget()).isEmpty())
                    throw new IllegalArgumentException("Unknown warp action target: " + npc.actionTarget());
                String key = npc.id().toLowerCase(Locale.ROOT);
                if (loaded.putIfAbsent(key, npc) != null)
                    throw new IllegalArgumentException("Duplicate NPC id: " + npc.id());
            } catch (Exception exception) {
                SkyBlockRPG.LOGGER.error("Failed to load NPC data from {}", entry.getKey(), exception);
            }
        }
        DEFINITIONS.clear();
        DEFINITIONS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock NPC definitions.", DEFINITIONS.size());
    }

    public static Optional<NpcDefinition> get(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }
    public static Collection<NpcDefinition> all() { return List.copyOf(DEFINITIONS.values()); }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsString();
    }
    private static String optional(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }
    private static double number(JsonObject json, String key, double fallback) {
        double value = json.has(key) ? json.get(key).getAsDouble() : fallback;
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite " + key);
        return value;
    }
}
