package com.skyrpg.skyblock.reforge;

import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.stats.StatModifier;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ReforgeManager {
    public enum Result { SUCCESS, EMPTY_SLOT, ITEM_NOT_REFORGEABLE, NO_COMPATIBLE_REFORGES, INSUFFICIENT_COINS }
    private ReforgeManager() {}

    public static Result roll(ServerPlayerEntity player, EquipmentSlot slot) {
        PlayerProfile profile = com.skyrpg.skyblock.profile.ProfileManager.get(player);
        var inventory = profile.getInventory();
        String itemId = inventory.getEquipment().get(slot);
        if (itemId == null) return Result.EMPTY_SLOT;
        ItemDefinition item = ItemRegistry.get(itemId).orElse(null);
        if (item == null || !item.reforgeable()) return Result.ITEM_NOT_REFORGEABLE;
        List<ReforgeDefinition> pool = ReforgeRegistry.forSlot(slot.id());
        if (pool.isEmpty()) return Result.NO_COMPATIBLE_REFORGES;
        ReforgeDefinition chosen = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        if (profile.getCoins() < chosen.cost()) return Result.INSUFFICIENT_COINS;
        if (!profile.removeCoins(chosen.cost())) return Result.INSUFFICIENT_COINS;
        inventory.setReforge(slot, chosen.id());
        com.skyrpg.skyblock.profile.ProfileManager.markChanged(player);
        return Result.SUCCESS;
    }

    public static List<StatModifier> modifiers(EquipmentSlot slot, String reforgeId) {
        ReforgeDefinition reforge = ReforgeRegistry.get(reforgeId).orElse(null);
        if (reforge == null || !reforge.allows(slot.id())) return List.of();
        List<StatModifier> result = new ArrayList<>();
        reforge.stats().forEach((stat, amount) -> result.add(new StatModifier(
                "reforge:" + slot.id() + ":" + reforge.id(), stat, amount, StatModifier.Operation.ADDITIVE)));
        return result;
    }
}
