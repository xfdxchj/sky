package com.skyrpg.skyblock.accessory;
import com.skyrpg.skyblock.inventory.PlayerInventory;
import com.skyrpg.skyblock.stats.StatModifier;
import com.skyrpg.skyblock.stats.StatType;
import java.util.*;
public final class AccessoryPowerManager {
 private AccessoryPowerManager() {}
 public static int getAccessoryPower(PlayerInventory inv,boolean dungeon){int total=0; for(String id:inv.getEffectiveAccessories()){var d=com.skyrpg.skyblock.item.ItemRegistry.get(id).orElse(null); if(d!=null){int v=d.accessoryPower(); if(dungeon&&d.dungeonAccessory())v*=2; total+=v;}} return total;}
 public static double getStatsMultiplier(int ap){return ap<=0?0.0:29.97*Math.pow(Math.log(0.0019*ap+1.0),1.2);}
 public static List<StatModifier> getPowerModifiers(String selected,int ap){PowerDefinition p=PowerRegistry.get(selected).orElse(null); if(p==null||ap<=0)return List.of(); double m=getStatsMultiplier(ap); List<StatModifier> out=new ArrayList<>(); p.stats().forEach((s,v)->out.add(new StatModifier("accessory_power:"+p.id(),s,v*m,StatModifier.Operation.ADDITIVE))); return out;}
 public static int getTuningPoints(int ap){return Math.max(0,ap/10);}
 public static double tuningValue(StatType s){return switch(s){case HEALTH,DEFENSE,SPEED,STRENGTH,CRIT_DAMAGE,INTELLIGENCE->1.0;case CRIT_CHANCE->0.5;case ATTACK_SPEED->0.3;default->0.0;};}
 public static boolean isTunable(StatType s){return tuningValue(s)>0;}
}
