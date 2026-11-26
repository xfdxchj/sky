package com.skyrpg.skyblock.combat;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World-persistent SkyBlock HP pools keyed by Minecraft entity UUID.
 * Vanilla health is used only as a visual proportion; this data is authoritative
 * for registered SkyBlock mobs attacked through the SkyBlock weapon pipeline.
 */
public final class SkyBlockMobHealthData extends PersistentState {
    public static final String STORAGE_ID = "skyblockrpg_mob_health";
    public static final PersistentState.Type<SkyBlockMobHealthData> TYPE =
            new PersistentState.Type<>(SkyBlockMobHealthData::new, SkyBlockMobHealthData::fromNbt, DataFixTypes.LEVEL);

    private final Map<UUID, MobHealth> mobs = new HashMap<>();

    public record MobHealth(String mobId, double current, double maximum) {}

    public static SkyBlockMobHealthData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, STORAGE_ID);
    }

    public double getOrCreate(LivingEntity entity, MobDefinition definition) {
        MobHealth state = mobs.get(entity.getUuid());
        if (state == null || !state.mobId().equals(definition.id()) || state.maximum() <= 0.0) {
            state = new MobHealth(definition.id(), definition.health(), definition.health());
            mobs.put(entity.getUuid(), state);
            markDirty();
        }
        return state.current();
    }

    public void setCurrent(LivingEntity entity, MobDefinition definition, double current) {
        MobHealth old = mobs.get(entity.getUuid());
        double maximum = old != null && old.mobId().equals(definition.id())
                ? old.maximum() : definition.health();
        double safeCurrent = Double.isFinite(current) ? Math.max(0.0, Math.min(current, maximum)) : 0.0;
        mobs.put(entity.getUuid(), new MobHealth(definition.id(), safeCurrent, maximum));
        markDirty();
    }

    public void remove(LivingEntity entity) {
        if (mobs.remove(entity.getUuid()) != null) markDirty();
    }

    private static SkyBlockMobHealthData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        SkyBlockMobHealthData data = new SkyBlockMobHealthData();
        NbtList list = nbt.getList("mobs", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            try {
                UUID uuid = UUID.fromString(entry.getString("uuid"));
                String mobId = entry.getString("mobId");
                double maximum = entry.getDouble("maximum");
                double current = entry.getDouble("current");
                if (mobId.isBlank() || !Double.isFinite(maximum) || maximum <= 0.0 || !Double.isFinite(current)) continue;
                data.mobs.put(uuid, new MobHealth(mobId, Math.max(0.0, Math.min(current, maximum)), maximum));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed mob entries without losing other saved HP pools.
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (var entry : mobs.entrySet()) {
            NbtCompound mob = new NbtCompound();
            mob.putString("uuid", entry.getKey().toString());
            mob.putString("mobId", entry.getValue().mobId());
            mob.putDouble("current", entry.getValue().current());
            mob.putDouble("maximum", entry.getValue().maximum());
            list.add(mob);
        }
        nbt.put("mobs", list);
        nbt.putInt("dataVersion", 1);
        return nbt;
    }
}