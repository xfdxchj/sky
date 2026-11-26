package com.skyrpg.skyblock.reforge;

import com.skyrpg.skyblock.stats.StatType;
import java.util.List;
import java.util.Map;

public record ReforgeDefinition(
        String id, String name, List<String> allowedSlots, long cost,
        Map<StatType, Double> stats, boolean testFixture) {
    public ReforgeDefinition {
        id = id.trim().toLowerCase(java.util.Locale.ROOT);
        allowedSlots = allowedSlots.stream().map(s -> s.trim().toLowerCase(java.util.Locale.ROOT)).toList();
        stats = Map.copyOf(stats);
    }

    public boolean allows(String slotId) {
        return allowedSlots.contains(slotId.toLowerCase(java.util.Locale.ROOT));
    }
}
