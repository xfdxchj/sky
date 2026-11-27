package com.skyrpg.life.fishing;

import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Registry of normal fish, trophy fish and sea creatures, ported from the reference
 * server's fishing configuration. Entity mappings for sea creatures are project
 * stand-ins, not verified Hypixel entity data.
 */
public final class FishingRegistry {
    private static final List<FishDefinition> FISH = new ArrayList<>();
    private static final List<TrophyFishDefinition> TROPHY_FISH = new ArrayList<>();
    private static final List<SeaCreatureDefinition> SEA_CREATURES = new ArrayList<>();

    private FishingRegistry() {}

    public static void clearFish() {
        FISH.clear();
    }

    public static void clearTrophyFish() {
        TROPHY_FISH.clear();
    }

    public static void clearSeaCreatures() {
        SEA_CREATURES.clear();
    }

    public static void addFish(FishDefinition definition) {
        FISH.add(definition);
    }

    public static void addTrophyFish(TrophyFishDefinition definition) {
        TROPHY_FISH.add(definition);
    }

    public static void addSeaCreature(SeaCreatureDefinition definition) {
        SEA_CREATURES.add(definition);
    }

    public static void registerDefaults() {
        // DEFAULT_WATER fish table
        fish("raw_fish", "Raw Fish", 36, 5);
        fish("raw_salmon", "Raw Salmon", 22, 6);
        fish("pufferfish", "Pufferfish", 10, 7);
        fish("ink_sac", "Ink Sac", 8, 5);
        fish("lily_pad", "Lily Pad", 8, 5);
        fish("clay_ball", "Clay Ball", 6, 5);
        fish("prismarine_crystals", "Prismarine Crystals", 4, 6);
        fish("prismarine_shard", "Prismarine Shard", 3, 6);
        fish("tropical_fish", "Tropical Fish", 2, 8);
        fish("sponge", "Sponge", 1, 10);

        // Trophy fish (18)
        trophy("sulphur_skitter", "Sulphur Skitter", 0.30);
        trophy("blobfish", "Blobfish", 0.25);
        trophy("obfuscated_1", "Obfuscated 1", 0.25);
        trophy("steaming_hot_flounder", "Steaming-Hot Flounder", 0.20);
        trophy("gusher", "Gusher", 0.20);
        trophy("obfuscated_2", "Obfuscated 2", 0.20);
        trophy("slugfish", "Slugfish", 0.15);
        trophy("obfuscated_3", "Obfuscated 3", 0.10);
        trophy("flyfish", "Flyfish", 0.08);
        trophy("vanille", "Vanille", 0.08);
        trophy("lavahorse", "Lavahorse", 0.04);
        trophy("mana_ray", "Mana Ray", 0.0);
        trophy("volcanic_stonefish", "Volcanic Stonefish", 0.03);
        trophy("skeleton_fish", "Skeleton Fish", 0.02);
        trophy("moldfin", "Moldfin", 0.02);
        trophy("soul_fish", "Soul Fish", 0.02);
        trophy("karate_fish", "Karate Fish", 0.02);
        trophy("golden_fish", "Golden Fish", 0.0);

        // Water sea creatures (13)
        seaCreature("squid", "Squid", EntityType.SQUID, 1, 20);
        seaCreature("sea_walker", "Sea Walker", EntityType.ZOMBIE, 4, 40);
        seaCreature("night_squid", "Night Squid", EntityType.GLOW_SQUID, 6, 80);
        seaCreature("sea_guardian", "Sea Guardian", EntityType.GUARDIAN, 8, 120);
        seaCreature("sea_witch", "Sea Witch", EntityType.WITCH, 10, 150);
        seaCreature("sea_archer", "Sea Archer", EntityType.SKELETON, 15, 200);
        seaCreature("monster_of_the_deep", "Monster of the Deep", EntityType.ELDER_GUARDIAN, 20, 300);
        seaCreature("catfish", "Catfish", EntityType.COD, 24, 400);
        seaCreature("carrot_king", "Carrot King", EntityType.ZOMBIE, 26, 500);
        seaCreature("sea_leech", "Sea Leech", EntityType.SQUID, 30, 650);
        seaCreature("guardian_defender", "Guardian Defender", EntityType.GUARDIAN, 35, 900);
        seaCreature("water_hydra", "Water Hydra", EntityType.GUARDIAN, 60, 7500);
        seaCreature("the_sea_emperor", "The Sea Emperor", EntityType.ELDER_GUARDIAN, 80, 20000);

        // Lava sea creatures (8)
        seaCreature("magma_slug", "Magma Slug", EntityType.MAGMA_CUBE, 21, 450);
        seaCreature("moogma", "Moogma", EntityType.MAGMA_CUBE, 22, 500);
        seaCreature("lava_leech", "Lava Leech", EntityType.MAGMA_CUBE, 24, 550);
        seaCreature("pyroclastic_worm", "Pyroclastic Worm", EntityType.MAGMA_CUBE, 28, 800);
        seaCreature("fire_eel", "Fire Eel", EntityType.BLAZE, 28, 800);
        seaCreature("lava_blaze", "Lava Blaze", EntityType.BLAZE, 30, 1000);
        seaCreature("taurus", "Taurus", EntityType.PIGLIN, 36, 2500);
        seaCreature("lord_jawbus", "Lord Jawbus", EntityType.WITHER_SKELETON, 45, 15000);
    }

    private static void fish(String id, String displayName, int weight, double xp) {
        FISH.add(new FishDefinition(Identifier.of("skyblock_life", "fish_" + id),
                displayName, Identifier.of("skyblock_life", id), weight, xp));
    }

    private static void trophy(String id, String displayName, double chance) {
        TROPHY_FISH.add(new TrophyFishDefinition(Identifier.of("skyblock_life", "trophy_" + id),
                displayName, chance,
                Identifier.of("skyblock_life", "bronze_" + id),
                Identifier.of("skyblock_life", "silver_" + id),
                Identifier.of("skyblock_life", "gold_" + id),
                Identifier.of("skyblock_life", "diamond_" + id)));
    }

    private static void seaCreature(String id, String displayName, EntityType<?> type,
                                    int requiredLevel, double xp) {
        SEA_CREATURES.add(new SeaCreatureDefinition(Identifier.of("skyblock_life", id), displayName,
                type, requiredLevel, xp, Identifier.of("skyblock_life", "raw_fish")));
    }

    public static Collection<FishDefinition> fish() {
        return Collections.unmodifiableList(FISH);
    }

    public static List<TrophyFishDefinition> trophyFish() {
        return Collections.unmodifiableList(TROPHY_FISH);
    }

    public static List<SeaCreatureDefinition> seaCreatures() {
        return Collections.unmodifiableList(SEA_CREATURES);
    }
}
