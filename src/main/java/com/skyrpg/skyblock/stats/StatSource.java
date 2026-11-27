package com.skyrpg.skyblock.stats;

import java.util.Locale;

/**
 * Origin of a stat contribution. This is metadata for the Core aggregation pipeline;
 * it does not imply that every source category is currently populated.
 */
public enum StatSource {
    BASE,
    EQUIPMENT,
    ACCESSORY,
    SKILL,
    BUFF,
    TEMPORARY,
    OTHER;

    public static StatSource fromSourceId(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) return OTHER;
        String id = sourceId.trim().toLowerCase(Locale.ROOT);
        if (id.startsWith("equipment:") || id.startsWith("equip:")) return EQUIPMENT;
        if (id.startsWith("accessory:") || id.startsWith("power:") || id.startsWith("accessory_tuning:")) return ACCESSORY;
        if (id.startsWith("skill:")) return SKILL;
        if (id.startsWith("buff:")) return BUFF;
        if (id.startsWith("temporary:") || id.startsWith("temp:")) return TEMPORARY;
        return OTHER;
    }
}
