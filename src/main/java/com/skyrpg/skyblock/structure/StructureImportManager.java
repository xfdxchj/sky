package com.skyrpg.skyblock.structure;

import net.minecraft.block.Block;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.server.world.ServerWorld;

public final class StructureImportManager {
    public record PasteResult(boolean success, String message, int volume) {}
    private StructureImportManager() {}

    public static PasteResult paste(ServerWorld world, StructureDefinition definition) {
        return paste(world, definition, new BlockPos(definition.originX(), definition.originY(), definition.originZ()));
    }

    public static PasteResult paste(ServerWorld world, StructureDefinition definition, BlockPos origin) {
        if (!world.getRegistryKey().getValue().toString().equals(definition.dimension()))
            return fail("Structure dimension does not match the selected world.");

        Identifier templateId = Identifier.tryParse(definition.template());
        if (templateId == null) return fail("Invalid structure template identifier.");
        StructureTemplate template = world.getStructureTemplateManager().getTemplate(templateId).orElse(null);
        if (template == null) {
            return fail("Template not found: " + templateId
                    + ". Ensure the map datapack is installed and run /reload.");
        }

        var size = template.getSize();
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1)
            return fail("Structure template has invalid dimensions.");
        long volume = (long) size.getX() * size.getY() * size.getZ();
        if (volume > definition.maxBlocks())
            return fail("Structure volume " + volume + " exceeds manifest max_blocks " + definition.maxBlocks() + ".");

        StructurePlacementData placement = new StructurePlacementData()
                .setPosition(origin)
                .setRotation(definition.rotation())
                .setMirror(definition.mirror())
                .setIgnoreEntities(true)
                .setUpdateNeighbors(true);
        BlockBox bounds = template.calculateBoundingBox(placement, origin);

        if (bounds.getMinY() < world.getBottomY() || bounds.getMaxY() >= world.getTopY())
            return fail("Structure extends outside the world's build height.");
        if (!world.getWorldBorder().contains(new BlockPos(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ()))
                || !world.getWorldBorder().contains(new BlockPos(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ())))
            return fail("Structure bounding box crosses the world border.");

        // Atomic safety policy: require the entire transformed bounding box to be air.
        // This prevents partial pastes and protects existing terrain/buildings.
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        for (int x = bounds.getMinX(); x <= bounds.getMaxX(); x++) {
            for (int y = bounds.getMinY(); y <= bounds.getMaxY(); y++) {
                for (int z = bounds.getMinZ(); z <= bounds.getMaxZ(); z++) {
                    cursor.set(x, y, z);
                    if (!world.getBlockState(cursor).isAir())
                        return fail("Paste cancelled: target area is not empty at " + x + ", " + y + ", " + z + ".");
                }
            }
        }

        boolean placed = template.place(world, origin, origin, placement, Random.create(), Block.NOTIFY_ALL);
        if (!placed) return fail("Minecraft rejected the structure placement.");
        return new PasteResult(true, "Pasted structure '" + definition.name()
                + "' at " + origin.toShortString() + " (rotation=" + definition.rotation()
                + ", mirror=" + definition.mirror() + ").", (int) volume);
    }

    private static PasteResult fail(String message) {
        return new PasteResult(false, message, 0);
    }
}
