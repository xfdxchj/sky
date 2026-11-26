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

/** Persistent SkyBlock profile data for one Minecraft player. */
public final class PlayerProfile {
    public static final int CURRENT_VERSION = 4;

    private final UUID playerId;
    private String lastKnownName;
    private long coins;
    private long bankCoins;
    private final PlayerStats stats;
    private PlayerInventory inventory;
    private final Map<SkillType, Double> skillExperience;
    private int profileVersion;
    private final Set<String> discoveredRegions = new LinkedHashSet<>();

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
