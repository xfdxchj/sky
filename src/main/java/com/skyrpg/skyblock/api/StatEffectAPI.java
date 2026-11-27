package com.skyrpg.skyblock.api;

import com.skyrpg.skyblock.stats.StatEffectManager;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatSource;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Public, namespaced API for extension-owned temporary stat effects.
 *
 * The effect ID and source ID must share a non-Core namespace. Reusing an
 * effect ID refreshes that extension's effect; a different ID creates a
 * separate contribution. All writes are server-authoritative.
 */
public final class StatEffectAPI {
    public static final int API_VERSION = 1;
    private static final String CORE_NAMESPACE = "skyblockrpg";

    private StatEffectAPI() {}

    /**
     * Applies or refreshes an extension-owned effect.
     *
     * @param effectId stable namespaced effect ID, e.g. skyblock_life:well_fed
     * @param sourceId namespaced source ID, e.g. skyblock_life:farming_food
     * @param durationTicks positive Overworld game ticks, or -1 for permanent
     */
    public static boolean apply(ServerPlayerEntity player, Identifier effectId, Identifier sourceId,
                                StatSource source, StatType stat, double amount,
                                StatModifier.Operation operation, long durationTicks) {
        if (!isExtensionId(effectId) || !isExtensionId(sourceId)
                || !effectId.getNamespace().equals(sourceId.getNamespace())) {
            return false;
        }
        if (source != StatSource.BUFF && source != StatSource.TEMPORARY && source != StatSource.OTHER) {
            return false;
        }
        return StatEffectManager.apply(player, effectId.toString(), sourceId.toString(),
                source, stat, amount, operation, durationTicks);
    }

    /** Removes only the effect addressed by its full namespaced ID. */
    public static boolean remove(ServerPlayerEntity player, String ownerNamespace, Identifier effectId) {
        if (ownerNamespace == null || ownerNamespace.isBlank() || CORE_NAMESPACE.equals(ownerNamespace)
                || !isExtensionId(effectId) || !ownerNamespace.equals(effectId.getNamespace())) return false;
        return StatEffectManager.remove(player, effectId.toString());
    }

    private static boolean isExtensionId(Identifier id) {
        return id != null && !CORE_NAMESPACE.equals(id.getNamespace());
    }
}
