package com.skyrpg.life.common.feedback;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;

/** Small feedback helpers shared by the four life skills. */
public final class LifeFeedback {
    private LifeFeedback() {}

    public static void blockSound(ServerWorld world, BlockPos pos, SoundEvent sound) {
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 0.5F, 1.0F);
    }
}
