package com.skyrpg.skyblock.item;

/** Rarity tiers used by current SkyBlock accessory power values. */
public enum ItemRarity {
    COMMON(3), UNCOMMON(5), RARE(8), EPIC(12), LEGENDARY(16), MYTHIC(22),
    DIVINE(28), SPECIAL(3), VERY_SPECIAL(5), ULTIMATE(1), ADMIN(22);
    private final int accessoryPower;
    ItemRarity(int accessoryPower) { this.accessoryPower = accessoryPower; }
    public int accessoryPower() { return accessoryPower; }
}
