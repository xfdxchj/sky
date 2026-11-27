package com.skyrpg.skyblock.item;

import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.reforge.ReforgeDefinition;
import com.skyrpg.skyblock.reforge.ReforgeRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.UUID;

/**
 * Converts the legacy logical-inventory NBT shape into the versioned instance shape.
 *
 * <p>The method is intentionally pure: it does not mutate the supplied legacy tag or
 * replace the live PlayerInventory. P2-01C will connect this result to profile storage.</p>
 */
public final class LegacyInventoryMigrator {
    public static final int INVENTORY_FORMAT_VERSION = 1;

    private LegacyInventoryMigrator() {}

    public static NbtCompound migrate(NbtCompound legacy, int inventoryCapacity, int accessoryCapacity) {
        if (legacy == null) throw new IllegalArgumentException("Legacy inventory tag is required");
        if (inventoryCapacity < 0 || accessoryCapacity < 0) {
            throw new IllegalArgumentException("Inventory capacities cannot be negative");
        }

        NbtCompound migrated = new NbtCompound();
        migrated.putInt("formatVersion", INVENTORY_FORMAT_VERSION);
        NbtList instances = new NbtList();
        NbtList quarantine = new NbtList();
        NbtCompound equipmentRefs = new NbtCompound();
        NbtList accessoryRefs = new NbtList();
        int usedSlots = 0;

        NbtList oldItems = legacy.getList("items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < oldItems.size(); i++) {
            NbtCompound raw = oldItems.getCompound(i).copy();
            String id = raw.getString("id");
            int count = raw.getInt("count");
            ItemDefinition definition = ItemRegistry.get(id).orElse(null);
            if (definition == null) {
                quarantine(quarantine, "items[" + i + "]", "unknown_item_definition", raw);
                continue;
            }
            if (count <= 0) {
                quarantine(quarantine, "items[" + i + "]", "invalid_non_positive_count", raw);
                continue;
            }

            int remaining = count;
            while (remaining > 0) {
                int stackCount = Math.min(remaining, definition.maxStackSize());
                if (usedSlots >= inventoryCapacity) {
                    NbtCompound overflow = raw.copy();
                    overflow.putInt("remainingCount", remaining);
                    quarantine(quarantine, "items[" + i + "]", "inventory_capacity_exceeded", overflow);
                    break;
                }
                try {
                    ItemInstance instance = ItemInstance.create(definition, stackCount);
                    instances.add(ItemInstanceNbtCodec.encode(instance));
                    usedSlots++;
                    remaining -= stackCount;
                } catch (IllegalArgumentException exception) {
                    quarantine(quarantine, "items[" + i + "]", "invalid_item_instance: " + exception.getMessage(), raw);
                    break;
                }
            }
        }

        NbtCompound oldEquipment = legacy.getCompound("equipment");
        NbtCompound oldReforges = legacy.getCompound("reforges");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.ACCESSORY || !oldEquipment.contains(slot.id(), NbtElement.STRING_TYPE)) continue;
            String itemId = oldEquipment.getString(slot.id());
            if (itemId.isBlank()) continue;
            ItemDefinition definition = ItemRegistry.get(itemId).orElse(null);
            NbtCompound raw = new NbtCompound();
            raw.putString("slot", slot.id());
            raw.putString("id", itemId);
            if (definition == null || !definition.isEquippable() || definition.equipmentSlot() != slot) {
                quarantine(quarantine, "equipment." + slot.id(), "unknown_or_incompatible_equipment", raw);
                continue;
            }

            String reforgeId = oldReforges.getString(slot.id());
            if (!reforgeId.isBlank()) {
                ReforgeDefinition reforge = ReforgeRegistry.get(reforgeId).orElse(null);
                if (reforge == null || !reforge.allows(slot.id()) || !definition.reforgeable()) {
                    NbtCompound invalidReforge = raw.copy();
                    invalidReforge.putString("reforgeId", reforgeId);
                    quarantine(quarantine, "reforges." + slot.id(), "unknown_or_incompatible_reforge", invalidReforge);
                    reforgeId = "";
                }
            }

            try {
                ItemInstance instance = ItemInstance.restore(UUID.randomUUID(),
                        net.minecraft.util.Identifier.tryParse(definition.id().contains(":")
                                ? definition.id() : "skyblockrpg:" + definition.id()),
                        1, reforgeId, java.util.Map.of(), 0, 0, new NbtCompound(), definition);
                instances.add(ItemInstanceNbtCodec.encode(instance));
                equipmentRefs.putString(slot.id(), instance.instanceId().toString());
            } catch (IllegalArgumentException exception) {
                quarantine(quarantine, "equipment." + slot.id(), "invalid_equipment_instance: " + exception.getMessage(), raw);
            }
        }

        NbtList oldAccessories = legacy.getList("accessoryBag", NbtElement.STRING_TYPE);
        for (int i = 0; i < oldAccessories.size(); i++) {
            String id = oldAccessories.getString(i);
            ItemDefinition definition = ItemRegistry.get(id).orElse(null);
            NbtElement raw = NbtString.of(id);
            if (definition == null || definition.type() != ItemType.ACCESSORY) {
                quarantine(quarantine, "accessoryBag[" + i + "]", "unknown_or_non_accessory_item", raw);
                continue;
            }
            if (accessoryRefs.size() >= accessoryCapacity) {
                quarantine(quarantine, "accessoryBag[" + i + "]", "accessory_capacity_exceeded", raw);
                continue;
            }
            try {
                ItemInstance instance = ItemInstance.create(definition, 1);
                instances.add(ItemInstanceNbtCodec.encode(instance));
                accessoryRefs.add(NbtString.of(instance.instanceId().toString()));
            } catch (IllegalArgumentException exception) {
                quarantine(quarantine, "accessoryBag[" + i + "]", "invalid_accessory_instance: " + exception.getMessage(), raw);
            }
        }

        migrated.put("instances", instances);
        migrated.put("equipmentRefs", equipmentRefs);
        migrated.put("accessoryRefs", accessoryRefs);
        migrated.put("quarantine", quarantine);
        migrated.putInt("quarantineCount", quarantine.size());
        return migrated;
    }

    private static void quarantine(NbtList quarantine, String source, String reason, NbtElement raw) {
        NbtCompound entry = new NbtCompound();
        entry.putString("source", source);
        entry.putString("reason", reason == null ? "unknown" : reason);
        entry.put("raw", raw.copy());
        quarantine.add(entry);
    }
}
