package com.skyrpg.skyblock.stats;

/** A named stat contribution. ADDITIVE and MULTIPLICATIVE are kept separate for later Hypixel-style pipelines. */
public record StatModifier(String sourceId, StatType stat, double amount, Operation operation) {
    public enum Operation { ADDITIVE, MULTIPLICATIVE, OVERRIDE }
}
