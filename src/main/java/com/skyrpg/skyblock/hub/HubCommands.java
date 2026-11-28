package com.skyrpg.skyblock.hub;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.skyrpg.skyblock.map.MapDefinition;
import com.skyrpg.skyblock.map.MapRegistry;
import com.skyrpg.skyblock.map.MapWorldData;
import com.skyrpg.skyblock.world.SkyBlockWorldResolver;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class HubCommands {
    private HubCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> command() {
        return literal("hub")
                .executes(context -> {
                    var server = context.getSource().getServer();
                    context.getSource().sendFeedback(() -> Text.literal("Hub | Map: "
                            + (MapRegistry.get("hub").isPresent() ? "loaded" : "not loaded")
                            + " | Points of interest: " + HubRegistry.all().size()
                            + " | Runtime anchors: " + HubPoiWorldData.get(server).all().size()
                            + " | Spawn: " + (HubWorldData.get(server).getSpawn() != null ? "configured" : "not configured")), false);
                    return 1;
                })
                .then(literal("status").executes(context -> {
                    var source = context.getSource();
                    var map = MapRegistry.get("hub").orElse(null);
                    var record = MapWorldData.get(source.getServer()).get("hub");
                    source.sendFeedback(() -> Text.literal("Hub map resource: " + (map == null ? "MISSING" :
                            map.sizeX() + "x" + map.sizeY() + "x" + map.sizeZ() + " / " + map.slices().size() + " slices")), false);
                    source.sendFeedback(() -> Text.literal("Assembly: " + (record == null ? "NOT STARTED" : record.status()
                            + " | origin " + record.originX() + " " + record.originY() + " " + record.originZ())), false);
                    source.sendFeedback(() -> Text.literal("Safe spawn: " +
                            (HubWorldData.get(source.getServer()).getSpawn() != null ? "CONFIGURED" : "NOT CONFIGURED")), false);
                    source.sendFeedback(() -> Text.literal("Measured POI anchors: " + HubPoiWorldData.get(source.getServer()).all().size()
                            + " / " + HubRegistry.all().size()), false);
                    source.sendFeedback(() -> Text.literal("First-join/rejoin routing activates only when the map is COMPLETE and a safe spawn is saved."), false);
                    return 1;
                }))
                .then(literal("setspawn").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    HubManager.Result result = HubManager.configureSpawn(player);
                    if (!result.success()) {
                        context.getSource().sendError(Text.literal(result.message()));
                        return 0;
                    }
                    context.getSource().sendFeedback(() -> Text.literal(result.message()), false);
                    return 1;
                }))
                .then(literal("anchor")
                        .then(argument("poiId", StringArgumentType.word()).requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    var player = context.getSource().getPlayerOrThrow();
                                    String poiId = StringArgumentType.getString(context, "poiId");
                                    HubLocationDefinition poi = HubRegistry.get(poiId).orElse(null);
                                    if (poi == null) {
                                        context.getSource().sendError(Text.literal("Unknown Hub POI: " + poiId));
                                        return 0;
                                    }
                                    MapDefinition map = MapRegistry.get("hub").orElse(null);
                                    MapWorldData.Record record = MapWorldData.get(player.getServer()).get("hub");
                                    if (map == null || record == null || record.status() != MapWorldData.Status.COMPLETE) {
                                        context.getSource().sendError(Text.literal("Hub map must be fully assembled before recording POI anchors."));
                                        return 0;
                                    }
                                    String dimension = player.getServerWorld().getRegistryKey().getValue().toString();
                                    if (!SkyBlockWorldResolver.isSkyBlockWorld(player.getServerWorld())
                                            || !dimension.equals(record.dimension()) || !dimension.equals(poi.dimension())) {
                                        context.getSource().sendError(Text.literal("Stand inside the Hub map in its configured SkyBlock dimension."));
                                        return 0;
                                    }
                                    BlockPos pos = player.getBlockPos();
                                    if (pos.getX() < record.originX() || pos.getX() >= (long) record.originX() + map.sizeX()
                                            || pos.getY() < record.originY() || pos.getY() >= (long) record.originY() + map.sizeY()
                                            || pos.getZ() < record.originZ() || pos.getZ() >= (long) record.originZ() + map.sizeZ()) {
                                        context.getSource().sendError(Text.literal("Your position is outside the assembled Hub map bounds."));
                                        return 0;
                                    }
                                    var world = player.getServerWorld();
                                    if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())
                                            || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()) {
                                        context.getSource().sendError(Text.literal("Anchor position must have a solid floor and two clear blocks."));
                                        return 0;
                                    }
                                    HubPoiWorldData.get(player.getServer()).put(new HubPoiWorldData.Anchor(
                                            poi.id(), dimension, player.getX(), player.getY(), player.getZ(),
                                            player.getYaw(), player.getPitch()));
                                    context.getSource().sendFeedback(() -> Text.literal("Saved project-local anchor for "
                                            + poi.displayName() + " [" + poi.id() + "] at "
                                            + format(player.getX(), player.getY(), player.getZ()) + "."), false);
                                    return 1;
                                }))
                        .then(literal("clear").requires(source -> source.hasPermissionLevel(2))
                                .then(argument("poiId", StringArgumentType.word()).executes(context -> {
                                    String poiId = StringArgumentType.getString(context, "poiId");
                                    if (!HubPoiWorldData.get(context.getSource().getServer()).remove(poiId)) {
                                        context.getSource().sendError(Text.literal("No runtime anchor is saved for POI: " + poiId));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal("Cleared runtime anchor for POI '" + poiId + "'."), false);
                                    return 1;
                                }))))
                .then(literal("npc")
                        .then(literal("list").executes(context -> {
                            var placements = HubNpcWorldData.get(context.getSource().getServer()).all();
                            context.getSource().sendFeedback(() -> Text.literal("Registered Hub NPC placements: " + placements.size()), false);
                            for (HubNpcWorldData.Placement placement : placements) {
                                context.getSource().sendFeedback(() -> Text.literal(placement.poiId() + " | "
                                        + placement.npcId() + " | UUID " + placement.entityUuid()
                                        + " | " + placement.dimension() + " "
                                        + format(placement.x(), placement.y(), placement.z())), false);
                            }
                            return 1;
                        }))
                        .then(literal("forget").requires(source -> source.hasPermissionLevel(2))
                                .then(argument("poiId", StringArgumentType.word()).executes(context -> {
                                    String poiId = StringArgumentType.getString(context, "poiId");
                                    boolean removed = HubNpcWorldData.get(context.getSource().getServer()).remove(poiId);
                                    if (!removed) {
                                        context.getSource().sendError(Text.literal("No persistent NPC placement is registered for POI: " + poiId));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal("Forgot the NPC placement record for '" + poiId
                                            + "'. Ensure the old entity was removed manually before placing a replacement."), false);
                                    return 1;
                                }))))
                .then(literal("place-npc").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("poiId", StringArgumentType.word())
                                .then(argument("npcId", StringArgumentType.word()).executes(context -> {
                                    var player = context.getSource().getPlayerOrThrow();
                                    HubNpcManager.Result result = HubNpcManager.place(player,
                                            StringArgumentType.getString(context, "poiId"),
                                            StringArgumentType.getString(context, "npcId"));
                                    if (!result.success()) {
                                        context.getSource().sendError(Text.literal(result.message()));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal(result.message()), false);
                                    return 1;
                                }))))
                .then(literal("place-npc-at-anchor").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("poiId", StringArgumentType.word())
                                .then(argument("npcId", StringArgumentType.word()).executes(context -> {
                                    var player = context.getSource().getPlayerOrThrow();
                                    HubNpcManager.Result result = HubNpcManager.placeAtAnchor(player,
                                            StringArgumentType.getString(context, "poiId"),
                                            StringArgumentType.getString(context, "npcId"));
                                    if (!result.success()) {
                                        context.getSource().sendError(Text.literal(result.message()));
                                        return 0;
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal(result.message()), false);
                                    return 1;
                                }))))
                .then(literal("visit").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    HubManager.Result result = HubManager.visit(player);
                    if (!result.success()) {
                        context.getSource().sendError(Text.literal(result.message()));
                        return 0;
                    }
                    context.getSource().sendFeedback(() -> Text.literal(result.message()), false);
                    return 1;
                }))
                .then(literal("list").executes(context -> {
                    var anchors = HubPoiWorldData.get(context.getSource().getServer());
                    context.getSource().sendFeedback(() -> Text.literal("Hub points of interest: "
                            + HubRegistry.all().size() + " | Anchored: " + anchors.all().size()), false);
                    for (HubLocationDefinition location : HubRegistry.all()) {
                        HubPoiWorldData.Anchor anchor = anchors.get(location.id());
                        context.getSource().sendFeedback(() -> Text.literal(location.id() + " | "
                                + location.displayName() + " | " + location.category()
                                + " | Reference XYZ " + format(location.x(), location.y(), location.z())
                                + (anchor == null ? " | NOT ANCHORED" : " | Runtime " + format(anchor.x(), anchor.y(), anchor.z()))), false);
                    }
                    return 1;
                }))
                .then(literal("info").then(argument("locationId", StringArgumentType.word()).executes(context -> {
                    String id = StringArgumentType.getString(context, "locationId");
                    HubLocationDefinition location = HubRegistry.get(id).orElse(null);
                    if (location == null) {
                        context.getSource().sendError(Text.literal("Unknown Hub location: " + id));
                        return 0;
                    }
                    HubPoiWorldData.Anchor anchor = HubPoiWorldData.get(context.getSource().getServer()).get(id);
                    context.getSource().sendFeedback(() -> Text.literal(location.displayName()
                            + " [" + location.id() + "]"), false);
                    context.getSource().sendFeedback(() -> Text.literal("Category: " + location.category()
                            + " | Parent: " + (location.parentId().isBlank() ? "none" : location.parentId())), false);
                    context.getSource().sendFeedback(() -> Text.literal("Reference dimension: " + location.dimension()
                            + " | Reference coordinates: " + format(location.x(), location.y(), location.z())), false);
                    context.getSource().sendFeedback(() -> Text.literal(anchor == null
                            ? "Project-local anchor: NOT RECORDED"
                            : "Project-local anchor: " + anchor.dimension() + " " + format(anchor.x(), anchor.y(), anchor.z())), false);
                    if (!location.description().isBlank())
                        context.getSource().sendFeedback(() -> Text.literal(location.description()), false);
                    context.getSource().sendFeedback(() -> Text.literal("Source: " + location.sourceType()
                            + " | " + location.sourceUrl()), false);
                    if (!location.referenceRevision().isBlank())
                        context.getSource().sendFeedback(() -> Text.literal("Reference: " + location.referenceRevision()), false);
                    return 1;
                })))
                .then(literal("locate").then(argument("locationId", StringArgumentType.word()).executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    String id = StringArgumentType.getString(context, "locationId");
                    HubLocationDefinition location = HubRegistry.get(id).orElse(null);
                    if (location == null) {
                        context.getSource().sendError(Text.literal("Unknown Hub location: " + id));
                        return 0;
                    }
                    HubPoiWorldData.Anchor anchor = HubPoiWorldData.get(player.getServer()).get(id);
                    if (anchor == null) {
                        context.getSource().sendError(Text.literal("This POI has no project-local anchor. An operator must stand at the real location and run /skyblock hub anchor " + id + "."));
                        return 0;
                    }
                    String currentDimension = player.getServerWorld().getRegistryKey().getValue().toString();
                    if (!currentDimension.equals(anchor.dimension())) {
                        context.getSource().sendError(Text.literal("Location is in " + anchor.dimension()
                                + ", while you are in " + currentDimension + "."));
                        return 0;
                    }
                    double dx = anchor.x() - player.getX();
                    double dy = anchor.y() - player.getY();
                    double dz = anchor.z() - player.getZ();
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    context.getSource().sendFeedback(() -> Text.literal(location.displayName()
                            + " | XYZ " + format(anchor.x(), anchor.y(), anchor.z())
                            + " | Distance: " + String.format(java.util.Locale.ROOT, "%.1f", distance) + " blocks"), false);
                    return 1;
                })))
                .then(literal("nearest").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    String dimension = player.getServerWorld().getRegistryKey().getValue().toString();
                    var anchors = HubPoiWorldData.get(player.getServer()).all().stream()
                            .filter(anchor -> anchor.dimension().equals(dimension))
                            .sorted(Comparator.comparingDouble(anchor -> squaredDistance(anchor, player.getX(), player.getY(), player.getZ())))
                            .limit(5).toList();
                    if (anchors.isEmpty()) {
                        context.getSource().sendFeedback(() -> Text.literal("No Hub POIs have measured project-local anchors in this dimension. Use /skyblock hub anchor <poiId> while standing at each real location."), false);
                        return 1;
                    }
                    context.getSource().sendFeedback(() -> Text.literal("Nearest Hub points:"), false);
                    for (HubPoiWorldData.Anchor anchor : anchors) {
                        HubLocationDefinition location = HubRegistry.get(anchor.poiId()).orElse(null);
                        if (location == null) continue;
                        double distance = Math.sqrt(squaredDistance(anchor, player.getX(), player.getY(), player.getZ()));
                        context.getSource().sendFeedback(() -> Text.literal(location.displayName() + " ["
                                + location.id() + "] - " + String.format(java.util.Locale.ROOT, "%.1f", distance) + " blocks"), false);
                    }
                    return 1;
                }));
    }

    private static double squaredDistance(HubPoiWorldData.Anchor anchor, double x, double y, double z) {
        double dx = anchor.x() - x;
        double dy = anchor.y() - y;
        double dz = anchor.z() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static String format(double x, double y, double z) {
        return String.format(java.util.Locale.ROOT, "%.1f, %.1f, %.1f", x, y, z);
    }
}
