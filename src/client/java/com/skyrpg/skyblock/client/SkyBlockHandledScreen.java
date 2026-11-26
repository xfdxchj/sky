package com.skyrpg.skyblock.client;

import com.skyrpg.skyblock.gui.SkyBlockScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public final class SkyBlockHandledScreen extends HandledScreen<SkyBlockScreenHandler> {
    public SkyBlockHandledScreen(SkyBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 114;
        playerInventoryTitle = null;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF171D2B);
        context.fill(x + 4, y + 4, x + backgroundWidth - 4, y + 20, 0xFF26364D);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = y + 25 + row * 18;
                context.fill(sx, sy, sx + 18, sy + 18, 0xFF34445A);
                context.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF202B3B);
            }
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, title, 8, 8, 0xFFFFFF, false);
    }

}
