package com.skyrpg.life.mixin;

import com.skyrpg.life.fishing.FishingService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Awards a SkyBlock fishing catch when the vanilla bobber successfully reels in
 * something (the vanilla use() return value is the experience amount).
 */
@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberMixin {
    @Inject(method = "use", at = @At("RETURN"))
    private void skyblocklife$onCatch(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() <= 0) {
            return;
        }
        FishingBobberEntity bobber = (FishingBobberEntity) (Object) this;
        PlayerEntity owner = bobber.getPlayerOwner();
        if (!(owner instanceof ServerPlayerEntity player)) {
            return;
        }
        FishingService.onCatch(player, bobber.getWorld().getRandom());
    }
}
