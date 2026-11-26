package com.skyrpg.skyblock.gui;

import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.quest.QuestDefinition;
import com.skyrpg.skyblock.quest.QuestManager;
import com.skyrpg.skyblock.quest.QuestRegistry;
import com.skyrpg.skyblock.economy.ShopOffer;
import com.skyrpg.skyblock.economy.ShopRegistry;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;

import java.util.HashMap;
import java.util.Map;

public final class GuiMenuManager {
    private GuiMenuManager() {}

    public static void openMain(ServerPlayerEntity player) {
        SimpleInventory inventory = new SimpleInventory(27);
        Map<Integer, String> actions = new HashMap<>();
        button(inventory, 10, Items.PLAYER_HEAD, "Profile", "View your SkyBlock profile", actions, "profile");
        button(inventory, 12, Items.GOLD_INGOT, "Bank", "View your Purse and Bank", actions, "bank");
        button(inventory, 14, Items.BOOK, "Quests", "View available and active quests", actions, "quests");
        button(inventory, 16, Items.EMERALD, "Shop", "Browse registered shop offers", actions, "shop");
        button(inventory, 20, Items.IRON_CHESTPLATE, "Equipment", "View equipped armor and weapon", actions, "equipment");
        button(inventory, 22, Items.AMETHYST_SHARD, "Accessories", "Manage your Accessory Bag", actions, "accessories");
        open(player, inventory, "main", "SkyBlock Menu", actions);
    }

    private static void openProfile(ServerPlayerEntity player) {
        var profile = ProfileManager.get(player);
        SimpleInventory inventory = new SimpleInventory(27);
        set(inventory, 11, Items.PLAYER_HEAD, "Player Profile",
                "Player: " + profile.getLastKnownName(), "Purse: " + profile.getCoins());
        set(inventory, 13, Items.IRON_CHESTPLATE, "Statistics", "Use /skyblock stats for full details");
        set(inventory, 15, Items.ARROW, "Back", "Return to the SkyBlock Menu");
        open(player, inventory, "profile", "Your Profile", Map.of(15, "main"));
    }

    private static void openBank(ServerPlayerEntity player) {
        var profile = ProfileManager.get(player);
        SimpleInventory inventory = new SimpleInventory(27);
        set(inventory, 11, Items.GOLD_INGOT, "Purse", profile.getCoins() + " coins");
        set(inventory, 13, Items.GOLD_BLOCK, "Bank", profile.getBankCoins() + " coins");
        set(inventory, 15, Items.ARROW, "Back", "Return to the SkyBlock Menu");
        open(player, inventory, "bank", "Bank", Map.of(15, "main"));
    }

    private static void openQuests(ServerPlayerEntity player) {
        SimpleInventory inventory = new SimpleInventory(27);
        Map<Integer, String> actions = new HashMap<>();
        int slot = 10;
        for (QuestDefinition quest : QuestRegistry.all()) {
            if (slot > 16) break;
            var profile = ProfileManager.get(player);
            boolean completed = profile.isQuestCompleted(quest.id());
            boolean active = profile.isQuestActive(quest.id());
            String state = completed ? "Completed" : active
                    ? "In progress " + profile.getQuestProgress(quest.id()) + "/" + quest.requiredCount()
                    : "Click to accept";
            set(inventory, slot, completed ? Items.LIME_DYE : active ? Items.WRITABLE_BOOK : Items.BOOK,
                    quest.title(), state, quest.description());
            if (!completed && !active) actions.put(slot, "accept:" + quest.id());
            slot++;
        }
        set(inventory, 22, Items.ARROW, "Back", "Return to the SkyBlock Menu");
        actions.put(22, "main");
        open(player, inventory, "quests", "Quests", actions);
    }

    private static void openShop(ServerPlayerEntity player) {
        SimpleInventory inventory = new SimpleInventory(27);
        int slot = 10;
        for (ShopOffer offer : ShopRegistry.all()) {
            if (slot > 16) break;
            set(inventory, slot++, Items.EMERALD, offer.itemId(),
                    "Buy: " + offer.buyPrice() + " coins",
                    offer.canSell() ? "Sell: " + offer.sellPrice() + " coins" : "Selling not accepted");
        }
        set(inventory, 22, Items.ARROW, "Back", "Return to the SkyBlock Menu");
        open(player, inventory, "shop", "Shop Catalogue", Map.of(22, "main"));
    }


