package com.skyrpg.skyblock.island;

import java.util.UUID;

/** Persistent owner-bound private island placement. */
public record PrivateIslandRecord(
        UUID ownerId,
        String dimension,
        int centerX,
        int centerZ,
        int surfaceY,
        int radius,
        int thickness,
        double spawnX,
        double spawnY,
        double spawnZ,
        long createdAt
) {
    public PrivateIslandRecord {
        if (ownerId == null) throw new IllegalArgumentException("ownerId is required");
        if (dimension == null || dimension.isBlank()) throw new IllegalArgumentException("dimension is required");
        if (radius < 8 || radius > 128) throw new IllegalArgumentException("radius must be 8..128");
        if (thickness < 3 || thickness > 64) throw new IllegalArgumentException("thickness must be 3..64");
        if (!Double.isFinite(spawnX) || !Double.isFinite(spawnY) || !Double.isFinite(spawnZ))
            throw new IllegalArgumentException("spawn coordinates must be finite");
    }

    public boolean containsFootprint(int x, int z) {
        double dx = (x - centerX) / (double) radius;
        double dz = (z - centerZ) / (double) radius;
        return dx * dx + dz * dz <= 1.0;
    }

    public boolean containsProtectedPosition(int x, int y, int z) {
        return containsFootprint(x, z) && y >= surfaceY - thickness - 2 && y <= surfaceY + 64;
    }
}
