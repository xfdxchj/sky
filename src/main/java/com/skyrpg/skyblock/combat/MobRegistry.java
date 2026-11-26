package com.skyrpg.skyblock.combat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import java.io.Reader;
import java.util.*;

public final class MobRegistry {
    private static final MobDefinition UNREGISTERED = new MobDefinition(
            "unregistered", "Unregistered Creature", "", 0, 0, 0, 0, 0, 0,
            "project-placeholder", "", "", "none", "unregistered", "unknown",
            List.of(), "unknown", List.of());
    private static final Map<String, MobDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<String, MobDefinition> ENTITY_TYPES = new HashMap<>();
    private MobRegistry() {}

    public static void reload(ResourceManager manager) {
        Map<String, MobDefinition> loaded = new LinkedHashMap<>();
        Map<String, MobDefinition> byEntity = new HashMap<>();
        Map<Identifier, Resource> resources = manager.findResources("skyblock/mobs",
                id -> id.getNamespace().equals("skyblockrpg") && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject j = JsonParser.parseReader(reader).getAsJsonObject();
                String id = required(j, "id");
                List<String> types = new ArrayList<>();
                if (j.has("mob_types") && j.get("mob_types").isJsonArray())
                    j.getAsJsonArray("mob_types").forEach(v -> types.add(v.getAsString()));
                List<MobDropDefinition> drops = new ArrayList<>();
                if (j.has("drops") && j.get("drops").isJsonArray()) {
                    for (var value : j.getAsJsonArray("drops")) {
                        JsonObject d = value.getAsJsonObject();
                        drops.add(new MobDropDefinition(optional(d,"item_id",""),
                                integer(d,"min_count",0), integer(d,"max_count",0),
                                number(d,"chance",0.0), bool(d,"chance_known",d.has("chance")), optional(d,"drop_type","item")));
                    }
                }
                MobDefinition d = new MobDefinition(id, optional(j,"name",id), optional(j,"entity_type",""),
                    integer(j,"level",0), number(j,"health",0), number(j,"damage",0), number(j,"defense",0),
                    longNumber(j,"coin_reward",0), number(j,"combat_xp_reward",0),
                    optional(j,"source_type","unknown"), optional(j,"source_url",""),
                    optional(j,"retrieved_at",""), optional(j,"confidence","unverified"),
                    optional(j,"family_id",id), optional(j,"category","unknown"), types,
                    optional(j,"area","unknown"), drops);
                if (loaded.putIfAbsent(d.id().toLowerCase(Locale.ROOT), d) != null)
                    throw new IllegalArgumentException("Duplicate mob id: " + d.id());
                if (!d.entityType().isBlank() && byEntity.putIfAbsent(d.entityType(), d) != null)
                    throw new IllegalArgumentException("Duplicate entity mapping: " + d.entityType());
            } catch (Exception e) {
                SkyBlockRPG.LOGGER.error("Failed to load mob data from {}", entry.getKey(), e);
            }
        }
        DEFINITIONS.clear(); DEFINITIONS.putAll(loaded);
        ENTITY_TYPES.clear(); ENTITY_TYPES.putAll(byEntity);
        SkyBlockRPG.LOGGER.info("Loaded {} SkyBlock mob definitions ({} entity mappings).",
                DEFINITIONS.size(), ENTITY_TYPES.size());
    }
    public static MobDefinition get(Entity entity) {
        if (entity == null) return UNREGISTERED;
        String id = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        return ENTITY_TYPES.getOrDefault(id, UNREGISTERED);
    }
    public static MobDefinition getAttackingMob(DamageSource source) {
        if (source == null) return UNREGISTERED;
        Entity attacker = source.getAttacker();
        if (attacker instanceof ProjectileEntity projectile && projectile.getOwner() != null)
            attacker = projectile.getOwner();
        if (attacker == null && source.getSource() instanceof ProjectileEntity projectile)
            attacker = projectile.getOwner();
        return attacker == null ? UNREGISTERED : get(attacker);
    }
    public static Optional<MobDefinition> getById(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id == null ? "" : id.toLowerCase(Locale.ROOT)));
    }
    public static Collection<MobDefinition> all() {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }
    private static String required(JsonObject j,String k) {
        if (!j.has(k) || j.get(k).isJsonNull()) throw new IllegalArgumentException("Missing " + k);
        return j.get(k).getAsString();
    }
    private static String optional(JsonObject j,String k,String d) {
        return j.has(k) && !j.get(k).isJsonNull() ? j.get(k).getAsString() : d;
    }
    private static int integer(JsonObject j,String k,int d) { return j.has(k) ? j.get(k).getAsInt() : d; }
    private static long longNumber(JsonObject j,String k,long d) { return j.has(k) ? j.get(k).getAsLong() : d; }
    private static double number(JsonObject j,String k,double d) { return j.has(k) ? j.get(k).getAsDouble() : d; }
    private static boolean bool(JsonObject j,String k,boolean d) { return j.has(k) ? j.get(k).getAsBoolean() : d; }
}