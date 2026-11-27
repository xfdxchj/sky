package com.skyrpg.skyblock.island;

import com.skyrpg.skyblock.combat.MobDefinition;
import com.skyrpg.skyblock.combat.MobRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

/**
 * Small project-owned adventure fixture used to make the first combat quest playable
 * before the measured Hypixel Graveyard map slice is ready.
 */
public final class StarterGraveyardManager {
    private static final int CENTER_X = 256;
    private static final int CENTER_Z = 0;
    private static final int SURFACE_Y = 180;
    private static final String MARKER_TAG = "skyblockrpg_starter_graveyard_ready";

    private StarterGraveyardManager() {}

    public static void ensure(ServerWorld world) {
        BlockPos marker = new BlockPos(CENTER_X, SURFACE_Y + 1, CENTER_Z);
        if (!world.getBlockState(marker).isOf(Blocks.MOSSY_COBBLESTONE)) {
            IslandDefinition island = new IslandDefinition(
                    "starter_graveyard_fixture", "Starter Graveyard (Project Fixture)",
                    "minecraft:overworld", CENTER_X, CENTER_Z, SURFACE_Y,
                    30, 26, 16, CENTER_X + 0.5, SURFACE_Y + 7.0, CENTER_Z + 0.5,
                    "starter_graveyard", "starter_graveyard",
                    "minecraft:coarse_dirt", "minecraft:dirt", "minecraft:stone", true);
            IslandManager.GenerationResult generated = IslandManager.generate(world, island);
            if (!generated.success() || generated.placedBlocks() == 0) {
                return;
            }
            buildLandmarks(world);
            world.setBlockState(marker, Blocks.MOSSY_COBBLESTONE.getDefaultState(), Block.NOTIFY_ALL);
        }
        ensureZombies(world);
    }

    private static void buildLandmarks(ServerWorld world) {
        int[][] graves = {{-10, -5}, {-4, -8}, {4, -7}, {10, -3}, {-8, 7}, {7, 8}};
        for (int[] grave : graves) {
            BlockPos base = new BlockPos(CENTER_X + grave[0], SURFACE_Y + 2, CENTER_Z + grave[1]);
            place(world, base, Blocks.COBBLESTONE);
            place(world, base.up(), Blocks.MOSSY_COBBLESTONE);
            place(world, base.up(2), Blocks.STONE_BRICK_WALL);
        }
        for (int dx = -3; dx <= 3; dx++) {
            place(world, new BlockPos(CENTER_X + dx, SURFACE_Y + 2, CENTER_Z - 13), Blocks.COBBLESTONE);
        }
    }

    private static void ensureZombies(ServerWorld world) {
        MobDefinition definition = MobRegistry.getById("graveyard_zombie").orElse(null);
        if (definition == null) return;
        BlockBox area = new BlockBox(CENTER_X - 24, SURFACE_Y - 4, CENTER_Z - 20,
                CENTER_X + 24, SURFACE_Y + 16, CENTER_Z + 20);
        long existing = world.getEntitiesByClass(LivingEntity.class, area,
                entity -> entity.getCommandTags().contains(MobRegistry.entityTag(definition.id()))).size();
        int[][] spawn = {{-12, 0}, {0, 10}, {12, -2}};
        for (int i = (int) existing; i < spawn.length; i++) {
            Entity entity = EntityType.ZOMBIE.create(world);
            if (!(entity instanceof LivingEntity living)) continue;
            double x = CENTER_X + spawn[i][0] + 0.5;
            double z = CENTER_Z + spawn[i][1] + 0.5;
            entity.refreshPositionAndAngles(x, SURFACE_Y + 3.0, z, 0.0F, 0.0F);
            entity.addCommandTag(MobRegistry.entityTag(definition.id()));
            entity.setCustomName(Text.literal("[Lv" + definition.level() + "] " + definition.displayName()));
            entity.setCustomNameVisible(true);
            living.setHealth(living.getMaxHealth());
            world.spawnEntity(entity);
        }
    }

    private static void place(ServerWorld world, BlockPos pos, net.minecraft.block.Block block) {
        if (world.getWorldBorder().contains(pos) && world.getBlockState(pos).isAir())
            world.setBlockState(pos, block.getDefaultState(), Block.NOTIFY_ALL);
    }
}
