package com.skyrpg.skyblock.structure;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
                    context.getSource().sendFeedback(() -> Text.literal("Origin: " + definition.originX() + ", "
                            + definition.originY() + ", " + definition.originZ()
                            + " | Rotation: " + definition.rotation() + " | Mirror: " + definition.mirror()), false);
                    context.getSource().sendFeedback(() -> Text.literal("Source: "
                            + (definition.source().isBlank() ? "unspecified" : definition.source())
                            + " | License: " + (definition.license().isBlank() ? "unspecified" : definition.license())), false);
                    if (!definition.sourceUrl().isBlank())
                        context.getSource().sendFeedback(() -> Text.literal("Source URL: " + definition.sourceUrl()), false);
                    return 1;
                })))
                .then(literal("paste").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("structureId", StringArgumentType.word()).executes(context -> {
                            String id = StringArgumentType.getString(context, "structureId");
                            StructureDefinition definition = StructureRegistry.get(id).orElse(null);
                            if (definition == null) {
                                context.getSource().sendError(Text.literal("Unknown structure manifest: " + id));
                                return 0;
                            }
                            var player = context.getSource().getPlayerOrThrow();
                            StructureImportManager.PasteResult result =
                                    StructureImportManager.paste(player.getServerWorld(), definition);
                            if (!result.success()) {
                                context.getSource().sendError(Text.literal("Structure paste failed: " + result.message()));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(result.message()
                                    + " Bounding-box volume: " + result.volume() + " blocks."), false);
                            return 1;
                        })));
    }
}
