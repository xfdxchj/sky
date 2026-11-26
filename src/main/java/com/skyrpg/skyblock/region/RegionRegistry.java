package com.skyrpg.skyblock.region;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class RegionRegistry {
    private static final Map<String, RegionDefinition> DEFINITIONS = new LinkedHashMap<>();
    private RegionRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, RegionDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/regions",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                RegionDefinition region = new RegionDefinition(
                        required(json, "id"), optional(json, "name", required(json, "id")),
                        required(json, "dimension"),
                        number(json, "min_x"), number(json, "min_y"), number(json, "min_z"),
                        number(json, "max_x"), number(json, "max_y"), number(json, "max_z"),
                        integer(json, "priority", 0));
                String key = region.id().toLowerCase(Locale.ROOT);
                if (loaded.putIfAbsent(key, region) != null)
                    throw new IllegalArgumentException("Duplicate region id: " + region.id());
                if (net.minecraft.util.Identifier.tryParse(region.dimension()) == null)
                    throw new IllegalArgumentException("Invalid region dimension: " + region.dimension());
            } catch (Exception exception) {
                SkyBlockRPG.LOGGER.error("Failed to load region data from {}", entry.getKey(), exception);
            }
        }
        DEFINITIONS.clear();
        DEFINITIONS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock regions.", DEFINITIONS.size());
    }

    public static Optional<RegionDefinition> get(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }

    public static Collection<RegionDefinition> all() {
        return List.copyOf(DEFINITIONS.values());
    }

    /** Highest priority wins; for equal priority, the smallest containing volume is most specific. */
    public static Optional<RegionDefinition> find(String dimension, double x, double y, double z) {
        return DEFINITIONS.values().stream()
                .filter(region -> region.contains(dimension, x, y, z))
                .sorted(Comparator.comparingInt(RegionDefinition::priority).reversed()
                        .thenComparingDouble(RegionDefinition::volume))
                .findFirst();
    }

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
    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }
}
