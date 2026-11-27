package com.skyrpg.life.common.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.life.SkyBlockLife;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.Optional;

/** Reads Life Skills JSON data files from the mod's datapack namespace. */
public final class LifeResourceReader {
    private LifeResourceReader() {}

    /** Returns the parsed JSON object, or null when the file is missing or invalid. */
    public static JsonObject readJson(ResourceManager manager, String path) {
        Identifier id = Identifier.of(SkyBlockLife.MOD_ID, path);
        Optional<Resource> resource = manager.getResource(id);
        if (resource.isEmpty()) {
            return null;
        }
        try (Reader reader = resource.get().getReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception exception) {
            SkyBlockLife.LOGGER.error("Failed to read Life Skills data {}.", id, exception);
            return null;
        }
    }
}
