package com.skyrpg.skyblock.profile;

import com.skyrpg.skyblock.skill.SkillType;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** World-scoped persistent container for all player SkyBlock profiles. */
public final class SkyBlockWorldData extends PersistentState {
    public static final String STORAGE_ID = "skyblockrpg_profiles";
    public static final PersistentState.Type<SkyBlockWorldData> TYPE =
            new PersistentState.Type<>(SkyBlockWorldData::new, SkyBlockWorldData::fromNbt, DataFixTypes.LEVEL);

    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    public static SkyBlockWorldData get(net.minecraft.server.MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, STORAGE_ID);
    }

    public PlayerProfile getOrCreate(UUID playerId, String playerName) {
        PlayerProfile profile = profiles.get(playerId);
        if (profile == null) {
            profile = new PlayerProfile(playerId, playerName);
            profiles.put(playerId, profile);
            markDirty();
        } else if (playerName != null && !playerName.equals(profile.getLastKnownName())) {
            profile.setLastKnownName(playerName);
            markDirty();
        }
        return profile;
    }

    public PlayerProfile get(UUID playerId) { return profiles.get(playerId); }
    public Collection<PlayerProfile> getProfiles() { return profiles.values(); }
    public void markChanged() { markDirty(); }

    private static SkyBlockWorldData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        SkyBlockWorldData data = new SkyBlockWorldData();
        NbtList list = nbt.getList("profiles", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            try {
                UUID uuid = UUID.fromString(entry.getString("uuid"));
                PlayerProfile profile = new PlayerProfile(uuid, entry.getString("name"));
                profile.setProfileVersion(PlayerProfile.CURRENT_VERSION); // Older profile schemas migrate additively to the current schema.
                long coins = Math.max(0L, entry.getLong("coins"));
                if (coins > 0) profile.addCoins(coins);
                profile.setBankCoinsForLoad(Math.max(0L, entry.getLong("bankCoins")));
                NbtList discovered = entry.getList("discoveredRegions", NbtElement.STRING_TYPE);
                for (int j = 0; j < discovered.size(); j++) profile.discoverRegion(discovered.getString(j));

                NbtCompound skills = entry.getCompound("skills");
                for (String key : skills.getKeys()) {
                    SkillType.fromId(key).ifPresent(type -> {
                        double xp = skills.getDouble(key);
                        if (xp > 0 && Double.isFinite(xp)) profile.addSkillExperience(type, xp);
                    });
                }

                if (entry.contains("inventory", NbtElement.COMPOUND_TYPE)) {
                    profile.setInventory(com.skyrpg.skyblock.inventory.PlayerInventory.fromNbt(entry.getCompound("inventory")));
                }

                NbtCompound stats = entry.getCompound("stats");
                for (String key : stats.getKeys()) {
                    StatType.fromId(key).ifPresent(type -> {
                        double value = stats.getDouble(key);
                        if (Double.isFinite(value)) profile.getStats().setBase(type, value);
                    });
                }
                data.profiles.put(uuid, profile);
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed profile entries; other players' profiles remain loadable.
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (PlayerProfile profile : profiles.values()) {
            NbtCompound entry = new NbtCompound();
            entry.putString("uuid", profile.getPlayerId().toString());
            entry.putString("name", profile.getLastKnownName());
            entry.putInt("profileVersion", profile.getProfileVersion());
            entry.putLong("coins", profile.getCoins());
            entry.putLong("bankCoins", profile.getBankCoins());
            NbtList discovered = new NbtList();
            profile.getDiscoveredRegions().forEach(id -> discovered.add(NbtString.of(id)));
            entry.put("discoveredRegions", discovered);

            NbtCompound skills = new NbtCompound();
            profile.getSkillExperience().forEach((type, xp) -> skills.putDouble(type.id(), xp));
            entry.put("skills", skills);

            NbtCompound stats = new NbtCompound();
            profile.getStats().getBaseValues().forEach((type, value) -> stats.putDouble(type.id(), value));
            entry.put("stats", stats);
            entry.put("inventory", profile.getInventory().toNbt());

            list.add(entry);
        }
        nbt.put("profiles", list);
        nbt.putInt("dataVersion", 4);
        return nbt;
    }
}
