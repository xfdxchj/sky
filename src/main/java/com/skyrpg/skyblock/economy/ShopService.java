package com.skyrpg.skyblock.economy;

import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ShopService {
    public record TransactionResult(boolean success, String message) {}
    private ShopService() {}

    public static TransactionResult buy(ServerPlayerEntity player, String itemId, int count) {
        if (count <= 0) return fail("Count must be positive.");
        ShopOffer offer = ShopRegistry.get(itemId).orElse(null);
        if (offer == null) return fail("This item is not sold by any registered shop.");
        ItemDefinition item = ItemRegistry.get(offer.itemId()).orElse(null);
        if (item == null) return fail("The shop item definition is missing.");
        final long total;
        try { total = Math.multiplyExact(offer.buyPrice(), (long) count); }
        catch (ArithmeticException ex) { return fail("Purchase total is too large."); }

        PlayerProfile profile = ProfileManager.get(player);
        if (profile.getCoins() < total) return fail("Not enough coins. Required: " + total + ", balance: " + profile.getCoins() + ".");
        if (!profile.getInventory().add(item.id(), count)) return fail("Not enough inventory space.");
        if (!profile.removeCoins(total)) {
            profile.getInventory().remove(item.id(), count);
            return fail("Could not complete payment.");
        }
        ProfileManager.markChanged(player);
        return ok("Bought " + count + " x " + item.name() + " for " + total + " coins.");
    }

    public static TransactionResult sell(ServerPlayerEntity player, String itemId, int count) {
        if (count <= 0) return fail("Count must be positive.");
        ShopOffer offer = ShopRegistry.get(itemId).orElse(null);
        if (offer == null) return fail("This item is not accepted by any registered shop.");
        if (!offer.canSell()) return fail("This item cannot be sold to the shop.");
        ItemDefinition item = ItemRegistry.get(offer.itemId()).orElse(null);
        if (item == null) return fail("The shop item definition is missing.");
        final long total;
        try { total = Math.multiplyExact(offer.sellPrice(), (long) count); }
        catch (ArithmeticException ex) { return fail("Sale total is too large."); }

        PlayerProfile profile = ProfileManager.get(player);
        int owned = profile.getInventory().getCount(item.id());
        if (owned < count) return fail("Not enough items. Required: " + count + ", owned: " + owned + ".");
        if (!profile.getInventory().remove(item.id(), count)) return fail("Could not remove items from inventory.");
        if (!profile.addCoins(total)) {
            profile.getInventory().add(item.id(), count);
            return fail("Coin balance limit reached; sale was cancelled.");
        }
        ProfileManager.markChanged(player);
        return ok("Sold " + count + " x " + item.name() + " for " + total + " coins.");
    }

    private static TransactionResult ok(String message) { return new TransactionResult(true, message); }
    private static TransactionResult fail(String message) { return new TransactionResult(false, message); }
}
