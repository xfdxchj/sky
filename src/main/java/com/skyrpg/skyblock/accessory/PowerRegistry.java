package com.skyrpg.skyblock.accessory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyrpg.skyblock.SkyBlockRPG;
import com.skyrpg.skyblock.stats.StatType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import java.io.Reader;
import java.util.*;
public final class PowerRegistry {
 private static final Map<String,PowerDefinition> POWERS=new LinkedHashMap<>();
 private PowerRegistry() {}
 public static void reload(ResourceManager manager) {
  Map<String,PowerDefinition> loaded=new LinkedHashMap<>();
  Map<Identifier,Resource> resources=manager.findResources("skyblock/powers",id->id.getNamespace().equals(SkyBlockRPG.MOD_ID)&&id.getPath().endsWith(".json"));
  for(var entry:resources.entrySet()) try(Reader reader=entry.getValue().getReader()) {
   JsonObject j=JsonParser.parseReader(reader).getAsJsonObject();
   String id=req(j,"id").toLowerCase(Locale.ROOT), name=j.has("name")?j.get("name").getAsString():id;
   String stone=j.has("power_stone_item_id")?j.get("power_stone_item_id").getAsString():"";
   boolean starter=j.has("starter")&&j.get("starter").getAsBoolean();
   Map<StatType,Double> stats=new EnumMap<>(StatType.class);
   if(j.has("stats")) for(var e:j.getAsJsonObject("stats").entrySet()) { StatType t=StatType.fromId(e.getKey()).orElseThrow(); double v=e.getValue().getAsDouble(); if(!Double.isFinite(v)) throw new IllegalArgumentException("Non-finite stat"); stats.put(t,v); }
   if(loaded.putIfAbsent(id,new PowerDefinition(id,name,stats,stone,starter))!=null) throw new IllegalArgumentException("Duplicate power "+id);
  } catch(Exception e) { SkyBlockRPG.LOGGER.error("Failed to load power definition from {}",entry.getKey(),e); }
  POWERS.clear(); POWERS.putAll(loaded); SkyBlockRPG.LOGGER.info("Loaded {} accessory powers.",POWERS.size());
 }
 public static Optional<PowerDefinition> get(String id){return Optional.ofNullable(POWERS.get(id==null?"":id.toLowerCase(Locale.ROOT)));}
 public static Collection<PowerDefinition> all(){return Collections.unmodifiableCollection(POWERS.values());}
 private static String req(JsonObject j,String k){if(!j.has(k)||j.get(k).isJsonNull())throw new IllegalArgumentException("Missing "+k);return j.get(k).getAsString();}
}
