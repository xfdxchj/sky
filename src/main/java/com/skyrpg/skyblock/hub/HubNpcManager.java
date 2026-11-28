package com.skyrpg.skyblock.hub;

import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.map.MapDefinition;
import com.skyrpg.skyblock.map.MapRegistry;
import com.skyrpg.skyblock.map.MapWorldData;
import com.skyrpg.skyblock.npc.NpcDefinition;
import com.skyrpg.skyblock.npc.NpcManager;
import com.skyrpg.skyblock.npc.NpcRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Manual, map-relative Hub NPC placement. Wiki POI coordinates are intentionally never used as spawn coordinates. */
public final class HubNpcManager {
    public record Result(boolean success, String message) {}
    private static final String POI_TAG_PREFIX = "skyblockrpg_hub_poi_";
    private HubNpcManager() {}

    public static Result place(ServerPlayerEntity player, String poiId, String npcId) {
        if (player == null || player.getServer() == null) return fail("A server player is required.");
        HubLocationDefinition poi = HubRegistry.get(poiId).orElse(null);
        if (poi == null || !poi.category().equals("npc")) return fail("Unknown Hub NPC point of interest: " + poiId);
        NpcDefinition npc = NpcRegistry.get(npcId).orElse(null);
        if (npc == null) return fail("Unknown NPC definition: " + npcId);
        MapDefinition map = MapRegistry.get("hub").orElse(null);
        MapWorldData.Record record = MapWorldData.get(player.getServer()).get("hub");
        if (map == null || record == null || record.status() != MapWorldData.Status.COMPLETE)
            return fail("Hub map must be fully assembled before placing NPCs.");
        String dimension = player.getServerWorld().getRegistryKey().getValue().toString();
        if (!dimension.equals(record.dimension()) || !dimension.equals(poi.dimension()))
            return fail("Stand inside the assembled Hub map in its configured dimension.");
        BlockPos pos = player.getBlockPos();
        if (pos.getX() < record.originX() || pos.getX() >= (long) record.originX() + map.sizeX()
                || pos.getY() < record.originY() || pos.getY() >= (long) record.originY() + map.sizeY()
                || pos.getZ() < record.originZ() || pos.getZ() >= (long) record.originZ() + map.sizeZ())
            return fail("You must stand inside the assembled Hub map. The POI reference coordinates are not local spawn coordinates.");
        var world = player.getServerWorld();
        if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())
                || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir())
            return fail("NPC placement requires a solid floor and two clear blocks above your feet.");
        HubNpcWorldData placements = HubNpcWorldData.get(player.getServer());
        HubNpcWorldData.Placement existing = placements.get(poi.id());
        if (existing != null) {
            return fail("This Hub POI already has a persistent NPC placement ("
                    + existing.npcId() + ", UUID " + existing.entityUuid() + "). Remove the old entity, then run /skyblock hub npc forget "
                    + poi.id() + " before replacing it.");
        }
        String poiTag = POI_TAG_PREFIX + poi.id().toLowerCase(java.util.Locale.ROOT);
        Identifier entityId = Identifier.tryParse(npc.entityType());
        EntityType<?> type = entityId == null ? null : Registries.ENTITY_TYPE.getOrEmpty(entityId).orElse(null);
        if (type == null) return fail("Unknown configured entity type: " + npc.entityType());
        Entity entity = type.create(world);
        if (!(entity instanceof LivingEntity)) return fail("Configured NPC entity must be living: " + npc.entityType());
        entity.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0.0F);
        entity.addCommandTag(NpcManager.entityTag(npc.id()));
        entity.addCommandTag(poiTag);
        entity.setCustomName(Text.literal(npc.displayName()));
        entity.setCustomNameVisible(true);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        if (entity instanceof MobEntity mob) mob.setAiDisabled(true);
        if (!world.spawnEntity(entity)) return fail("Minecraft rejected the NPC entity spawn.");
        placements.put(new HubNpcWorldData.Placement(poi.id(), npc.id(), entity.getUuidAsString(),
                dimension, entity.getX(), entity.getY(), entity.getZ()));
        SkyBlockRPG.LOGGER.info("Placed Hub NPC '{}' ({}) UUID {} at {} in {}.",
                npc.id(), poi.id(), entity.getUuidAsString(), pos, dimension);
        return new Result(true, "Placed " + npc.displayName() + " for Hub POI '" + poi.displayName()
                + "'. Its UUID is recorded in world data to prevent duplicate placement across unloaded chunks and restarts.");
    }

    private static Result fail(String message) { return new Result(false, message); }
}