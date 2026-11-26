package com.skyrpg.skyblock.warp;

/** Fixed warp destination. Coordinates and dimension are supplied by project data, not inferred. */
public record WarpDefinition(
        String id, String displayName, String dimension,
        double x, double y, double z, float yaw, float pitch,
        String requiredRegionId, String description) {

    public WarpDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Warp id cannot be blank");
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        if (dimension == null || dimension.isBlank()) throw new IllegalArgumentException("Warp dimension cannot be blank");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch))
            throw new IllegalArgumentException("Warp coordinates and rotation must be finite");
        requiredRegionId = requiredRegionId == null ? "" : requiredRegionId;
        description = description == null ? "" : description;
    }
}
