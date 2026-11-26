package com.skyrpg.skyblock.connection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.warp.WarpDefinition;
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

public final class ConnectionRegistry {
    private static final Map<String, ConnectionDefinition> CONNECTIONS = new LinkedHashMap<>();
    private ConnectionRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, ConnectionDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/connections",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                ConnectionDefinition.Type type = ConnectionDefinition.Type.valueOf(
                        required(json, "type").toUpperCase(Locale.ROOT));
                ConnectionDefinition definition = new ConnectionDefinition(
                        required(json, "id"), optional(json, "name", required(json, "id")),
                        type, required(json, "dimension"),
                        number(json, "start_x"), number(json, "start_y"), number(json, "start_z"),
                        number(json, "end_x"), number(json, "end_y"), number(json, "end_z"),
                        json.has("width") ? integer(json, "width") : 1,
                        optional(json, "block_id", ""),
                        required(json, "source_warp_id"), required(json, "target_warp_id"),
                        optionalNumber(json, "activation_radius", 4),
                        json.has("test_fixture") && json.get("test_fixture").getAsBoolean());

                WarpDefinition source = WarpRegistry.get(definition.sourceWarpId()).orElseThrow(
                        () -> new IllegalArgumentException("Unknown source warp: " + definition.sourceWarpId()));
                WarpDefinition target = WarpRegistry.get(definition.targetWarpId()).orElseThrow(
                        () -> new IllegalArgumentException("Unknown target warp: " + definition.targetWarpId()));
                if (!source.dimension().equals(definition.dimension()))
                    throw new IllegalArgumentException("Source warp dimension does not match connection dimension");
                if (type == ConnectionDefinition.Type.BRIDGE
                        && !target.dimension().equals(definition.dimension()))
                    throw new IllegalArgumentException("Bridge endpoints must be in the same dimension");
                if (type == ConnectionDefinition.Type.BRIDGE
                        && (!near(definition.startX(), definition.startY(), definition.startZ(), source, 8.0)
                        || !near(definition.endX(), definition.endY(), definition.endZ(), target, 8.0)))
                    throw new IllegalArgumentException("Bridge geometry endpoints must be within 8 blocks of their linked warps");
                if (type == ConnectionDefinition.Type.PORTAL
                        && !near(definition.startX(), definition.startY(), definition.startZ(), source,
                        definition.activationRadius()))
                    throw new IllegalArgumentException("Portal entrance must be within activation_radius of its source warp");
                if (type == ConnectionDefinition.Type.BRIDGE) {
                    Identifier blockId = Identifier.tryParse(definition.blockId());
                    if (blockId == null || !Registries.BLOCK.containsId(blockId))
                        throw new IllegalArgumentException("Unknown bridge block: " + definition.blockId());
                }

                String key = definition.id().toLowerCase(Locale.ROOT);
                if (loaded.putIfAbsent(key, definition) != null)
                    throw new IllegalArgumentException("Duplicate connection id: " + definition.id());
            } catch (Exception exception) {
                SkyBlockRPG.LOGGER.error("Failed to load map connection from {}", entry.getKey(), exception);
            }
        }
        CONNECTIONS.clear();
        CONNECTIONS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock map connections.", CONNECTIONS.size());
    }

    public static Optional<ConnectionDefinition> get(String id) {
        return Optional.ofNullable(CONNECTIONS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }

    public static Collection<ConnectionDefinition> all() {
        return List.copyOf(CONNECTIONS.values());
    }

    public static List<ConnectionDefinition> connectedTo(String warpId) {
        return CONNECTIONS.values().stream()
                .filter(connection -> connection.sourceWarpId().equalsIgnoreCase(warpId)
                        || connection.targetWarpId().equalsIgnoreCase(warpId))
                .toList();
    }

    private static boolean near(double x, double y, double z, WarpDefinition warp, double radius) {
        double dx = x - warp.x();
        double dy = y - warp.y();
        double dz = z - warp.z();
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull())
            throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsString();
    }
    private static String optional(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }
    private static int integer(JsonObject json, String key) { return json.get(key).getAsInt(); }
    private static double number(JsonObject json, String key) {
        double value = json.get(key).getAsDouble();
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite " + key);
        return value;
    }
    private static double optionalNumber(JsonObject json, String key, double fallback) {
        double value = json.has(key) ? json.get(key).getAsDouble() : fallback;
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite " + key);
        return value;
    }
}
