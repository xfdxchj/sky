package com.skyrpg.skyblock.npc;

import com.skyrpg.skyblock.economy.ShopOffer;
import com.skyrpg.skyblock.economy.ShopRegistry;
import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.region.RegionDefinition;
import com.skyrpg.skyblock.region.RegionManager;
import com.skyrpg.skyblock.warp.WarpManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class NpcManager {
    private static final String ENTITY_TAG_PREFIX = "skyblockrpg_npc_";
    private NpcManager() {}

    public static String entityTag(String npcId) {
        return ENTITY_TAG_PREFIX + npcId.toLowerCase(java.util.Locale.ROOT);
    }

    public static NpcDefinition get(Entity entity) {
        if (entity == null) return null;
        for (String tag : entity.getCommandTags()) {
            if (tag.startsWith(ENTITY_TAG_PREFIX)) {
                NpcDefinition definition = NpcRegistry.get(tag.substring(ENTITY_TAG_PREFIX.length())).orElse(null);
                if (definition != null) return definition;
            }
        }
        return null;
    }

    /** Returns true for every tagged NPC, including denied interactions, so vanilla trading is suppressed. */
    public static boolean interact(ServerPlayerEntity player, Entity entity) {
        NpcDefinition npc = get(entity);
        if (npc == null) return false;

        if (player.squaredDistanceTo(entity) > npc.interactionRadius() * npc.interactionRadius()) {
            player.sendMessage(Text.literal(npc.displayName() + " is too far away."), true);
            return true;
        }

        if (!npc.requiredRegionId().isBlank()) {
            RegionDefinition current = RegionManager.getCurrent(player);
            if (current == null || !current.id().equalsIgnoreCase(npc.requiredRegionId())) {
                player.sendMessage(Text.literal("This NPC can only be used in region: " + npc.requiredRegionId()), true);
                return true;
            }
        }

        player.sendMessage(Text.literal("§e[NPC] " + npc.displayName()), false);
        for (String line : npc.dialogue()) player.sendMessage(Text.literal(line), false);
        runAction(player, npc);
        return true;
    }

    private static void runAction(ServerPlayerEntity player, NpcDefinition npc) {
        PlayerProfile profile = ProfileManager.get(player);
        switch (npc.action()) {
            case "bank" -> player.sendMessage(Text.literal(
                    "Bank | Purse: " + profile.getCoins() + " coins | Bank: " + profile.getBankCoins() + " coins"
                            + " | Use /skyblock bank deposit <amount> or withdraw <amount>."), false);
            case "shop" -> {
                player.sendMessage(Text.literal("Shop catalogue (" + ShopRegistry.all().size() + " offers):"), false);
                for (ShopOffer offer : ShopRegistry.all()) {
                    ItemDefinition item = ItemRegistry.get(offer.itemId()).orElse(null);
                    String name = item == null ? offer.itemId() : item.name();
                    player.sendMessage(Text.literal(offer.itemId() + " (" + name + ") | Buy: " + offer.buyPrice()
                            + " | Sell: " + (offer.canSell() ? Long.toString(offer.sellPrice()) : "not accepted")), false);
                }
                player.sendMessage(Text.literal("Use /skyblock shop buy <itemId> [count] to purchase."), false);
            }
            case "warp" -> {
                WarpManager.WarpResult result = WarpManager.teleport(player, npc.actionTarget());
                player.sendMessage(Text.literal(result.message()), false);
            }
            default -> { }
        }
    }
}
