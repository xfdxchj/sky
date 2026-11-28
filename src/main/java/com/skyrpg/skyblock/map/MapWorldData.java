package com.skyrpg.skyblock.map;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** World-persistent checkpoints for multi-structure map assembly. */
public final class MapWorldData extends PersistentState {
    public static final String STORAGE_ID = "skyblockrpg_map_generation";
    public static final PersistentState.Type<MapWorldData> TYPE =
            new PersistentState.Type<>(MapWorldData::new, MapWorldData::fromNbt, DataFixTypes.LEVEL);

    public enum Status { PREFLIGHT, PLACING, COMPLETE, FAILED, INTERRUPTED }

    public record Record(String mapId, String dimension, int originX, int originY, int originZ,
                         Status status, long preflightCursor, int nextSlice,
                         boolean sliceInProgress, String message) {}

    private final Map<String, Record> records = new LinkedHashMap<>();

    private MapWorldData() {}

    public static MapWorldData get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, STORAGE_ID);
    }

    public Record get(String mapId) {
        return records.get(mapId == null ? "" : mapId.toLowerCase(java.util.Locale.ROOT));
    }

    public Collection<Record> all() { return List.copyOf(records.values()); }

    public void put(Record record) {
        records.put(record.mapId().toLowerCase(java.util.Locale.ROOT), record);
        markDirty();
    }

    private static MapWorldData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        MapWorldData data = new MapWorldData();
        NbtList list = nbt.getList("maps", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound tag = list.getCompound(i);
            try {
                Status status = Status.valueOf(tag.getString("status"));
                Record record = new Record(tag.getString("mapId"), tag.getString("dimension"),
                        tag.getInt("originX"), tag.getInt("originY"), tag.getInt("originZ"),
                        status, Math.max(0L, tag.getLong("preflightCursor")),
                        Math.max(0, tag.getInt("nextSlice")), tag.getBoolean("sliceInProgress"),
                        tag.getString("message"));
                if (!record.mapId().isBlank()) data.records.put(record.mapId().toLowerCase(java.util.Locale.ROOT), record);
            } catch (RuntimeException ignored) {
                // Ignore one malformed map checkpoint without losing other map records.
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Record record : records.values()) {
            NbtCompound tag = new NbtCompound();
            tag.putString("mapId", record.mapId());
            tag.putString("dimension", record.dimension());
            tag.putInt("originX", record.originX());
            tag.putInt("originY", record.originY());
            tag.putInt("originZ", record.originZ());
            tag.putString("status", record.status().name());
            tag.putLong("preflightCursor", record.preflightCursor());
            tag.putInt("nextSlice", record.nextSlice());
            tag.putBoolean("sliceInProgress", record.sliceInProgress());
            tag.putString("message", record.message());
            list.add(tag);
        }
        nbt.put("maps", list);
        nbt.putInt("dataVersion", 1);
        return nbt;
    }
}
