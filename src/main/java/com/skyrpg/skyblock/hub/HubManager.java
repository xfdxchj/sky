package com.skyrpg.skyblock.hub;

import com.skyrpg.skyblock.map.MapDefinition;
import com.skyrpg.skyblock.map.MapRegistry;
import com.skyrpg.skyblock.map.MapWorldData;
import com.skyrpg.skyblock.world.SkyBlockWorldResolver;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

/** Runtime gate for Hub routing. The Hub must be assembled and have a validated landing point. */
public final class HubManager {
    public record Result(boolean success, String message) {}
    private HubManager() {}

    public static Result configureSpawn(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) return fail("A server player is required.");
        ServerWorld world = player.getServerWorld();
        if (!SkyBlockWorldResolver.isSkyBlockWorld(world))
            return fail("Hub spawn must be configured inside the dedicated SkyBlock void world.");
        MapDefinition hub = MapRegistry.get("hub").orElse(null);
        if (hub == null) return fail("Hub map resources are not loaded. Build with -PincludeCommunityMaps=true.");
        MapWorldData.Record record = MapWorldData.get(player.getServer()).get("hub");
        if (record == null || record.status() != MapWorldData.Status.COMPLETE)
            return fail("Hub map assembly must be COMPLETE before configuring its spawn.");
        if (!record.dimension().equals(world.getRegistryKey().getValue().toString()))
            return fail("Hub map and player are in different dimensions.");
        BlockPos pos = player.getBlockPos();
        if (!insideMap(record, hub, pos))
            return fail("Stand at the intended Village landing point inside the assembled Hub map.");
        if (!isSafeLanding(world, pos))
            return fail("Spawn position is unsafe. Stand with a solid floor and two clear blocks above.");
        HubWorldData.get(player.getServer()).setSpawn(new HubWorldData.Spawn(
                world.getRegistryKey().getValue().toString(),
                player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
        return new Result(true, "Hub spawn saved at " + format(player.getX(), player.getY(), player.getZ()) + ".");
    }

    public static Result visit(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) return fail("A server player is required.");
        HubWorldData.Spawn spawn = HubWorldData.get(player.getServer()).getSpawn();
        if (spawn == null) return fail("Hub is not ready: assemble the Hub map and configure its spawn first.");
        MapDefinition hub = MapRegistry.get("hub").orElse(null);
        MapWorldData.Record record = MapWorldData.get(player.getServer()).get("hub");
        if (hub == null || record == null || record.status() != MapWorldData.Status.COMPLETE)
            return fail("Hub map is not fully assembled. Routing is disabled.");
        if (!spawn.dimension().equals(record.dimension())) return fail("Saved Hub spawn dimension does not match the Hub map.");
        Identifier id = Identifier.tryParse(spawn.dimension());
        if (id == null) return fail("Saved Hub spawn has an invalid dimension.");
        ServerWorld target = player.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
        if (target == null || !SkyBlockWorldResolver.isSkyBlockWorld(target))
            return fail("Saved Hub destination is not in the dedicated SkyBlock world.");
        BlockPos pos = BlockPos.ofFloored(spawn.x(), spawn.y(), spawn.z());
        if (!insideMap(record, hub, pos) || !target.getWorldBorder().contains(pos) || !isSafeLanding(target, pos))
            return fail("Saved Hub spawn is no longer safe or is outside the assembled map. Reconfigure it in-game.");
        player.teleport(target, spawn.x(), spawn.y(), spawn.z(), Set.of(), spawn.yaw(), spawn.pitch());
        return new Result(true, "Returned to Hub.");
    }

    public static boolean isConfigured(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) return false;
        return HubWorldData.get(player.getServer()).getSpawn() != null;
    }

    private static boolean insideMap(MapWorldData.Record record, MapDefinition map, BlockPos pos) {
        return pos.getX() >= record.originX() && pos.getX() < (long) record.originX() + map.sizeX()
                && pos.getY() >= record.originY() && pos.getY() < (long) record.originY() + map.sizeY()
                && pos.getZ() >= record.originZ() && pos.getZ() < (long) record.originZ() + map.sizeZ();
    }

    private static boolean isSafeLanding(ServerWorld world, BlockPos pos) {
        return pos.getY() > world.getBottomY() && pos.getY() + 1 < world.getTopY()
                && world.getWorldBorder().contains(pos)
                && world.getBlockState(pos.down()).isSolidBlock(world, pos.down())
                && world.getBlockState(pos).isAir()
                && world.getBlockState(pos.up()).isAir();
    }

    private static Result fail(String message) { return new Result(false, message); }
    private static String format(double x, double y, double z) {
        return String.format(java.util.Locale.ROOT, "%.2f, %.2f, %.2f", x, y, z);
    }
}
