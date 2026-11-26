package com.skyrpg.skyblock;

import com.skyrpg.skyblock.command.SkyBlockCommands;
import com.skyrpg.skyblock.config.SkyBlockConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.combat.MobRegistry;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.economy.ShopRegistry;
import com.skyrpg.skyblock.region.RegionRegistry;
import com.skyrpg.skyblock.region.RegionManager;
import com.skyrpg.skyblock.warp.WarpRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for SkyBlock RPG.
 * Keep this class limited to bootstrapping; gameplay systems belong in their own packages.
 */
public final class SkyBlockRPG implements ModInitializer {
    public static final String MOD_ID = "skyblockrpg";
    public static final String MOD_NAME = "SkyBlock RPG";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        SkyBlockConfig.load();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> SkyBlockCommands.register(dispatcher)
        );

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MobRegistry.reload(server.getResourceManager());
            ItemRegistry.reload(server.getResourceManager());
            ShopRegistry.reload(server.getResourceManager());
            RegionRegistry.reload(server.getResourceManager());
            WarpRegistry.reload(server.getResourceManager());
            LOGGER.info("SkyBlock RPG is active in world '{}'.", server.getSaveProperties().getLevelName());
        });

        ServerTickEvents.END_PLAYER_TICK.register(RegionManager::tick);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> RegionManager.remove(handler.getPlayer()));

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RegionManager.clear());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var profile = ProfileManager.get(handler.getPlayer());
            LOGGER.info("Loaded SkyBlock profile for {} ({}).",
                    handler.getPlayer().getName().getString(), profile.getPlayerId());
        });

        LOGGER.info("{} v{} initialized. P3 combat mixin is ready.", MOD_NAME, "0.1.0-alpha.3");
    }
}
