package com.skyrpg.skyblock.combat;

import java.util.List;

public record MobDefinition(
        String id, String displayName, String entityType, String spawnEntityType, int level,
        double health, double damage, double defense, long coinReward,
        double combatXpReward, String sourceType, String sourceUrl,
        String retrievedAt, String confidence, String familyId,
        String category, List<String> mobTypes, String area,
        List<MobDropDefinition> drops) {
    public MobDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Mob id cannot be blank");
        if (displayName == null || displayName.isBlank()) displayName = id;
        entityType = entityType == null ? "" : entityType;
        spawnEntityType = spawnEntityType == null || spawnEntityType.isBlank() ? entityType : spawnEntityType;
        level = Math.max(0, level);
        health = nonNegative(health);
        damage = nonNegative(damage);
        defense = nonNegative(defense);
        coinReward = Math.max(0L, coinReward);
        combatXpReward = nonNegative(combatXpReward);
        sourceType = sourceType == null ? "unknown" : sourceType;
        sourceUrl = sourceUrl == null ? "" : sourceUrl;
        retrievedAt = retrievedAt == null ? "" : retrievedAt;
        confidence = confidence == null ? "unverified" : confidence;
        familyId = familyId == null || familyId.isBlank() ? id : familyId;
        category = category == null ? "unknown" : category;
        mobTypes = mobTypes == null ? List.of() : List.copyOf(mobTypes);
        area = area == null ? "unknown" : area;
        drops = drops == null ? List.of() : List.copyOf(drops);
    }
    private static double nonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }
}