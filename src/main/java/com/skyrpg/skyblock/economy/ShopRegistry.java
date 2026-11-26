package com.skyrpg.skyblock.economy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.item.ItemRegistry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import java.io.Reader;
import java.util.*;

public final class ShopRegistry {
    private static final Map<String, ShopOffer> OFFERS = new LinkedHashMap<>();
    private ShopRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, ShopOffer> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/shops",
                id -> id.getNamespace().equals(SkyBlockRPG.MOD_ID) && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                String shopId = required(root, "shop_id");
                String shopName = required(root, "name");
                JsonArray offers = root.getAsJsonArray("offers");
                if (offers == null) throw new IllegalArgumentException("Missing offers array");
                Map<String, ShopOffer> fileOffers = new LinkedHashMap<>();
                for (var element : offers) {
                    JsonObject json = element.getAsJsonObject();
                    String itemId = required(json, "item_id").toLowerCase(Locale.ROOT);
                    if (ItemRegistry.get(itemId).isEmpty()) throw new IllegalArgumentException("Unknown item id: " + itemId);
                    ShopOffer offer = new ShopOffer(shopId, shopName, itemId,
                            requiredLong(json, "buy_price"), optionalLong(json, "sell_price", 0L));
                    if (fileOffers.putIfAbsent(itemId, offer) != null || loaded.containsKey(itemId)) {
                        throw new IllegalArgumentException("Duplicate shop offer: " + itemId);
                    }
                }
                loaded.putAll(fileOffers);
            } catch (Exception e) {
                SkyBlockRPG.LOGGER.error("Failed to load shop data from {}", entry.getKey(), e);
            }
        }
        OFFERS.clear();
        OFFERS.putAll(loaded);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock shop offers.", OFFERS.size());
    }

    public static Optional<ShopOffer> get(String itemId) {
        return Optional.ofNullable(OFFERS.get(itemId == null ? "" : itemId.toLowerCase(Locale.ROOT)));
    }
    public static Collection<ShopOffer> all() { return Collections.unmodifiableCollection(OFFERS.values()); }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsString();
    }
    private static long requiredLong(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) throw new IllegalArgumentException("Missing " + key);
        return json.get(key).getAsLong();
    }
    private static long optionalLong(JsonObject json, String key, long fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsLong() : fallback;
    }
}
