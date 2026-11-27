package com.skyrpg.skyblock.island;

import com.skyrpg.skyblock.npc.NpcManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Project-owned reconstruction of the documented first Private Island layout.
 * Terrain and decorative landmarks are generated separately from gameplay entities/data.
 */
public final class PrivateIslandStarterLayout {
    public record Result(boolean success, String message, int placedBlocks) {}
    private PrivateIslandStarterLayout() {}

    public static Result generate(ServerWorld world, int centerX, int centerZ, int surfaceY) {
        int mainX = centerX - 15;
        int sideX = centerX + 15;
        IslandDefinition main = island("private_starter_main", mainX, centerZ, surfaceY, 16, 16,
                mainX + 0.5, surfaceY + 6.0, centerZ + 0.5);
        IslandDefinition side = island("private_starter_side", sideX, centerZ, surfaceY, 14, 14,
                sideX + 0.5, surfaceY + 6.0, centerZ + 0.5);

        IslandManager.GenerationResult first = IslandManager.generate(world, main);
        if (!first.success() || first.placedBlocks() == 0)
            return new Result(false, "Main island: " + first.message(), first.placedBlocks());
        IslandManager.GenerationResult second = IslandManager.generate(world, side);
        if (!second.success() || second.placedBlocks() == 0)
            return new Result(false, "Side island: " + second.message(), first.placedBlocks() + second.placedBlocks());

        int placed = first.placedBlocks() + second.placedBlocks();
        placed += buildOakTree(world, new BlockPos(mainX, surfaceY + 1, centerZ - 4));
        placed += buildBridge(world, mainX + 14, centerZ, sideX - 12, surfaceY);
        placed += carveCaveAndChest(world, sideX, centerZ, surfaceY);
        placed += buildPortalLandmark(world, sideX, centerZ, surfaceY);
        placed += buildMinionDisplay(world, sideX, centerZ, surfaceY);
        spawnJerry(world, sideX + 2.5, surfaceY + 1.0, centerZ - 4.5);
        return new Result(true, "Generated the two-island starter layout.", placed);
    }

    private static IslandDefinition island(String id, int x, int z, int y, int rx, int rz,
                                           double spawnX, double spawnY, double spawnZ) {
        return new IslandDefinition(id, "Private Island", "minecraft:overworld",
                x, z, y, rx, rz, 18, spawnX, spawnY, spawnZ, "", "",
                "minecraft:grass_block", "minecraft:dirt", "minecraft:stone", false);
    }