    private static void openEquipment(ServerPlayerEntity player) {
        var inventoryData = ProfileManager.get(player).getInventory();
        SimpleInventory inventory = new SimpleInventory(27);
        EquipmentSlot[] slots = {EquipmentSlot.WEAPON, EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE,
                EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS};
        int[] positions = {10, 12, 14, 16, 22};
        for (int i = 0; i < slots.length; i++) {
            String id = inventoryData.getEquipment().get(slots[i]);
            String label = id == null ? "Empty" : id;
            set(inventory, positions[i], Items.IRON_CHESTPLATE, slots[i].id(), label);
        }
        set(inventory, 26, Items.ARROW, "Back", "Return to the SkyBlock Menu");
        open(player, inventory, "equipment", "Equipment", Map.of(26, "main"));
    }

    private static void openAccessories(ServerPlayerEntity player) {
        var inventoryData = ProfileManager.get(player).getInventory();
        SimpleInventory inventory = new SimpleInventory(27);
        Map<Integer, String> actions = new HashMap<>();
        set(inventory, 4, Items.AMETHYST_SHARD, "Accessory Bag",
                inventoryData.getAccessoryBag().size() + "/" + com.skyrpg.skyblock.inventory.PlayerInventory.ACCESSORY_BAG_CAPACITY + " slots used",
                "Use /skyblock accessory add <itemId> to add accessories");
        int slot = 10;
        for (String id : inventoryData.getAccessoryBag()) {
            if (slot > 16) break;
            var def = com.skyrpg.skyblock.item.ItemRegistry.get(id).orElse(null);
            set(inventory, slot, Items.AMETHYST_SHARD, def == null ? id : def.name(),
                    "Stored in Accessory Bag", "Click to return to inventory");
            actions.put(slot, "accessory_remove:" + id);
            slot++;
        }
        set(inventory, 22, Items.ARROW, "Back", "Return to the SkyBlock Menu");
        actions.put(22, "main");
        open(player, inventory, "accessories", "Accessory Bag", actions);
    }

    public static void handleClick(ServerPlayerEntity player, String action) {
        if (action == null) return;
        if (action.startsWith("accessory_remove:")) {
            String itemId = action.substring("accessory_remove:".length());
            var profile = ProfileManager.get(player);
            if (profile.getInventory().removeAccessory(itemId)) {
                ProfileManager.markChanged(player);
                player.sendMessage(Text.literal("Returned " + itemId + " to your inventory."), false);
            } else {
                player.sendMessage(Text.literal("Could not remove accessory. Check inventory space."), false);
            }
            openAccessories(player);
            return;
        }
        if (action.startsWith("accept:")) {
            String questId = action.substring("accept:".length());
            QuestManager.Result result = QuestManager.accept(player, questId);
            player.sendMessage(Text.literal(result == QuestManager.Result.SUCCESS
                    ? "Quest accepted: " + questId : "Could not accept quest: " + result), false);
            openQuests(player);
            return;
        }
        switch (action) {
            case "main" -> openMain(player);
            case "profile" -> openProfile(player);
            case "bank" -> openBank(player);
            case "quests" -> openQuests(player);
            case "shop" -> openShop(player);
            case "equipment" -> openEquipment(player);
            case "accessories" -> openAccessories(player);
            default -> { }
        }
    }

    private static void open(ServerPlayerEntity player, SimpleInventory inventory, String menuId,
                             String title, Map<Integer, String> actions) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, ignored) ->
                        new SkyBlockScreenHandler(syncId, playerInventory, inventory, menuId, actions),
                Text.literal(title)));
    }

    private static void button(SimpleInventory inventory, int slot, Item item, String name,
                               String lore, Map<Integer, String> actions, String action) {
        set(inventory, slot, item, name, lore);
        actions.put(slot, action);
    }

    private static void set(SimpleInventory inventory, int slot, Item item, String name, String... lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.YELLOW));
        if (lore.length > 0) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(
                    java.util.Arrays.stream(lore).map(Text::literal).toList()));
        }
        inventory.setStack(slot, stack);
    }
}
