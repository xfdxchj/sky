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

/** World-persistent one-NPC-per-Hub-POI placement registry. */
public final class HubNpcWorldData extends PersistentState {
    public static final String STORAGE_ID = "skyblockrpg_hub_npcs";
    public static final PersistentState.Type<HubNpcWorldData> TYPE =
            new PersistentState.Type<>(HubNpcWorldData::new, HubNpcWorldData::fromNbt, DataFixTypes.LEVEL);

    public record Placement(String poiId, String npcId, String entityUuid, String dimension,
                            double x, double y, double z) {}

    private final Map<String, Placement> placements = new LinkedHashMap<>();
    private HubNpcWorldData() {}

    public static HubNpcWorldData get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, STORAGE_ID);
    }
    public Placement get(String poiId) { return placements.get(key(poiId)); }
    public Collection<Placement> all() { return List.copyOf(placements.values()); }
    public void put(Placement placement) {
        placements.put(key(placement.poiId()), placement);
        markDirty();
    }
    public boolean remove(String poiId) {
        boolean removed = placements.remove(key(poiId)) != null;
        if (removed) markDirty();
        return removed;
    }
    private static String key(String id) { return id == null ? "" : id.toLowerCase(Locale.ROOT); }

    private static HubNpcWorldData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        HubNpcWorldData data = new HubNpcWorldData();
        NbtList list = nbt.getList("placements", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound tag = list.getCompound(i);
            try {
                Placement placement = new Placement(tag.getString("poiId"), tag.getString("npcId"),
                        tag.getString("entityUuid"), tag.getString("dimension"),
                        tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
                if (!placement.poiId().isBlank() && !placement.entityUuid().isBlank()
                        && !placement.dimension().isBlank() && Double.isFinite(placement.x())
                        && Double.isFinite(placement.y()) && Double.isFinite(placement.z())) {
                    data.placements.put(key(placement.poiId()), placement);
                }
            } catch (RuntimeException ignored) {
                // Ignore a malformed record without discarding valid placements.
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Placement placement : placements.values()) {
            NbtCompound tag = new NbtCompound();
            tag.putString("poiId", placement.poiId());
            tag.putString("npcId", placement.npcId());
            tag.putString("entityUuid", placement.entityUuid());
            tag.putString("dimension", placement.dimension());
            tag.putDouble("x", placement.x());
            tag.putDouble("y", placement.y());
            tag.putDouble("z", placement.z());
            list.add(tag);
        }
        nbt.put("placements", list);
        nbt.putInt("dataVersion", 1);
        return nbt;
    }
}
