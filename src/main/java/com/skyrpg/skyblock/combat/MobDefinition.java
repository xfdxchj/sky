package com.skyrpg.skyblock.combat;

/** Combat metadata layered over vanilla entities; does not replace their AI or model. */
public record MobDefinition(String id, String displayName, double defense, long coinReward, double combatXpReward) {
    public MobDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Mob id cannot be blank");
        if (displayName == null || displayName.isBlank()) displayName = id;
        defense = Double.isFinite(defense) ? Math.max(0.0, defense) : 0.0;
        coinReward = Math.max(0L, coinReward);
        combatXpReward = Double.isFinite(combatXpReward) ? Math.max(0.0, combatXpReward) : 0.0;
    }
}
