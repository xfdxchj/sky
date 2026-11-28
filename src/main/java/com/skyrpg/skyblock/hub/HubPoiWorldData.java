package com.skyrpg.skyblock.hub;

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
import java.util.Locale;
import java.util.Map;

/** Project-local, measured runtime anchors for Hub POIs. Wiki reference coordinates are never used here. */
public final class HubPoiWorldData extends PersistentState {
    public static final String STORAGE_ID = "skyblockrpg_hub_poi_anchors";
    public static final PersistentState.Type<HubPoiWorldData> TYPE =
            new PersistentState.Type<>(HubPoiWorldData::new, HubPoiWorldData::fromNbt, DataFixTypes.LEVEL);

    public record Anchor(String poiId, String dimension, double x, double y, double z, float yaw, float pitch) {}

    private final Map<String, Anchor> anchors = new LinkedHashMap<>();
    private HubPoiWorldData() {}

    public static HubPoiWorldData get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, STORAGE_ID);
    }

    public Anchor get(String poiId) { return anchors.get(key(poiId)); }
    public Collection<Anchor> all() { return List.copyOf(anchors.values()); }

    public void put(Anchor anchor) {
        anchors.put(key(anchor.poiId()), anchor);
        markDirty();
    }

    public boolean remove(String poiId) {
        boolean removed = anchors.remove(key(poiId)) != null;
        if (removed) markDirty();
        return removed;
    }

    private static String key(String id) { return id == null ? "" : id.toLowerCase(Locale.ROOT); }

    private static HubPoiWorldData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        HubPoiWorldData data = new HubPoiWorldData();
        NbtList list = nbt.getList("anchors", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound tag = list.getCompound(i);
            try {
                Anchor anchor = new Anchor(tag.getString("poiId"), tag.getString("dimension"),
                        tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"),
                        tag.getFloat("yaw"), tag.getFloat("pitch"));
                if (!anchor.poiId().isBlank() && !anchor.dimension().isBlank()
                        && Double.isFinite(anchor.x()) && Double.isFinite(anchor.y()) && Double.isFinite(anchor.z())
                        && Float.isFinite(anchor.yaw()) && Float.isFinite(anchor.pitch())) {
                    data.anchors.put(key(anchor.poiId()), anchor);
                }
            } catch (RuntimeException ignored) {
                // Ignore malformed anchors without discarding valid entries.
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Anchor anchor : anchors.values()) {
            NbtCompound tag = new NbtCompound();
            tag.putString("poiId", anchor.poiId());
            tag.putString("dimension", anchor.dimension());
            tag.putDouble("x", anchor.x());
            tag.putDouble("y", anchor.y());
            tag.putDouble("z", anchor.z());
            tag.putFloat("yaw", anchor.yaw());
            tag.putFloat("pitch", anchor.pitch());
            list.add(tag);
        }
        nbt.put("anchors", list);
        nbt.putInt("dataVersion", 1);
        return nbt;
    }
}
