package com.skyrpg.skyblock.map;

import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.structure.StructureDefinition;
import com.skyrpg.skyblock.structure.StructureImportManager;
import com.skyrpg.skyblock.structure.StructureRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Incremental, restart-aware map assembler. It preflights the complete target
 * cuboid before placing any slice, then places at most one slice per server tick.
 */
public final class MapAssemblyManager {
    private static final int PREFLIGHT_BLOCKS_PER_TICK = 32_768;
    private static final Map<String, Job> ACTIVE = new LinkedHashMap<>();

    private static final class Job {
        final MapDefinition definition;
        final ServerWorld world;
        final BlockPos origin;
        long cursor;
        int nextSlice;
        Phase phase;

        Job(MapDefinition definition, ServerWorld world, BlockPos origin, long cursor, int nextSlice, Phase phase) {
            this.definition = definition;
            this.world = world;
            this.origin = origin;
            this.cursor = cursor;
            this.nextSlice = nextSlice;
            this.phase = phase;
        }
    }

    private enum Phase { PREFLIGHT, PLACING }

    private MapAssemblyManager() {}

    public record Result(boolean success, String message) {}

    public static Result start(MinecraftServer server, String mapId, BlockPos origin) {
        MapDefinition definition = MapRegistry.get(mapId).orElse(null);
        if (definition == null) return fail("Unknown logical map: " + mapId);
        String key = definition.id().toLowerCase(java.util.Locale.ROOT);
        if (ACTIVE.containsKey(key)) return fail("Map assembly is already running: " + definition.id());
        if (!ACTIVE.isEmpty()) return fail("Another map assembly is already running. Only one map may be assembled at a time.");

        MapWorldData data = MapWorldData.get(server);
        MapWorldData.Record previous = data.get(definition.id());
        if (previous != null) {
            if (previous.status() == MapWorldData.Status.COMPLETE)
                return fail("Map is already generated. Use /skyblock map status " + definition.id() + ".");
            if (previous.status() == MapWorldData.Status.PREFLIGHT || previous.status() == MapWorldData.Status.PLACING)
                return fail("A saved generation is already active or needs recovery (" + previous.status()
                        + "). Inspect /skyblock map status " + definition.id() + ".");
            if (previous.status() == MapWorldData.Status.INTERRUPTED)
                return fail("The previous generation was interrupted during a slice. Automatic retry is unsafe; inspect the target and restore it before retrying.");
        }

        Identifier dimensionId = Identifier.tryParse(definition.dimension());
        if (dimensionId == null) return fail("Invalid map dimension: " + definition.dimension());
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimensionId);
        ServerWorld world = server.getWorld(worldKey);
        if (world == null) return fail("Map dimension is not loaded: " + definition.dimension());

        long volume = (long) definition.sizeX() * definition.sizeY() * definition.sizeZ();
        if (volume > 30_000_000L) return fail("Map volume exceeds the 30 million block safety limit.");
        long maxX = (long) origin.getX() + definition.sizeX() - 1;
        long maxY = (long) origin.getY() + definition.sizeY() - 1;
        long maxZ = (long) origin.getZ() + definition.sizeZ() - 1;
        if (origin.getY() < world.getBottomY() || maxY >= world.getTopY())
            return fail("Map bounds exceed the target dimension build height.");
        if (maxX > Integer.MAX_VALUE || maxZ > Integer.MAX_VALUE
                || !world.getWorldBorder().contains(origin)
                || !world.getWorldBorder().contains(new BlockPos((int) maxX, (int) maxY, (int) maxZ)))
            return fail("Map bounds exceed the target world border.");

        for (MapDefinition.Slice slice : definition.slices()) {
            StructureDefinition structure = StructureRegistry.get(slice.structureId()).orElse(null);
            if (structure == null) return fail("Missing structure manifest: " + slice.structureId());
            if (!structure.dimension().equals(definition.dimension()))
                return fail("Structure dimension mismatch for slice " + slice.structureId());
            Identifier templateId = Identifier.tryParse(structure.template());
            StructureTemplate template = templateId == null ? null
                    : world.getStructureTemplateManager().getTemplate(templateId).orElse(null);
            if (template == null) return fail("Structure template not found: " + structure.template());
            var size = template.getSize();
            if (size.getX() != slice.sizeX() || size.getY() != slice.sizeY() || size.getZ() != slice.sizeZ())
                return fail("Template dimensions do not match map manifest for " + slice.structureId()
                        + ": template=" + size + ", manifest=" + slice.sizeX() + "x" + slice.sizeY() + "x" + slice.sizeZ());
        }

