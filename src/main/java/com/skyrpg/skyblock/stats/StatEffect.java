package com.skyrpg.skyblock.stats;

/**
 * A persistent modifier with an absolute expiry measured in Overworld game ticks.
 * An expiry of -1 means the effect does not expire automatically.
 */
public record StatEffect(String id, StatModifier modifier, long expiresAtGameTime) {
    public StatEffect {
        if (id == null || id.isBlank() || id.length() > 128) {
            throw new IllegalArgumentException("Effect ID must contain 1-128 characters");
        }
        id = id.trim();
        if (modifier == null || modifier.stat() == null || modifier.operation() == null
                || modifier.source() == null || modifier.sourceId() == null || modifier.sourceId().isBlank()) {
            throw new IllegalArgumentException("A complete stat modifier is required");
        }
        if (!Double.isFinite(modifier.amount())) {
            throw new IllegalArgumentException("Effect amount must be finite");
        }
        if (expiresAtGameTime < -1L) {
            throw new IllegalArgumentException("Effect expiry must be -1 or a non-negative game time");
        }
    }

    public boolean isExpired(long gameTime) {
        return expiresAtGameTime >= 0L && gameTime >= expiresAtGameTime;
    }
}
