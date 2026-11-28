package com.skyrpg.skyblock.hub;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.skyrpg.skyblock.map.MapRegistry;
import com.skyrpg.skyblock.map.MapWorldData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class HubCommands {
    private HubCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> command() {
        return literal("hub")
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Hub | Map: "
                            + (MapRegistry.get("hub").isPresent() ? "loaded" : "not loaded")
                            + " | Points of interest: " + HubRegistry.all().size()
                            + " | Spawn: " + (HubWorldData.get(context.getSource().getServer()).getSpawn() != null ? "configured" : "not configured")), false);
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
                    context.getSource().sendFeedback(() -> Text.literal("Hub points of interest: "
                            + HubRegistry.all().size()), false);
                    for (HubLocationDefinition location : HubRegistry.all()) {
                        context.getSource().sendFeedback(() -> Text.literal(location.id() + " | "
                                + location.displayName() + " | " + location.category()
                                + " | Reference XYZ " + format(location.x(), location.y(), location.z())
                                + (location.hasRuntimeCoordinates() ? " | runtime" : " | reference only")), false);
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
                    context.getSource().sendFeedback(() -> Text.literal(location.displayName()
                            + " [" + location.id() + "]"), false);
                    context.getSource().sendFeedback(() -> Text.literal("Category: " + location.category()
                            + " | Parent: " + (location.parentId().isBlank() ? "none" : location.parentId())), false);
                    context.getSource().sendFeedback(() -> Text.literal("Dimension: " + location.dimension()
                            + " | " + (location.hasRuntimeCoordinates() ? "Runtime coordinates: " : "Reference coordinates only: ")
                            + format(location.x(), location.y(), location.z())), false);
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
                    if (!location.hasRuntimeCoordinates()) {
                        context.getSource().sendError(Text.literal("This POI uses reference-world coordinates. It cannot be located in the imported map until a verified coordinate transform or project-local anchor is recorded."));
                        return 0;
                    }
                    String currentDimension = player.getServerWorld().getRegistryKey().getValue().toString();
                    if (!currentDimension.equals(location.dimension())) {
                        context.getSource().sendError(Text.literal("Location is in " + location.dimension()
                                + ", while you are in " + currentDimension + "."));
                        return 0;
                    }
                    double distance = Math.sqrt(location.squaredDistance(player.getX(), player.getY(), player.getZ()));
                    context.getSource().sendFeedback(() -> Text.literal(location.displayName()
                            + " | XYZ " + format(location.x(), location.y(), location.z())
                            + " | Distance: " + String.format(java.util.Locale.ROOT, "%.1f", distance) + " blocks"), false);
                    return 1;
                })))
                .then(literal("nearest").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    String dimension = player.getServerWorld().getRegistryKey().getValue().toString();
                    var nearest = HubRegistry.nearest(dimension, player.getX(), player.getY(), player.getZ(), 5)
                            .stream().filter(HubLocationDefinition::hasRuntimeCoordinates).toList();
                    if (nearest.isEmpty()) {
                        context.getSource().sendFeedback(() -> Text.literal("No Hub POIs have verified project-local coordinates in this dimension. Reference coordinates are excluded."), false);
                        return 1;
                    }
                    context.getSource().sendFeedback(() -> Text.literal("Nearest Hub points:"), false);
                    for (HubLocationDefinition location : nearest) {
                        double distance = Math.sqrt(location.squaredDistance(player.getX(), player.getY(), player.getZ()));
                        context.getSource().sendFeedback(() -> Text.literal(location.displayName() + " ["
                                + location.id() + "] - " + String.format(java.util.Locale.ROOT, "%.1f", distance) + " blocks"), false);
                    }
                    return 1;
                }));
    }

    private static String format(double x, double y, double z) {
        return String.format(java.util.Locale.ROOT, "%.1f, %.1f, %.1f", x, y, z);
    }
}
