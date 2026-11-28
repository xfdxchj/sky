package com.skyrpg.skyblock.hub;

import net.minecraft.util.Identifier;

import java.net.URI;

/** A sourced Hub point of interest. Coordinates are reference points, not region bounds. */
public record HubLocationDefinition(
        String id, String displayName, String parentId, String category,
        String dimension, double x, double y, double z,
        String description, String sourceType, String sourceUrl, String referenceRevision,
        String regionId, String warpId, boolean testFixture) {

    public HubLocationDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Hub location id cannot be blank");
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        parentId = parentId == null ? "" : parentId;
        if (category == null || !java.util.List.of("area", "service", "npc", "landmark").contains(category))
            throw new IllegalArgumentException("Unsupported Hub location category: " + category);
        if (dimension == null || Identifier.tryParse(dimension) == null)
            throw new IllegalArgumentException("Invalid Hub location dimension: " + dimension);
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
            throw new IllegalArgumentException("Hub location coordinates must be finite");
        description = description == null ? "" : description;
        if (sourceType == null || !java.util.List.of("community_wiki", "official", "project_fixture").contains(sourceType))
            throw new IllegalArgumentException("Unsupported Hub location source_type: " + sourceType);
        sourceUrl = sourceUrl == null ? "" : sourceUrl;
        if (sourceUrl.isBlank()) throw new IllegalArgumentException("Hub location source_url is required");
        try {
            URI uri = URI.create(sourceUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null)
                throw new IllegalArgumentException("Hub location source_url must be an HTTPS URL");
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid Hub location source_url: " + sourceUrl, exception);
        }
        referenceRevision = referenceRevision == null ? "" : referenceRevision;
        regionId = regionId == null ? "" : regionId;
        warpId = warpId == null ? "" : warpId;
    }

    /** True only when coordinates were authored for this project's runtime world. */
    public boolean hasRuntimeCoordinates() {
        return "project_fixture".equals(sourceType) && !testFixture;
    }

    public double squaredDistance(double px, double py, double pz) {
        double dx = x - px;
        double dy = y - py;
        double dz = z - pz;
        return dx * dx + dy * dy + dz * dz;
    }
}
