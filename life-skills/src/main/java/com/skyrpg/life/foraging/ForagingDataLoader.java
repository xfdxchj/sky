package com.skyrpg.life.foraging;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.life.common.data.LifeResourceReader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

/** Loads the tree table from JSON. */
public final class ForagingDataLoader {
    private ForagingDataLoader() {}

    public static void load(ResourceManager manager) {
        JsonObject root = LifeResourceReader.readJson(manager, "skyblock/foraging/trees.json");
        if (root == null || !root.has("trees")) {
            return;
        }
        ForagingRegistry.clear();
        for (JsonElement element : root.getAsJsonArray("trees")) {
            JsonObject json = element.getAsJsonObject();
            String id = json.get("id").getAsString();
            Set<Identifier> logs = new HashSet<>();
            for (JsonElement log : json.getAsJsonArray("logs")) {
                Identifier logId = Identifier.tryParse(log.getAsString());
                if (logId != null) {
                    logs.add(logId);
                }
            }
            ForagingRegistry.add(new TreeDefinition(
                    Identifier.of(SkyBlockLife.MOD_ID, "tree_" + id),
                    json.get("display_name").getAsString(),
                    logs,
                    Identifier.tryParse(json.get("drop").getAsString()),
                    json.get("xp").getAsDouble()));
        }
    }
}
