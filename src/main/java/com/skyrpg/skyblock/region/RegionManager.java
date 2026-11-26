package com.skyrpg.skyblock.region;

import com.skyrpg.skyblock.profile.ProfileManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Server-side region lookup and first-discovery tracking. */
public final class RegionManager {
    private static final Map<UUID, String> LAST_REGION = new HashMap<>();
    private RegionManager() {}

    public static RegionDefinition getCurrent(ServerPlayerEntity player) {
        String dimension = player.getServerWorld().getRegistryKey().getValue().toString();
        return RegionRegistry.find(dimension, player.getX(), player.getY(), player.getZ()).orElse(null);
    }

    public static void tick(ServerPlayerEntity player) {
        RegionDefinition current = getCurrent(player);
        String currentId = current == null ? "" : current.id();
        String previousId = LAST_REGION.put(player.getUuid(), currentId);
        if (Objects.equals(previousId, currentId) || current == null) return;

        boolean firstDiscovery = ProfileManager.get(player).discoverRegion(current.id());
        if (firstDiscovery) ProfileManager.markChanged(player);
        player.sendMessage(Text.literal("Region: " + current.displayName()
                + (firstDiscovery ? " (Discovered)" : "")), true);
    }

    public static void clear() {
        LAST_REGION.clear();
    }
}
