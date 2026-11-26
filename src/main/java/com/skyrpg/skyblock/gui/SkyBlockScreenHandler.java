package com.skyrpg.skyblock.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Map;

/** Server-authoritative 27-slot menu. Menu actions are handled before vanilla item movement. */
public final class SkyBlockScreenHandler extends ScreenHandler {
    public static ScreenHandlerType<SkyBlockScreenHandler> TYPE;
    private final Inventory inventory;
    private final String menuId;
    private final Map<Integer, String> actions;

    public SkyBlockScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new net.minecraft.inventory.SimpleInventory(27), "client", Map.of());
    }

    public SkyBlockScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, String menuId, Map<Integer, String> actions) {
        super(TYPE, syncId);
        this.inventory = inventory;
        this.menuId = menuId;
        this.actions = Map.copyOf(actions);
        checkSize(inventory, 27);
        inventory.onOpen(playerInventory.player);
        for (int i = 0; i < 27; i++) {
            addSlot(new Slot(inventory, i, 8 + (i % 9) * 18, 25 + (i / 9) * 18) {
                @Override public boolean canTakeItems(PlayerEntity playerEntity) { return false; }
                @Override public boolean canInsert(ItemStack stack) { return false; }
            });
        }
        // No player inventory slots: this is a menu, not an item storage container.
    }

    public String getMenuId() { return menuId; }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < 27 && player instanceof ServerPlayerEntity serverPlayer
                && actionType == SlotActionType.PICKUP && button == 0) {
            GuiMenuManager.handleClick(serverPlayer, actions.get(slotIndex));
            return;
        }
        // Ignore all other slot actions to prevent moving menu icons into player inventory.
    }

    @Override public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }
    @Override public boolean canUse(PlayerEntity player) { return inventory.canPlayerUse(player); }
    @Override public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }
}