        Job job = new Job(definition, world, origin, 0L, 0, Phase.PREFLIGHT);
        ACTIVE.put(key, job);
        data.put(record(job, MapWorldData.Status.PREFLIGHT, false, "Preflighting target area."));
        SkyBlockRPG.LOGGER.info("Started map preflight '{}' at {} in {} ({} blocks).",
                definition.id(), origin.toShortString(), definition.dimension(), volume);
        return new Result(true, "Started map preflight for '" + definition.name() + "' (" + volume
                + " blocks). No blocks will be placed until the complete target volume passes.");
    }

    /** Called once per server tick. */
    public static void tick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;
        var iterator = ACTIVE.entrySet().iterator();
        if (!iterator.hasNext()) return;
        var entry = iterator.next();
        String key = entry.getKey();
        Job job = entry.getValue();
        MapWorldData data = MapWorldData.get(server);

        if (job.phase == Phase.PREFLIGHT) {
            long total = (long) job.definition.sizeX() * job.definition.sizeY() * job.definition.sizeZ();
            long end = Math.min(total, job.cursor + PREFLIGHT_BLOCKS_PER_TICK);
            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (; job.cursor < end; job.cursor++) {
                int x = (int) (job.cursor % job.definition.sizeX());
                int z = (int) ((job.cursor / job.definition.sizeX()) % job.definition.sizeZ());
                int y = (int) (job.cursor / ((long) job.definition.sizeX() * job.definition.sizeZ()));
                pos.set(job.origin.getX() + x, job.origin.getY() + y, job.origin.getZ() + z);
                if (!job.world.getBlockState(pos).isAir()) {
                    String message = "Preflight failed: target is not air at " + pos.toShortString()
                            + ". No map blocks were placed.";
                    data.put(record(job, MapWorldData.Status.FAILED, false, message));
                    SkyBlockRPG.LOGGER.warn("Map '{}' preflight failed at {}.", job.definition.id(), pos.toShortString());
                    iterator.remove();
                    return;
                }
            }
            if (job.cursor >= total) {
                job.phase = Phase.PLACING;
                data.put(record(job, MapWorldData.Status.PLACING, false, "Preflight complete; placing structure slices."));
                SkyBlockRPG.LOGGER.info("Map '{}' preflight complete; beginning {} structure slices.",
                        job.definition.id(), job.definition.slices().size());
            } else if (job.cursor % (PREFLIGHT_BLOCKS_PER_TICK * 4L) < PREFLIGHT_BLOCKS_PER_TICK) {
                data.put(record(job, MapWorldData.Status.PREFLIGHT, false,
                        "Preflight progress: " + job.cursor + "/" + total + " blocks."));
            }
            return;
        }

        if (job.nextSlice >= job.definition.slices().size()) {
            data.put(record(job, MapWorldData.Status.COMPLETE, false, "All map slices placed."));
            SkyBlockRPG.LOGGER.info("Map '{}' generation completed.", job.definition.id());
            iterator.remove();
            return;
        }

        MapDefinition.Slice slice = job.definition.slices().get(job.nextSlice);
        StructureDefinition structure = StructureRegistry.get(slice.structureId()).orElse(null);
        if (structure == null) {
            data.put(record(job, MapWorldData.Status.FAILED, false, "Missing structure manifest: " + slice.structureId()));
            iterator.remove();
            return;
        }

        data.put(record(job, MapWorldData.Status.PLACING, true, "Placing slice "
                + (job.nextSlice + 1) + "/" + job.definition.slices().size() + ": " + slice.structureId()));
        BlockPos sliceOrigin = job.origin.add(slice.offsetX(), slice.offsetY(), slice.offsetZ());
        StructureImportManager.PasteResult result = StructureImportManager.paste(job.world, structure, sliceOrigin);
        if (!result.success()) {
            data.put(record(job, MapWorldData.Status.FAILED, false,
                    "Slice " + slice.structureId() + " failed: " + result.message()));
            SkyBlockRPG.LOGGER.error("Map '{}' slice '{}' failed: {}", job.definition.id(), slice.structureId(), result.message());
            iterator.remove();
            return;
        }
        job.nextSlice++;
        data.put(record(job, MapWorldData.Status.PLACING, false, "Placed "
                + job.nextSlice + "/" + job.definition.slices().size() + " slices."));
        if (job.nextSlice >= job.definition.slices().size()) {
            data.put(record(job, MapWorldData.Status.COMPLETE, false, "All map slices placed."));
            SkyBlockRPG.LOGGER.info("Map '{}' generation completed.", job.definition.id());
            iterator.remove();
        }
    }

    /** Rebuild in-memory jobs from safe checkpoints after a server restart. */
    public static void recover(MinecraftServer server) {
        ACTIVE.clear();
        MapWorldData data = MapWorldData.get(server);
        for (MapWorldData.Record saved : data.all()) {
            if (saved.status() != MapWorldData.Status.PREFLIGHT
                    && saved.status() != MapWorldData.Status.PLACING) continue;

            MapDefinition definition = MapRegistry.get(saved.mapId()).orElse(null);
            ServerWorld world = resolveWorld(server, saved.dimension());
            if (saved.status() == MapWorldData.Status.PLACING && saved.sliceInProgress()) {
                data.put(new MapWorldData.Record(saved.mapId(), saved.dimension(), saved.originX(), saved.originY(),
                        saved.originZ(), MapWorldData.Status.INTERRUPTED, saved.preflightCursor(), saved.nextSlice(),
                        true, "Server stopped during slice placement; inspect this slice before retrying."));
                continue;
            }

            if (definition == null || world == null || !definition.dimension().equals(saved.dimension())) {
                data.put(new MapWorldData.Record(saved.mapId(), saved.dimension(), saved.originX(), saved.originY(),
                        saved.originZ(), MapWorldData.Status.FAILED, saved.preflightCursor(), saved.nextSlice(),
                        false, "Recovery refused: map definition or saved dimension is missing or no longer matches."));
                SkyBlockRPG.LOGGER.error("Could not recover map '{}': missing definition/world or dimension mismatch.",
                        saved.mapId());
                continue;
            }

            BlockPos origin = new BlockPos(saved.originX(), saved.originY(), saved.originZ());
            if (saved.status() == MapWorldData.Status.PREFLIGHT) {
                Job job = new Job(definition, world, origin, 0L, 0, Phase.PREFLIGHT);
                ACTIVE.put(definition.id().toLowerCase(java.util.Locale.ROOT), job);
                data.put(record(job, MapWorldData.Status.PREFLIGHT, false,
                        "Restarted preflight from the beginning."));
            } else if (saved.nextSlice() <= definition.slices().size()) {
                Job job = new Job(definition, world, origin,
                        (long) definition.sizeX() * definition.sizeY() * definition.sizeZ(),
                        saved.nextSlice(), Phase.PLACING);
                ACTIVE.put(definition.id().toLowerCase(java.util.Locale.ROOT), job);
            } else {
                data.put(new MapWorldData.Record(saved.mapId(), saved.dimension(), saved.originX(), saved.originY(),
                        saved.originZ(), MapWorldData.Status.FAILED, saved.preflightCursor(), saved.nextSlice(),
                        false, "Recovery refused: saved slice cursor exceeds the current map definition."));
            }
        }
    }

    public static MapWorldData.Record status(MinecraftServer server, String mapId) {
        return MapWorldData.get(server).get(mapId);
    }

    public static void clear() { ACTIVE.clear(); }

    private static ServerWorld resolveWorld(MinecraftServer server, String dimension) {
        Identifier id = Identifier.tryParse(dimension);
        return id == null ? null : server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }

    private static MapWorldData.Record record(Job job, MapWorldData.Status status, boolean sliceInProgress, String message) {
        return new MapWorldData.Record(job.definition.id(), job.definition.dimension(),
                job.origin.getX(), job.origin.getY(), job.origin.getZ(), status,
                job.cursor, job.nextSlice, sliceInProgress, message);
    }

    private static Result fail(String message) { return new Result(false, message); }
}
