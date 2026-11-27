package com.skyrpg.skyblock.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import java.io.Reader;
import java.util.*;

public final class ItemRegistry {
    private static final Map<String, ItemDefinition> ITEMS = new LinkedHashMap<>();
    private ItemRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, ItemDefinition> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/items", id -> id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject j = JsonParser.parseReader(reader).getAsJsonObject();
                String rawId = required(j,"id").trim();
                String resourceNamespace = entry.getKey().getNamespace();
                String id;
                if (rawId.contains(":")) {
                    Identifier parsedId = Identifier.tryParse(rawId);
                    if (parsedId == null || !parsedId.getNamespace().equals(resourceNamespace)) {
                        throw new IllegalArgumentException("Item ID must belong to its data-pack namespace: " + rawId);
                    }
                    id = parsedId.toString();
                } else if (resourceNamespace.equals("skyblockrpg")) {
                    if (Identifier.tryParse("skyblockrpg:" + rawId) == null) {
                        throw new IllegalArgumentException("Invalid Core item ID: " + rawId);
                    }
                    id = rawId;
                } else {
                    Identifier parsedId = Identifier.tryParse(resourceNamespace + ":" + rawId);
                    if (parsedId == null) throw new IllegalArgumentException("Invalid extension item ID: " + rawId);
                    id = parsedId.toString();
                }
                ItemRarity rarity = ItemRarity.valueOf(required(j,"rarity").toUpperCase(Locale.ROOT));
                ItemType type = ItemType.valueOf(required(j,"type").toUpperCase(Locale.ROOT));
                String slotId = optional(j,"equipment_slot","");
                EquipmentSlot slot = slotId.isBlank() ? null : EquipmentSlot.fromId(slotId).orElseThrow(() -> new IllegalArgumentException("Unknown slot: "+slotId));
                int max = j.has("max_stack_size") ? j.get("max_stack_size").getAsInt() : 1;
                if (max < 1 || max > 64) throw new IllegalArgumentException("max_stack_size must be between 1 and 64");
                Map<StatType,Double> stats = new EnumMap<>(StatType.class);
                if (j.has("stats") && j.get("stats").isJsonObject()) for (var stat : j.getAsJsonObject("stats").entrySet()) {
                    StatType key = StatType.fromId(stat.getKey()).orElseThrow(() -> new IllegalArgumentException("Unknown stat: "+stat.getKey()));
                    double amount = stat.getValue().getAsDouble();
                    if (!Double.isFinite(amount)) throw new IllegalArgumentException("Non-finite stat value: " + stat.getKey());
                    stats.put(key, amount);
                }
                List<String> lore = new ArrayList<>();
                if (j.has("lore") && j.get("lore").isJsonArray()) j.getAsJsonArray("lore").forEach(line -> lore.add(line.getAsString()));
                String accessoryLine = optional(j, "accessory_line", id);
                boolean dungeonAccessory = j.has("dungeon_accessory") && j.get("dungeon_accessory").getAsBoolean();
                int accessoryPowerOverride = j.has("accessory_power") ? j.get("accessory_power").getAsInt() : -1;
                boolean reforgeable = j.has("reforgeable") ? j.get("reforgeable").getAsBoolean() : slot != null;
                if (accessoryPowerOverride < -1) throw new IllegalArgumentException("Invalid accessory_power override");
                ItemDefinition definition = new ItemDefinition(id,optional(j,"name",id),rarity,type,slot,max,stats,lore,
                        accessoryLine,dungeonAccessory,accessoryPowerOverride,reforgeable);
                if (loaded.putIfAbsent(id.toLowerCase(Locale.ROOT),definition) != null) throw new IllegalArgumentException("Duplicate item id: "+id);
            } catch (Exception e) {
                SkyBlockRPG.LOGGER.error("Failed to load item data from {}",entry.getKey(),e);
            }
        }
        ITEMS.clear(); ITEMS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock item definitions.",ITEMS.size());
    }
    public static Optional<ItemDefinition> get(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        ItemDefinition exact = ITEMS.get(normalized);
        if (exact != null) return Optional.of(exact);
        if (!normalized.contains(":")) return Optional.ofNullable(ITEMS.get("skyblockrpg:" + normalized));
        if (normalized.startsWith("skyblockrpg:")) {
            return Optional.ofNullable(ITEMS.get(normalized.substring("skyblockrpg:".length())));
        }
        return Optional.empty();
    }
    public static Collection<ItemDefinition> all() { return Collections.unmodifiableCollection(ITEMS.values()); }
    private static String required(JsonObject j,String k) {
        if (!j.has(k) || j.get(k).isJsonNull()) throw new IllegalArgumentException("Missing "+k);
        return j.get(k).getAsString();
    }
    private static String optional(JsonObject j,String k,String d) { return j.has(k) && !j.get(k).isJsonNull() ? j.get(k).getAsString() : d; }
}