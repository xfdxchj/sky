package com.skyrpg.skyblock.profile;

import net.minecraft.util.Identifier;
import com.skyrpg.skyblock.stats.StatType;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatSource;
import com.skyrpg.skyblock.stats.StatEffect;
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

                NbtList activeQuests = entry.getList("activeQuests", NbtElement.COMPOUND_TYPE);
                for (int j = 0; j < activeQuests.size(); j++) {
                    NbtCompound quest = activeQuests.getCompound(j);
                    profile.loadQuestProgress(quest.getString("id"), quest.getInt("progress"));
                }
                NbtList completedQuests = entry.getList("completedQuests", NbtElement.STRING_TYPE);
                for (int j = 0; j < completedQuests.size(); j++) profile.loadCompletedQuest(completedQuests.getString(j));

                NbtCompound skills = entry.getCompound("skills");
                for (String key : skills.getKeys()) {
                    Identifier skillId = parseStoredIdentifier(key, "skyblockrpg");
                    if (skillId != null) {
                        double xp = skills.getDouble(key);
                        if (xp >= 0 && Double.isFinite(xp)) profile.loadSkillExperience(skillId, xp);
                    }
                }

                NbtCompound moduleData = entry.getCompound("moduleData");
                for (String key : moduleData.getKeys()) {
                    Identifier dataId = Identifier.tryParse(key);
                    if (dataId != null && moduleData.contains(key, NbtElement.COMPOUND_TYPE)) {
                        profile.setModuleData(dataId, moduleData.getCompound(key));
                    }
                }

                if (entry.contains("inventory", NbtElement.COMPOUND_TYPE)) {
                    profile.setInventory(com.skyrpg.skyblock.inventory.PlayerInventory.fromNbt(entry.getCompound("inventory")));
                }
                NbtList unlockedPowers = entry.getList("unlockedPowers", NbtElement.STRING_TYPE);
                java.util.Set<String> unlocked = new java.util.LinkedHashSet<>();
                for (int j = 0; j < unlockedPowers.size(); j++) unlocked.add(unlockedPowers.getString(j));
                profile.loadPowerData(entry.getString("selectedPower"), unlocked);
                NbtCompound tuning = entry.getCompound("tuningAllocations");
                Map<StatType, Integer> tuningValues = new java.util.EnumMap<>(StatType.class);
                for (String key : tuning.getKeys()) StatType.fromId(key).ifPresent(type -> {
                    int points = tuning.getInt(key);
                    if (points > 0) tuningValues.put(type, points);
                });
                profile.loadTuningAllocations(tuningValues);

                NbtList statEffects = entry.getList("statEffects", NbtElement.COMPOUND_TYPE);
                for (int j = 0; j < statEffects.size(); j++) {
                    NbtCompound effectTag = statEffects.getCompound(j);
                    try {
                        String effectId = effectTag.getString("id");
                        String sourceId = effectTag.getString("sourceId");
                        StatType stat = StatType.fromId(effectTag.getString("stat")).orElse(null);
                        double amount = effectTag.getDouble("amount");
                        StatModifier.Operation operation = StatModifier.Operation.valueOf(effectTag.getString("operation"));
                        StatSource source = StatSource.valueOf(effectTag.getString("source"));
                        long expiresAt = effectTag.contains("expiresAt") ? effectTag.getLong("expiresAt") : -1L;
                        if (stat != null && Double.isFinite(amount)
                                && (source == StatSource.BUFF || source == StatSource.TEMPORARY || source == StatSource.OTHER)) {
                            profile.putStatEffect(new StatEffect(effectId,
                                    new StatModifier(sourceId, stat, amount, operation, source), expiresAt));
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Ignore malformed effects without invalidating the rest of this player profile.
                    }
                }

                NbtCompound stats = entry.getCompound("stats");
                for (String key : stats.getKeys()) {
                    StatType.fromId(key).ifPresent(type -> {
                        double value = stats.getDouble(key);
                        if (Double.isFinite(value)) profile.getStats().setBase(type, value);
                    });
                }
                if (entry.contains("currentHealth", NbtElement.NUMBER_TYPE)) {
                    profile.loadCurrentHealth(entry.getDouble("currentHealth"));
                } else {
                    // Schema <= 9 profiles predate persistent RPG HP; migrate them at full derived health.
                    profile.initializeCurrentHealthIfMissing();
                }
                profile.setProfileVersion(PlayerProfile.CURRENT_VERSION);
                data.profiles.put(uuid, profile);
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed profile entries; other players' profiles remain loadable.
            }
        }
        return data;
    }

    private static Identifier parseStoredIdentifier(String raw, String legacyNamespace) {
        if (raw == null || raw.isBlank()) return null;
        return Identifier.tryParse(raw.contains(":") ? raw : legacyNamespace + ":" + raw);
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
            entry.putDouble("currentHealth", profile.getCurrentHealth());
            NbtList discovered = new NbtList();
            profile.getDiscoveredRegions().forEach(id -> discovered.add(NbtString.of(id)));
            entry.put("discoveredRegions", discovered);

            NbtList activeQuests = new NbtList();
            profile.getActiveQuests().forEach((id, progress) -> {
                NbtCompound quest = new NbtCompound();
                quest.putString("id", id);
                quest.putInt("progress", progress);
                activeQuests.add(quest);
            });
            entry.put("activeQuests", activeQuests);
            NbtList completedQuests = new NbtList();
            profile.getCompletedQuests().forEach(id -> completedQuests.add(NbtString.of(id)));
            entry.put("completedQuests", completedQuests);

            NbtCompound skills = new NbtCompound();
            profile.getSkillExperience().forEach((skillId, xp) -> skills.putDouble(skillId.toString(), xp));
            entry.put("skills", skills);

            NbtCompound moduleData = new NbtCompound();
            profile.getModuleDataSnapshot().forEach((dataId, value) -> moduleData.put(dataId.toString(), value.copy()));
            entry.put("moduleData", moduleData);

            NbtCompound stats = new NbtCompound();
            profile.getStats().getBaseValues().forEach((type, value) -> stats.putDouble(type.id(), value));
            entry.put("stats", stats);
            NbtList statEffects = new NbtList();
            for (StatEffect effect : profile.getStatEffects()) {
                NbtCompound effectTag = new NbtCompound();
                effectTag.putString("id", effect.id());
                effectTag.putString("sourceId", effect.modifier().sourceId());
                effectTag.putString("stat", effect.modifier().stat().id());
                effectTag.putDouble("amount", effect.modifier().amount());
                effectTag.putString("operation", effect.modifier().operation().name());
                effectTag.putString("source", effect.modifier().source().name());
                effectTag.putLong("expiresAt", effect.expiresAtGameTime());
                statEffects.add(effectTag);
            }
            entry.put("statEffects", statEffects);
            entry.put("inventory", profile.getInventory().toNbt());
            entry.putString("selectedPower", profile.getSelectedPower());
            NbtList unlockedPowers = new NbtList();
            profile.getUnlockedPowers().forEach(id -> unlockedPowers.add(NbtString.of(id)));
            entry.put("unlockedPowers", unlockedPowers);
            NbtCompound tuning = new NbtCompound();
            profile.getTuningAllocations().forEach((type, points) -> tuning.putInt(type.id(), points));
            entry.put("tuningAllocations", tuning);

            list.add(entry);
        }
        nbt.put("profiles", list);
        nbt.putInt("dataVersion", 10);
        return nbt;
    }
}
