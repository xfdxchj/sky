package com.skyrpg.skyblock.mixin;

import com.skyrpg.skyblock.combat.CombatManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Server-side interception point for vanilla melee attack requests. */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void skyblockrpg$handleSkyBlockMeleeAttack(Entity target, CallbackInfo ci) {
        // The client-side PlayerEntity is a ClientPlayerEntity, not a ServerPlayerEntity.
        if (!((Object) this instanceof ServerPlayerEntity player)) return;
        if (!(target instanceof LivingEntity livingTarget)) return;
        if (!CombatManager.shouldHandleAttack(player, livingTarget)) return;

        CombatManager.attack(player, (ServerWorld) player.getWorld(), livingTarget);
        // Do not also apply vanilla weapon damage after the SkyBlock damage pipeline.
        ci.cancel();
    }
}
