package com.skyrpg.skyblock.client;

import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.gui.SkyBlockScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;

public final class SkyBlockGuiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkyBlockScreenHandler.TYPE = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(SkyBlockRPG.MOD_ID, "menu"),
                new ScreenHandlerType<>(SkyBlockScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
        HandledScreens.register(SkyBlockScreenHandler.TYPE, SkyBlockHandledScreen::new);
    }
}