    private static int buildOakTree(ServerWorld world, BlockPos base) {
        int placed = 0;
        for (int y = 0; y < 5; y++) placed += placeIfAir(world, base.add(0, y, 0), Blocks.OAK_LOG.getDefaultState());
        for (int y = 3; y <= 5; y++) {
            int radius = y == 5 ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && y != 4) continue;
                    BlockPos pos = base.add(dx, y, dz);
                    if (world.getBlockState(pos).isAir())
                        placed += placeIfAir(world, pos, Blocks.OAK_LEAVES.getDefaultState());
                }
            }
        }
        return placed;
    }

    private static int buildBridge(ServerWorld world, int fromX, int z, int toX, int y) {
        int placed = 0;
        int min = Math.min(fromX, toX);
        int max = Math.max(fromX, toX);
        for (int x = min; x <= max; x++) {
            for (int dz = -1; dz <= 1; dz++) {
                placed += placeIfAir(world, new BlockPos(x, y, z + dz), Blocks.STONE_BRICKS.getDefaultState());
            }
        }
        return placed;
    }

    private static int carveCaveAndChest(ServerWorld world, int x, int z, int surfaceY) {
        int placed = 0;
        // A compact cave pocket in the lower front face of the second island.
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 1; dy++) {
                for (int dz = 5; dz <= 12; dz++) {
                    if ((dx * dx) / 10.0 + (dy * dy) / 5.0 + ((dz - 8.0) * (dz - 8.0)) / 24.0 <= 1.35) {
                        BlockPos pos = new BlockPos(x + dx, surfaceY - 7 + dy, z + dz);
                        if (!world.getBlockState(pos).isAir()) {
                            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                            placed++;
                        }
                    }
                }
            }
        }
        BlockPos torch = new BlockPos(x - 2, surfaceY - 5, z + 8);
        placed += placeIfAir(world, torch, Blocks.TORCH.getDefaultState());
        BlockPos chestPos = new BlockPos(x, surfaceY - 6, z + 9);
        if (world.getBlockState(chestPos).isAir()) {
            world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_ALL);
            BlockEntity blockEntity = world.getBlockEntity(chestPos);
            if (blockEntity instanceof ChestBlockEntity chest) {
                List<ItemStack> contents = List.of(
                        new ItemStack(Items.LAVA_BUCKET),
                        new ItemStack(Items.WATER_BUCKET),
                        new ItemStack(Items.BONE_MEAL, 3),
                        new ItemStack(Items.DIRT, 5),
                        new ItemStack(Items.GRASS_BLOCK, 7),
                        new ItemStack(Items.COBBLESTONE, 8)
                );
                for (int i = 0; i < contents.size(); i++) chest.setStack(i, contents.get(i));
                chest.markDirty();
                placed++;
            }
        }
        return placed;
    }

    private static int buildPortalLandmark(ServerWorld world, int x, int z, int y) {
        int placed = 0;
        // Inactive decorative portal: the real Hub destination is added with the Hub world slice.
        for (int dy = 0; dy <= 4; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                boolean frame = dx == -2 || dx == 2 || dy == 0 || dy == 4;
                BlockPos pos = new BlockPos(x + dx, y + 1 + dy, z - 10);
                if (frame) placed += placeIfAir(world, pos, Blocks.OBSIDIAN.getDefaultState());
                else placed += placeIfAir(world, pos, Blocks.PURPLE_STAINED_GLASS.getDefaultState());
            }
        }
        return placed;
    }

    private static int buildMinionDisplay(ServerWorld world, int x, int z, int y) {
        int placed = 0;
        // Visual-only pedestal until the Core Minion gameplay system exists.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                placed += placeIfAir(world, new BlockPos(x + dx, y + 1, z + 10 + dz),
                        Blocks.STONE_BRICKS.getDefaultState());
            }
        }
        placed += placeIfAir(world, new BlockPos(x, y + 2, z + 10), Blocks.COBBLESTONE.getDefaultState());
        placed += placeIfAir(world, new BlockPos(x, y + 3, z + 10), Blocks.COBBLESTONE.getDefaultState());
        return placed;
    }

    private static void spawnJerry(ServerWorld world, double x, double y, double z) {
        BlockPos pos = BlockPos.ofFloored(x, y, z);
        for (Entity entity : world.getOtherEntities(null, new net.minecraft.util.math.Box(
                pos.getX() - 2, pos.getY() - 2, pos.getZ() - 2,
                pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2))) {
            if (entity.getCommandTags().contains(NpcManager.entityTag("jerry"))) return;
        }
        Entity entity = net.minecraft.entity.EntityType.VILLAGER.create(world);
        if (!(entity instanceof MobEntity mob)) return;
        entity.refreshPositionAndAngles(x, y, z, 180.0F, 0.0F);
        entity.addCommandTag(NpcManager.entityTag("jerry"));
        entity.setCustomName(Text.literal("Jerry"));
        entity.setCustomNameVisible(true);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        mob.setAiDisabled(true);
        world.spawnEntity(entity);
    }

    private static int placeIfAir(ServerWorld world, BlockPos pos, BlockState state) {
        if (!world.getWorldBorder().contains(pos) || !world.getBlockState(pos).isAir()) return 0;
        return world.setBlockState(pos, state, Block.NOTIFY_ALL) ? 1 : 0;
    }
}
