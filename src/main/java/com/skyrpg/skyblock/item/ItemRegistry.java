package com.skyrpg.skyblock.item;

import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.stats.StatType;
import java.util.*;

/** Initial built-in item catalogue. Definitions will move to data resources in a later stage. */
public final class ItemRegistry {
    private static final Map<String, ItemDefinition> ITEMS = new LinkedHashMap<>();
    static {
        register(new ItemDefinition("training_sword", "Training Sword", ItemRarity.COMMON, ItemType.WEAPON, EquipmentSlot.WEAPON, 1, Map.of(StatType.DAMAGE, 10.0, StatType.STRENGTH, 10.0, StatType.CRIT_CHANCE, 25.0, StatType.CRIT_DAMAGE, 50.0), List.of("A basic weapon for new adventurers.")));
        register(new ItemDefinition("iron_helmet", "Iron Helmet", ItemRarity.COMMON, ItemType.ARMOR, EquipmentSlot.HELMET, 1, Map.of(StatType.HEALTH, 15.0, StatType.DEFENSE, 10.0), List.of("A simple iron helmet.")));
        register(new ItemDefinition("iron_chestplate", "Iron Chestplate", ItemRarity.COMMON, ItemType.ARMOR, EquipmentSlot.CHESTPLATE, 1, Map.of(StatType.HEALTH, 30.0, StatType.DEFENSE, 20.0), List.of("A simple iron chestplate.")));
        register(new ItemDefinition("iron_leggings", "Iron Leggings", ItemRarity.COMMON, ItemType.ARMOR, EquipmentSlot.LEGGINGS, 1, Map.of(StatType.HEALTH, 20.0, StatType.DEFENSE, 15.0), List.of("A simple pair of iron leggings.")));
        register(new ItemDefinition("iron_boots", "Iron Boots", ItemRarity.COMMON, ItemType.ARMOR, EquipmentSlot.BOOTS, 1, Map.of(StatType.HEALTH, 10.0, StatType.DEFENSE, 5.0), List.of("A simple pair of iron boots.")));
        register(new ItemDefinition("talisman_of_health", "Talisman of Health", ItemRarity.UNCOMMON, ItemType.ACCESSORY, EquipmentSlot.ACCESSORY, 1, Map.of(StatType.HEALTH, 25.0), List.of("An accessory that grants additional health.")));
        register(new ItemDefinition("enchanted_iron", "Enchanted Iron", ItemRarity.UNCOMMON, ItemType.MATERIAL, null, 64, Map.of(), List.of("A refined crafting material.")));
        register(new ItemDefinition("golden_apple", "Golden Apple", ItemRarity.UNCOMMON, ItemType.CONSUMABLE, null, 16, Map.of(), List.of("A consumable item. Use is not implemented in P2.")));
    }
    private ItemRegistry() {}
    public static void register(ItemDefinition definition) { ITEMS.put(definition.id(), definition); }
    public static Optional<ItemDefinition> get(String id) { return Optional.ofNullable(ITEMS.get(id == null ? "" : id.toLowerCase(Locale.ROOT))); }
    public static Collection<ItemDefinition> all() { return Collections.unmodifiableCollection(ITEMS.values()); }
}
