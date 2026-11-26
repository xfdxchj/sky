package com.skyrpg.skyblock.structure;

import net.minecraft.util.Identifier;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;

public record StructureDefinition(
        String id, String name, String template, String dimension,
        int originX, int originY, int originZ,
        BlockRotation rotation, BlockMirror mirror,
        int maxBlocks, String source, String sourceUrl, String license,
        boolean testFixture) {
    public StructureDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Structure id cannot be blank");
        name = name == null || name.isBlank() ? id : name;
        if (template == null || Identifier.tryParse(template) == null)
            throw new IllegalArgumentException("Invalid structure template identifier: " + template);
        if (dimension == null || Identifier.tryParse(dimension) == null)
            throw new IllegalArgumentException("Invalid dimension: " + dimension);
        if (rotation == null || mirror == null) throw new IllegalArgumentException("Rotation and mirror are required");
        if (maxBlocks < 1 || maxBlocks > 250_000)
            throw new IllegalArgumentException("max_blocks must be between 1 and 250000");
        source = source == null ? "" : source;
        sourceUrl = sourceUrl == null ? "" : sourceUrl;
        license = license == null ? "" : license;
    }
}
