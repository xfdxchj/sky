package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import net.minecraft.util.Identifier;
import java.util.Collection;
import java.util.Optional;

/** Public read-only access to item definitions loaded from Core and extension data packs. */
public final class ItemAPI {
    public static final int API_VERSION = 1;
    private ItemAPI() {}

    public static Optional<ItemView> get(Identifier id) {
        return id == null ? Optional.empty() : ItemRegistry.get(id.toString()).map(ItemAPI::toView);
    }

    /** Legacy Core item IDs without a namespace remain accepted. */
    public static Optional<ItemView> get(String id) {
        return ItemRegistry.get(id).map(ItemAPI::toView);
    }

    public static Collection<ItemView> getAll() {
        return ItemRegistry.all().stream().map(ItemAPI::toView).toList();
    }

    private static ItemView toView(ItemDefinition definition) {
        return new ItemView(
                Identifier.tryParse(definition.id()),
                definition.name(),
                definition.rarity().name(),
                definition.type().name(),
                definition.equipmentSlot() == null ? "" : definition.equipmentSlot().id(),
                definition.maxStackSize(),
                definition.stats(),
                definition.lore(),
                definition.accessoryLine(),
                definition.dungeonAccessory(),
                definition.accessoryPower(),
                definition.reforgeable()
        );
    }
}
