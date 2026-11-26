package com.skyrpg.skyblock.reforge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import java.io.Reader;
import java.util.*;

public final class ReforgeRegistry {
    private static final Map<String, ReforgeDefinition> REFORGES = new LinkedHashMap<>();
    private ReforgeRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, ReforgeDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/reforges",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject j = JsonParser.parseReader(reader).getAsJsonObject();
                String id = required(j, "id").toLowerCase(Locale.ROOT);
                String name = required(j, "name");
                List<String> slots = new ArrayList<>();
                j.getAsJsonArray("allowed_slots").forEach(v -> slots.add(v.getAsString()));
                long cost = j.has("cost") ? j.get("cost").getAsLong() : 500L;
                if (cost < 0) throw new IllegalArgumentException("Reforge cost cannot be negative");
                Map<StatType, Double> stats = new EnumMap<>(StatType.class);
                if (j.has("stats") && j.get("stats").isJsonObject()) {
                    for (var stat : j.getAsJsonObject("stats").entrySet()) {
                        StatType type = StatType.fromId(stat.getKey())
                                .orElseThrow(() -> new IllegalArgumentException("Unknown stat: " + stat.getKey()));
                        double amount = stat.getValue().getAsDouble();
                        if (!Double.isFinite(amount)) throw new IllegalArgumentException("Non-finite stat: " + stat.getKey());
                        stats.put(type, amount);
                    }
                }
                boolean fixture = j.has("test_fixture") && j.get("test_fixture").getAsBoolean();
                ReforgeDefinition value = new ReforgeDefinition(id, name, slots, cost, stats, fixture);
                if (slots.isEmpty()) throw new IllegalArgumentException("allowed_slots cannot be empty");
                if (loaded.putIfAbsent(id, value) != null) throw new IllegalArgumentException("Duplicate reforge id: " + id);
            } catch (Exception e) {
                SkyBlockRPG.LOGGER.error("Failed to load reforge data from {}", entry.getKey(), e);
            }
        }
        REFORGES.clear();
        REFORGES.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock reforge definitions.", REFORGES.size());
    }

    public static Optional<ReforgeDefinition> get(String id) {
        return Optional.ofNullable(REFORGES.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }
    public static Collection<ReforgeDefinition> all() {
        return Collections.unmodifiableCollection(REFORGES.values());
    }
    public static List<ReforgeDefinition> forSlot(String slotId) {
        return REFORGES.values().stream().filter(r -> r.allows(slotId)).toList();
    }
    private static String required(JsonObject j, String key) {
        if (!j.has(key) || j.get(key).isJsonNull()) throw new IllegalArgumentException("Missing " + key);
        return j.get(key).getAsString();
    }
}
