package com.skyrpg.life.foraging;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of tree types, indexed by log block.
 *
 * <p>Only vanilla log families are registered; special trees (fig, mangrove...) need map content
 * and are intentionally out of scope.</p>
 */
public final class ForagingRegistry {
    private static final Map<Identifier, TreeDefinition> TREES = new LinkedHashMap<>();
    private static final Map<Identifier, TreeDefinition> BY_LOG = new HashMap<>();

    private ForagingRegistry() {}

    public static void clear() {
        TREES.clear();
        BY_LOG.clear();
    }

    public static void add(TreeDefinition tree) {
        register(tree);
    }

    public static void registerDefaults() {
        log("oak");
        log("birch");
        log("spruce");
        log("jungle");
        log("acacia");
        log("dark_oak");
    }

    private static void log(String wood) {
        Identifier drop = Identifier.of("skyblock_life", wood + "_log");
        TreeDefinition tree = new TreeDefinition(Identifier.of("skyblock_life", "tree_" + wood),
                titleCase(wood), Set.of(Identifier.of("minecraft", wood + "_log")), drop, 5.0);
        register(tree);
    }

    private static void register(TreeDefinition tree) {
        TREES.put(tree.treeId(), tree);
        for (Identifier logBlock : tree.logBlocks()) {
            BY_LOG.put(logBlock, tree);
        }
    }

    public static Optional<TreeDefinition> byLog(Identifier blockId) {
        return Optional.ofNullable(blockId == null ? null : BY_LOG.get(blockId));
    }

    public static Collection<TreeDefinition> all() {
        return Collections.unmodifiableCollection(TREES.values());
    }

    private static String titleCase(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).replace('_', ' ');
    }
}
