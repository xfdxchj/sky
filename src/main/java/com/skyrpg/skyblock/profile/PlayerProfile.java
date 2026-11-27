package com.skyrpg.skyblock.profile;

import com.skyrpg.skyblock.skill.SkillType;
import net.minecraft.util.Identifier;
import net.minecraft.nbt.NbtCompound;
import com.skyrpg.skyblock.stats.PlayerStats;
import com.skyrpg.skyblock.inventory.PlayerInventory;
import com.skyrpg.skyblock.stats.StatType;
import com.skyrpg.skyblock.accessory.AccessoryPowerManager;
import com.skyrpg.skyblock.accessory.PowerRegistry;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatEffect;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashMap;

/** Persistent SkyBlock profile data for one Minecraft player. */
public final class PlayerProfile {
    public static final int CURRENT_VERSION = 9;

    private final UUID playerId;
    private String lastKnownName;
    private long coins;
    private long bankCoins;
    private final PlayerStats stats;
    private PlayerInventory inventory;
    private final Map<Identifier, Double> skillExperience;
    private final Map<Identifier, NbtCompound> moduleData = new LinkedHashMap<>();
    private int profileVersion;
    private final Set<String> discoveredRegions = new LinkedHashSet<>();
    private final Map<String, Integer> activeQuests = new HashMap<>();
    private final Set<String> completedQuests = new LinkedHashSet<>();
    private String selectedPower = "fortuitous";
    private final Set<String> unlockedPowers = new LinkedHashSet<>(Set.of("fortuitous", "pretty", "protected", "simple", "warrior"));
    private final Map<StatType, Integer> tuningAllocations = new EnumMap<>(StatType.class);
    private final Map<String, StatEffect> statEffects = new LinkedHashMap<>();

    public PlayerProfile(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.lastKnownName = playerName == null ? "" : playerName;
        this.coins = 0L;
        this.bankCoins = 0L;
        this.stats = new PlayerStats();
        this.inventory = new PlayerInventory();
        this.skillExperience = new LinkedHashMap<>();
        this.profileVersion = CURRENT_VERSION;
    }

    public UUID getPlayerId() { return playerId; }
    public String getLastKnownName() { return lastKnownName; }
    public void setLastKnownName(String name) { this.lastKnownName = name == null ? "" : name; }
    public long getCoins() { return coins; }
    public long getBankCoins() { return bankCoins; }
    public void setBankCoinsForLoad(long amount) { bankCoins = Math.max(0L, amount); }

    public boolean depositCoins(long amount) {
        if (amount <= 0 || coins < amount || bankCoins > Long.MAX_VALUE - amount) return false;
        coins -= amount;
        bankCoins += amount;
        return true;
    }

    public boolean withdrawCoins(long amount) {
        if (amount <= 0 || bankCoins < amount || coins > Long.MAX_VALUE - amount) return false;
        bankCoins -= amount;
        coins += amount;
        return true;
    }

    public boolean addCoins(long amount) {
        if (amount <= 0 || coins > Long.MAX_VALUE - amount) return false;
        coins += amount;
        return true;
    }

