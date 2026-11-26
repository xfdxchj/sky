package com.skyrpg.skyblock.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.skyrpg.skyblock.economy.CurrencyManager;
import com.skyrpg.skyblock.economy.ShopOffer;
import com.skyrpg.skyblock.economy.ShopRegistry;
import com.skyrpg.skyblock.economy.ShopService;
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
import com.skyrpg.skyblock.region.RegionDefinition;
import com.skyrpg.skyblock.region.RegionManager;
import com.skyrpg.skyblock.region.RegionRegistry;
import com.skyrpg.skyblock.warp.WarpDefinition;
import com.skyrpg.skyblock.warp.WarpManager;
import com.skyrpg.skyblock.warp.WarpRegistry;
import com.skyrpg.skyblock.npc.NpcDefinition;
import com.skyrpg.skyblock.npc.NpcManager;
import com.skyrpg.skyblock.npc.NpcRegistry;
import com.skyrpg.skyblock.quest.QuestDefinition;
import com.skyrpg.skyblock.quest.QuestManager;
import com.skyrpg.skyblock.quest.QuestRegistry;
import com.skyrpg.skyblock.gui.GuiMenuManager;
import com.skyrpg.skyblock.accessory.AccessoryPowerManager;
import com.skyrpg.skyblock.accessory.PowerDefinition;
import com.skyrpg.skyblock.accessory.PowerRegistry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SkyBlockCommands {
    private SkyBlockCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("skyblock")
                .then(literal("menu").executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    GuiMenuManager.openMain(player);
                    return 1;
                }))
                .then(literal("status").executes(context -> {
                    ServerCommandSource source = context.getSource();
                    String worldName = source.getServer().getSaveProperties().getLevelName();
                    source.sendFeedback(() -> Text.literal("[SkyBlock RPG] Core loaded successfully."), false);
                    source.sendFeedback(() -> Text.literal("World: " + worldName), false);
                    source.sendFeedback(() -> Text.literal("Minecraft: 1.21.1 | Mod: 0.1.0-alpha.3 (P4.6)"), false);
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
                .then(literal("bank")
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            PlayerProfile profile = ProfileManager.get(player);
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "Bank | Purse: " + profile.getCoins() + " coins | Bank: " + profile.getBankCoins() + " coins"), false);
                            return 1;
                        })
                        .then(literal("deposit").then(argument("amount", LongArgumentType.longArg(1)).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            long amount = LongArgumentType.getLong(context, "amount");
                            PlayerProfile profile = ProfileManager.get(player);
                            if (!profile.depositCoins(amount)) {
                                context.getSource().sendError(Text.literal("Deposit failed: insufficient purse balance or bank balance limit reached."));
                                return 0;
                            }
                            ProfileManager.markChanged(player);
                            context.getSource().sendFeedback(() -> Text.literal("Deposited " + amount
                                    + " coins. Purse: " + profile.getCoins() + " | Bank: " + profile.getBankCoins()), false);
                            return 1;
                        }))))
                        .then(literal("withdraw").then(argument("amount", LongArgumentType.longArg(1)).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            long amount = LongArgumentType.getLong(context, "amount");
                            PlayerProfile profile = ProfileManager.get(player);
                            if (!profile.withdrawCoins(amount)) {
                                context.getSource().sendError(Text.literal("Withdrawal failed: insufficient bank balance or purse balance limit reached."));
                                return 0;
                            }
                            ProfileManager.markChanged(player);
                            context.getSource().sendFeedback(() -> Text.literal("Withdrew " + amount
                                    + " coins. Purse: " + profile.getCoins() + " | Bank: " + profile.getBankCoins()), false);
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
                .then(literal("shop")
                        .executes(context -> {
                            var offers = ShopRegistry.all();
                            context.getSource().sendFeedback(() -> Text.literal("Registered shop offers: " + offers.size()), false);
                            for (ShopOffer offer : offers) {
                                ItemDefinition item = ItemRegistry.get(offer.itemId()).orElse(null);
                                String itemName = item == null ? offer.itemId() : item.name();
                                context.getSource().sendFeedback(() -> Text.literal(
                                        offer.shopName() + " | " + offer.itemId() + " (" + itemName + ")"
                                                + " | Buy: " + offer.buyPrice()
                                                + " | Sell: " + (offer.canSell() ? Long.toString(offer.sellPrice()) : "not accepted")), false);
                            }
                            return 1;
                        })
                        .then(literal("buy").then(argument("itemId", StringArgumentType.word())
                                .executes(context -> trade(context.getSource(), true,
                                        StringArgumentType.getString(context, "itemId"), 1))
                                .then(argument("count", IntegerArgumentType.integer(1, 2304)).executes(context ->
                                        trade(context.getSource(), true, StringArgumentType.getString(context, "itemId"),
                                                IntegerArgumentType.getInteger(context, "count"))))))
                        .then(literal("sell").then(argument("itemId", StringArgumentType.word())
                                .executes(context -> trade(context.getSource(), false,
                                        StringArgumentType.getString(context, "itemId"), 1))
                                .then(argument("count", IntegerArgumentType.integer(1, 2304)).executes(context ->
                                        trade(context.getSource(), false, StringArgumentType.getString(context, "itemId"),
                                                IntegerArgumentType.getInteger(context, "count")))))))
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
                            context.getSource().sendFeedback(() -> Text.literal("Entity mapping: " + (mob.entityType().isBlank() ? "none" : mob.entityType()) + " | Spawn type: " + (mob.spawnEntityType().isBlank() ? "none" : mob.spawnEntityType())), false);
                            context.getSource().sendFeedback(() -> Text.literal("Family: " + mob.familyId() + " | Category: " + mob.category()), false);
                            context.getSource().sendFeedback(() -> Text.literal("Area: " + mob.area() + " | Types: " + String.join(", ", mob.mobTypes())), false);
                            context.getSource().sendFeedback(() -> Text.literal("Drops: " + mob.drops().size()), false);
                            mob.drops().forEach(drop -> context.getSource().sendFeedback(() -> Text.literal(
                                    drop.itemId() + " x" + drop.minCount() + "-" + drop.maxCount()
                                            + " | Chance: " + (drop.chanceKnown() ? String.format("%.6f%%", drop.chance() * 100.0) : "unknown")), false));
                            context.getSource().sendFeedback(() -> Text.literal("Source: " + mob.sourceType() + " | " + mob.confidence() + (mob.sourceUrl().isBlank() ? "" : " | " + mob.sourceUrl())), false);
                            return 1;
                        })))
                        .then(literal("spawn").requires(source -> source.hasPermissionLevel(2))
                                .then(argument("mobId", StringArgumentType.word()).executes(context ->
                                        spawnMob(context.getSource(), StringArgumentType.getString(context, "mobId")))))
                )
                .then(literal("region")
                        .then(literal("list").executes(context -> {
                            var regions = RegionRegistry.all();
                            context.getSource().sendFeedback(() -> Text.literal("Registered regions: " + regions.size()), false);
                            for (RegionDefinition region : regions) context.getSource().sendFeedback(
                                    () -> Text.literal(region.id() + " | " + region.displayName() + " | " + region.dimension()), false);
                            return 1;
                        }))
                        .then(literal("here").executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            RegionDefinition region = RegionManager.getCurrent(player);
                            if (region == null) {
                                context.getSource().sendFeedback(() -> Text.literal("Current region: Unassigned"), false);
                            } else {
                                context.getSource().sendFeedback(() -> Text.literal("Current region: " + region.displayName()
                                        + " [" + region.id() + "] | Discovered: "
                                        + ProfileManager.get(player).hasDiscoveredRegion(region.id())), false);
                            }
                            return 1;
                        })))
                .then(literal("npc")
                        .then(literal("list").executes(context -> {
                            var npcs = NpcRegistry.all();
                            context.getSource().sendFeedback(() -> Text.literal("Loaded SkyBlock NPCs: " + npcs.size()), false);
                            for (NpcDefinition npc : npcs) context.getSource().sendFeedback(
                                    () -> Text.literal(npc.id() + " | " + npc.displayName() + " | " + npc.action()
                                            + (npc.requiredRegionId().isBlank() ? "" : " | region: " + npc.requiredRegionId())), false);
                            return 1;
                        }))
                        .then(literal("info").then(argument("npcId", StringArgumentType.word()).executes(context -> {
                            String id = StringArgumentType.getString(context, "npcId");
                            NpcDefinition npc = NpcRegistry.get(id).orElse(null);
                            if (npc == null) {
                                context.getSource().sendError(Text.literal("Unknown SkyBlock NPC: " + id));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(npc.displayName() + " [" + npc.id() + "]"), false);
                            context.getSource().sendFeedback(() -> Text.literal("Entity: " + npc.entityType()
                                    + " | Interaction radius: " + npc.interactionRadius()), false);
                            context.getSource().sendFeedback(() -> Text.literal("Required region: "
                                    + (npc.requiredRegionId().isBlank() ? "none" : npc.requiredRegionId())
                                    + " | Action: " + npc.action()
                                    + (npc.actionTarget().isBlank() ? "" : " -> " + npc.actionTarget())), false);
                            npc.dialogue().forEach(line -> context.getSource().sendFeedback(() -> Text.literal(line), false));
                            return 1;
                        })))
                        .then(literal("spawn").requires(source -> source.hasPermissionLevel(2))
                                .then(argument("npcId", StringArgumentType.word()).executes(context ->
                                        spawnNpc(context.getSource(), StringArgumentType.getString(context, "npcId"))))))
                .then(literal("quest")
                        .then(literal("list").executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            var profile = ProfileManager.get(player);
                            context.getSource().sendFeedback(() -> Text.literal("SkyBlock Quests: " + QuestRegistry.all().size()), false);
                            for (QuestDefinition quest : QuestRegistry.all()) {
                                context.getSource().sendFeedback(() -> Text.literal(quest.id() + " | " + quest.title()
                                        + " | " + QuestManager.status(profile, quest)), false);
                            }
                            return 1;
                        }))
                        .then(literal("info").then(argument("questId", StringArgumentType.word()).executes(context -> {
                            String id = StringArgumentType.getString(context, "questId");
                            QuestDefinition quest = QuestRegistry.get(id).orElse(null);
                            if (quest == null) {
                                context.getSource().sendError(Text.literal("Unknown quest: " + id));
                                return 0;
                            }
                            var player = context.getSource().getPlayerOrThrow();
                            var profile = ProfileManager.get(player);
                            context.getSource().sendFeedback(() -> Text.literal(quest.title() + " [" + quest.id() + "]"), false);
                            context.getSource().sendFeedback(() -> Text.literal(quest.description()), false);
                            context.getSource().sendFeedback(() -> Text.literal("Objective: talk to " + quest.targetNpcId()
                                    + " (" + profile.getQuestProgress(quest.id()) + "/" + quest.requiredCount() + ")"), false);
                            context.getSource().sendFeedback(() -> Text.literal("Status: " + QuestManager.status(profile, quest)), false);
                            context.getSource().sendFeedback(() -> Text.literal("Rewards: " + quest.rewardCoins() + " coins"
                                    + (quest.rewardSkillXp() > 0 ? ", " + quest.rewardSkillXp() + " " + quest.rewardSkill().id() + " XP" : "")), false);
                            return 1;
                        })))
                        .then(literal("accept").then(argument("questId", StringArgumentType.word()).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "questId");
                            QuestManager.Result result = QuestManager.accept(player, id);
                            switch (result) {
                                case SUCCESS -> context.getSource().sendFeedback(() -> Text.literal("Quest accepted: " + id), false);
                                case UNKNOWN_QUEST -> context.getSource().sendError(Text.literal("Unknown quest: " + id));
                                case ALREADY_ACTIVE -> context.getSource().sendError(Text.literal("Quest is already active: " + id));
                                case ALREADY_COMPLETED -> context.getSource().sendError(Text.literal("Quest already completed: " + id));
                                default -> context.getSource().sendError(Text.literal("Could not accept quest: " + id));
                            }
                            return result == QuestManager.Result.SUCCESS ? 1 : 0;
                        })))
                        .then(literal("abandon").then(argument("questId", StringArgumentType.word()).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "questId");
                            QuestManager.Result result = QuestManager.abandon(player, id);
                            if (result == QuestManager.Result.SUCCESS) {
                                context.getSource().sendFeedback(() -> Text.literal("Quest abandoned: " + id), false);
                                return 1;
                            }
                            context.getSource().sendError(Text.literal("Quest is not active: " + id));
                            return 0;
                        }))))
                .then(literal("warp")
                        .then(literal("list").executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            var profile = ProfileManager.get(player);
                            context.getSource().sendFeedback(() -> Text.literal("Registered warps: " + WarpRegistry.all().size()), false);
                            for (WarpDefinition warp : WarpRegistry.all()) {
                                boolean unlocked = warp.requiredRegionId().isBlank()
                                        || profile.hasDiscoveredRegion(warp.requiredRegionId());
                                context.getSource().sendFeedback(() -> Text.literal(warp.id() + " | " + warp.displayName()
                                        + " | " + (unlocked ? "Unlocked" : "Locked: discover " + warp.requiredRegionId())), false);
                            }
                            return 1;
                        }))
                        .then(argument("warpId", StringArgumentType.word()).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "warpId");
                            WarpManager.WarpResult result = WarpManager.teleport(player, id);
                            if (!result.success()) {
                                context.getSource().sendError(Text.literal(result.message()));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(result.message()), false);
                            return 1;
                        })))
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
                .then(literal("accessory")
                        .then(literal("list").executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            var inventory = ProfileManager.get(player).getInventory();
                            context.getSource().sendFeedback(() -> Text.literal("Accessory Bag: "
                                    + inventory.getAccessoryBag().size() + "/" + com.skyrpg.skyblock.inventory.PlayerInventory.ACCESSORY_BAG_CAPACITY), false);
                            if (inventory.getAccessoryBag().isEmpty()) {
                                context.getSource().sendFeedback(() -> Text.literal("(empty)"), false);
                            }
                            for (String id : inventory.getAccessoryBag()) {
                                ItemDefinition item = ItemRegistry.get(id).orElse(null);
                                context.getSource().sendFeedback(() -> Text.literal(id
                                        + (item == null ? "" : " | " + item.name())), false);
                            }
                            return 1;
                        }))
                        .then(literal("add").then(argument("itemId", StringArgumentType.word()).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "itemId");
                            var inventory = ProfileManager.get(player).getInventory();
                            if (!inventory.addAccessory(id)) {
                                context.getSource().sendError(Text.literal("Cannot add accessory. Check that it is an accessory in your inventory, not already in the bag, and that the bag has space."));
                                return 0;
                            }
                            ProfileManager.markChanged(player);
                            context.getSource().sendFeedback(() -> Text.literal("Added " + id + " to your Accessory Bag."), false);
                            return 1;
                        })))
                        .then(literal("remove").then(argument("itemId", StringArgumentType.word()).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "itemId");
                            var inventory = ProfileManager.get(player).getInventory();
                            if (!inventory.removeAccessory(id)) {
                                context.getSource().sendError(Text.literal("Cannot remove accessory. Check the item id and available inventory space."));
                                return 0;
                            }
                            ProfileManager.markChanged(player);
                            context.getSource().sendFeedback(() -> Text.literal("Removed " + id + " from your Accessory Bag."), false);
                            return 1;
                        })))
                        .then(literal("power").executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            var profile = ProfileManager.get(player);
                            int ap = profile.getAccessoryPower();
                            context.getSource().sendFeedback(() -> Text.literal("Accessory Power: " + ap
                                    + " | Multiplier: " + String.format("%.2f", AccessoryPowerManager.getStatsMultiplier(ap))), false);
                            context.getSource().sendFeedback(() -> Text.literal("Selected Power: " + profile.getSelectedPower()
                                    + " | Tuning Points: " + profile.getUsedTuningPoints() + "/" + AccessoryPowerManager.getTuningPoints(ap)), false);
                            for (PowerDefinition power : PowerRegistry.all()) {
                                context.getSource().sendFeedback(() -> Text.literal((profile.getUnlockedPowers().contains(power.id()) ? "[Unlocked] " : "[Locked] ")
                                        + power.id() + " - " + power.name()), false);
                            }
                            return 1;
                        })
                        .then(literal("select").then(argument("powerId", StringArgumentType.word()).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "powerId");
                            var profile = ProfileManager.get(player);
                            if (!profile.selectPower(id)) {
                                context.getSource().sendError(Text.literal("Power is unknown or not unlocked: " + id));
                                return 0;
                            }
                            ProfileManager.markChanged(player);
                            context.getSource().sendFeedback(() -> Text.literal("Selected accessory power: " + id), false);
                            return 1;
                        })))
                        .then(literal("unlock").then(argument("powerId", StringArgumentType.word()).executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "powerId");
                            PowerDefinition power = PowerRegistry.get(id).orElse(null);
                            if (power == null || power.starter() || power.powerStoneItemId().isBlank()) {
                                context.getSource().sendError(Text.literal("Unknown power or no Power Stone configured: " + id));
                                return 0;
                            }
                            var profile = ProfileManager.get(player);
                            var inventory = profile.getInventory();
                            if (profile.getUnlockedPowers().contains(power.id())) {
                                context.getSource().sendError(Text.literal("Power already unlocked: " + id));
                                return 0;
                            }
                            if (inventory.getCount(power.powerStoneItemId()) < 9) {
                                context.getSource().sendError(Text.literal("Unlocking " + id + " requires 9x " + power.powerStoneItemId()));
                                return 0;
                            }
                            if (!profile.unlockPower(id) || !inventory.remove(power.powerStoneItemId(), 9)) {
                                context.getSource().sendError(Text.literal("Could not unlock power; no items were consumed."));
                                return 0;
                            }
                            ProfileManager.markChanged(player);
                            context.getSource().sendFeedback(() -> Text.literal("Unlocked accessory power: " + id), false);
                            return 1;
                        }))))
                        .then(literal("tuning").executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            var profile = ProfileManager.get(player);
                            int available = AccessoryPowerManager.getTuningPoints(profile.getAccessoryPower());
                            context.getSource().sendFeedback(() -> Text.literal("Tuning Points: " + profile.getUsedTuningPoints()
                                    + "/" + available), false);
                            for (StatType stat : StatType.values()) {
                                int points = profile.getTuningAllocations().getOrDefault(stat, 0);
                                if (AccessoryPowerManager.isTunable(stat) || points > 0) {
                                    context.getSource().sendFeedback(() -> Text.literal(stat.id() + ": " + points
                                            + " (+" + String.format("%.2f", points * AccessoryPowerManager.tuningValue(stat)) + ")"), false);
                                }
                            }
                            return 1;
                        })
                        .then(literal("set").then(argument("stat", StringArgumentType.word())
                                .then(argument("points", IntegerArgumentType.integer(0, 100000)).executes(context -> {
                                    var player = context.getSource().getPlayerOrThrow();
                                    String statId = StringArgumentType.getString(context, "stat");
                                    var stat = StatType.fromId(statId);
                                    if (stat.isEmpty()) {
                                        context.getSource().sendError(Text.literal("Unknown stat: " + statId));
                                        return 0;
                                    }
                                    int points = IntegerArgumentType.getInteger(context, "points");
                                    var profile = ProfileManager.get(player);
                                    if (!profile.setTuningPoints(stat.get(), points)) {
                                        context.getSource().sendError(Text.literal("Invalid tuning stat or insufficient available points."));
                                        return 0;
                                    }
                                    ProfileManager.markChanged(player);
                                    context.getSource().sendFeedback(() -> Text.literal("Set " + stat.get().id()
                                            + " tuning to " + points + " points."), false);
                                    return 1;
                                })))))
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

    private static int trade(ServerCommandSource source, boolean buying, String itemId, int count)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrThrow();
        ShopService.TransactionResult result = buying
                ? ShopService.buy(player, itemId, count)
                : ShopService.sell(player, itemId, count);
        if (!result.success()) {
            source.sendError(Text.literal(result.message()));
            return 0;
        }
        source.sendFeedback(() -> Text.literal(result.message()), false);
        return 1;
    }

    private static int spawnMob(ServerCommandSource source, String mobId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrThrow();
        MobDefinition mob = MobRegistry.getById(mobId).orElse(null);
        if (mob == null) {
            source.sendError(Text.literal("Unknown SkyBlock mob: " + mobId));
            return 0;
        }
        if (mob.spawnEntityType().isBlank()) {
            source.sendError(Text.literal("Mob has no spawn entity configured: " + mob.id()));
            return 0;
        }
        Identifier entityId = Identifier.tryParse(mob.spawnEntityType());
        if (entityId == null) {
            source.sendError(Text.literal("Invalid spawn entity type: " + mob.spawnEntityType()));
            return 0;
        }
        EntityType<?> type = Registries.ENTITY_TYPE.getOrEmpty(entityId).orElse(null);
        if (type == null) {
            source.sendError(Text.literal("Unknown Minecraft entity type: " + entityId));
            return 0;
        }
        Entity entity = type.create(player.getServerWorld());
        if (!(entity instanceof LivingEntity living)) {
            source.sendError(Text.literal("Configured entity is not a living entity: " + entityId));
            return 0;
        }
        var position = player.getPos().add(player.getRotationVec(1.0F).multiply(2.0));
        entity.refreshPositionAndAngles(position.x, position.y, position.z, player.getYaw(), 0.0F);
        entity.addCommandTag(MobRegistry.entityTag(mob.id()));
        entity.setCustomName(Text.literal("[Lv" + mob.level() + "] " + mob.displayName()));
        entity.setCustomNameVisible(true);
        living.setHealth(living.getMaxHealth());
        if (!player.getServerWorld().spawnEntity(entity)) {
            source.sendError(Text.literal("Could not spawn mob: " + mob.id()));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Spawned " + mob.displayName() + " [Lv" + mob.level()
                + "] with " + mob.health() + " SkyBlock HP. Tag: " + MobRegistry.entityTag(mob.id())), false);
        return 1;
    }

    private static int spawnNpc(ServerCommandSource source, String npcId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrThrow();
        NpcDefinition npc = NpcRegistry.get(npcId).orElse(null);
        if (npc == null) {
            source.sendError(Text.literal("Unknown NPC id: " + npcId));
            return 0;
        }
        Identifier entityId = Identifier.tryParse(npc.entityType());
        if (entityId == null) {
            source.sendError(Text.literal("Invalid NPC entity type: " + npc.entityType()));
            return 0;
        }
        EntityType<?> type = Registries.ENTITY_TYPE.getOrEmpty(entityId).orElse(null);
        if (type == null) {
            source.sendError(Text.literal("Unknown Minecraft entity type: " + entityId));
            return 0;
        }
        Entity entity = type.create(player.getServerWorld());
        if (!(entity instanceof LivingEntity)) {
            source.sendError(Text.literal("Configured NPC entity is not living: " + entityId));
            return 0;
        }
        var position = player.getPos().add(player.getRotationVec(1.0F).multiply(2.0));
        entity.refreshPositionAndAngles(position.x, position.y, position.z, player.getYaw(), 0.0F);
        entity.addCommandTag(NpcManager.entityTag(npc.id()));
        entity.setCustomName(Text.literal(npc.displayName()));
        entity.setCustomNameVisible(true);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        if (entity instanceof MobEntity mobEntity) mobEntity.setAiDisabled(true);
        if (!player.getServerWorld().spawnEntity(entity)) {
            source.sendError(Text.literal("Could not spawn NPC: " + npc.id()));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Spawned NPC " + npc.displayName()
                + " [" + npc.id() + "] with tag " + NpcManager.entityTag(npc.id())), false);
        return 1;
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
