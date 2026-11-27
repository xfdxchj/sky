package com.skyrpg.skyblock;

import com.skyrpg.skyblock.command.SkyBlockCommands;
import com.skyrpg.skyblock.config.SkyBlockConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.profile.PlayerHealthManager;
import com.skyrpg.skyblock.profile.PlayerHealthRegenManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import com.skyrpg.skyblock.stats.StatEffectManager;
import com.skyrpg.skyblock.skill.SkillRegistry;
import com.skyrpg.skyblock.combat.MobRegistry;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.accessory.PowerRegistry;
import com.skyrpg.skyblock.reforge.ReforgeRegistry;
import com.skyrpg.skyblock.economy.ShopRegistry;
import com.skyrpg.skyblock.region.RegionRegistry;
import com.skyrpg.skyblock.region.RegionManager;
import com.skyrpg.skyblock.warp.WarpRegistry;
import com.skyrpg.skyblock.island.IslandRegistry;
import com.skyrpg.skyblock.island.PrivateIslandManager;
import com.skyrpg.skyblock.structure.StructureRegistry;
import com.skyrpg.skyblock.connection.ConnectionRegistry;
import com.skyrpg.skyblock.hub.HubRegistry;
import com.skyrpg.skyblock.npc.NpcRegistry;
import com.skyrpg.skyblock.npc.NpcManager;
import com.skyrpg.skyblock.quest.QuestRegistry;
import com.skyrpg.skyblock.gui.SkyBlockGui;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.util.ActionResult;
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
        SkillRegistry.registerBuiltins();
        SkyBlockGui.register();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> SkyBlockCommands.register(dispatcher)
        );

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MobRegistry.reload(server.getResourceManager());
            ItemRegistry.reload(server.getResourceManager());
            PowerRegistry.reload(server.getResourceManager());
            ReforgeRegistry.reload(server.getResourceManager());
            ShopRegistry.reload(server.getResourceManager());
            RegionRegistry.reload(server.getResourceManager());
            WarpRegistry.reload(server.getResourceManager());
            HubRegistry.reload(server.getResourceManager());
            ConnectionRegistry.reload(server.getResourceManager());
            IslandRegistry.reload(server.getResourceManager());
            StructureRegistry.reload(server.getResourceManager());
            NpcRegistry.reload(server.getResourceManager());
            QuestRegistry.reload(server.getResourceManager());
            LOGGER.info("SkyBlock RPG is active in world '{}'.", server.getSaveProperties().getLevelName());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            StatEffectManager.tick(server);
            for (net.minecraft.server.network.ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                RegionManager.tick(player);
                PlayerHealthManager.tick(player);
                PlayerHealthRegenManager.tick(player);
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(world instanceof net.minecraft.server.world.ServerWorld serverWorld)
                    || !(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)) return true;
            return !PrivateIslandManager.isProtectedFrom(serverPlayer, serverWorld, pos);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || !(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }
            var serverWorld = serverPlayer.getServerWorld();
            var clicked = hitResult.getBlockPos();
            var placementTarget = clicked.offset(hitResult.getSide());
            if (PrivateIslandManager.isProtectedFrom(serverPlayer, serverWorld, clicked)
                    || PrivateIslandManager.isProtectedFrom(serverPlayer, serverWorld, placementTarget)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient || !(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)
                    || serverPlayer.isSpectator()) return ActionResult.PASS;
            return NpcManager.interact(serverPlayer, entity) ? ActionResult.SUCCESS : ActionResult.PASS;
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            RegionManager.remove(handler.getPlayer());
            PlayerHealthManager.remove(handler.getPlayer());
            PlayerHealthRegenManager.remove(handler.getPlayer());
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            RegionManager.clear();
            PlayerHealthManager.clear();
            PlayerHealthRegenManager.clear();
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PlayerHealthManager.onRespawn(newPlayer);
            PlayerHealthRegenManager.onRespawn(newPlayer);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var profile = ProfileManager.get(handler.getPlayer());
            PlayerHealthManager.onJoin(handler.getPlayer());
            PlayerHealthRegenManager.onJoin(handler.getPlayer());
            LOGGER.info("Loaded SkyBlock profile for {} ({}).",
                    handler.getPlayer().getName().getString(), profile.getPlayerId());
        });

        LOGGER.info("{} v{} initialized. P5.4 Private Island foundation is ready.", MOD_NAME, "0.1.0-alpha.3");
    }
}
