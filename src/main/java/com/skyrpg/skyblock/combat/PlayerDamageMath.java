package com.skyrpg.skyblock.combat;

/** Pure incoming-damage math for player RPG Defense. No Minecraft entity side effects. */
public final class PlayerDamageMath {
    private static final double BASE_DEFENSE = 100.0;
    private static final double MAX_DAMAGE = 1_000_000_000.0;
    private static final double MAX_DEFENSE = 1_000_000.0;

    private PlayerDamageMath() {}

    /**
     * Applies the appropriate defensive stat exactly once.
     *
     * NORMAL uses Defense; TRUE uses True Defense; BYPASS_RPG_DEFENSE leaves
     * the incoming amount unchanged. Minecraft armor, resistance and shields
     * are separate concerns handled by the later damage bridge.
     */
    public static double applyDefense(double incomingDamage, double defense,
                                      double trueDefense, DamageKind kind) {
        double damage = finiteClamp(incomingDamage, 0.0, MAX_DAMAGE);
        if (damage <= 0.0 || kind == null || kind == DamageKind.BYPASS_RPG_DEFENSE) {
            return damage;
        }

        double effectiveDefense = finiteClamp(
                kind == DamageKind.TRUE ? trueDefense : defense,
                0.0,
                MAX_DEFENSE
        );
        double multiplier = BASE_DEFENSE / (BASE_DEFENSE + effectiveDefense);
        double result = damage * multiplier;
        return Double.isFinite(result) ? finiteClamp(result, 0.0, MAX_DAMAGE) : 0.0;
    }

    public static double damageMultiplier(double defense) {
        double safeDefense = finiteClamp(defense, 0.0, MAX_DEFENSE);
        return BASE_DEFENSE / (BASE_DEFENSE + safeDefense);
    }

    private static double finiteClamp(double value, double min, double max) {
        if (!Double.isFinite(value)) return min;
        return Math.max(min, Math.min(value, max));
    }
}
