package com.skyrpg.skyblock.combat;

/** Hypixel-style melee damage calculation, isolated from Minecraft entity APIs. */
public final class CombatMath {
    private static final double MAX_STAT = 1_000_000.0;
    private static final double MAX_DAMAGE = 1_000_000_000.0;

    private CombatMath() {}

    /**
     * Simplified melee formula based on the current community-documented SkyBlock formula:
     * floor((5 + WeaponDamage) * (1 + Strength / 100) * CritMultiplier * DefenseMultiplier).
     * Additive bonuses, enchantments, combat level and ability modifiers are not implemented yet.
     */
    public static double calculateDamage(double weaponDamage, double strength, double critDamage,
                                         double targetDefense, boolean critical) {
        double weapon = finiteClamp(weaponDamage, 0.0, MAX_STAT);
        double str = finiteClamp(strength, 0.0, MAX_STAT);
        double cd = finiteClamp(critDamage, 0.0, MAX_STAT);
        double defense = finiteClamp(targetDefense, 0.0, MAX_STAT);

        double strengthMultiplier = 1.0 + str / 100.0;
        double criticalMultiplier = critical ? 1.0 + cd / 100.0 : 1.0;
        double defenseMultiplier = 100.0 / (100.0 + defense);
        double result = (5.0 + weapon) * strengthMultiplier * criticalMultiplier * defenseMultiplier;
        if (!Double.isFinite(result)) return MAX_DAMAGE;
        return Math.floor(Math.max(0.0, Math.min(result, MAX_DAMAGE)));
    }

    public static double critChance(double value) {
        return finiteClamp(value, 0.0, 100.0);
    }

    private static double finiteClamp(double value, double min, double max) {
        if (!Double.isFinite(value)) return min;
        return Math.max(min, Math.min(value, max));
    }
}
