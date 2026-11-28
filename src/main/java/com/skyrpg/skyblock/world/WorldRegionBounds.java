package com.skyrpg.skyblock.world;

import net.minecraft.util.math.BlockPos;

/** Inclusive axis-aligned allocation bounds used to keep independent SkyBlock areas apart. */
public record WorldRegionBounds(
        String dimension,
        int minX, int minY, int minZ,
        int maxX, int maxY, int maxZ) {

    public WorldRegionBounds {
        if (dimension == null || dimension.isBlank()) {
            throw new IllegalArgumentException("Region dimension cannot be blank");
        }
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("Region minimum bounds must not exceed maximum bounds");
        }
    }

    public static WorldRegionBounds from(String dimension, BlockPos origin, int sizeX, int sizeY, int sizeZ) {
        if (sizeX < 1 || sizeY < 1 || sizeZ < 1) {
            throw new IllegalArgumentException("Region sizes must be positive");
        }
        long maxX = (long) origin.getX() + sizeX - 1;
        long maxY = (long) origin.getY() + sizeY - 1;
        long maxZ = (long) origin.getZ() + sizeZ - 1;
        if (maxX > Integer.MAX_VALUE || maxY > Integer.MAX_VALUE || maxZ > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Region bounds exceed integer world coordinates");
        }
        return new WorldRegionBounds(dimension, origin.getX(), origin.getY(), origin.getZ(),
                (int) maxX, (int) maxY, (int) maxZ);
    }

    public boolean overlaps(WorldRegionBounds other) {
        if (other == null || !dimension.equals(other.dimension)) return false;
        return minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY
                && minZ <= other.maxZ && maxZ >= other.minZ;
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }
}
