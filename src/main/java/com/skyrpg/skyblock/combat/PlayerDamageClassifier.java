package com.skyrpg.skyblock.combat;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;

/**
 * Maps vanilla damage sources to the Core RPG damage semantics.
 *
 * Minecraft armor-bypass tags are deliberately not treated as Hypixel True
 * Damage. Until a source explicitly declares a SkyBlock damage kind, it stays
 * NORMAL. Fatal administrative / out-of-world sources bypass RPG Defense.
 */
public final class PlayerDamageClassifier {
    private PlayerDamageClassifier() {}

    public static DamageKind classify(DamageSource source) {
        if (source == null) return DamageKind.NORMAL;

        if (source.isOf(DamageTypes.OUT_OF_WORLD)
                || source.isOf(DamageTypes.GENERIC_KILL)) {
            return DamageKind.BYPASS_RPG_DEFENSE;
        }

        return DamageKind.NORMAL;
    }
}
