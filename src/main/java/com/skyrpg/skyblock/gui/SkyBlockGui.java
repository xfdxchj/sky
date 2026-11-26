package com.skyrpg.skyblock.gui;

import com.skyrpg.skyblock.SkyBlockRPG;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class SkyBlockGui {
    private SkyBlockGui() {}

    public static void register() {
        SkyBlockScreenHandler.TYPE = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(SkyBlockRPG.MOD_ID, "menu"),
                new ScreenHandlerType<>(SkyBlockScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    }
}
