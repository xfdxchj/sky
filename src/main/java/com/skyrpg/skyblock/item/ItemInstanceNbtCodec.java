package com.skyrpg.skyblock.item;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import com.skyrpg.skyblock.reforge.ReforgeDefinition;
import com.skyrpg.skyblock.reforge.ReforgeRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Versioned NBT codec for individual SkyBlock item instances. */
public final class ItemInstanceNbtCodec {
    public static final int FORMAT_VERSION = 1;

    private ItemInstanceNbtCodec() {}

    public static NbtCompound encode(ItemInstance instance) {
        NbtCompound tag = new NbtCompound();
        tag.putInt("formatVersion", FORMAT_VERSION);
        tag.putString("instanceId", instance.instanceId().toString());
        tag.putString("definitionId", instance.definitionId().toString());
        tag.putInt("count", instance.count());
        tag.putString("reforgeId", instance.reforgeId());
        tag.putInt("stars", instance.stars());
        tag.putInt("upgradeLevel", instance.upgradeLevel());

        NbtList enchantments = new NbtList();
        instance.enchantments().forEach((id, level) -> {
            NbtCompound enchantment = new NbtCompound();
            enchantment.putString("id", id.toString());
            enchantment.putInt("level", level);
            enchantments.add(enchantment);
        });
        tag.put("enchantments", enchantments);
        tag.put("customData", instance.customData());
        return tag;
    }

    /**
     * Decodes and validates an instance against the currently loaded item definition.
     * Unknown definitions and malformed data are reported to the caller for quarantine.
     */
    public static ItemInstance decode(NbtCompound tag) {
        if (tag == null) throw new IllegalArgumentException("Item instance tag is required");
        int version = tag.getInt("formatVersion");
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported item instance format version: " + version);
        }

        UUID instanceId;
        try {
            instanceId = UUID.fromString(tag.getString("instanceId"));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid item instance UUID", exception);
        }

        Identifier definitionId = Identifier.tryParse(tag.getString("definitionId"));
        if (definitionId == null) throw new IllegalArgumentException("Invalid item definition ID");
        ItemDefinition definition = ItemRegistry.get(definitionId.toString()).orElse(null);
        if (definition == null) throw new IllegalArgumentException("Unknown item definition: " + definitionId);

        Map<Identifier, Integer> enchantments = new LinkedHashMap<>();
        NbtList enchantmentList = tag.getList("enchantments", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < enchantmentList.size(); i++) {
            NbtCompound entry = enchantmentList.getCompound(i);
            Identifier id = Identifier.tryParse(entry.getString("id"));
            int level = entry.getInt("level");
            if (id == null || enchantments.putIfAbsent(id, level) != null) {
                throw new IllegalArgumentException("Invalid or duplicate enchantment entry");
            }
        }

        String reforgeId = tag.getString("reforgeId");
        if (!reforgeId.isBlank()) {
            ReforgeDefinition reforge = ReforgeRegistry.get(reforgeId).orElse(null);
            if (reforge == null || !definition.reforgeable() || definition.equipmentSlot() == null
                    || !reforge.allows(definition.equipmentSlot().id())) {
                throw new IllegalArgumentException("Unknown or incompatible reforge: " + reforgeId);
            }
        }

        NbtCompound customData = tag.contains("customData", NbtElement.COMPOUND_TYPE)
                ? tag.getCompound("customData").copy() : new NbtCompound();
        return ItemInstance.restore(instanceId, definitionId, tag.getInt("count"),
                reforgeId, enchantments, tag.getInt("stars"),
                tag.getInt("upgradeLevel"), customData, definition);
    }
}
