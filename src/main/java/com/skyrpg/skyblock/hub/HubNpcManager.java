package com.skyrpg.skyblock.hub;

import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.map.MapDefinition;
import com.skyrpg.skyblock.map.MapRegistry;
import com.skyrpg.skyblock.map.MapWorldData;
import com.skyrpg.skyblock.npc.NpcDefinition;
import com.skyrpg.skyblock.npc.NpcManager;
import com.skyrpg.skyblock.npc.NpcRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Hub NPC placement from either an operator's current position or a measured project-local POI anchor. */
public final class HubNpcManager {
    public record Result(boolean success, String message) {}
    private static final String POI_TAG_PREFIX = "skyblockrpg_hub_poi_";
    private HubNpcManager() {}

    public static Result place(ServerPlayerEntity player, String poiId, String npcId) {
        if (player == null) return fail("A server player is required.");
        return placeAt(player, poiId, npcId, player.getX(), player.getY(), player.getZ(), player.getYaw(), 0.0F, true);
    }

    public static Result placeAtAnchor(ServerPlayerEntity player, String poiId, String npcId) {
        if (player == null || player.getServer() == null) return fail("A server player is required.");
        HubPoiWorldData.Anchor anchor = HubPoiWorldData.get(player.getServer()).get(poiId);
        if (anchor == null) return fail("No measured runtime anchor exists for POI '" + poiId
                + "'. Record it first with /skyblock hub anchor " + poiId + ".");
        String currentDimension = player.getServerWorld().getRegistryKey().getValue().toString();
        if (!currentDimension.equals(anchor.dimension()))
            return fail("The POI anchor is in " + anchor.dimension() + "; enter that dimension before placing its NPC.");
        return placeAt(player, poiId, npcId, anchor.x(), anchor.y(), anchor.z(), anchor.yaw(), anchor.pitch(), false);
    }

    private static Result placeAt(ServerPlayerEntity player, String poiId, String npcId,
                                  double x, double y, double z, float yaw, float pitch, boolean requirePlayerAtLocation) {
        if (player == null || player.getServer() == null) return fail("A server player is required.");
        HubLocationDefinition poi = HubRegistry.get(poiId).orElse(null);
        if (poi == null || !poi.category().equals("npc")) return fail("Unknown Hub NPC point of interest: " + poiId);
        NpcDefinition npc = NpcRegistry.get(npcId).orElse(null);
        if (npc == null) return fail("Unknown NPC definition: " + npcId);
        MapDefinition map = MapRegistry.get("hub").orElse(null);
        MapWorldData.Record record = MapWorldData.get(player.getServer()).get("hub");
        if (map == null || record == null || record.status() != MapWorldData.Status.COMPLETE)
            return fail("Hub map must be fully assembled before placing NPCs.");

        ServerWorld world = player.getServerWorld();
        String dimension = world.getRegistryKey().getValue().toString();
        if (!dimension.equals(record.dimension()) || !dimension.equals(poi.dimension()))
            return fail("The NPC location must be in the assembled Hub map's configured dimension.");
        BlockPos pos = BlockPos.ofFloored(x, y, z);
        if (pos.getX() < record.originX() || pos.getX() >= (long) record.originX() + map.sizeX()
                || pos.getY() < record.originY() || pos.getY() >= (long) record.originY() + map.sizeY()
                || pos.getZ() < record.originZ() || pos.getZ() >= (long) record.originZ() + map.sizeZ())
            return fail("NPC location is outside the assembled Hub map bounds.");
        if (requirePlayerAtLocation) {
            BlockPos playerPos = player.getBlockPos();
            if (playerPos.getX() < record.originX() || playerPos.getX() >= (long) record.originX() + map.sizeX()
                    || playerPos.getY() < record.originY() || playerPos.getY() >= (long) record.originY() + map.sizeY()
                    || playerPos.getZ() < record.originZ() || playerPos.getZ() >= (long) record.originZ() + map.sizeZ())
                return fail("You must stand inside the assembled Hub map. The POI reference coordinates are not local spawn coordinates.");
        }
        if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())
                || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir())
            return fail("NPC placement requires a solid floor and two clear blocks above the recorded feet position.");

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
        entity.refreshPositionAndAngles(x, y, z, yaw, pitch);
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
                + "' at " + String.format(java.util.Locale.ROOT, "%.1f, %.1f, %.1f", x, y, z)
                + ". Its UUID is recorded in world data.");
    }

    private static Result fail(String message) { return new Result(false, message); }
}
