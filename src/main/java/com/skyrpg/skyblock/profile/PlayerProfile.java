package com.skyrpg.skyblock.profile;

import com.skyrpg.skyblock.skill.SkillType;
import com.skyrpg.skyblock.stats.PlayerStats;
import com.skyrpg.skyblock.inventory.PlayerInventory;
import com.skyrpg.skyblock.stats.StatType;
import com.skyrpg.skyblock.accessory.AccessoryPowerManager;
import com.skyrpg.skyblock.accessory.PowerRegistry;
import com.skyrpg.skyblock.stats.StatModifier;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashMap;

/** Persistent SkyBlock profile data for one Minecraft player. */
public final class PlayerProfile {
    public static final int CURRENT_VERSION = 7;

    private final UUID playerId;
    private String lastKnownName;
    private long coins;
    private long bankCoins;
    private final PlayerStats stats;
    private PlayerInventory inventory;
    private final Map<SkillType, Double> skillExperience;
    private int profileVersion;
    private final Set<String> discoveredRegions = new LinkedHashSet<>();
    private final Map<String, Integer> activeQuests = new HashMap<>();
    private final Set<String> completedQuests = new LinkedHashSet<>();
    private String selectedPower = "fortuitous";
    private final Set<String> unlockedPowers = new LinkedHashSet<>(Set.of("fortuitous", "pretty"));
    private final Map<StatType, Integer> tuningAllocations = new EnumMap<>(StatType.class);

    public PlayerProfile(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.lastKnownName = playerName == null ? "" : playerName;
        this.coins = 0L;
        this.bankCoins = 0L;
        this.stats = new PlayerStats();
        this.inventory = new PlayerInventory();
        this.skillExperience = new EnumMap<>(SkillType.class);
        for (SkillType type : SkillType.values()) skillExperience.put(type, 0.0);
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
    public Map<StatType, Integer> getTuningAllocations() { return Collections.unmodifiableMap(tuningAllocations); }

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
        if (unlocked != null) for (String id : unlocked) {
            if (id != null && PowerRegistry.get(id).isPresent()) unlockedPowers.add(id.toLowerCase(java.util.Locale.ROOT));
        }
        selectedPower = unlockedPowers.contains(selected) ? selected : "fortuitous";
    }

    public boolean setTuningPoints(StatType stat, int points) {
        if (!AccessoryPowerManager.isTunable(stat) || points < 0) return false;
        int usedOther = tuningAllocations.entrySet().stream()
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

    public int getUsedTuningPoints() { return tuningAllocations.values().stream().mapToInt(Integer::intValue).sum(); }

    public double getFinalStat(StatType type) {
        java.util.List<StatModifier> modifiers = new java.util.ArrayList<>(inventory.getStatModifiers());
        int ap = getAccessoryPower();
        modifiers.addAll(AccessoryPowerManager.getPowerModifiers(selectedPower, ap));
        int tuning = tuningAllocations.getOrDefault(type, 0);
        double tuningValue = AccessoryPowerManager.tuningValue(type) * tuning;
        if (tuningValue != 0) modifiers.add(new StatModifier("accessory_tuning:" + type.id(), type,
                tuningValue, StatModifier.Operation.ADDITIVE));
        return stats.getFinal(type, modifiers);
    }
    public Map<SkillType, Double> getSkillExperience() { return skillExperience; }
    public double getSkillExperience(SkillType type) { return skillExperience.getOrDefault(type, 0.0); }

    public boolean addSkillExperience(SkillType type, double amount) {
        if (amount <= 0 || !Double.isFinite(amount)) return false;
        double current = getSkillExperience(type);
        double next = current + amount;
        if (!Double.isFinite(next)) return false;
        skillExperience.put(type, next);
        return true;
    }

    public int getProfileVersion() { return profileVersion; }
    public void setProfileVersion(int profileVersion) { this.profileVersion = profileVersion; }
}
