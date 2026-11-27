package com.skyrpg.life.command;

import com.mojang.brigadier.CommandDispatcher;
import com.skyrpg.life.common.data.LifeDataStore;
import com.skyrpg.life.skill.LifeSkills;
import com.skyrpg.skyblock.api.SkillAPI;
import com.skyrpg.skyblock.api.StatAPI;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.literal;

/** Diagnostic commands for the four life skills. */
public final class LifeSkillCommands {
    private LifeSkillCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("skyblock_life")
                .then(literal("skills").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    for (Identifier skill : new Identifier[]{LifeSkills.MINING, LifeSkills.FISHING,
                            LifeSkills.FARMING, LifeSkills.FORAGING}) {
                        String name = skill.getPath();
                        int level = SkillAPI.getLevel(player, skill);
                        double xp = SkillAPI.getExperience(player, skill);
                        double progress = SkillAPI.getProgress(player, skill) * 100.0;
                        context.getSource().sendFeedback(() -> Text.literal(
                                name + " | Lv " + level + " | XP " + String.format("%.0f", xp)
                                        + " | " + String.format("%.1f", progress) + "%"), false);
                    }
                    return 1;
                }))
                .then(literal("stats").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    context.getSource().sendFeedback(() -> Text.literal("Life Skills stats:"), false);
                    context.getSource().sendFeedback(() -> Text.literal("blocks_mined: "
                            + LifeDataStore.get(player, LifeSkills.MINING).getLong("blocks_mined")), false);
                    context.getSource().sendFeedback(() -> Text.literal("logs_collected: "
                            + LifeDataStore.get(player, LifeSkills.FORAGING).getLong("logs_collected")), false);
                    context.getSource().sendFeedback(() -> Text.literal("crops_harvested: "
                            + LifeDataStore.get(player, LifeSkills.FARMING).getLong("crops_harvested")), false);
                    context.getSource().sendFeedback(() -> Text.literal("fish_caught: "
                            + LifeDataStore.get(player, LifeSkills.FISHING).getLong("fish_caught")), false);
                    context.getSource().sendFeedback(() -> Text.literal("sea_creatures_killed: "
                            + LifeDataStore.get(player, LifeSkills.FISHING).getLong("sea_creatures_killed")), false);
                    return 1;
                }))
                .then(literal("fortune").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    context.getSource().sendFeedback(() -> Text.literal(
                            "MINING_FORTUNE: " + StatAPI.getFinal(player, StatType.MINING_FORTUNE)
                                    + " | FARMING_FORTUNE: " + StatAPI.getFinal(player, StatType.FARMING_FORTUNE)
                                    + " | FORAGING_FORTUNE: " + StatAPI.getFinal(player, StatType.FORAGING_FORTUNE)
                                    + " | FISHING_SPEED: " + StatAPI.getFinal(player, StatType.FISHING_SPEED)), false);
                    return 1;
                }))
        );
    }
}
