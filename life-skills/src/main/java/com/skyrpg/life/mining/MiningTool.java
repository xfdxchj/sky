package com.skyrpg.life.mining;

import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Tool category and breaking power for vanilla items.
 *
 * <p>Life Skills does not own a tool item system yet, so vanilla tools are used
 * as a development stand-in, not verified Hypixel tool data.</p>
 */
public final class MiningTool {
    private static final Map<String, Integer> PICKAXE_POWER = Map.of(
            "minecraft:wooden_pickaxe", 1,
            "minecraft:stone_pickaxe", 2,
            "minecraft:iron_pickaxe", 3,
            "minecraft:golden_pickaxe", 3,
            "minecraft:diamond_pickaxe", 4,
            "minecraft:netherite_pickaxe", 5
    );

    private MiningTool() {}

    public static MiningToolType typeOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return MiningToolType.NONE;
        }
        Item item = stack.getItem();
        if (item instanceof PickaxeItem) {
            return MiningToolType.PICKAXE;
        }
        if (item instanceof AxeItem) {
            return MiningToolType.AXE;
        }
        if (item instanceof HoeItem) {
            return MiningToolType.HOE;
        }
        return MiningToolType.NONE;
    }

    public static int breakingPowerOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return PICKAXE_POWER.getOrDefault(id.toString(), 0);
    }
}
