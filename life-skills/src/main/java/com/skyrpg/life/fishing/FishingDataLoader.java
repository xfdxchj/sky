package com.skyrpg.life.fishing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.life.common.data.LifeResourceReader;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.Optional;

/** Loads fishing data from JSON. Each list is only replaced when its file loads successfully. */
public final class FishingDataLoader {
    private FishingDataLoader() {}

    public static void load(ResourceManager manager) {
        loadFish(manager);
        loadTrophyFish(manager);
        loadSeaCreatures(manager);
    }

    private static void loadFish(ResourceManager manager) {
        JsonObject root = LifeResourceReader.readJson(manager, "skyblock/fishing/fish.json");
        if (root == null || !root.has("fish")) {
            return;
        }
        FishingRegistry.clearFish();
        for (JsonElement element : root.getAsJsonArray("fish")) {
            JsonObject json = element.getAsJsonObject();
            String id = json.get("id").getAsString();
            FishingRegistry.addFish(new FishDefinition(
                    Identifier.of(SkyBlockLife.MOD_ID, "fish_" + id),
                    json.get("display_name").getAsString(),
                    Identifier.of(SkyBlockLife.MOD_ID, id),
                    json.get("weight").getAsInt(),
                    json.get("skill_xp").getAsDouble()));
        }
    }

    private static void loadTrophyFish(ResourceManager manager) {
        JsonObject root = LifeResourceReader.readJson(manager, "skyblock/fishing/trophy_fish.json");
        if (root == null || !root.has("trophy_fish")) {
            return;
        }
        FishingRegistry.clearTrophyFish();
        for (JsonElement element : root.getAsJsonArray("trophy_fish")) {
            JsonObject json = element.getAsJsonObject();
            String id = json.get("id").getAsString();
            FishingRegistry.addTrophyFish(new TrophyFishDefinition(
                    Identifier.of(SkyBlockLife.MOD_ID, "trophy_" + id),
                    json.get("display_name").getAsString(),
                    json.get("chance").getAsDouble(),
                    Identifier.of(SkyBlockLife.MOD_ID, "bronze_" + id),
                    Identifier.of(SkyBlockLife.MOD_ID, "silver_" + id),
                    Identifier.of(SkyBlockLife.MOD_ID, "gold_" + id),
                    Identifier.of(SkyBlockLife.MOD_ID, "diamond_" + id)));
        }
    }

    private static void loadSeaCreatures(ResourceManager manager) {
        JsonObject root = LifeResourceReader.readJson(manager, "skyblock/fishing/sea_creatures.json");
        if (root == null || !root.has("sea_creatures")) {
            return;
        }
        FishingRegistry.clearSeaCreatures();
        for (JsonElement element : root.getAsJsonArray("sea_creatures")) {
            JsonObject json = element.getAsJsonObject();
            String id = json.get("id").getAsString();
            Identifier entityId = Identifier.tryParse(json.get("entity_type").getAsString());
            if (entityId == null) {
                continue;
            }
            Optional<EntityType<?>> type = Registries.ENTITY_TYPE.getOrEmpty(entityId);
            if (type.isEmpty()) {
                SkyBlockLife.LOGGER.warn("Unknown sea creature entity type {} for {}", entityId, id);
                continue;
            }
            FishingRegistry.addSeaCreature(new SeaCreatureDefinition(
                    Identifier.of(SkyBlockLife.MOD_ID, id),
                    json.get("display_name").getAsString(),
                    type.get(),
                    json.get("required_level").getAsInt(),
                    json.get("skill_xp").getAsDouble(),
                    Identifier.of(SkyBlockLife.MOD_ID, "raw_fish")));
        }
    }
}
