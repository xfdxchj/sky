package com.skyrpg.skyblock.inventory;

import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import java.util.*;

/** Logical SkyBlock inventory, intentionally separate from vanilla's 36-slot inventory. */
public final class PlayerInventory {
    public static final int CAPACITY = 36;
    private final Map<String, Integer> items = new LinkedHashMap<>();
    private final EnumMap<EquipmentSlot, String> equipment = new EnumMap<>(EquipmentSlot.class);

    public Map<String, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public Map<EquipmentSlot, String> getEquipment() { return Collections.unmodifiableMap(equipment); }
    public int getCount(String itemId) { return items.getOrDefault(itemId, 0); }
    public int getUsedSlots() {
        int slots = 0;
        for (var entry : items.entrySet()) {
            int max = ItemRegistry.get(entry.getKey()).map(ItemDefinition::maxStackSize).orElse(64);
            slots += (int) (((long) entry.getValue() + max - 1L) / max);
        }
        return slots;
    }

    public boolean add(String itemId, int count) {
        Optional<ItemDefinition> found = ItemRegistry.get(itemId);
        if (found.isEmpty() || count <= 0) return false;
        ItemDefinition def = found.get();
        int current = getCount(def.id());
        if (count > Integer.MAX_VALUE - current) return false;
        int newCount = current + count;
        int requiredSlots = (int) (((long) newCount + def.maxStackSize() - 1L) / def.maxStackSize());
        int oldSlots = current == 0 ? 0 : (int) (((long) current + def.maxStackSize() - 1L) / def.maxStackSize());
        if (getUsedSlots() - oldSlots + requiredSlots > CAPACITY) return false;
        items.put(def.id(), newCount);
        return true;
    }

    public boolean remove(String itemId, int count) {
        if (count <= 0) return false;
        int current = getCount(itemId);
        if (current < count) return false;
        if (current == count) items.remove(itemId); else items.put(itemId, current - count);
        return true;
    }

    public boolean equip(String itemId) {
        ItemDefinition def = ItemRegistry.get(itemId).orElse(null);
        if (def == null || !def.isEquippable() || getCount(def.id()) < 1) return false;
        EquipmentSlot slot = def.equipmentSlot();
        String previous = equipment.get(slot);
        if (!remove(def.id(), 1)) return false;
        if (previous != null && !add(previous, 1)) {
            add(def.id(), 1);
            return false;
        }
        equipment.put(slot, def.id());
        return true;
    }

    public boolean unequip(EquipmentSlot slot) {
        String itemId = equipment.get(slot);
        if (itemId == null) return false;
        if (!add(itemId, 1)) return false;
        equipment.remove(slot);
        return true;
    }

    public List<StatModifier> getStatModifiers() {
        List<StatModifier> result = new ArrayList<>();
        equipment.forEach((slot, id) -> ItemRegistry.get(id).ifPresent(def ->
                def.stats().forEach((stat, amount) -> result.add(new StatModifier("equipment:" + id, stat, amount, StatModifier.Operation.ADDITIVE)))));
        return result;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtList itemList = new NbtList();
        items.forEach((id, count) -> { NbtCompound entry = new NbtCompound(); entry.putString("id", id); entry.putInt("count", count); itemList.add(entry); });
        nbt.put("items", itemList);
        NbtCompound equipped = new NbtCompound();
        equipment.forEach((slot, id) -> equipped.putString(slot.id(), id));
        nbt.put("equipment", equipped);
        return nbt;
    }

    public static PlayerInventory fromNbt(NbtCompound nbt) {
        PlayerInventory inventory = new PlayerInventory();
        NbtList list = nbt.getList("items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            String id = entry.getString("id"); int count = entry.getInt("count");
            if (count > 0) inventory.add(id, count);
        }
        NbtCompound equipped = nbt.getCompound("equipment");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            String id = equipped.getString(slot.id());
            ItemDefinition def = ItemRegistry.get(id).orElse(null);
            if (def != null && def.isEquippable() && def.equipmentSlot() == slot) inventory.equipment.put(slot, id);
        }
        return inventory;
    }
}
