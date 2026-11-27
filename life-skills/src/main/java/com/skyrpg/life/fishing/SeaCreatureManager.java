package com.skyrpg.life.fishing;

import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.life.common.data.LifeDataStore;
import com.skyrpg.life.skill.LifeSkills;
import com.skyrpg.skyblock.api.RewardAPI;
import com.skyrpg.skyblock.api.SkillAPI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spawns and rewards sea creatures. Owner association is in-memory because
 * sea creatures are transient, short-lived combat entities.
 */
public final class SeaCreatureManager {
    private static final String TAG_PREFIX = "skyblock_life:seacreature:";
    private static final Map<UUID, ServerPlayerEntity> OWNERS = new HashMap<>();

    private SeaCreatureManager() {}

    public static void spawn(ServerPlayerEntity player, SeaCreatureDefinition definition) {
        ServerWorld world = player.getServerWorld();
        Entity entity = definition.entityType().create(world);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        var position = player.getPos().add(player.getRotationVec(1.0F).multiply(3.0));
        living.refreshPositionAndAngles(position.x, player.getY(), position.z, 0.0F, 0.0F);
        living.addCommandTag(TAG_PREFIX + definition.id());
        living.setCustomName(Text.literal(definition.displayName()));
        living.setCustomNameVisible(true);
        if (!world.spawnEntity(living)) {
            return;
        }
        OWNERS.put(living.getUuid(), player);
        player.sendMessage(Text.literal("A sea creature appeared: " + definition.displayName() + "!"), false);
    }

    public static void onDeath(LivingEntity entity, ServerPlayerEntity killer) {
        SeaCreatureDefinition definition = definitionOf(entity);
        if (definition == null) {
            return;
        }
        RewardAPI.giveItem(killer, definition.dropItem(), 1);
        LifeDataStore.incrementLong(killer, LifeSkills.FISHING, "sea_creatures_killed", 1);
        if (definition.skillXp() > 0) {
            SkillAPI.addExperience(killer, LifeSkills.FISHING, definition.skillXp());
        }
        OWNERS.remove(entity.getUuid());
        killer.sendMessage(Text.literal("Defeated sea creature " + definition.displayName() + "!"), false);
        SkyBlockLife.LOGGER.debug("Sea creature {} killed by {}.", definition.id(), killer.getName().getString());
    }

    public static SeaCreatureDefinition definitionOf(Entity entity) {
        for (String tag : entity.getCommandTags()) {
            if (!tag.startsWith(TAG_PREFIX)) {
                continue;
            }
            Identifier id = Identifier.tryParse(tag.substring(TAG_PREFIX.length()));
            if (id == null) {
                continue;
            }
            for (SeaCreatureDefinition definition : FishingRegistry.seaCreatures()) {
                if (definition.id().equals(id)) {
                    return definition;
                }
            }
        }
        return null;
    }
}
