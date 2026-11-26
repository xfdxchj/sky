package com.skyrpg.skyblock.config;

import com.skyrpg.skyblock.SkyBlockRPG;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Minimal local configuration bootstrap. Gameplay data will be stored in world SavedData later. */
public final class SkyBlockConfig {
    private static final String FILE_NAME = "skyblockrpg.properties";
    private static final Properties VALUES = new Properties();

    private SkyBlockConfig() {}

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path)) {
                VALUES.load(reader);
            } catch (IOException exception) {
                SkyBlockRPG.LOGGER.error("Failed to read {}. Defaults will be used.", FILE_NAME, exception);
            }
        } else {
            VALUES.setProperty("debug_logging", "true");
            VALUES.setProperty("show_startup_message", "true");
            save(path);
        }
        SkyBlockRPG.LOGGER.info("Configuration loaded from {}", path);
    }

    private static void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (var writer = Files.newBufferedWriter(path)) {
                VALUES.store(writer, "SkyBlock RPG configuration");
            }
        } catch (IOException exception) {
            SkyBlockRPG.LOGGER.error("Failed to create {}.", FILE_NAME, exception);
        }
    }
}
