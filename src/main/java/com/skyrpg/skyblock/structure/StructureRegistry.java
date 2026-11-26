package com.skyrpg.skyblock.structure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class StructureRegistry {
    private static final Map<String, StructureDefinition> STRUCTURES = new LinkedHashMap<>();
    private StructureRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, StructureDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/structures",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String id = required(json, "id");
                StructureDefinition definition = new StructureDefinition(
                        id, optional(json, "name", id), required(json, "template"),
                        required(json, "dimension"),
                        integer(json, "origin_x"), integer(json, "origin_y"), integer(json, "origin_z"),
                        rotation(optional(json, "rotation", "none")),
                        mirror(optional(json, "mirror", "none")),
                        json.has("max_blocks") ? integer(json, "max_blocks") : 250_000,
                        optional(json, "source", ""), optional(json, "source_url", ""),
                        optional(json, "license", ""),
                        json.has("test_fixture") && json.get("test_fixture").getAsBoolean());
                String key = definition.id().toLowerCase(Locale.ROOT);
                if (loaded.putIfAbsent(key, definition) != null)
                    throw new IllegalArgumentException("Duplicate structure id: " + definition.id());
            } catch (Exception e) {
                SkyBlockRPG.LOGGER.error("Failed to load structure manifest from {}", entry.getKey(), e);
            }
        }
        STRUCTURES.clear();
        STRUCTURES.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock structure manifests.", STRUCTURES.size());
    }

    public static Optional<StructureDefinition> get(String id) {
        return Optional.ofNullable(STRUCTURES.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }

    public static Collection<StructureDefinition> all() {
        return List.copyOf(STRUCTURES.values());
    }

    private static BlockRotation rotation(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "none" -> BlockRotation.NONE;
            case "clockwise_90" -> BlockRotation.CLOCKWISE_90;
            case "clockwise_180" -> BlockRotation.CLOCKWISE_180;
            case "counterclockwise_90" -> BlockRotation.COUNTERCLOCKWISE_90;
            default -> throw new IllegalArgumentException("Unknown rotation: " + value);
        };
    }

    private static BlockMirror mirror(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "none" -> BlockMirror.NONE;
            case "left_right" -> BlockMirror.LEFT_RIGHT;
            case "front_back" -> BlockMirror.FRONT_BACK;
            default -> throw new IllegalArgumentException("Unknown mirror: " + value);
        };
    }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull())
            throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsString();
    }

    private static String optional(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }

    private static int integer(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull())
            throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsInt();
    }
}
