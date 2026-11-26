package com.skyrpg.skyblock.skill;

import java.util.Locale;
import java.util.Optional;

/** Stable internal IDs. This list is a technical starting point, not a claim of complete current-game parity. */
public enum SkillType {
    COMBAT, FARMING, MINING, FORAGING, FISHING,
    ENCHANTING, ALCHEMY, TAMING, CARPENTRY, RUNECRAFTING, SOCIAL, HUNTING;

    public String id() { return name().toLowerCase(Locale.ROOT); }

    public static Optional<SkillType> fromId(String id) {
        if (id == null) return Optional.empty();
        try { return Optional.of(valueOf(id.trim().toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return Optional.empty(); }
    }
}
