# Core Public API v3 — Life Skills Integration Contract

**Status:** Implemented in Core source; local build and runtime verification pending  
**Core Mod ID:** `skyblockrpg`  
**API marker:** `CoreApi.API_VERSION = 3`  
**Target:** Minecraft 1.21.1 / Fabric / Java 21

Core remains the primary game mod. This API is the supported boundary for optional extensions such as `skyblock_life`; it is not a second gameplay framework.

## 1. Dependency and compatibility

Life Skills must declare a hard Fabric mod dependency on `skyblockrpg`. It must not copy Core classes or access profile internals.

Public entry points live under `com.skyrpg.skyblock.api`.

| API | Version | Purpose |
|---|---:|---|
| `CoreApi` | 3 | Aggregate compatibility marker |
| `SkillAPI` | 1 | Namespaced skill registration and progression |
| `PlayerDataAPI` | 1 | Extension-owned persistent player data |
| `StatAPI` | 1 | Read Core's shared stat pipeline |
| `ItemAPI` | 1 | Read API-owned item views from Core/extension data packs |
| `InventoryAPI` | 1 | Safe access to the logical SkyBlock inventory |
| `CurrencyAPI` | 1 | Purse and Bank operations |
| `RewardAPI` | 1 | Common item, coin and skill-XP reward facade |

The individual API versions are independent. Check `CoreApi.API_VERSION >= 3` before using the v2 surface.

## 2. Skill API

Class: `com.skyrpg.skyblock.api.SkillAPI`

Life Skills registers these IDs during its mod initialization:

- `skyblock_life:mining`
- `skyblock_life:fishing`
- `skyblock_life:farming`
- `skyblock_life:foraging`

Use the API-owned `SkillDefinition` record to define display name, max level and XP curve. It is located at `com.skyrpg.skyblock.api.SkillDefinition`.

```java
Identifier mining = Identifier.of("skyblock_life", "mining");
boolean registered = SkillAPI.registerSkill(
    new SkillDefinition(mining, "Mining", 50, miningXpCurve)
);
double xp = SkillAPI.getExperience(player, mining);
int level = SkillAPI.getLevel(player, mining);
double progress = SkillAPI.getProgress(player, mining); // 0.0..1.0
double remaining = SkillAPI.getExperienceToNextLevel(player, mining);
boolean awarded = SkillAPI.addExperience(player, mining, 25.0);
```

Rules:
- IDs must be namespaced.
- Duplicate registration returns false and does not replace the original definition.
- XP can only be awarded to a registered skill.
- Core owns XP and level persistence; Life Skills must not store duplicate XP/level values.
- A skill definition's curve is an XP requirement per level. Do not use guessed values as verified Hypixel data.

## 4A. Skill level stat rewards

Extensions may register exact stat rewards for their own registered skill IDs:

```java
SkillAPI.registerStatReward(
    Identifier.of("skyblock_life", "mining"),
    10,
    StatType.MINING_FORTUNE,
    5.0,
    StatModifier.Operation.ADDITIVE
);
```

The reward becomes active at the required level and remains active at higher levels. Multiple level thresholds may be registered to create stepwise progression. Duplicate reward keys are rejected. The skill must already be registered, and the reward skill ID must not use the reserved Core namespace.

The amount and threshold are explicit content data supplied by the extension. Core does not derive bonuses from the XP curve, and the smoke-test values are not Hypixel values. Register only values supported by the extension's verified data source.

## 3. Player data API

Class: `PlayerDataAPI`

Use one namespaced data key per gameplay family:

- `skyblock_life:mining`
- `skyblock_life:fishing`
- `skyblock_life:farming`
- `skyblock_life:foraging`

```java
Identifier key = Identifier.of("skyblock_life", "mining");
NbtCompound data = PlayerDataAPI.get(player, key).orElseGet(NbtCompound::new);
data.putLong("blocks_mined", data.getLong("blocks_mined") + 1L);
PlayerDataAPI.set(player, key, data);
```

`get` returns a defensive copy. Mutations must be written back through `set`. Core retains extension data even while that extension is absent. Extension keys must use the extension's own namespace; the reserved `skyblockrpg` namespace is rejected by this API.

## 4. Stat API

Class: `StatAPI`

```java
double miningSpeed = StatAPI.getFinal(player, StatType.MINING_SPEED);
double miningFortune = StatAPI.getFinal(player, StatType.MINING_FORTUNE);
double farmingSpeed = StatAPI.getFinal(player, StatType.FARMING_SPEED);
double farmingFortune = StatAPI.getFinal(player, StatType.FARMING_FORTUNE);
double foragingFortune = StatAPI.getFinal(player, StatType.FORAGING_FORTUNE);
double fishingSpeed = StatAPI.getFinal(player, StatType.FISHING_SPEED);
double fishingFortune = StatAPI.getFinal(player, StatType.FISHING_FORTUNE);
double seaCreatureChance = StatAPI.getFinal(player, StatType.SEA_CREATURE_CHANCE);
```

