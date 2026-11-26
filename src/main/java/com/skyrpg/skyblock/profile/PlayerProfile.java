package com.skyrpg.skyblock.profile;

import com.skyrpg.skyblock.skill.SkillType;
import com.skyrpg.skyblock.stats.PlayerStats;
import com.skyrpg.skyblock.inventory.PlayerInventory;
import com.skyrpg.skyblock.stats.StatType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashMap;

/** Persistent SkyBlock profile data for one Minecraft player. */
public final class PlayerProfile {
    public static final int CURRENT_VERSION = 6;

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
    public double getFinalStat(StatType type) { return stats.getFinal(type, inventory.getStatModifiers()); }
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
