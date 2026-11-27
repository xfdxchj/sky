package com.skyrpg.skyblock.accessory;

import com.skyrpg.skyblock.inventory.PlayerInventory;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatSource;
import com.skyrpg.skyblock.stats.StatType;
import java.util.*;

public final class AccessoryPowerManager {
    private AccessoryPowerManager() {}

    public static int getAccessoryPower(PlayerInventory inv, boolean dungeon) {
        if (inv == null) return 0;
        long total = 0L;
        for (String id : inv.getEffectiveAccessories()) {
            var definition = com.skyrpg.skyblock.item.ItemRegistry.get(id).orElse(null);
            if (definition == null) continue;
            long value = definition.accessoryPower();
            if (dungeon && definition.dungeonAccessory()) value *= 2L;
            total = Math.min(Integer.MAX_VALUE, total + value);
        }
        return (int) total;
    }

    public static double getStatsMultiplier(int ap) {
        return ap <= 0 ? 0.0 : 29.97 * Math.pow(Math.log(0.0019 * ap + 1.0), 1.2);
    }

    public static List<StatModifier> getPowerModifiers(String selected, int ap) {
        PowerDefinition power = PowerRegistry.get(selected).orElse(null);
        if (power == null || ap <= 0) return List.of();
        double multiplier = getStatsMultiplier(ap);
        List<StatModifier> result = new ArrayList<>();
        power.stats().forEach((stat, value) -> result.add(new StatModifier(
                "accessory_power:" + power.id(), stat, value * multiplier,
                StatModifier.Operation.ADDITIVE, StatSource.ACCESSORY)));
        return List.copyOf(result);
    }

    public static int getTuningPoints(int ap) { return Math.max(0, ap / 10); }

    public static double tuningValue(StatType stat) {
        if (stat == null) return 0.0;
        return switch (stat) {
            case HEALTH, DEFENSE, SPEED, STRENGTH, CRIT_DAMAGE, INTELLIGENCE -> 1.0;
            case CRIT_CHANCE -> 0.2;
            case ATTACK_SPEED -> 0.3;
            default -> 0.0;
        };
    }

    public static boolean isTunable(StatType stat) { return tuningValue(stat) > 0; }
}
