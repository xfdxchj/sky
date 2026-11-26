package com.skyrpg.skyblock.stats;

import java.util.Locale;
import java.util.Optional;

/** Extensible stat identifiers for the first RPG data layer. */
public enum StatType {
    HEALTH, DEFENSE, TRUE_DEFENSE, DAMAGE, STRENGTH, CRIT_CHANCE, CRIT_DAMAGE,
    ATTACK_SPEED, FEROCITY, INTELLIGENCE, ABILITY_DAMAGE, SPEED,
    MAGIC_FIND, PET_LUCK, MINING_SPEED, MINING_FORTUNE, FARMING_SPEED, FARMING_FORTUNE, FORAGING_SPEED, FORAGING_FORTUNE,
    FISHING_SPEED, FISHING_FORTUNE, SEA_CREATURE_CHANCE, WISDOM_COMBAT, WISDOM_FARMING,
    WISDOM_MINING, WISDOM_FORAGING, WISDOM_FISHING, WISDOM_ENCHANTING,
    WISDOM_ALCHEMY, WISDOM_TAMING;

    public String id() { return name().toLowerCase(Locale.ROOT); }

    public static Optional<StatType> fromId(String id) {
        if (id == null) return Optional.empty();
        try { return Optional.of(valueOf(id.trim().toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return Optional.empty(); }
    }
}
