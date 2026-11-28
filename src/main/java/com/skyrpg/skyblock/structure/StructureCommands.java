package com.skyrpg.skyblock.structure;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class StructureCommands {
    private StructureCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> command() {
        return literal("structure")
                .then(literal("list").executes(context -> {
                    var definitions = StructureRegistry.all();
                    context.getSource().sendFeedback(() -> Text.literal("Registered structure manifests: " + definitions.size()), false);
                    for (StructureDefinition definition : definitions) {
                        context.getSource().sendFeedback(() -> Text.literal(definition.id() + " | " + definition.name()
                                + " | " + definition.template()
                                + (definition.testFixture() ? " | PROJECT TEST FIXTURE" : "")), false);
                    }
                    return 1;
                }))
                .then(literal("info").then(argument("structureId", StringArgumentType.word()).executes(context -> {
                    String id = StringArgumentType.getString(context, "structureId");
                    StructureDefinition definition = StructureRegistry.get(id).orElse(null);
                    if (definition == null) {
                        context.getSource().sendError(Text.literal("Unknown structure manifest: " + id));
                        return 0;
                    }
                    context.getSource().sendFeedback(() -> Text.literal(definition.name() + " [" + definition.id() + "]"), false);
                    context.getSource().sendFeedback(() -> Text.literal("Template: " + definition.template()
                            + " | Dimension: " + definition.dimension()), false);
                    context.getSource().sendFeedback(() -> Text.literal("Default origin: " + definition.originX() + ", "
                            + definition.originY() + ", " + definition.originZ()
                            + " | Rotation: " + definition.rotation() + " | Mirror: " + definition.mirror()), false);
                    context.getSource().sendFeedback(() -> Text.literal("Max volume: " + definition.maxBlocks()
                            + " | Source: " + (definition.source().isBlank() ? "unspecified" : definition.source())
                            + " | License: " + (definition.license().isBlank() ? "unspecified" : definition.license())), false);
                    if (!definition.sourceUrl().isBlank())
                        context.getSource().sendFeedback(() -> Text.literal("Source URL: " + definition.sourceUrl()), false);
                    return 1;
                })))
                .then(literal("paste").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("structureId", StringArgumentType.word())
                                .executes(context -> paste(context.getSource(),
                                        StringArgumentType.getString(context, "structureId"), null))
                                .then(argument("x", IntegerArgumentType.integer())
                                        .then(argument("y", IntegerArgumentType.integer())
                                                .then(argument("z", IntegerArgumentType.integer())
                                                        .executes(context -> paste(context.getSource(),
                                                                StringArgumentType.getString(context, "structureId"),
                                                                new BlockPos(
                                                                        IntegerArgumentType.getInteger(context, "x"),
                                                                        IntegerArgumentType.getInteger(context, "y"),
                                                                        IntegerArgumentType.getInteger(context, "z")))))))));
    }

    private static int paste(ServerCommandSource source, String id, BlockPos overrideOrigin)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        StructureDefinition definition = StructureRegistry.get(id).orElse(null);
        if (definition == null) {
            source.sendError(Text.literal("Unknown structure manifest: " + id));
            return 0;
        }
        var player = source.getPlayerOrThrow();
        BlockPos origin = overrideOrigin == null
                ? new BlockPos(definition.originX(), definition.originY(), definition.originZ())
                : overrideOrigin;
        StructureImportManager.PasteResult result =
                StructureImportManager.paste(player.getServerWorld(), definition, origin);
        if (!result.success()) {
            source.sendError(Text.literal("Structure paste failed: " + result.message()));
            return 0;
        }
        source.sendFeedback(() -> Text.literal(result.message()
                + " Bounding-box volume: " + result.volume() + " blocks."), false);
        return 1;
    }
}
