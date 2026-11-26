package com.skyrpg.skyblock.client;

import com.skyrpg.skyblock.gui.SkyBlockScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public final class SkyBlockGuiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(SkyBlockScreenHandler.TYPE, SkyBlockHandledScreen::new);
    }
}
