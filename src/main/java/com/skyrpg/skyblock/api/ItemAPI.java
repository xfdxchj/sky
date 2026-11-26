package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import net.minecraft.util.Identifier;
import java.util.Collection;
import java.util.Optional;

/** Public read-only access to item definitions loaded from Core and extension datapacks. */
public final class ItemAPI {
    public static final int API_VERSION = 1;
    private ItemAPI() {}

    public static Optional<ItemDefinition> get(Identifier id) {
        return id == null ? Optional.empty() : ItemRegistry.get(id.toString());
    }

    /** Legacy Core item IDs without a namespace remain accepted. */
    public static Optional<ItemDefinition> get(String id) {
        return ItemRegistry.get(id);
    }

    public static Collection<ItemDefinition> getAll() {
        return ItemRegistry.all();
    }
}
