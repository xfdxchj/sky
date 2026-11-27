package com.skyrpg.skyblock.item;

import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.stats.StatType;
import java.util.List;
import java.util.Map;

/** Immutable definition; player-owned stacks only store the stable item id and count. */
public record ItemDefinition(
        String id, String name, ItemRarity rarity, ItemType type,
        EquipmentSlot equipmentSlot, int maxStackSize,
        Map<StatType, Double> stats, List<String> lore,
        String accessoryLine, boolean dungeonAccessory, int accessoryPowerOverride, boolean reforgeable) {
    public ItemDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Item id is required");
        if (maxStackSize < 1 || maxStackSize > 64) throw new IllegalArgumentException("maxStackSize must be between 1 and 64");
        if (stats == null || lore == null) throw new IllegalArgumentException("stats and lore are required");
        stats.forEach((stat, amount) -> {
            if (stat == null || amount == null || !Double.isFinite(amount))
                throw new IllegalArgumentException("Item stat values must be finite and have a stat key");
        });
        stats = Map.copyOf(stats);
        lore = List.copyOf(lore);
        accessoryLine = accessoryLine == null || accessoryLine.isBlank() ? id : accessoryLine.trim().toLowerCase(java.util.Locale.ROOT);
    }
    public boolean isEquippable() { return equipmentSlot != null; }
    public int accessoryPower() { return accessoryPowerOverride >= 0 ? accessoryPowerOverride : rarity.accessoryPower(); }
}
