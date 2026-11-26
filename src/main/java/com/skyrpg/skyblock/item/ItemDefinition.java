package com.skyrpg.skyblock.item;

import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.stats.StatType;
import java.util.List;
import java.util.Map;

/** Immutable definition; player-owned stacks only store the stable item id and count. */
public record ItemDefinition(
        String id, String name, ItemRarity rarity, ItemType type,
        EquipmentSlot equipmentSlot, int maxStackSize,
        Map<StatType, Double> stats, List<String> lore) {
    public ItemDefinition {
        stats = Map.copyOf(stats);
        lore = List.copyOf(lore);
    }
    public boolean isEquippable() { return equipmentSlot != null; }
}
