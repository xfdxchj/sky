package com.skyrpg.skyblock.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.structure.StructureRegistry;
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

/** Loads logical maps from mod/datapack resources and validates their structure references. */
public final class MapRegistry {
    private static final Map<String, MapDefinition> MAPS = new LinkedHashMap<>();

    private MapRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, MapDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/maps",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray dimensions = requiredArray(json, "source_size");
                if (dimensions.size() != 3) throw new IllegalArgumentException("source_size must contain 3 integers");
                JsonArray slicesJson = requiredArray(json, "slices");
                java.util.ArrayList<MapDefinition.Slice> slices = new java.util.ArrayList<>();
                for (var element : slicesJson) {
                    JsonObject slice = element.getAsJsonObject();
                    JsonArray offset = requiredArray(slice, "offset");
                    JsonArray size = requiredArray(slice, "size");
                    if (offset.size() != 3 || size.size() != 3)
                        throw new IllegalArgumentException("Slice offset and size must contain 3 integers");
                    String structureId = required(slice, "structure_id");
                    if (StructureRegistry.get(structureId).isEmpty())
                        throw new IllegalArgumentException("Unknown structure reference: " + structureId);
                    slices.add(new MapDefinition.Slice(structureId,
                            offset.get(0).getAsInt(), offset.get(1).getAsInt(), offset.get(2).getAsInt(),
                            size.get(0).getAsInt(), size.get(1).getAsInt(), size.get(2).getAsInt()));
                }
                String id = required(json, "id");
                MapDefinition definition = new MapDefinition(id, optional(json, "name", id),
                        required(json, "dimension"),
                        dimensions.get(0).getAsInt(), dimensions.get(1).getAsInt(), dimensions.get(2).getAsInt(),
                        slices, optional(json, "source", ""), optional(json, "source_url", ""),
                        optional(json, "license", ""));
                if (loaded.putIfAbsent(id.toLowerCase(Locale.ROOT), definition) != null)
                    throw new IllegalArgumentException("Duplicate map id: " + id);
            } catch (Exception exception) {
                SkyBlockRPG.LOGGER.error("Failed to load map definition from {}", entry.getKey(), exception);
            }
        }
        MAPS.clear();
        MAPS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} logical map definitions.", MAPS.size());
    }

    public static Optional<MapDefinition> get(String id) {
        return Optional.ofNullable(MAPS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }

    public static Collection<MapDefinition> all() {
        return List.copyOf(MAPS.values());
    }

    private static JsonArray requiredArray(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray())
            throw new IllegalArgumentException("Missing array " + key);
        return json.getAsJsonArray(key);
    }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull())
            throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsString();
    }

    private static String optional(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }
}
