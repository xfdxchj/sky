package com.skyrpg.skyblock.gui;

import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.economy.BankManager;
import com.skyrpg.skyblock.hub.HubManager;
import com.skyrpg.skyblock.island.PrivateIslandManager;
import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.reforge.ReforgeDefinition;
import com.skyrpg.skyblock.reforge.ReforgeManager;
import com.skyrpg.skyblock.reforge.ReforgeRegistry;
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
        button(inventory, 18, Items.GRASS_BLOCK, "Create Private Island", "Create your own persistent island", actions, "island_create");
        button(inventory, 24, Items.OAK_DOOR, "Private Island Home", "Return to your existing island", actions, "island_home");
        button(inventory, 26, Items.COMPASS, "Hub", "Return to the public Hub", actions, "hub_visit");
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

    public static void openBank(ServerPlayerEntity player) {
        var profile = ProfileManager.get(player);
        SimpleInventory inventory = new SimpleInventory(27);
        Map<Integer, String> actions = new HashMap<>();
        set(inventory, 4, Items.GOLD_BLOCK, "Bank",
                "Purse: " + profile.getCoins() + " coins",
                "Bank balance: " + profile.getBankCoins() + " coins");
        button(inventory, 10, Items.GOLD_INGOT, "Deposit 100", "Move 100 coins to the Bank", actions, "bank_deposit:100");
        button(inventory, 11, Items.GOLD_INGOT, "Deposit 1,000", "Move 1,000 coins to the Bank", actions, "bank_deposit:1000");
        button(inventory, 12, Items.CHEST, "Deposit All", "Move your entire purse to the Bank", actions, "bank_deposit_all");
        button(inventory, 14, Items.GOLD_NUGGET, "Withdraw 100", "Move 100 coins to your Purse", actions, "bank_withdraw:100");
        button(inventory, 15, Items.GOLD_NUGGET, "Withdraw 1,000", "Move 1,000 coins to your Purse", actions, "bank_withdraw:1000");
        button(inventory, 16, Items.BARREL, "Withdraw All", "Move the maximum possible amount to your Purse", actions, "bank_withdraw_all");
        button(inventory, 22, Items.ARROW, "Back", "Return to the SkyBlock Menu", actions, "main");
        open(player, inventory, "bank", "Bank", actions);
    }

    /** Blacksmith menu backed by the same server-side reforge service as /skyblock reforge roll. */
    public static void openBlacksmith(ServerPlayerEntity player) {
        var profile = ProfileManager.get(player);
        var data = profile.getInventory();
        SimpleInventory inventory = new SimpleInventory(27);
        Map<Integer, String> actions = new HashMap<>();
        EquipmentSlot[] slots = {EquipmentSlot.WEAPON, EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE,
                EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS};
        int[] positions = {10, 12, 14, 16, 22};
        Item[] icons = {Items.IRON_SWORD, Items.IRON_HELMET, Items.IRON_CHESTPLATE,
                Items.IRON_LEGGINGS, Items.IRON_BOOTS};
        set(inventory, 4, Items.ANVIL, "Blacksmith",
                "Select an equipped item to roll a random compatible reforge.",
                "The displayed price range depends on the available reforge pool.",
                "Reforging replaces the item's current reforge.");
        for (int i = 0; i < slots.length; i++) {
            EquipmentSlot slot = slots[i];
            String itemId = data.getEquipment().get(slot);
            if (itemId == null) {
                set(inventory, positions[i], Items.BARRIER, slot.id(), "No item equipped");
                continue;
            }
            var pool = ReforgeRegistry.forSlot(slot.id());
            long minCost = pool.stream().mapToLong(ReforgeDefinition::cost).min().orElse(0L);
            long maxCost = pool.stream().mapToLong(ReforgeDefinition::cost).max().orElse(0L);
            String currentId = data.getReforge(slot);
            String currentName = ReforgeRegistry.get(currentId).map(ReforgeDefinition::name)
                    .orElse(currentId == null ? "None" : currentId);
            set(inventory, positions[i], icons[i], slot.id(),
                    "Equipped: " + itemId,
                    "Current reforge: " + currentName,
                    "Compatible reforges: " + pool.size(),
                    pool.isEmpty() ? "No reforge data available"
                            : "Roll cost: " + minCost + (minCost == maxCost ? "" : " - " + maxCost) + " coins",
                    "Click to roll");
            if (!pool.isEmpty()) actions.put(positions[i], "reforge_roll:" + slot.id());
        }
        set(inventory, 26, Items.ARROW, "Close", "Close the Blacksmith menu");
        actions.put(26, "close");
        open(player, inventory, "blacksmith", "Blacksmith", actions);
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
        var profile = ProfileManager.get(player);
        int accessoryPower = profile.getAccessoryPower();
        set(inventory, 4, Items.AMETHYST_SHARD, "Accessory Bag",
                inventoryData.getAccessoryBag().size() + "/" + com.skyrpg.skyblock.inventory.PlayerInventory.ACCESSORY_BAG_CAPACITY + " slots used",
                "Accessory Power: " + accessoryPower,
                "Selected Power: " + profile.getSelectedPower(),
                "Multiplier: " + String.format("%.2f", com.skyrpg.skyblock.accessory.AccessoryPowerManager.getStatsMultiplier(accessoryPower)),
                "Tuning Points: " + profile.getUsedTuningPoints() + "/" + com.skyrpg.skyblock.accessory.AccessoryPowerManager.getTuningPoints(accessoryPower),
                "Use /skyblock accessory power to manage Powers");
        int[] accessorySlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        int index = 0;
        for (String id : inventoryData.getAccessoryBag()) {
            if (index >= accessorySlots.length) break;
            int slot = accessorySlots[index++];
            var def = com.skyrpg.skyblock.item.ItemRegistry.get(id).orElse(null);
            set(inventory, slot, Items.AMETHYST_SHARD, def == null ? id : def.name(),
                    "Stored in Accessory Bag", "Click to return to inventory");
            actions.put(slot, "accessory_remove:" + id);
        }
        set(inventory, 22, Items.ARROW, "Back", "Return to the SkyBlock Menu");
        actions.put(22, "main");
        open(player, inventory, "accessories", "Accessory Bag", actions);
    }

    public static void handleClick(ServerPlayerEntity player, String action) {
        if (action == null) return;
        if (action.startsWith("bank_deposit:") || action.startsWith("bank_withdraw:")) {
            String[] parts = action.split(":", 2);
            long amount = Long.parseLong(parts[1]);
            boolean changed = parts[0].equals("bank_deposit")
                    ? BankManager.deposit(player, amount) : BankManager.withdraw(player, amount);
            player.sendMessage(Text.literal(changed
                    ? (parts[0].equals("bank_deposit") ? "Deposited " : "Withdrew ") + amount + " coins."
                    : "Bank transfer failed: check your balance and purse/bank limits."), false);
            openBank(player);
            return;
        }
        if (action.equals("bank_deposit_all") || action.equals("bank_withdraw_all")) {
            var profile = ProfileManager.get(player);
            long amount = action.equals("bank_deposit_all") ? profile.getCoins()
                    : Math.min(profile.getBankCoins(), Long.MAX_VALUE - profile.getCoins());
            boolean changed = amount > 0 && (action.equals("bank_deposit_all")
                    ? BankManager.deposit(player, amount) : BankManager.withdraw(player, amount));
            player.sendMessage(Text.literal(changed
                    ? (action.equals("bank_deposit_all") ? "Deposited " : "Withdrew ") + amount + " coins."
                    : "No transferable coins, or the balance limit would be exceeded."), false);
            openBank(player);
            return;
        }
        if (action.startsWith("reforge_roll:")) {
            String slotId = action.substring("reforge_roll:".length());
            EquipmentSlot slot = EquipmentSlot.fromId(slotId).orElse(null);
            if (slot == null || slot == EquipmentSlot.ACCESSORY) {
                player.sendMessage(Text.literal("Invalid equipment slot."), false);
                openBlacksmith(player);
                return;
            }
            ReforgeManager.Result result = ReforgeManager.roll(player, slot);
            if (result == ReforgeManager.Result.SUCCESS) {
                String id = ProfileManager.get(player).getInventory().getReforge(slot);
                ReforgeDefinition reforge = ReforgeRegistry.get(id).orElse(null);
                player.sendMessage(Text.literal("Blacksmith applied "
                        + (reforge == null ? id : reforge.name()) + " to " + slot.id()
                        + (reforge == null ? "." : " for " + reforge.cost() + " coins.")), false);
            } else {
                player.sendMessage(Text.literal("Reforge failed: "
                        + result.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ')), false);
            }
            openBlacksmith(player);
            return;
        }
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
            case "blacksmith" -> openBlacksmith(player);
            case "close" -> player.closeHandledScreen();
            case "quests" -> openQuests(player);
            case "shop" -> openShop(player);
            case "equipment" -> openEquipment(player);
            case "accessories" -> openAccessories(player);
            case "island_create" -> {
                PrivateIslandManager.CreationResult result = PrivateIslandManager.create(player);
                if (!result.success()) {
                    player.sendMessage(Text.literal(result.message()), false);
                    openMain(player);
                    return;
                }
                player.closeHandledScreen();
                if (!PrivateIslandManager.visitHome(player)) {
                    player.sendMessage(Text.literal("Island exists, but its spawn could not be reached."), false);
                    return;
                }
                player.sendMessage(Text.literal(result.message()), false);
            }
            case "island_home" -> {
                player.closeHandledScreen();
                if (!PrivateIslandManager.visitHome(player)) {
                    player.sendMessage(Text.literal("You do not have a Private Island yet. Use Create Private Island first."), false);
                }
            }
            case "hub_visit" -> {
                player.closeHandledScreen();
                HubManager.Result result = HubManager.visit(player);
                player.sendMessage(Text.literal(result.message()), false);
            }
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
                    java.util.Arrays.stream(lore).<Text>map(Text::literal).toList()));
        }
        inventory.setStack(slot, stack);
    }
}
