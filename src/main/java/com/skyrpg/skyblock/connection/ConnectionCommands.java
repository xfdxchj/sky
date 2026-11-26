package com.skyrpg.skyblock.connection;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ConnectionCommands {
    private ConnectionCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> command() {
        return literal("connection")
                .then(literal("list").executes(context -> {
                    var connections = ConnectionRegistry.all();
                    context.getSource().sendFeedback(() -> Text.literal("Registered map connections: " + connections.size()), false);
                    for (ConnectionDefinition connection : connections) {
                        context.getSource().sendFeedback(() -> Text.literal(connection.id() + " | "
                                + connection.type() + " | " + connection.sourceWarpId() + " -> "
                                + connection.targetWarpId()
                                + (connection.testFixture() ? " | PROJECT TEST FIXTURE" : "")), false);
                    }
                    return 1;
                }))
                .then(literal("info").then(argument("connectionId", StringArgumentType.word()).executes(context -> {
                    String id = StringArgumentType.getString(context, "connectionId");
                    ConnectionDefinition connection = ConnectionRegistry.get(id).orElse(null);
                    if (connection == null) {
                        context.getSource().sendError(Text.literal("Unknown map connection: " + id));
                        return 0;
                    }
                    context.getSource().sendFeedback(() -> Text.literal(connection.name() + " [" + connection.id() + "]"), false);
                    context.getSource().sendFeedback(() -> Text.literal("Type: " + connection.type()
                            + " | Dimension: " + connection.dimension()), false);
                    context.getSource().sendFeedback(() -> Text.literal("Route: " + connection.sourceWarpId()
                            + " -> " + connection.targetWarpId()), false);
                    if (connection.type() == ConnectionDefinition.Type.BRIDGE)
                        context.getSource().sendFeedback(() -> Text.literal("Start: "
                                + connection.startX() + ", " + connection.startY() + ", " + connection.startZ()
                                + " | End: " + connection.endX() + ", " + connection.endY() + ", " + connection.endZ()
                                + " | Width: " + connection.width() + " | Block: " + connection.blockId()), false);
                    else
                        context.getSource().sendFeedback(() -> Text.literal("Portal entrance: "
                                + connection.startX() + ", " + connection.startY() + ", " + connection.startZ()
                                + " | Activation radius: " + connection.activationRadius()), false);
                    if (connection.testFixture())
                        context.getSource().sendFeedback(() -> Text.literal("PROJECT TEST FIXTURE — not Hypixel map data."), false);
                    return 1;
                })))
                .then(literal("routes").then(argument("warpId", StringArgumentType.word()).executes(context -> {
                    String warpId = StringArgumentType.getString(context, "warpId");
                    var routes = ConnectionRegistry.connectedTo(warpId);
                    context.getSource().sendFeedback(() -> Text.literal("Connections touching warp '"
                            + warpId + "': " + routes.size()), false);
                    for (ConnectionDefinition connection : routes) {
                        boolean outgoing = connection.sourceWarpId().equalsIgnoreCase(warpId);
                        String relation = outgoing ? "to: " : "from: ";
                        String other = outgoing ? connection.targetWarpId() : connection.sourceWarpId();
                        context.getSource().sendFeedback(() -> Text.literal(connection.id() + " | "
                                + connection.type() + " | " + relation + other), false);
                    }
                    return 1;
                })))
                .then(literal("build").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("connectionId", StringArgumentType.word()).executes(context -> {
                            String id = StringArgumentType.getString(context, "connectionId");
                            ConnectionDefinition connection = ConnectionRegistry.get(id).orElse(null);
                            if (connection == null) {
                                context.getSource().sendError(Text.literal("Unknown map connection: " + id));
                                return 0;
                            }
                            var player = context.getSource().getPlayerOrThrow();
                            var result = ConnectionManager.buildBridge(player.getServerWorld(), connection);
                            if (!result.success()) {
                                context.getSource().sendError(Text.literal("Connection build failed: " + result.message()));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(result.message()
                                    + " Placed blocks: " + result.placedBlocks()), false);
                            return 1;
                        })))
                .then(literal("travel").then(argument("connectionId", StringArgumentType.word()).executes(context -> {
                    String id = StringArgumentType.getString(context, "connectionId");
                    ConnectionDefinition connection = ConnectionRegistry.get(id).orElse(null);
                    if (connection == null) {
                        context.getSource().sendError(Text.literal("Unknown map connection: " + id));
                        return 0;
                    }
                    var player = context.getSource().getPlayerOrThrow();
                    var result = ConnectionManager.travel(player, connection);
                    if (!result.success()) {
                        context.getSource().sendError(Text.literal(result.message()));
                        return 0;
                    }
                    context.getSource().sendFeedback(() -> Text.literal(result.message()), false);
                    return 1;
                })));
    }
}
