package com.skyrpg.skyblock.accessory;
import com.skyrpg.skyblock.stats.StatType;
import java.util.Map;
public record PowerDefinition(String id, String name, Map<StatType, Double> stats, String powerStoneItemId, boolean starter) {
 public PowerDefinition { stats = Map.copyOf(stats); }
}
