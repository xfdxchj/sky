package com.skyrpg.skyblock.map;

import net.minecraft.util.Identifier;

import java.util.List;

/** Logical map composed of one or more vanilla structure templates. */
public record MapDefinition(
        String id,
        String name,
        String dimension,
        int sizeX,
        int sizeY,
        int sizeZ,
        List<Slice> slices,
        String source,
        String sourceUrl,
        String license) {

    public record Slice(String structureId, int offsetX, int offsetY, int offsetZ,
                        int sizeX, int sizeY, int sizeZ) {
        public Slice {
            if (structureId == null || structureId.isBlank())
                throw new IllegalArgumentException("Map slice structure id cannot be blank");
            if (offsetX < 0 || offsetY < 0 || offsetZ < 0)
                throw new IllegalArgumentException("Map slice offsets cannot be negative");
            if (sizeX < 1 || sizeY < 1 || sizeZ < 1)
                throw new IllegalArgumentException("Map slice dimensions must be positive");
        }
    }

    public MapDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Map id cannot be blank");
        name = name == null || name.isBlank() ? id : name;
        if (Identifier.tryParse(dimension) == null) throw new IllegalArgumentException("Invalid map dimension: " + dimension);
        if (sizeX < 1 || sizeY < 1 || sizeZ < 1) throw new IllegalArgumentException("Map dimensions must be positive");
        long mapVolume = (long) sizeX * sizeY * sizeZ;
        if (mapVolume > 30_000_000L)
            throw new IllegalArgumentException("Map volume exceeds the 30 million block safety limit");
        slices = List.copyOf(slices);
        if (slices.isEmpty()) throw new IllegalArgumentException("Map must contain at least one structure slice");
        if (slices.size() > 512) throw new IllegalArgumentException("Map cannot contain more than 512 structure slices");
        long coveredVolume = 0L;
        for (int i = 0; i < slices.size(); i++) {
            Slice slice = slices.get(i);
            if ((long) slice.offsetX() + slice.sizeX() > sizeX
                    || (long) slice.offsetY() + slice.sizeY() > sizeY
                    || (long) slice.offsetZ() + slice.sizeZ() > sizeZ) {
                throw new IllegalArgumentException("Map slice extends outside map bounds: " + slice.structureId());
            }
            coveredVolume += (long) slice.sizeX() * slice.sizeY() * slice.sizeZ();
            for (int j = 0; j < i; j++) {
                Slice other = slices.get(j);
                boolean overlapX = slice.offsetX() < (long) other.offsetX() + other.sizeX()
                        && other.offsetX() < (long) slice.offsetX() + slice.sizeX();
                boolean overlapY = slice.offsetY() < (long) other.offsetY() + other.sizeY()
                        && other.offsetY() < (long) slice.offsetY() + slice.sizeY();
                boolean overlapZ = slice.offsetZ() < (long) other.offsetZ() + other.sizeZ()
                        && other.offsetZ() < (long) slice.offsetZ() + slice.sizeZ();
                if (overlapX && overlapY && overlapZ)
                    throw new IllegalArgumentException("Overlapping map slices: "
                            + other.structureId() + " and " + slice.structureId());
            }
        }
        if (coveredVolume != mapVolume)
            throw new IllegalArgumentException("Map slices do not tile the complete map bounds: covered "
                    + coveredVolume + " of " + mapVolume + " blocks.");
        source = source == null ? "" : source;
        sourceUrl = sourceUrl == null ? "" : sourceUrl;
        license = license == null ? "" : license;
    }
}
