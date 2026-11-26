package com.skyrpg.skyblock.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.skyrpg.skyblock.economy.CurrencyManager;
import com.skyrpg.skyblock.combat.MobDefinition;
import com.skyrpg.skyblock.combat.MobRegistry;
import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.skill.SkillDefinition;
import com.skyrpg.skyblock.skill.SkillManager;
import com.skyrpg.skyblock.skill.SkillType;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SkyBlockCommands {
    private SkyBlockCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("skyblock")
                .then(literal("status").executes(context -> {
                    ServerCommandSource source = context.getSource();
                    String worldName = source.getServer().getSaveProperties().getLevelName();
                    source.sendFeedback(() -> Text.literal("[SkyBlock RPG] Core loaded successfully."), false);
                    source.sendFeedback(() -> Text.literal("World: " + worldName), false);
                    source.sendFeedback(() -> Text.literal("Minecraft: 1.21.1 | Mod: 0.1.0-alpha.3 (P3)"), false);
                    return 1;
                }))
                .then(literal("profile").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    PlayerProfile profile = ProfileManager.get(player);
                    context.getSource().sendFeedback(() -> Text.literal(
                            "SkyBlock Profile: " + profile.getLastKnownName()
                                    + " | UUID: " + profile.getPlayerId()
                                    + " | Coins: " + profile.getCoins()), false);
                    return 1;
                }))
                .then(literal("stats").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    PlayerProfile profile = ProfileManager.get(player);
                    context.getSource().sendFeedback(() -> Text.literal("SkyBlock Stats (including equipped items)"), false);
                    for (StatType type : StatType.values()) {
                        double value = profile.getFinalStat(type);
                        if (value != 0) context.getSource().sendFeedback(
                                () -> Text.literal(type.id() + ": " + String.format("%.2f", value)
                                        + " (base: " + String.format("%.2f", profile.getStats().getBase(type)) + ")"), false);
                    }
                    return 1;
                }))
                .then(literal("coins")
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            long coins = CurrencyManager.getCoins(player);
                            context.getSource().sendFeedback(() -> Text.literal("Coins: " + coins), false);
                            return 1;
                        })
                        .then(literal("add").requires(source -> source.hasPermissionLevel(2))
                                .then(argument("amount", LongArgumentType.longArg(1)).executes(context -> {
                                    var player = context.getSource().getPlayerOrThrow();
                                    long amount = LongArgumentType.getLong(context, "amount");
                                    if (!CurrencyManager.addCoins(player, amount)) {
                                        context.getSource().sendError(Text.literal("Could not add coins (invalid amount or overflow)."));
                                        return 0;
                                    }
                                    long total = CurrencyManager.getCoins(player);
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Added " + amount + " coins. Balance: " + total), false);
                                    return 1;
                                })))
                        .then(literal("remove").requires(source -> source.hasPermissionLevel(2))
                                .then(argument("amount", LongArgumentType.longArg(1)).executes(context -> {
                                    var player = context.getSource().getPlayerOrThrow();
                                    long amount = LongArgumentType.getLong(context, "amount");
                                    if (!CurrencyManager.removeCoins(player, amount)) {
                                        context.getSource().sendError(Text.literal("Insufficient coins or invalid amount."));
                                        return 0;
                                    }
                                    long total = CurrencyManager.getCoins(player);
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Removed " + amount + " coins. Balance: " + total), false);
                                    return 1;
                                }))))
                .then(literal("skill").then(argument("skill", StringArgumentType.word())
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "skill");
                            var type = SkillType.fromId(id);
                            if (type.isEmpty()) {
                                context.getSource().sendError(Text.literal("Unknown skill: " + id));
                                return 0;
                            }
                            double xp = SkillManager.getExperience(player, type.get());
                            int level = SkillDefinition.levelFromTotalXp(xp);
                            double progress = SkillDefinition.progressInLevel(xp) * 100.0;
                            context.getSource().sendFeedback(() -> Text.literal(
                                    type.get().id() + " | Level " + level + " | XP " + xp
                                            + " | Progress " + String.format("%.1f", progress) + "%"), false);
                            return 1;
                        })
                        .then(literal("add").requires(source -> source.hasPermissionLevel(2))
                                .then(argument("amount", DoubleArgumentType.doubleArg(0.000001)).executes(context -> {
                                    var player = context.getSource().getPlayerOrThrow();
                                    String id = StringArgumentType.getString(context, "skill");
                                    var type = SkillType.fromId(id);
                                    if (type.isEmpty()) {
                                        context.getSource().sendError(Text.literal("Unknown skill: " + id));
                                        return 0;
                                    }
                                    double amount = DoubleArgumentType.getDouble(context, "amount");
                                    if (!SkillManager.addExperience(player, type.get(), amount)) {
                                        context.getSource().sendError(Text.literal("Could not add skill XP."));
                                        return 0;
                                    }
                                    int level = SkillManager.getLevel(player, type.get());
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Added " + amount + " " + type.get().id()
                                                    + " XP. Current level: " + level), false);
                                    return 1;
                                })))))
                .then(literal("combat").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    PlayerProfile profile = ProfileManager.get(player);
                    String weaponId = profile.getInventory().getEquipment().getOrDefault(EquipmentSlot.WEAPON, "none");
                    context.getSource().sendFeedback(() -> Text.literal("SkyBlock Combat | Weapon: " + weaponId), false);
                    for (StatType type : new StatType[]{StatType.DAMAGE, StatType.STRENGTH, StatType.CRIT_CHANCE, StatType.CRIT_DAMAGE}) {
                        double value = profile.getFinalStat(type);
                        context.getSource().sendFeedback(() -> Text.literal(type.id() + ": " + String.format("%.2f", value)), false);
                    }
                    context.getSource().sendFeedback(() -> Text.literal("Attack living mobs while a SkyBlock weapon is equipped."), false);
                    return 1;
                }))
                .then(literal("mob")
                        .then(literal("list").executes(context -> {
                            var mobs = MobRegistry.all();
                            context.getSource().sendFeedback(() -> Text.literal("Loaded SkyBlock mobs: " + mobs.size()), false);
                            for (MobDefinition mob : mobs) {
                                context.getSource().sendFeedback(() -> Text.literal(
                                        mob.id() + " | " + mob.displayName() + " | Lv" + mob.level()
                                                + " | HP " + mob.health() + " | Damage " + mob.damage()
                                                + " | Defense " + mob.defense()), false);
                            }
                            return 1;
                        }))
                        .then(literal("info").then(argument("mobId", StringArgumentType.word()).executes(context -> {
                            String id = StringArgumentType.getString(context, "mobId");
                            MobDefinition mob = MobRegistry.getById(id).orElse(null);
                            if (mob == null) {
                                context.getSource().sendError(Text.literal("Unknown SkyBlock mob: " + id));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(mob.displayName() + " [" + mob.id() + "] | Lv" + mob.level()), false);
                            context.getSource().sendFeedback(() -> Text.literal("HP " + mob.health() + " | Damage " + mob.damage() + " | Defense " + mob.defense()), false);
                            context.getSource().sendFeedback(() -> Text.literal("Rewards: " + mob.coinReward() + " coins, " + mob.combatXpReward() + " Combat XP"), false);
                            context.getSource().sendFeedback(() -> Text.literal("Entity mapping: " + (mob.entityType().isBlank() ? "none" : mob.entityType())), false);
                            context.getSource().sendFeedback(() -> Text.literal("Source: " + mob.sourceType() + " | " + mob.confidence()), false);
                            return 1;
                        }))))
                .then(literal("items").executes(context -> {
                    for (ItemDefinition item : ItemRegistry.all()) {
                        context.getSource().sendFeedback(() -> Text.literal(
                                item.id() + " | " + item.name() + " | " + item.rarity().name()
                                        + (item.isEquippable() ? " | slot: " + item.equipmentSlot().id() : "")), false);
                    }
                    return 1;
                }))
                .then(literal("item")
                        .then(literal("give").requires(source -> source.hasPermissionLevel(2))
                                .then(argument("itemId", StringArgumentType.word())
                                        .executes(context -> giveItem(context.getSource(), StringArgumentType.getString(context, "itemId"), 1))
                                        .then(argument("count", IntegerArgumentType.integer(1, 2304)).executes(context ->
                                                giveItem(context.getSource(), StringArgumentType.getString(context, "itemId"),
                                                        IntegerArgumentType.getInteger(context, "count"))))))
                        .then(literal("info").then(argument("itemId", StringArgumentType.word()).executes(context -> {
                            String id = StringArgumentType.getString(context, "itemId");
                            ItemDefinition item = ItemRegistry.get(id).orElse(null);
                            if (item == null) {
                                context.getSource().sendError(Text.literal("Unknown item id: " + id));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(item.name() + " [" + item.id() + "] | " + item.rarity() + " | " + item.type()), false);
                            context.getSource().sendFeedback(() -> Text.literal("Slot: " + (item.isEquippable() ? item.equipmentSlot().id() : "none") + " | Max stack: " + item.maxStackSize()), false);
                            item.stats().forEach((stat, value) -> context.getSource().sendFeedback(() -> Text.literal(stat.id() + ": " + value), false));
                            item.lore().forEach(line -> context.getSource().sendFeedback(() -> Text.literal(line), false));
                            return 1;
                        }))))
                .then(literal("inventory").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    var inventory = ProfileManager.get(player).getInventory();
                    context.getSource().sendFeedback(() -> Text.literal(
                            "SkyBlock Inventory: " + inventory.getUsedSlots() + "/" + com.skyrpg.skyblock.inventory.PlayerInventory.CAPACITY + " slots used"), false);
                    if (inventory.getItems().isEmpty()) context.getSource().sendFeedback(() -> Text.literal("(empty)"), false);
                    inventory.getItems().forEach((id, count) -> context.getSource().sendFeedback(
                            () -> Text.literal(id + " x" + count), false));
                    return 1;
                }))
                .then(literal("equipment").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    var equipment = ProfileManager.get(player).getInventory().getEquipment();
                    context.getSource().sendFeedback(() -> Text.literal("Equipped items:"), false);
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        String id = equipment.getOrDefault(slot, "Empty");
                        context.getSource().sendFeedback(() -> Text.literal(slot.id() + ": " + id), false);
                    }
                    return 1;
                }))
                .then(literal("equip").then(argument("itemId", StringArgumentType.word()).executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    String id = StringArgumentType.getString(context, "itemId");
                    var inventory = ProfileManager.get(player).getInventory();
                    if (!inventory.equip(id)) {
                        context.getSource().sendError(Text.literal("Cannot equip " + id + ". Check item id, slot compatibility and inventory space."));
                        return 0;
                    }
                    ProfileManager.markChanged(player);
                    context.getSource().sendFeedback(() -> Text.literal("Equipped " + id + "."), false);
                    return 1;
                })))
                .then(literal("unequip").then(argument("slot", StringArgumentType.word()).executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    String id = StringArgumentType.getString(context, "slot");
                    var slot = EquipmentSlot.fromId(id);
                    if (slot.isEmpty()) {
                        context.getSource().sendError(Text.literal("Unknown equipment slot: " + id));
                        return 0;
                    }
                    if (!ProfileManager.get(player).getInventory().unequip(slot.get())) {
                        context.getSource().sendError(Text.literal("Slot is empty or inventory has no free space."));
                        return 0;
                    }
                    ProfileManager.markChanged(player);
                    context.getSource().sendFeedback(() -> Text.literal("Unequipped " + slot.get().id() + "."), false);
                    return 1;
                })))
        );
    }

    private static int giveItem(ServerCommandSource source, String itemId, int count) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrThrow();
        ItemDefinition definition = ItemRegistry.get(itemId).orElse(null);
        if (definition == null) {
            source.sendError(Text.literal("Unknown item id: " + itemId + ". Use /skyblock items."));
            return 0;
        }
        var inventory = ProfileManager.get(player).getInventory();
        if (!inventory.add(definition.id(), count)) {
            source.sendError(Text.literal("Could not add item: invalid count or inventory is full."));
            return 0;
        }
        ProfileManager.markChanged(player);
        source.sendFeedback(() -> Text.literal("Gave " + count + " x " + definition.name() + " to " + player.getName().getString()), false);
        return 1;
    }
}
