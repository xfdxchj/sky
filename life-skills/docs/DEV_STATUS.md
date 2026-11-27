# SkyBlock RPG Life Skills — 开发状态

**版本：** `0.1.0-alpha.1`
**环境：** Minecraft 1.21.1 / Fabric / Java 21 / Gradle 8.12.1
**依赖：** `skyblockrpg` Core（API v3）
**整理时间：** 2026-11-27

---

## 1. 已完成

### 技能注册与等级奖励
- 注册四个技能：`skyblock_life:mining` / `fishing` / `farming` / `foraging`
- 每个技能 60 级，等级奖励通过 Core `SkillAPI.registerStatReward` 注册
- 奖励按参照项目配置：
  - Mining：每级 +4 Mining Fortune、+1 Defense（15 级后 +2）
  - Farming：每级 +4 Farming Fortune、+2 Health
  - Foraging：每级 +4 Foraging Fortune、+1 Strength
  - Fishing：每级 +2 Health

### Mining
- Handler 体系：Pickaxe / Axe / Hoe / Instant
- 28 个可挖方块（20 基础 + 8 矮人矿）
- `BreakingTask` 等价实现：`PlayerBreakingSpeedMixin` 自定义挖掘时间
- 掉落、Mining Fortune、经验、统计

### Foraging
- 8 种树（6 原版 + fig + mangrove）
- 树发现、整树砍伐、树叶衰败、30 秒再生、树礼物
- 砍树进度 action bar
- 完成/再生音效（参照项目用 `entity.creaking.death`；1.21.1 无此音效，用 `entity.wither.death` 替代）

### Farming
- 11 种作物（小麦、胡萝卜、土豆、甜菜根、甘蔗、仙人掌、棕/红蘑菇、地狱疣、南瓜、西瓜）
- 收获、Farming Fortune、经验、统计

### Fishing
- 21 种海怪（14 水 + 7 熔岩）
- 18 种奖杯鱼（bronze/silver/gold/diamond 四档）
- 10 种普通鱼（按权重）
- Sea Creature 生成与击杀奖励
- 钓获 mixin：`FishingBobberMixin`

### 数据驱动
所有内容数据在 `data/skyblock_life/skyblock/`：

```
mining/blocks.json         28 个方块
foraging/trees.json        8 种树
farming/crops.json         11 种作物
fishing/fish.json          10 种鱼
fishing/sea_creatures.json 21 种海怪
fishing/trophy_fish.json   18 种奖杯鱼
items/*.json               113 个物品
```

加载器：`MiningDataLoader`、`ForagingDataLoader`、`FarmingDataLoader`、`FishingDataLoader`。
服务器的 `SERVER_STARTED` 事件中加载，文件缺失时保留 Java 默认数据。

### 命令
- `/skyblock_life skills` — 四技能等级/经验/进度
- `/skyblock_life stats` — 各技能统计
- `/skyblock_life fortune` — 当前 Fortune 属性

---

## 2. 未完成

| 项 | 原因 |
|---|---|
| GUI | Core 没有 GUI 扩展点，需要 Fabric 客户端 Screen |
| 图鉴 | 需要收集记录（Core 无 Collection API） |
| 鱼饵 / 鱼竿部件 | 依赖参照项目的自定义物品组件，Core 无 ItemInstance |
| 钓鱼热点 | 依赖地图/区域坐标 |
| 矿区配置（GoldMine 等） | 地图内容，明确不做 |
| Garden 系统 | 依赖 Garden 世界 |
| helix 树 | 使用 `pale_oak_log`，1.21.1 不存在 |

---

## 3. 文件结构

```
skyblock_life/
├── build.gradle
├── gradle.properties
├── fabric.mod.json
├── libs/skyblock-rpg-0.1.0-alpha.3.jar
├── src/main/java/com/skyrpg/life/
│   ├── SkyBlockLife.java
│   ├── command/LifeSkillCommands.java
│   ├── common/
│   │   ├── data/       LifeDataStore, LifeResourceReader
│   │   ├── drop/       DropEntry, DropTable
│   │   ├── fortune/    FortuneSource
│   │   ├── reward/     LifeRewardService
│   │   └── scheduler/  LifeScheduler
│   ├── mining/         handler + 方块 + loader
│   ├── foraging/       tree + loader
│   ├── farming/        crop + loader
│   ├── fishing/        海怪 + 奖杯鱼 + loader + SeaCreatureManager
│   ├── mixin/          PlayerBreakingSpeedMixin, FishingBobberMixin
│   └── skill/          LifeSkills, LifeSkillRegistry
└── src/main/resources/
    ├── fabric.mod.json
    ├── skyblock_life.mixins.json
    └── data/skyblock_life/skyblock/...
```

---

## 4. 构建

```bash
# 1. 把 Core 生产 jar 放到 libs/
#    libs/skyblock-rpg-0.1.0-alpha.3.jar
# 2. 构建
gradle build
```

产物：`build/libs/skyblock-life-0.1.0-alpha.1.jar`

---

## 5. 已知限制

- 未做 `runClient` 运行时验证，只验证了编译
- 未覆盖原版掉落（挖矿/砍树/收获仍会掉原版物品，SkyBlock 物品进逻辑背包）
- 矮人矿用伪装方块（GRAY_WOOL、POLY_DIORITE 等），与参照项目一致
- Fortune 公式为占位（每 100 Fortune +1 掉落），非 Hypixel 数值
- 技能 XP 曲线为占位（每级 100 XP），非 Hypixel 数值
