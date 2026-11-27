package com.skyrpg.life.farming;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.life.common.data.LifeResourceReader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

/** Loads the crop table from JSON. */
public final class FarmingDataLoader {
    private FarmingDataLoader() {}

    public static void load(ResourceManager manager) {
        JsonObject root = LifeResourceReader.readJson(manager, "skyblock/farming/crops.json");
        if (root == null || !root.has("crops")) {
            return;
        }
        FarmingRegistry.clear();
        for (JsonElement element : root.getAsJsonArray("crops")) {
            JsonObject json = element.getAsJsonObject();
            String id = json.get("id").getAsString();
            Set<Identifier> blocks = new HashSet<>();
            for (JsonElement block : json.getAsJsonArray("blocks")) {
                Identifier blockId = Identifier.tryParse(block.getAsString());
                if (blockId != null) {
                    blocks.add(blockId);
                }
            }
            FarmingRegistry.add(new CropDefinition(
                    Identifier.of(SkyBlockLife.MOD_ID, "crop_" + id),
                    json.get("display_name").getAsString(),
                    blocks,
                    Identifier.tryParse(json.get("drop").getAsString()),
                    json.get("xp").getAsDouble()));
        }
    }
}
