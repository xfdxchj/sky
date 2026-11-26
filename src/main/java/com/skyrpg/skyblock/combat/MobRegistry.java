package com.skyrpg.skyblock.combat;

import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import java.util.Locale;
import java.util.Map;

/**
 * Temporary vanilla-entity to SkyBlock starter-mob mapping.
 * Reward values are based on the lowest listed Hypixel Private Island variants where available;
 * this is not a complete Hypixel mob catalogue or a claim that vanilla mobs have matching levels.
 */
public final class MobRegistry {
    private static final MobDefinition UNREGISTERED =
            new MobDefinition("unregistered", "Unregistered Creature", 0.0, 0L, 0.0);

    private static final Map<String, MobDefinition> DEFINITIONS = Map.of(
            "zombie", new MobDefinition("zombie", "Private Island Zombie (Lv1 data)", 0.0, 1L, 5.0),
            "skeleton", new MobDefinition("skeleton", "Private Island Skeleton (Lv1 data)", 0.0, 1L, 5.0),
            "spider", new MobDefinition("spider", "Private Island Spider (minimum-tier data)", 0.0, 1L, 5.0)
    );

    private MobRegistry() {}

    public static MobDefinition get(Entity entity) {
        String id = Registries.ENTITY_TYPE.getId(entity.getType()).getPath().toLowerCase(Locale.ROOT);
        return DEFINITIONS.getOrDefault(id, UNREGISTERED);
    }
}
