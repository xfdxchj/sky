package com.skyrpg.skyblock.region;

/** Axis-aligned region volume in one Minecraft dimension. Bounds are inclusive. */
public record RegionDefinition(
        String id, String displayName, String dimension,
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ, int priority) {

    public RegionDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Region id cannot be blank");
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        if (dimension == null || dimension.isBlank()) throw new IllegalArgumentException("Region dimension cannot be blank");
        if (!finite(minX, minY, minZ, maxX, maxY, maxZ))
            throw new IllegalArgumentException("Region bounds must be finite");
        if (minX > maxX || minY > maxY || minZ > maxZ)
            throw new IllegalArgumentException("Region minimum bounds must not exceed maximum bounds");
    }

    public boolean contains(String dimensionId, double x, double y, double z) {
        return dimension.equals(dimensionId)
                && x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public double volume() {
        return (maxX - minX + 1.0) * (maxY - minY + 1.0) * (maxZ - minZ + 1.0);
    }

    private static boolean finite(double... values) {
        for (double value : values) if (!Double.isFinite(value)) return false;
        return true;
    }
}