    public boolean removeCoins(long amount) {
        if (amount <= 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }

    public boolean hasDiscoveredRegion(String regionId) { return discoveredRegions.contains(regionId); }
    public boolean discoverRegion(String regionId) { return regionId != null && !regionId.isBlank() && discoveredRegions.add(regionId); }
    public Set<String> getDiscoveredRegions() { return Collections.unmodifiableSet(discoveredRegions); }

    public boolean isQuestActive(String questId) { return activeQuests.containsKey(normalizeQuestId(questId)); }
    public boolean isQuestCompleted(String questId) { return completedQuests.contains(normalizeQuestId(questId)); }
    public int getQuestProgress(String questId) { return activeQuests.getOrDefault(normalizeQuestId(questId), 0); }
    public Map<String, Integer> getActiveQuests() { return Collections.unmodifiableMap(activeQuests); }
    public Set<String> getCompletedQuests() { return Collections.unmodifiableSet(completedQuests); }
    public boolean acceptQuest(String questId) {
        String id = normalizeQuestId(questId);
        if (id.isBlank() || activeQuests.containsKey(id) || completedQuests.contains(id)) return false;
        activeQuests.put(id, 0);
        return true;
    }
    public boolean abandonQuest(String questId) { return activeQuests.remove(normalizeQuestId(questId)) != null; }
    public int addQuestProgress(String questId, int amount) {
        String id = normalizeQuestId(questId);
        if (amount <= 0 || !activeQuests.containsKey(id)) return 0;
        int next = (int) Math.min(Integer.MAX_VALUE, (long) activeQuests.get(id) + amount);
        activeQuests.put(id, next);
        return next;
    }
    public boolean completeQuest(String questId) {
        String id = normalizeQuestId(questId);
        if (activeQuests.remove(id) == null) return false;
        return completedQuests.add(id);
    }
    public void loadQuestProgress(String questId, int progress) {
        String id = normalizeQuestId(questId);
        if (!id.isBlank() && !completedQuests.contains(id)) activeQuests.put(id, Math.max(0, progress));
    }
    public void loadCompletedQuest(String questId) {
        String id = normalizeQuestId(questId);
        if (!id.isBlank()) { activeQuests.remove(id); completedQuests.add(id); }
    }
    private static String normalizeQuestId(String questId) {
        return questId == null ? "" : questId.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public PlayerStats getStats() { return stats; }
    public PlayerInventory getInventory() { return inventory; }
    public void setInventory(PlayerInventory inventory) { this.inventory = inventory == null ? new PlayerInventory() : inventory; }
    public int getAccessoryPower() { return AccessoryPowerManager.getAccessoryPower(inventory, false); }
    public String getSelectedPower() { return selectedPower; }
    public Set<String> getUnlockedPowers() { return Collections.unmodifiableSet(unlockedPowers); }
    public Map<StatType, Integer> getTuningAllocations() {
        Map<StatType, Integer> effective = new EnumMap<>(StatType.class);
        int remaining = AccessoryPowerManager.getTuningPoints(getAccessoryPower());
        for (StatType type : StatType.values()) {
            int stored = tuningAllocations.getOrDefault(type, 0);
            int accepted = Math.min(stored, remaining);
            if (accepted > 0) effective.put(type, accepted);
            remaining -= accepted;
        }
        return Collections.unmodifiableMap(effective);
    }

    public boolean selectPower(String powerId) {
        String id = powerId == null ? "" : powerId.trim().toLowerCase(java.util.Locale.ROOT);
        if (!unlockedPowers.contains(id) || PowerRegistry.get(id).isEmpty()) return false;
        selectedPower = id;
        return true;
    }

    public boolean unlockPower(String powerId) {
        String id = powerId == null ? "" : powerId.trim().toLowerCase(java.util.Locale.ROOT);
        var power = PowerRegistry.get(id).orElse(null);
        if (power == null || power.starter() || unlockedPowers.contains(id)) return false;
        unlockedPowers.add(id);
        return true;
    }

    public void loadPowerData(String selected, Set<String> unlocked) {
        unlockedPowers.clear();
        unlockedPowers.add("fortuitous");
        unlockedPowers.add("pretty");
        unlockedPowers.add("protected");
        unlockedPowers.add("simple");
        unlockedPowers.add("warrior");
        if (unlocked != null) for (String id : unlocked) {
            if (id != null && PowerRegistry.get(id).isPresent()) unlockedPowers.add(id.toLowerCase(java.util.Locale.ROOT));
        }
        selectedPower = unlockedPowers.contains(selected) ? selected : "fortuitous";
    }

    public boolean setTuningPoints(StatType stat, int points) {
        if (!AccessoryPowerManager.isTunable(stat) || points < 0) return false;
        int usedOther = getTuningAllocations().entrySet().stream()
                .filter(entry -> entry.getKey() != stat).mapToInt(Map.Entry::getValue).sum();
        if (usedOther + points > AccessoryPowerManager.getTuningPoints(getAccessoryPower())) return false;
        if (points == 0) tuningAllocations.remove(stat); else tuningAllocations.put(stat, points);
        return true;
    }

    public void loadTuningAllocations(Map<StatType, Integer> values) {
        tuningAllocations.clear();
        if (values == null) return;
        int remaining = AccessoryPowerManager.getTuningPoints(getAccessoryPower());
        for (var entry : values.entrySet()) {
            if (remaining <= 0) break;
            if (!AccessoryPowerManager.isTunable(entry.getKey()) || entry.getValue() <= 0) continue;
            int accepted = Math.min(entry.getValue(), remaining);
            tuningAllocations.put(entry.getKey(), accepted);
            remaining -= accepted;
        }
    }

    public int getUsedTuningPoints() { return getTuningAllocations().values().stream().mapToInt(Integer::intValue).sum(); }

    /**
     * Returns the current derived modifier snapshot from equipment, reforges,
     * accessory power and tuning. Skill/Buff/Temporary sources can be appended
     * here as their lifecycle managers are introduced.
     */
    public java.util.List<StatModifier> getStatModifiers() {
        java.util.List<StatModifier> modifiers = new java.util.ArrayList<>(inventory.getStatModifiers());
        int accessoryPower = getAccessoryPower();
        modifiers.addAll(AccessoryPowerManager.getPowerModifiers(selectedPower, accessoryPower));
        statEffects.values().forEach(effect -> modifiers.add(effect.modifier()));
        getTuningAllocations().forEach((stat, points) -> {
            double amount = AccessoryPowerManager.tuningValue(stat) * points;
            if (amount != 0.0) modifiers.add(new StatModifier(
                    "accessory_tuning:" + stat.id(), stat, amount,
                    StatModifier.Operation.ADDITIVE));
        });
        return java.util.List.copyOf(modifiers);
    }

    /** Adds or refreshes an effect by stable effect ID. */
    public boolean putStatEffect(StatEffect effect) {
        if (effect == null) return false;
        statEffects.put(effect.id(), effect);
        return true;
    }

    public boolean removeStatEffect(String effectId) {
        return effectId != null && statEffects.remove(effectId.trim()) != null;
    }

    public java.util.List<StatEffect> getStatEffects() {
        return java.util.List.copyOf(statEffects.values());
    }

    /** Removes effects whose absolute Overworld game-time expiry has been reached. */
    public boolean removeExpiredStatEffects(long gameTime) {
        return statEffects.values().removeIf(effect -> effect.isExpired(gameTime));
    }

    public double getFinalStat(StatType type) {
        return stats.getFinal(type, getStatModifiers());
    }

    /** Calculates a stat using only the requested modifier source categories. */
    public double getFinalStat(StatType type, Set<com.skyrpg.skyblock.stats.StatSource> includedSources) {
        return stats.getFinal(type, getStatModifiers(), includedSources);
    }
    /** Returns an immutable snapshot of all retained namespaced skill XP values. */
    public Map<Identifier, Double> getSkillExperience() { return Collections.unmodifiableMap(skillExperience); }

    public double getSkillExperience(Identifier skillId) {
        return skillId == null ? 0.0 : skillExperience.getOrDefault(skillId, 0.0);
    }

    /** Legacy compatibility for Core's original built-in skill identifiers. */
    public double getSkillExperience(SkillType type) {
        return type == null ? 0.0 : getSkillExperience(type.identifier());
    }

    public boolean addSkillExperience(Identifier skillId, double amount) {
        if (skillId == null || amount <= 0 || !Double.isFinite(amount)) return false;
        double next = getSkillExperience(skillId) + amount;
        if (!Double.isFinite(next)) return false;
        skillExperience.put(skillId, next);
        return true;
    }

    /** Legacy compatibility for Core's original built-in skill identifiers. */
    public boolean addSkillExperience(SkillType type, double amount) {
        return type != null && addSkillExperience(type.identifier(), amount);
    }

    /** Persistence hook: unknown extension skills must survive while their optional mod is absent. */
    public void loadSkillExperience(Identifier skillId, double amount) {
        if (skillId != null && amount >= 0 && Double.isFinite(amount)) skillExperience.put(skillId, amount);
    }

    public java.util.Optional<NbtCompound> getModuleData(Identifier key) {
        NbtCompound value = key == null ? null : moduleData.get(key);
        return value == null ? java.util.Optional.empty() : java.util.Optional.of(value.copy());
    }

    public void setModuleData(Identifier key, NbtCompound value) {
        if (key == null || value == null) throw new IllegalArgumentException("Module data key and value are required");
        moduleData.put(key, value.copy());
    }

    public boolean removeModuleData(Identifier key) {
        return key != null && moduleData.remove(key) != null;
    }

    public Map<Identifier, NbtCompound> getModuleDataSnapshot() {
        Map<Identifier, NbtCompound> snapshot = new LinkedHashMap<>();
        moduleData.forEach((key, value) -> snapshot.put(key, value.copy()));
        return Collections.unmodifiableMap(snapshot);
    }

    public int getProfileVersion() { return profileVersion; }
    public void setProfileVersion(int profileVersion) { this.profileVersion = profileVersion; }
}
