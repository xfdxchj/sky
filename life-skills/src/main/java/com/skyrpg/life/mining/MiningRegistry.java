package com.skyrpg.life.mining;

import com.skyrpg.life.common.drop.DropEntry;
import com.skyrpg.life.common.drop.DropTable;
import com.skyrpg.life.mining.handler.MiningHandlers;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of mineable blocks, modelled after SkyBlock's block catalogue.
 *
 * <p>Strength, power and XP values are placeholder development data, not verified Hypixel values.
 * Only pickaxe blocks are registered here; logs and crops belong to the Foraging/Farming modules.</p>
 */
public final class MiningRegistry {
    private static final Map<Identifier, MineableBlock> BLOCKS = new LinkedHashMap<>();

    private MiningRegistry() {}

    public static void clear() {
        BLOCKS.clear();
    }

    public static void add(MineableBlock block) {
        BLOCKS.put(block.blockId(), block);
    }

    public static void registerDefaults() {
        // Stone-like blocks (pickaxe)
        pickaxe("andesite", 15, 1, 1.0, stoneDrops());
        pickaxe("granite", 15, 1, 1.0, stoneDrops());
        pickaxe("diorite", 15, 1, 1.0, stoneDrops());
        pickaxe("stone", 15, 1, 1.0, stoneDrops());
        pickaxe("sand", 5, 1, 1.0, stoneDrops());
        pickaxe("netherrack", 4, 1, 1.0, stoneDrops());
        pickaxe("cobblestone", 20, 1, 1.0, stoneDrops());
        pickaxe("end_stone", 30, 1, 1.0, stoneDrops());
        pickaxe("ice", 10, 1, 1.0, stoneDrops());
        pickaxe("obsidian", 500, 4, 25.0, stoneDrops());

        // Ores (pickaxe)
        pickaxe("coal_ore", 30, 1, 5.0, drop("coal"));
        pickaxe("deepslate_coal_ore", 30, 1, 5.0, drop("coal"));
        pickaxe("iron_ore", 30, 2, 5.0, drop("iron"));
        pickaxe("deepslate_iron_ore", 30, 2, 5.0, drop("iron"));
        pickaxe("gold_ore", 30, 3, 6.0, drop("gold"));
        pickaxe("deepslate_gold_ore", 30, 3, 6.0, drop("gold"));
        pickaxe("diamond_ore", 30, 3, 8.0, drop("diamond"));
        pickaxe("deepslate_diamond_ore", 30, 3, 8.0, drop("diamond"));
        pickaxe("redstone_ore", 30, 3, 5.0, drop("redstone"));
        pickaxe("deepslate_redstone_ore", 30, 3, 5.0, drop("redstone"));
    }

    private static void pickaxe(String path, double strength, int power, double xp, DropTable drops) {
        register(new MineableBlock(Identifier.of("minecraft", path),
                MiningHandlers.pickaxe(strength, power), MiningBlockClassification.MINING, xp, drops));
    }

    private static DropTable stoneDrops() {
        return new DropTable(List.of(new DropEntry(Identifier.of("skyblock_life", "stone"), 1, 1, 1.0, true)));
    }

    private static DropTable drop(String itemPath) {
        return new DropTable(List.of(new DropEntry(Identifier.of("skyblock_life", itemPath), 1, 1, 1.0, true)));
    }

    public static void register(MineableBlock block) {
        BLOCKS.put(block.blockId(), block);
    }

    public static Optional<MineableBlock> get(Identifier id) {
        return Optional.ofNullable(id == null ? null : BLOCKS.get(id));
    }

    public static Collection<MineableBlock> all() {
        return Collections.unmodifiableCollection(BLOCKS.values());
    }
}
