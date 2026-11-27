package com.skyrpg.life.farming;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of crop types, indexed by crop block.
 *
 * <p>Only vanilla crop blocks are registered; Garden-specific crops need world content
 * and are intentionally out of scope.</p>
 */
public final class FarmingRegistry {
    private static final Map<Identifier, CropDefinition> CROPS = new LinkedHashMap<>();
    private static final Map<Identifier, CropDefinition> BY_BLOCK = new HashMap<>();

    private FarmingRegistry() {}

    public static void clear() {
        CROPS.clear();
        BY_BLOCK.clear();
    }

    public static void add(CropDefinition crop) {
        register(crop);
    }

    public static void registerDefaults() {
        crop("wheat", "Wheat", "wheat");
        crop("carrot", "Carrot", "carrots");
        crop("potato", "Potato", "potatoes");
        crop("beetroot", "Beetroot", "beetroots");
    }

    private static void crop(String id, String displayName, String blockPath) {
        CropDefinition crop = new CropDefinition(Identifier.of("skyblock_life", "crop_" + id),
                displayName, Set.of(Identifier.of("minecraft", blockPath)),
                Identifier.of("skyblock_life", id), 4.0);
        register(crop);
    }

    private static void register(CropDefinition crop) {
        CROPS.put(crop.cropId(), crop);
        for (Identifier block : crop.blocks()) {
            BY_BLOCK.put(block, crop);
        }
    }

    public static Optional<CropDefinition> byBlock(Identifier blockId) {
        return Optional.ofNullable(blockId == null ? null : BY_BLOCK.get(blockId));
    }

    public static Collection<CropDefinition> all() {
        return Collections.unmodifiableCollection(CROPS.values());
    }
}
