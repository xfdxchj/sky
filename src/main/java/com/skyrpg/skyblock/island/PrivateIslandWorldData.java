package com.skyrpg.skyblock.island;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** World-scoped ownership and placement index for player private islands. */
public final class PrivateIslandWorldData extends PersistentState {
    public static final String STORAGE_ID = "skyblockrpg_private_islands";
    public static final PersistentState.Type<PrivateIslandWorldData> TYPE =
            new PersistentState.Type<>(PrivateIslandWorldData::new, PrivateIslandWorldData::fromNbt, DataFixTypes.LEVEL);

    private final Map<UUID, PrivateIslandRecord> islands = new HashMap<>();
    private int nextSlot;

    private PrivateIslandWorldData() {}

    public static PrivateIslandWorldData get(net.minecraft.server.MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, STORAGE_ID);
    }

    public PrivateIslandRecord get(UUID ownerId) {
        return ownerId == null ? null : islands.get(ownerId);
    }

    public Collection<PrivateIslandRecord> all() {
        return java.util.List.copyOf(islands.values());
    }

    public int getNextSlot() {
        return nextSlot;
    }

    public void skipToSlot(int followingSlot) {
        if (followingSlot > nextSlot) {
            nextSlot = followingSlot;
            markDirty();
        }
    }

    public void add(PrivateIslandRecord record, int followingSlot) {
        if (record == null) throw new IllegalArgumentException("record is required");
        islands.put(record.ownerId(), record);
        nextSlot = Math.max(nextSlot, followingSlot);
        markDirty();
    }

    private static PrivateIslandWorldData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        PrivateIslandWorldData data = new PrivateIslandWorldData();
        data.nextSlot = Math.max(0, nbt.getInt("nextSlot"));
        NbtList list = nbt.getList("islands", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            try {
                PrivateIslandRecord record = new PrivateIslandRecord(
                        UUID.fromString(entry.getString("owner")),
                        entry.getString("dimension"),
                        entry.getInt("centerX"),
                        entry.getInt("centerZ"),
                        entry.getInt("surfaceY"),
                        entry.getInt("radius"),
                        entry.getInt("thickness"),
                        entry.getDouble("spawnX"),
                        entry.getDouble("spawnY"),
                        entry.getDouble("spawnZ"),
                        entry.getLong("createdAt")
                );
                data.islands.put(record.ownerId(), record);
            } catch (RuntimeException ignored) {
                // A malformed island record must not prevent other owners from loading.
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("nextSlot", nextSlot);
        NbtList list = new NbtList();
        for (PrivateIslandRecord record : islands.values()) {
            NbtCompound entry = new NbtCompound();
            entry.putString("owner", record.ownerId().toString());
            entry.putString("dimension", record.dimension());
            entry.putInt("centerX", record.centerX());
            entry.putInt("centerZ", record.centerZ());
            entry.putInt("surfaceY", record.surfaceY());
            entry.putInt("radius", record.radius());
            entry.putInt("thickness", record.thickness());
            entry.putDouble("spawnX", record.spawnX());
            entry.putDouble("spawnY", record.spawnY());
            entry.putDouble("spawnZ", record.spawnZ());
            entry.putLong("createdAt", record.createdAt());
            list.add(entry);
        }
        nbt.put("islands", list);
        nbt.putInt("dataVersion", 1);
        return nbt;
    }
}
