package com.skyrpg.life.foraging;

import com.skyrpg.life.SkyBlockLife;
import com.skyrpg.life.common.data.LifeDataStore;
import com.skyrpg.life.common.scheduler.LifeScheduler;
import com.skyrpg.life.skill.LifeSkills;
import com.skyrpg.skyblock.api.RewardAPI;
import com.skyrpg.skyblock.api.SkillAPI;
import com.skyrpg.skyblock.api.StatAPI;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracks and fells whole trees, decays leaves and regenerates the tree after a delay.
 *
 * <p>Simplified from the reference server implementation: no holograms or per-player
 * contribution tracking, but tree discovery, whole-tree felling, leaf decay and
 * timed regeneration are implemented.</p>
 */
public final class ForagingTreeManager {
    private static final int MAX_TREE_BLOCKS = 512;
    private static final int LEAF_SEARCH_DISTANCE = 4;
    private static final int REGEN_DELAY_TICKS = 20 * 30;
    private static final int[][] NEIGHBORS = neighbors();
    private static final Map<String, TrackedTree> TREES = new HashMap<>();

    private ForagingTreeManager() {}

    public static void breakLogs(ServerPlayerEntity player, ServerWorld world, BlockPos start, TreeDefinition tree) {
        String key = world.getRegistryKey().getValue() + "|" + tree.treeId() + "|" + start.toShortString();
        TrackedTree tracked = TREES.computeIfAbsent(key, ignored -> discover(world, start, tree));
        if (tracked == null) {
            return;
        }

        int sweep = 1 + fortuneBonus(player);
        List<BlockPos> targets = tracked.remainingLogs.stream().limit(sweep).toList();
        int felled = 0;
        for (BlockPos pos : targets) {
            if (!world.getBlockState(pos).isAir()) {
                world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState());
                felled++;
            }
            tracked.remainingLogs.remove(pos);
        }
        if (felled == 0) {
            return;
        }

        int remaining = tracked.remainingLogs.size();
        player.sendMessage(net.minecraft.text.Text.literal("Tree: " + remaining + " logs left"), true);

        for (int i = 0; i < felled; i++) {
            RewardAPI.giveItem(player, tree.dropItem(), 1);
        }
        LifeDataStore.incrementLong(player, LifeSkills.FORAGING, "logs_collected", felled);
        if (tree.baseXp() > 0) {
            SkillAPI.addExperience(player, LifeSkills.FORAGING, tree.baseXp() * felled);
        }

        if (tracked.remainingLogs.isEmpty()) {
            decayLeaves(world, tracked);
            awardTreeGift(world, player, tree);
            scheduleRegeneration(world, tracked);
            TREES.remove(key);
        }
    }

    private static TrackedTree discover(ServerWorld world, BlockPos start, TreeDefinition tree) {
        Map<BlockPos, BlockState> logs = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty() && logs.size() < MAX_TREE_BLOCKS) {
            BlockPos current = queue.removeFirst();
            if (logs.containsKey(current)) {
                continue;
            }
            BlockState state = world.getBlockState(current);
            Identifier blockId = Registries.BLOCK.getId(state.getBlock());
            if (!tree.logBlocks().contains(blockId)) {
                continue;
            }
            logs.put(current, state);
            for (int[] offset : NEIGHBORS) {
                queue.addLast(current.add(offset[0], offset[1], offset[2]));
            }
        }
        if (logs.isEmpty()) {
            return null;
        }

        Set<BlockPos> leaves = new HashSet<>();
        for (BlockPos log : logs.keySet()) {
            for (int x = -LEAF_SEARCH_DISTANCE; x <= LEAF_SEARCH_DISTANCE; x++) {
                for (int y = -LEAF_SEARCH_DISTANCE; y <= LEAF_SEARCH_DISTANCE; y++) {
                    for (int z = -LEAF_SEARCH_DISTANCE; z <= LEAF_SEARCH_DISTANCE; z++) {
                        BlockPos candidate = log.add(x, y, z);
                        if (isLeaf(world.getBlockState(candidate))) {
                            leaves.add(candidate);
                        }
                    }
                }
            }
        }
        TrackedTree tracked = new TrackedTree(new ArrayList<>(logs.keySet()), leaves);
        tracked.originalStates.putAll(logs);
        for (BlockPos leaf : leaves) {
            tracked.originalStates.put(leaf, world.getBlockState(leaf));
        }
        return tracked;
    }

    private static void decayLeaves(ServerWorld world, TrackedTree tree) {
        int index = 0;
        for (BlockPos leaf : tree.leaves) {
            int delay = index / 4 + 1;
            index++;
            LifeScheduler.schedule(() -> {
                if (isLeaf(world.getBlockState(leaf))) {
                    world.setBlockState(leaf, net.minecraft.block.Blocks.AIR.getDefaultState());
                }
            }, delay);
        }
    }

    private static void scheduleRegeneration(ServerWorld world, TrackedTree tree) {
        LifeScheduler.schedule(() -> {
            BlockPos first = null;
            for (Map.Entry<BlockPos, BlockState> entry : tree.originalStates.entrySet()) {
                if (world.getBlockState(entry.getKey()).isAir()) {
                    world.setBlockState(entry.getKey(), entry.getValue());
                    if (first == null) {
                        first = entry.getKey();
                    }
                }
            }
            if (first != null) {
                world.playSound(null, first, SoundEvents.BLOCK_WOOD_PLACE,
                        net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 0.75F);
            }
        }, REGEN_DELAY_TICKS);
    }

    private static void awardTreeGift(ServerWorld world, ServerPlayerEntity player, TreeDefinition tree) {
        RewardAPI.giveItem(player, tree.dropItem(), 8);
        SkillAPI.addExperience(player, LifeSkills.FORAGING, tree.baseXp() * 20);
        // Reference uses entity.creaking.death, which does not exist in 1.21.1; wither death is the closest available sound.
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_DEATH,
                net.minecraft.sound.SoundCategory.BLOCKS, 0.8F, 0.7F);
        player.sendMessage(net.minecraft.text.Text.literal("You chopped down a " + tree.displayName()
                + " tree! Tree Gift awarded."), false);
    }

    private static boolean isLeaf(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        return id != null && id.getPath().endsWith("_leaves");
    }

    // Placeholder formula: every 100 Foraging Fortune adds one extra log per swing. Not Hypixel data.
    private static int fortuneBonus(ServerPlayerEntity player) {
        double fortune = StatAPI.getFinal(player, StatType.FORAGING_FORTUNE);
        return (int) Math.max(0, Math.floor(fortune / 100.0));
    }

    private static int[][] neighbors() {
        List<int[]> offsets = new ArrayList<>(26);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        offsets.add(new int[]{x, y, z});
                    }
                }
            }
        }
        return offsets.toArray(int[][]::new);
    }

    private static final class TrackedTree {
        private final List<BlockPos> remainingLogs;
        private final Set<BlockPos> leaves;
        private final Map<BlockPos, BlockState> originalStates;

        private TrackedTree(List<BlockPos> logs, Set<BlockPos> leaves) {
            this.remainingLogs = new ArrayList<>(logs);
            this.leaves = leaves;
            this.originalStates = new HashMap<>();
        }
    }
}
