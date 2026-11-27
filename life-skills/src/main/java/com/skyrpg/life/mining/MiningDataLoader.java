package com.skyrpg.life.mining;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skyrpg.life.common.data.LifeResourceReader;
import com.skyrpg.life.common.drop.DropEntry;
import com.skyrpg.life.common.drop.DropTable;
import com.skyrpg.life.mining.handler.MiningHandler;
import com.skyrpg.life.mining.handler.MiningHandlers;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Loads the mining block table from JSON. */
public final class MiningDataLoader {
    private MiningDataLoader() {}

    public static void load(ResourceManager manager) {
        JsonObject root = LifeResourceReader.readJson(manager, "skyblock/mining/blocks.json");
        if (root == null || !root.has("blocks")) {
            return;
        }
        MiningRegistry.clear();
        for (JsonElement element : root.getAsJsonArray("blocks")) {
            JsonObject json = element.getAsJsonObject();
            List<DropEntry> drops = new ArrayList<>();
            if (json.has("drops")) {
                for (JsonElement dropElement : json.getAsJsonArray("drops")) {
                    JsonObject drop = dropElement.getAsJsonObject();
                    drops.add(new DropEntry(Identifier.tryParse(drop.get("item").getAsString()),
                            drop.get("min").getAsInt(), drop.get("max").getAsInt(),
                            drop.get("chance").getAsDouble(), true));
                }
            }
            MiningRegistry.add(new MineableBlock(
                    Identifier.of("minecraft", json.get("id").getAsString()),
                    handler(json),
                    MiningBlockClassification.MINING,
                    json.get("xp").getAsDouble(),
                    new DropTable(drops)));
        }
    }

    private static MiningHandler handler(JsonObject json) {
        String type = json.get("handler").getAsString();
        double strength = json.get("strength").getAsDouble();
        int power = json.get("power").getAsInt();
        return switch (type) {
            case "axe" -> MiningHandlers.axe(strength, power);
            case "hoe" -> MiningHandlers.hoe(strength, power);
            case "instant" -> MiningHandlers.instant();
            default -> MiningHandlers.pickaxe(strength, power);
        };
    }
}
