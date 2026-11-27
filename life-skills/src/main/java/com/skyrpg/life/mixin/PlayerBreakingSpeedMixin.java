package com.skyrpg.life.mixin;

import com.skyrpg.life.mining.MineableBlock;
import com.skyrpg.life.mining.MiningRegistry;
import com.skyrpg.life.mining.MiningService;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces vanilla break speed for registered mineable blocks with SkyBlock-style
 * break time derived from the block handler's strength and the player's Mining Speed.
 *
 * <p>Only the server value is authoritative; the client keeps vanilla prediction.</p>
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerBreakingSpeedMixin {
    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void skyblocklife$customBreakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ServerPlayerEntity player)) {
            return;
        }
        MineableBlock block = MiningRegistry.get(Registries.BLOCK.getId(state.getBlock())).orElse(null);
        if (block == null) {
            return;
        }
        float hardness = state.getBlock().getHardness();
        if (hardness <= 0.0F) {
            return;
        }
        double breakTicks = MiningService.breakTicks(player, block);
        if (breakTicks <= 0.0) {
            return;
        }
        // Vanilla delta = speed / hardness / 30; choose speed so delta = 1 / breakTicks.
        cir.setReturnValue((float) (hardness * 30.0 / breakTicks));
    }
}
