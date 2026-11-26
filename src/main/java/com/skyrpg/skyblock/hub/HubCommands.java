package com.skyrpg.skyblock.hub;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class HubCommands {
    private HubCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> command() {
        return literal("hub")
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Hub location catalogue | Loaded points: "
                            + HubRegistry.all().size()
                            + " | Coordinates are sourced reference points, not generated map terrain."), false);
                    return 1;
                })
                .then(literal("list").executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Hub points of interest: "
                            + HubRegistry.all().size()), false);
                    for (HubLocationDefinition location : HubRegistry.all()) {
                        context.getSource().sendFeedback(() -> Text.literal(location.id() + " | "
                                + location.displayName() + " | " + location.category()
                                + " | " + format(location.x(), location.y(), location.z())), false);
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
                            + " | Reference coordinates: " + format(location.x(), location.y(), location.z())), false);
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
                    var nearest = HubRegistry.nearest(dimension, player.getX(), player.getY(), player.getZ(), 5);
                    if (nearest.isEmpty()) {
                        context.getSource().sendFeedback(() -> Text.literal("No Hub points are registered in this dimension."), false);
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
