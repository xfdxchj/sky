package com.skyrpg.skyblock.hub;

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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class HubRegistry {
    private static final Map<String, HubLocationDefinition> LOCATIONS = new LinkedHashMap<>();
    private static String catalogVersion = "unknown";

    private HubRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, HubLocationDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/hub_locations",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                HubLocationDefinition location = new HubLocationDefinition(
                        required(json, "id"), optional(json, "name", required(json, "id")),
                        optional(json, "parent_id", ""), required(json, "category"),
                        required(json, "dimension"), number(json, "x"), number(json, "y"), number(json, "z"),
                        optional(json, "description", ""), required(json, "source_type"),
                        required(json, "source_url"), optional(json, "reference_revision", ""),
                        optional(json, "region_id", ""), optional(json, "warp_id", ""),
                        json.has("test_fixture") && json.get("test_fixture").getAsBoolean());

                if (!location.regionId().isBlank() && RegionRegistry.get(location.regionId()).isEmpty())
                    throw new IllegalArgumentException("Unknown region reference: " + location.regionId());
                if (!location.warpId().isBlank()) {
                    var warp = WarpRegistry.get(location.warpId()).orElse(null);
                    if (warp == null) throw new IllegalArgumentException("Unknown warp reference: " + location.warpId());
                    if (!warp.dimension().equals(location.dimension()))
                        throw new IllegalArgumentException("Hub location and warp dimensions do not match");
                }

                String key = location.id().toLowerCase(Locale.ROOT);
                if (loaded.putIfAbsent(key, location) != null)
                    throw new IllegalArgumentException("Duplicate Hub location id: " + location.id());
            } catch (Exception exception) {
                SkyBlockRPG.LOGGER.error("Failed to load Hub location data from {}", entry.getKey(), exception);
            }
        }
        Map<String, HubLocationDefinition> validated = new LinkedHashMap<>(loaded);
        for (HubLocationDefinition location : loaded.values()) {
            if (!location.parentId().isBlank()
                    && !loaded.containsKey(location.parentId().toLowerCase(Locale.ROOT))) {
                SkyBlockRPG.LOGGER.error("Skipping Hub location '{}' because parent '{}' does not exist.",
                        location.id(), location.parentId());
                validated.remove(location.id().toLowerCase(Locale.ROOT));
            }
        }

        LOCATIONS.clear();
        LOCATIONS.putAll(validated);
        SkyBlockRPG.LOGGER.info("Loaded {} sourced Hub points of interest.", LOCATIONS.size());
    }

    public static Optional<HubLocationDefinition> get(String id) {
        return Optional.ofNullable(LOCATIONS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }

    public static Collection<HubLocationDefinition> all() {
        return LOCATIONS.values().stream()
                .sorted(Comparator.comparing(HubLocationDefinition::id, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public static List<HubLocationDefinition> nearest(String dimension, double x, double y, double z, int limit) {
        if (limit < 1) return List.of();
        return LOCATIONS.values().stream()
                .filter(location -> location.dimension().equals(dimension))
                .sorted(Comparator.comparingDouble((HubLocationDefinition location) -> location.squaredDistance(x, y, z))
                        .thenComparing(HubLocationDefinition::id, String.CASE_INSENSITIVE_ORDER))
                .limit(limit)
                .toList();
    }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull())
            throw new IllegalArgumentException("Missing " + key);
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
}
