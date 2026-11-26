package com.skyrpg.skyblock.island;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.region.RegionRegistry;
import com.skyrpg.skyblock.warp.WarpRegistry;
import net.minecraft.registry.Registries;
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

public final class IslandRegistry {
    private static final Map<String, IslandDefinition> ISLANDS = new LinkedHashMap<>();
    private IslandRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, IslandDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/islands",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject j = JsonParser.parseReader(reader).getAsJsonObject();
                IslandDefinition island = new IslandDefinition(
                        required(j, "id"), optional(j, "name", required(j, "id")), required(j, "dimension"),
                        integer(j, "center_x"), integer(j, "center_z"), integer(j, "surface_y"),
                        integer(j, "radius_x"), integer(j, "radius_z"), integer(j, "thickness"),
                        number(j, "spawn_x"), number(j, "spawn_y"), number(j, "spawn_z"),
                        optional(j, "region_id", ""), optional(j, "warp_id", ""),
                        required(j, "top_block"), required(j, "soil_block"), required(j, "core_block"),
                        j.has("test_fixture") && j.get("test_fixture").getAsBoolean());
                if (Identifier.tryParse(island.dimension()) == null)
                    throw new IllegalArgumentException("Invalid dimension: " + island.dimension());
                validateBlock(island.topBlock());
                validateBlock(island.soilBlock());
                validateBlock(island.coreBlock());
                if (!island.regionId().isBlank() && RegionRegistry.get(island.regionId()).isEmpty())
                    throw new IllegalArgumentException("Unknown region: " + island.regionId());
                if (!island.warpId().isBlank() && WarpRegistry.get(island.warpId()).isEmpty())
                    throw new IllegalArgumentException("Unknown warp: " + island.warpId());
                String key = island.id().toLowerCase(Locale.ROOT);
                if (loaded.putIfAbsent(key, island) != null)
                    throw new IllegalArgumentException("Duplicate island id: " + island.id());
            } catch (Exception e) {
                SkyBlockRPG.LOGGER.error("Failed to load island map data from {}", entry.getKey(), e);
            }
        }
        ISLANDS.clear();
        ISLANDS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock island map definitions.", ISLANDS.size());
    }

    public static Optional<IslandDefinition> get(String id) {
        return Optional.ofNullable(ISLANDS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }
    public static Collection<IslandDefinition> all() { return List.copyOf(ISLANDS.values()); }

    private static void validateBlock(String raw) {
        Identifier id = Identifier.tryParse(raw);
        if (id == null || !Registries.BLOCK.containsId(id))
            throw new IllegalArgumentException("Unknown block id: " + raw);
    }
    private static String required(JsonObject j, String key) {
        if (!j.has(key) || j.get(key).isJsonNull()) throw new IllegalArgumentException("Missing " + key);
        return j.get(key).getAsString();
    }
    private static String optional(JsonObject j, String key, String fallback) {
        return j.has(key) && !j.get(key).isJsonNull() ? j.get(key).getAsString() : fallback;
    }
    private static int integer(JsonObject j, String key) { return j.get(key).getAsInt(); }
    private static double number(JsonObject j, String key) {
        double value = j.get(key).getAsDouble();
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite " + key);
        return value;
    }
}
