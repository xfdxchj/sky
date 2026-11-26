package com.skyrpg.skyblock.inventory;

import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.item.ItemType;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import java.util.*;

/** Logical SkyBlock inventory, separate from vanilla's inventory. */
public final class PlayerInventory {
    public static final int CAPACITY = 36;
    public static final int ACCESSORY_BAG_CAPACITY = 18;
    private final Map<String, Integer> items = new LinkedHashMap<>();
    private final EnumMap<EquipmentSlot, String> equipment = new EnumMap<>(EquipmentSlot.class);
    private final List<String> accessoryBag = new ArrayList<>();

    public Map<String, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public Map<EquipmentSlot, String> getEquipment() { return Collections.unmodifiableMap(equipment); }
    public List<String> getAccessoryBag() { return Collections.unmodifiableList(accessoryBag); }
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
        if (itemId == null || !add(itemId, 1)) return false;
        equipment.remove(slot);
        return true;
    }

    /** Moves one accessory into the bag. Duplicate copies may be stored; line resolution prevents stacking. */
    public boolean addAccessory(String itemId) {
        ItemDefinition def = ItemRegistry.get(itemId).orElse(null);
        if (def == null || def.type() != ItemType.ACCESSORY || accessoryBag.size() >= ACCESSORY_BAG_CAPACITY
                || getCount(def.id()) < 1) return false;
        if (!remove(def.id(), 1)) return false;
        accessoryBag.add(def.id());
        return true;
    }

    /** Returns an accessory to the logical inventory; the bag is unchanged if inventory insertion fails. */
    public boolean removeAccessory(String itemId) {
        ItemDefinition def = ItemRegistry.get(itemId).orElse(null);
        if (def == null) return false;
        int index = accessoryBag.indexOf(def.id());
        if (index < 0 || !add(def.id(), 1)) return false;
        accessoryBag.remove(index);
        return true;
    }

    /** Resolves duplicate items and upgrade lines; only the highest rarity in each line is effective. */
    public List<String> getEffectiveAccessories() {
        Map<String, ItemDefinition> winners = new LinkedHashMap<>();
        for (String id : accessoryBag) {
            ItemDefinition candidate = ItemRegistry.get(id).orElse(null);
            if (candidate == null || candidate.type() != ItemType.ACCESSORY) continue;
            ItemDefinition current = winners.get(candidate.accessoryLine());
            if (current == null || candidate.rarity().upgradeRank() > current.rarity().upgradeRank()
                    || (candidate.rarity() == current.rarity()
                    && candidate.accessoryPower() > current.accessoryPower())) {
                winners.put(candidate.accessoryLine(), candidate);
            }
        }
        return winners.values().stream().map(ItemDefinition::id).toList();
    }

    public List<StatModifier> getStatModifiers() {
        List<StatModifier> result = new ArrayList<>();
        equipment.forEach((slot, id) -> addItemModifiers(result, "equipment:" + slot.id() + ":" + id, id));
        for (String id : getEffectiveAccessories()) addItemModifiers(result, "accessory:" + id, id);
        return result;
    }

    private static void addItemModifiers(List<StatModifier> result, String source, String id) {
        ItemRegistry.get(id).ifPresent(def -> def.stats().forEach((stat, amount) ->
                result.add(new StatModifier(source, stat, amount, StatModifier.Operation.ADDITIVE))));
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtList itemList = new NbtList();
        items.forEach((id, count) -> {
            NbtCompound entry = new NbtCompound();
            entry.putString("id", id);
            entry.putInt("count", count);
            itemList.add(entry);
        });
        nbt.put("items", itemList);
        NbtCompound equipped = new NbtCompound();
        equipment.forEach((slot, id) -> equipped.putString(slot.id(), id));
        nbt.put("equipment", equipped);
        NbtList accessories = new NbtList();
        accessoryBag.forEach(id -> accessories.add(NbtString.of(id)));
        nbt.put("accessoryBag", accessories);
        return nbt;
    }

    public static PlayerInventory fromNbt(NbtCompound nbt) {
        PlayerInventory inventory = new PlayerInventory();
        NbtList list = nbt.getList("items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            String id = entry.getString("id");
            int count = entry.getInt("count");
            if (count > 0) inventory.add(id, count);
        }
        NbtCompound equipped = nbt.getCompound("equipment");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.ACCESSORY) continue;
            String id = equipped.getString(slot.id());
            ItemDefinition def = ItemRegistry.get(id).orElse(null);
            if (def != null && def.isEquippable() && def.equipmentSlot() == slot) inventory.equipment.put(slot, id);
        }
        NbtList accessories = nbt.getList("accessoryBag", NbtElement.STRING_TYPE);
        for (int i = 0; i < accessories.size() && inventory.accessoryBag.size() < ACCESSORY_BAG_CAPACITY; i++) {
            String id = accessories.getString(i);
            ItemDefinition def = ItemRegistry.get(id).orElse(null);
            if (def != null && def.type() == ItemType.ACCESSORY) {
                inventory.accessoryBag.add(def.id());
            }
        }
        return inventory;
    }
}
