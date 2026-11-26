package com.skyrpg.skyblock.inventory;

import java.util.Locale;
import java.util.Optional;

public enum EquipmentSlot {
    WEAPON, HELMET, CHESTPLATE, LEGGINGS, BOOTS, ACCESSORY;

    public String id() { return name().toLowerCase(Locale.ROOT); }
    public static Optional<EquipmentSlot> fromId(String id) {
        if (id == null) return Optional.empty();
        try { return Optional.of(valueOf(id.trim().toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException ex) { return Optional.empty(); }
    }
}
