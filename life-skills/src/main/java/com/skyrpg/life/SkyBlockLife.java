package com.skyrpg.life;

import com.skyrpg.life.command.LifeSkillCommands;
import com.skyrpg.life.common.scheduler.LifeScheduler;
import com.skyrpg.life.farming.FarmingDataLoader;
import com.skyrpg.life.farming.FarmingRegistry;
import com.skyrpg.life.farming.FarmingService;
import com.skyrpg.life.fishing.FishingDataLoader;
import com.skyrpg.life.fishing.FishingRegistry;
import com.skyrpg.life.fishing.SeaCreatureManager;
import com.skyrpg.life.foraging.ForagingDataLoader;
import com.skyrpg.life.foraging.ForagingRegistry;
import com.skyrpg.life.foraging.ForagingService;
import com.skyrpg.life.mining.MiningDataLoader;
import com.skyrpg.life.mining.MiningRegistry;
import com.skyrpg.life.mining.MiningService;
import com.skyrpg.life.skill.LifeSkillRegistry;
import com.skyrpg.skyblock.api.CoreApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Life Skills expansion entry point.
 *
 * <p>This mod depends on the SkyBlock RPG core mod. Core owns player profiles,
 * coins, stats, skill definitions, XP/level persistence and item storage.
 * Life Skills only registers its four skills through the public Core API and
 * must never keep a second copy of those systems.</p>
 */
public final class SkyBlockLife implements ModInitializer {
    public static final String MOD_ID = "skyblock_life";
    public static final String MOD_NAME = "SkyBlock RPG Life Skills";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        if (CoreApi.API_VERSION < 3) {
            LOGGER.error("{} requires SkyBlock RPG core API v3, found v{}. Life skills were not registered.",
                    MOD_NAME, CoreApi.API_VERSION);
            return;
        }

        LifeSkillRegistry.registerAll();
        MiningRegistry.registerDefaults();
        ForagingRegistry.registerDefaults();
        FarmingRegistry.registerDefaults();
        FishingRegistry.registerDefaults();

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return true;
            }
            return MiningService.canBreak(serverPlayer, state) && ForagingService.canBreak(serverPlayer, state);
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(world instanceof ServerWorld serverWorld)
                    || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return;
            }
            MiningService.onBlockBroken(serverWorld, serverPlayer, pos, state);
            ForagingService.onBlockBroken(serverWorld, serverPlayer, pos, state);
            FarmingService.onBlockBroken(serverWorld, serverPlayer, pos, state);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            FishingDataLoader.load(server.getResourceManager());
            MiningDataLoader.load(server.getResourceManager());
            ForagingDataLoader.load(server.getResourceManager());
            FarmingDataLoader.load(server.getResourceManager());
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> LifeScheduler.tick());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LifeScheduler.clear());

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof LivingEntity living
                    && damageSource.getAttacker() instanceof ServerPlayerEntity player) {
                SeaCreatureManager.onDeath(living, player);
            }
        });

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> LifeSkillCommands.register(dispatcher)
        );

        LOGGER.info("{} v{} initialized against SkyBlock RPG core API v{}.",
                MOD_NAME, "0.1.0-alpha.1", CoreApi.API_VERSION);
    }
}
