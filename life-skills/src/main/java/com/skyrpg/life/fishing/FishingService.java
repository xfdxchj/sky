package com.skyrpg.life.fishing;

import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.life.common.data.LifeDataStore;
import com.skyrpg.life.skill.LifeSkills;
import com.skyrpg.skyblock.api.RewardAPI;
import com.skyrpg.skyblock.api.SkillAPI;
import com.skyrpg.skyblock.api.StatAPI;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.List;

/**
 * Server-side Fishing catch resolution: sea creature, then trophy fish, then normal fish.
 */
public final class FishingService {
    private FishingService() {}

    public static void onCatch(ServerPlayerEntity player, Random random) {
        int level = SkillAPI.getLevel(player, LifeSkills.FISHING);

        if (random.nextDouble() < seaCreatureChance(player)) {
            SeaCreatureDefinition creature = pickSeaCreature(random, level);
            if (creature != null) {
                SeaCreatureManager.spawn(player, creature);
                return;
            }
        }

        TrophyFishDefinition trophy = rollTrophy(random);
        if (trophy != null) {
            long caught = LifeDataStore.get(player, LifeSkills.FISHING).getLong("fish_caught");
            giveCatch(player, trophy.itemForTier((int) caught), 300.0);
            player.sendMessage(net.minecraft.text.Text.literal("You caught a trophy fish: "
                    + trophy.displayName() + "!"), false);
            return;
        }

        FishDefinition fish = rollFish(random);
        if (fish != null) {
            giveCatch(player, fish.itemId(), fish.baseXp());
            SkyBlockLife.LOGGER.debug("Caught {} ({}).", fish.itemId(), fish.fishId());
        }
    }

    private static void giveCatch(ServerPlayerEntity player, Identifier itemId, double xp) {
        RewardAPI.giveItem(player, itemId, 1);
        LifeDataStore.incrementLong(player, LifeSkills.FISHING, "fish_caught", 1);
        if (xp > 0) {
            SkillAPI.addExperience(player, LifeSkills.FISHING, xp);
        }
    }

    private static double seaCreatureChance(ServerPlayerEntity player) {
        double chance = StatAPI.getFinal(player, StatType.SEA_CREATURE_CHANCE) / 100.0;
        return Math.max(0.0, Math.min(1.0, chance));
    }

    private static SeaCreatureDefinition pickSeaCreature(Random random, int level) {
        List<SeaCreatureDefinition> eligible = FishingRegistry.seaCreatures().stream()
                .filter(creature -> level >= creature.requiredFishingLevel())
                .toList();
        return eligible.isEmpty() ? null : eligible.get(random.nextInt(eligible.size()));
    }

    private static TrophyFishDefinition rollTrophy(Random random) {
        for (TrophyFishDefinition trophy : FishingRegistry.trophyFish()) {
            if (random.nextDouble() < trophy.chance()) {
                return trophy;
            }
        }
        return null;
    }

    private static FishDefinition rollFish(Random random) {
        List<FishDefinition> fish = List.copyOf(FishingRegistry.fish());
        int total = 0;
        for (FishDefinition definition : fish) {
            total += definition.weight();
        }
        if (total <= 0) {
            return null;
        }
        int pick = random.nextInt(total);
        for (FishDefinition definition : fish) {
            pick -= definition.weight();
            if (pick < 0) {
                return definition;
            }
        }
        return null;
    }
}
