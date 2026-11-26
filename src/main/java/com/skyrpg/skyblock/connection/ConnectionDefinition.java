package com.skyrpg.skyblock.connection;

public record ConnectionDefinition(
        String id, String name, Type type, String dimension,
        double startX, double startY, double startZ,
        double endX, double endY, double endZ,
        int width, String blockId,
        String sourceWarpId, String targetWarpId,
        double activationRadius, boolean testFixture) {

    public enum Type { BRIDGE, PORTAL }

    public ConnectionDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Connection id cannot be blank");
        name = name == null || name.isBlank() ? id : name;
        if (type == null) throw new IllegalArgumentException("Connection type is required");
        if (dimension == null || dimension.isBlank()) throw new IllegalArgumentException("Dimension is required");
        if (!finite(startX, startY, startZ, endX, endY, endZ, activationRadius))
            throw new IllegalArgumentException("Connection coordinates and radius must be finite");
        if (sourceWarpId == null || sourceWarpId.isBlank()
                || targetWarpId == null || targetWarpId.isBlank())
            throw new IllegalArgumentException("Source and target warp IDs are required");
        if (type == Type.BRIDGE) {
            if (width < 1 || width > 7 || width % 2 == 0)
                throw new IllegalArgumentException("Bridge width must be an odd number from 1 to 7");
            if (blockId == null || blockId.isBlank())
                throw new IllegalArgumentException("Bridge block_id is required");
            if (activationRadius < 0)
                throw new IllegalArgumentException("Bridge activation radius cannot be negative");
            double length = Math.hypot(endX - startX, endZ - startZ);
            if (length < 2 || length > 256)
                throw new IllegalArgumentException("Bridge horizontal length must be between 2 and 256 blocks");
        } else {
            if (activationRadius < 1 || activationRadius > 16)
                throw new IllegalArgumentException("Portal activation_radius must be between 1 and 16");
        }
    }

    private static boolean finite(double... values) {
        for (double value : values) if (!Double.isFinite(value)) return false;
        return true;
    }
}