The read API remains read-only. `StatEffectAPI` (v3) separately permits extension-owned BUFF / TEMPORARY / OTHER modifiers. It does not expose base, equipment, accessory or skill-source writes.

Current life-skill stat IDs include:
- `MINING_SPEED`, `MINING_FORTUNE`
- `FARMING_SPEED`, `FARMING_FORTUNE`
- `FORAGING_SPEED`, `FORAGING_FORTUNE`
- `FISHING_SPEED`, `FISHING_FORTUNE`, `SEA_CREATURE_CHANCE`
- `WISDOM_MINING`, `WISDOM_FARMING`, `WISDOM_FORAGING`, `WISDOM_FISHING`

This is a fixed Core stat catalogue, not arbitrary runtime stat registration. New shared stat types require a Core change and API compatibility review. Life Skills owns its local mechanic modifiers (for example a temporary fishing condition) and combines them in its own resolver; Core-owned and registered extension effects contribute to `StatAPI.getFinal` through the shared pipeline.

## 5. Item API and extension item data

Classes: `ItemAPI`, `ItemView`

Item definitions remain owned by Core's shared item registry. The registry now discovers `data/<namespace>/skyblock/items/*.json` resources from all namespaces, not only Core's namespace.

For an extension item, use a stable namespaced ID in its JSON definition:

```json
{
  "id": "skyblock_life:refined_mithril",
  "name": "Refined Mithril",
  "rarity": "RARE",
  "type": "MATERIAL",
  "max_stack_size": 64,
  "stats": {},
  "lore": ["A refined mining material."]
}
```

Resource location example:
`src/main/resources/data/skyblock_life/skyblock/items/refined_mithril.json`

Lookup:

```java
ItemView item = ItemAPI.get(Identifier.of("skyblock_life", "refined_mithril"))
    .orElseThrow();
```

Rules:
- Extension definitions must use the extension namespace in their IDs.
- Existing Core IDs without a namespace remain compatible.
- Item definitions are loaded when the integrated/dedicated server starts; do not assume they are available during the extension's `onInitialize` call.
- This API exposes definitions, not a complete vanilla Item registration or unique ItemInstance system.
- Supported item stat keys are the Core `StatType` IDs. Unknown stat keys cause that definition to be rejected and logged.
- Life Skills must not mutate the registry or replace another mod's item ID.

## 6. Inventory API

Class: `InventoryAPI`

```java
Identifier itemId = Identifier.of("skyblock_life", "refined_mithril");
int count = InventoryAPI.getCount(player, itemId);
boolean inserted = InventoryAPI.add(player, itemId, 3);
boolean removed = InventoryAPI.remove(player, itemId, 1);
```

Operations target the Core logical SkyBlock inventory, not Minecraft's vanilla inventory. Add/remove return false when the item is unknown, count is invalid, capacity is insufficient or the player lacks the requested amount. Successful changes mark the Core profile dirty.

## 7. Currency API

Class: `CurrencyAPI`

- `getPurse(player)`
- `getBank(player)`
- `addPurse(player, amount)`
- `removePurse(player, amount)`
- `deposit(player, amount)`
- `withdraw(player, amount)`

All amounts are positive integer coins. Methods return false for invalid amounts, insufficient balance or overflow. Do not directly mutate profile coin fields.

## 8. Reward API

Class: `RewardAPI`

```java
RewardAPI.giveItem(player, itemId, amount);
RewardAPI.giveCoins(player, coins);
RewardAPI.giveSkillExperience(player, miningSkillId, xp);
```

Each method returns whether that individual reward succeeded. This is not yet a multi-reward transaction: if several rewards form one indivisible result, the extension must validate capacity and balances before applying them, or request a transactional reward API from Core. Never silently ignore a false return.

## 9. Events and Minecraft actions

For block breaking, harvesting and vanilla fishing hooks, Life Skills should use Fabric's server-side callbacks and validate player/action/world state itself. The first Core API v3 release does not publish a generic gameplay event bus or quest-objective callback.

Do not award XP from client-side callbacks. Prevent duplicate processing and distinguish player actions from automation or unrelated mod changes.

## 10. Explicitly not included in v3

- Runtime registration of arbitrary new StatType values.
- Arbitrary runtime StatType registration or direct base/equipment/accessory/skill source writes.
- Unique ItemInstance / item NBT API.
- Multi-reward atomic transaction API.
- Quest objective API.
- Generic Core gameplay event bus.
- Core-owned collection framework.

These are not implied by the read APIs above. Submit a request before depending on them.

## 11. Compatibility rules

- Do not change a published method signature without an API version plan.
- New API classes may be added without breaking existing v1 entry points.
- Extension-facing method signatures should use API-owned DTOs rather than Core implementation classes.
- Breaking changes require a Core API marker increment and migration notes.
- Core-only startup must continue to work with no Life Skills or Dungeons installed.
- Extension data must survive uninstall/reinstall of an optional module.
