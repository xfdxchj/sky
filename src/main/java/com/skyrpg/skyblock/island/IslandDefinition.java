package com.skyrpg.skyblock.island;

public record IslandDefinition(
        String id, String name, String dimension,
        int centerX, int centerZ, int surfaceY,
        int radiusX, int radiusZ, int thickness,
        double spawnX, double spawnY, double spawnZ,
        String regionId, String warpId,
        String topBlock, String soilBlock, String coreBlock,
        boolean testFixture) {
    public IslandDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Island id cannot be blank");
        name = name == null || name.isBlank() ? id : name;
        if (dimension == null || dimension.isBlank()) throw new IllegalArgumentException("Dimension cannot be blank");
        if (radiusX < 4 || radiusZ < 4 || radiusX > 256 || radiusZ > 256)
            throw new IllegalArgumentException("Island radii must be between 4 and 256 blocks");
        if (thickness < 3 || thickness > 64) throw new IllegalArgumentException("Island thickness must be 3..64");
        if (!Double.isFinite(spawnX) || !Double.isFinite(spawnY) || !Double.isFinite(spawnZ))
            throw new IllegalArgumentException("Spawn coordinates must be finite");
        double spawnDx = (spawnX - centerX) / radiusX;
        double spawnDz = (spawnZ - centerZ) / radiusZ;
        if (spawnDx * spawnDx + spawnDz * spawnDz >= 1.0)
            throw new IllegalArgumentException("Island spawn must be inside the island footprint");
        if (spawnY <= surfaceY + 3)
            throw new IllegalArgumentException("Island spawn must be at least four blocks above the nominal surface");
        regionId = regionId == null ? "" : regionId;
        warpId = warpId == null ? "" : warpId;
        if (topBlock == null || topBlock.isBlank() || soilBlock == null || soilBlock.isBlank()
                || coreBlock == null || coreBlock.isBlank())
            throw new IllegalArgumentException("Island block palette cannot contain blank ids");
    }
}
