package com.skyrpg.skyblock.mixin;

import com.skyrpg.skyblock.combat.MobDefinition;
import com.skyrpg.skyblock.combat.MobRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Applies configured SkyBlock mob damage when a registered mob hits a player. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float skyblockrpg$useSkyBlockMobDamage(float vanillaDamage, DamageSource source) {
        if (!((Object) this instanceof ServerPlayerEntity)) return vanillaDamage;
        MobDefinition mob = MobRegistry.getAttackingMob(source);
        if (mob.id().equals("unregistered") || mob.damage() <= 0.0) return vanillaDamage;
        return (float) Math.min(1_000_000_000.0, mob.damage());
    }
}