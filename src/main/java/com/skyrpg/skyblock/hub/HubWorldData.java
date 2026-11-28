package com.skyrpg.skyblock.hub;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

/** Persistent, project-controlled Hub landing point, configured only after map assembly. */
public final class HubWorldData extends PersistentState {
    public static final String STORAGE_ID = "skyblockrpg_hub";
    public static final PersistentState.Type<HubWorldData> TYPE =
            new PersistentState.Type<>(HubWorldData::new, HubWorldData::fromNbt, DataFixTypes.LEVEL);

    public record Spawn(String dimension, double x, double y, double z, float yaw, float pitch) {}
    private Spawn spawn;

    private HubWorldData() {}

    public static HubWorldData get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, STORAGE_ID);
    }

    public Spawn getSpawn() { return spawn; }

    public void setSpawn(Spawn value) {
        if (value == null) throw new IllegalArgumentException("Hub spawn is required");
        spawn = value;
        markDirty();
    }

    private static HubWorldData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        HubWorldData data = new HubWorldData();
        if (nbt.getBoolean("spawnConfigured")) {
            try {
                data.spawn = new Spawn(nbt.getString("dimension"),
                        nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"),
                        nbt.getFloat("yaw"), nbt.getFloat("pitch"));
            } catch (RuntimeException ignored) {
                data.spawn = null;
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean("spawnConfigured", spawn != null);
        if (spawn != null) {
            nbt.putString("dimension", spawn.dimension());
            nbt.putDouble("x", spawn.x());
            nbt.putDouble("y", spawn.y());
            nbt.putDouble("z", spawn.z());
            nbt.putFloat("yaw", spawn.yaw());
            nbt.putFloat("pitch", spawn.pitch());
        }
        nbt.putInt("dataVersion", 1);
        return nbt;
    }
}
