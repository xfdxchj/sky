package com.skyrpg.skyblock.item;

/** Rarity tiers and current Hypixel SkyBlock Accessory Power values. */
public enum ItemRarity {
    COMMON(3, 0), UNCOMMON(5, 1), RARE(8, 2), EPIC(12, 3), LEGENDARY(16, 4), MYTHIC(22, 5),
    DIVINE(28, 6), SPECIAL(3, 0), VERY_SPECIAL(5, 1), ULTIMATE(1, 7), ADMIN(22, 8);
    private final int accessoryPower;
    private final int upgradeRank;
    ItemRarity(int accessoryPower, int upgradeRank) { this.accessoryPower = accessoryPower; this.upgradeRank = upgradeRank; }
    public int accessoryPower() { return accessoryPower; }
    public int upgradeRank() { return upgradeRank; }
}
