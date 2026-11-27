package com.skyrpg.skyblock.combat;

import com.skyrpg.skyblock.inventory.EquipmentSlot;
import com.skyrpg.skyblock.item.ItemDefinition;
import com.skyrpg.skyblock.item.ItemRegistry;
import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.skill.SkillType;
import com.skyrpg.skyblock.quest.QuestManager;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/** Server-authoritative SkyBlock melee combat. */
public final class CombatManager {
    private static final float MAX_MINECRAFT_DAMAGE = 1_000_000_000.0F;

    private CombatManager() {}

    public static boolean shouldHandleAttack(ServerPlayerEntity player, LivingEntity target) {
        if (player.isSpectator() || target instanceof PlayerEntity || !target.isAlive()) return false;
        PlayerProfile profile = ProfileManager.get(player);
        String weaponId = profile.getInventory().getEquipment().get(EquipmentSlot.WEAPON);
        if (weaponId == null) return false;
        ItemDefinition weapon = ItemRegistry.get(weaponId).orElse(null);
        return weapon != null && weapon.isEquippable()
                && weapon.equipmentSlot() == EquipmentSlot.WEAPON;
    }

    public static boolean attack(ServerPlayerEntity player, ServerWorld world, LivingEntity target) {
        if (!shouldHandleAttack(player, target)) return false;
        PlayerProfile profile = ProfileManager.get(player);
        String weaponId = profile.getInventory().getEquipment().get(EquipmentSlot.WEAPON);
        ItemDefinition weapon = ItemRegistry.get(weaponId).orElse(null);
        if (weapon == null) return false;

        double weaponDamage = profile.getFinalStat(StatType.DAMAGE);
        double critChance = CombatMath.critChance(profile.getFinalStat(StatType.CRIT_CHANCE));
        boolean critical = world.getRandom().nextDouble() * 100.0 < critChance;
        MobDefinition mob = MobRegistry.get(target);
        double calculatedDamage = CombatMath.calculateDamage(weaponDamage,
                profile.getFinalStat(StatType.STRENGTH), profile.getFinalStat(StatType.CRIT_DAMAGE),
                mob.defense(), critical);
        double safeDamage = Math.min(calculatedDamage, MAX_MINECRAFT_DAMAGE);

        if (safeDamage <= 0.0) {
            player.sendMessage(Text.literal("Your equipped weapon has no effective damage."), true);
            return true;
        }

        boolean customHealth = !mob.id().equals("unregistered") && mob.health() > 0.0;
        SkyBlockMobHealthData hpData = customHealth ? SkyBlockMobHealthData.get(world) : null;
        double hpBefore = customHealth ? hpData.getOrCreate(target, mob) : target.getHealth();
        float vanillaMax = Math.max(1.0F, target.getMaxHealth());

        // Keep Minecraft's visible health bar proportional to the authoritative SkyBlock HP pool.
        if (customHealth) target.setHealth((float) (vanillaMax * hpBefore / mob.health()));

        double requested = customHealth ? Math.min(safeDamage, hpBefore) : safeDamage;
        float vanillaDamage = (float) Math.min(MAX_MINECRAFT_DAMAGE,
                customHealth ? requested * vanillaMax / mob.health() : requested);
        float vanillaBefore = target.getHealth();
        boolean applied = target.damage(player.getDamageSources().playerAttack(player), vanillaDamage);
        if (!applied) {
            player.sendMessage(Text.literal("The target resisted the attack."), true);
            return true;
        }

        double hpAfter;
        if (customHealth) {
            // Convert the damage actually accepted by vanilla's damage pipeline back into SkyBlock HP.
            double vanillaLoss = Math.max(0.0, vanillaBefore - Math.max(0.0F, target.getHealth()));
            double acceptedSkyBlockDamage = vanillaLoss / vanillaMax * mob.health();
            hpAfter = Math.max(0.0, hpBefore - acceptedSkyBlockDamage);
            if (target.isDead() || hpAfter <= 0.0001) {
                hpAfter = 0.0;
                hpData.setCurrent(target, mob, 0.0);
                target.kill();
            } else {
                hpData.setCurrent(target, mob, hpAfter);
                target.setHealth((float) (vanillaMax * hpAfter / mob.health()));
            }
        } else {
            hpAfter = Math.max(0.0, target.getHealth());
        }

        double dealt = Math.max(0.0, hpBefore - hpAfter);
        player.sendMessage(Text.literal(String.format("%s: %.1f damage%s | HP: %.1f/%.1f",
                mob.displayName(), dealt, critical ? " CRITICAL!" : "", hpAfter,
                customHealth ? mob.health() : vanillaMax)), true);

        if (customHealth && hpAfter <= 0.0) {
            hpData.remove(target);
        }
        if (hpAfter <= 0.0 || !target.isAlive()) {
            if (mob.coinReward() > 0) profile.addCoins(mob.coinReward());
            if (mob.combatXpReward() > 0) profile.addSkillExperience(SkillType.COMBAT, mob.combatXpReward());
            if (!mob.id().equals("unregistered")) QuestManager.onMobKilled(player, mob.id());
            ProfileManager.markChanged(player);
            if (mob.coinReward() > 0 || mob.combatXpReward() > 0) {
                player.sendMessage(Text.literal("Defeated " + mob.displayName() + "! +" + mob.coinReward()
                        + " coins, +" + mob.combatXpReward() + " Combat XP."), false);
            } else {
                player.sendMessage(Text.literal("Defeated " + mob.displayName() + ". No configured rewards."), false);
            }
        }
        return true;
    }
}