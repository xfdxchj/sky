package com.skyrpg.skyblock.economy;

public record ShopOffer(String shopId, String shopName, String itemId, long buyPrice, long sellPrice) {
    public ShopOffer {
        if (shopId == null || shopId.isBlank()) throw new IllegalArgumentException("shopId is required");
        if (shopName == null || shopName.isBlank()) throw new IllegalArgumentException("shopName is required");
        if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId is required");
        if (buyPrice <= 0) throw new IllegalArgumentException("buyPrice must be positive");
        if (sellPrice < 0 || sellPrice > buyPrice) throw new IllegalArgumentException("sellPrice must be between zero and buyPrice");
    }
    public boolean canBuy() { return buyPrice > 0; }
    public boolean canSell() { return sellPrice > 0; }
}
