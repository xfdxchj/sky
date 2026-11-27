package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Map;

/** Read-only API representation of a Core item definition, without exposing registry internals. */
public record ItemView(
        Identifier id, String name, String rarity, String type, String equipmentSlot,
        int maxStackSize, Map<StatType, Double> stats, List<String> lore,
        String accessoryLine, boolean dungeonAccessory, int accessoryPower, boolean reforgeable) {
    public ItemView {
        stats = Map.copyOf(stats);
        lore = List.copyOf(lore);
    }
}
