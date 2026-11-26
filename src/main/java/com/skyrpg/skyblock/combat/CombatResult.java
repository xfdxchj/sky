package com.skyrpg.skyblock.combat;

/** Immutable outcome of one SkyBlock damage calculation. */
public record CombatResult(double damage, boolean critical, double defense, double healthBefore, double healthAfter) {
    public boolean killed() { return healthBefore > 0.0 && healthAfter <= 0.0; }
}
