package com.skyrpg.skyblock.warp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
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

public final class WarpRegistry {
    private static final Map<String, WarpDefinition> DEFINITIONS = new LinkedHashMap<>();
    private WarpRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, WarpDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/warps",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                WarpDefinition warp = new WarpDefinition(
                        required(json, "id"), optional(json, "name", required(json, "id")),
                        required(json, "dimension"), number(json, "x"), number(json, "y"), number(json, "z"),
                        (float) optionalNumber(json, "yaw", 0), (float) optionalNumber(json, "pitch", 0),
                        optional(json, "required_region_id", ""), optional(json, "description", ""));
                String key = warp.id().toLowerCase(Locale.ROOT);
                if (loaded.putIfAbsent(key, warp) != null)
                    throw new IllegalArgumentException("Duplicate warp id: " + warp.id());
                if (Identifier.tryParse(warp.dimension()) == null)
                    throw new IllegalArgumentException("Invalid warp dimension: " + warp.dimension());
            } catch (Exception exception) {
                SkyBlockRPG.LOGGER.error("Failed to load warp data from {}", entry.getKey(), exception);
            }
        }
        DEFINITIONS.clear();
        DEFINITIONS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock warps.", DEFINITIONS.size());
    }

    public static Optional<WarpDefinition> get(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }
    public static Collection<WarpDefinition> all() { return List.copyOf(DEFINITIONS.values()); }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsString();
    }
    private static String optional(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }
    private static double number(JsonObject json, String key) {
        double value = json.get(key).getAsDouble();
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite " + key);
        return value;
    }
    private static double optionalNumber(JsonObject json, String key, double fallback) {
        return json.has(key) ? json.get(key).getAsDouble() : fallback;
    }
}
