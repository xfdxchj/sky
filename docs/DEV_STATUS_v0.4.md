# SkyBlock RPG 开发状态 v0.4

> 本文是项目当前状态的事实记录；开发任务与验收标准见 [DEV_TASKS_v0.4.md](DEV_TASKS_v0.4.md)。
> 整理日期：2026-11-27
> 目标：尽可能还原当前 Hypixel SkyBlock 的核心系统与内容，采用 Minecraft 1.21.1 / Fabric / Java 21，单人运行。
> 重要：本文区分代码存在、构建通过、游戏内验证通过，不将三者混为一谈。

---

## 1. 当前代码基线

### 1.1 代码位置

| 引用 | 当前情况 |
|---|---|
| 默认分支 `main` | 已整合 P0 至 P5.2 的累计功能代码，并保留 v0.4 开发基线文档 |
| `p5.2-island-connections` | P5.2 集成来源分支；其代码已并入 main，后续可继续保留作历史参考 |
| 后续开发 | 默认直接在 `main` 上继续；只有改动风险、并行工作或回滚需求明显时才使用临时分支 |

P5.2 的功能代码已通过双父提交整合进 main。后续开发与测试以 main 为准；不要继续把旧分支当成唯一的完整代码入口。

### 1.2 项目环境

- Minecraft Java Edition 1.21.1
- Java 21
- Fabric Loader 0.16.x
- Fabric API 0.116.x
- Fabric Loom 1.9.2
- Mod ID：`skyblockrpg`
- Package：`com.skyrpg.skyblock`
- 单人 Integrated Server；服务端权威逻辑，客户端负责界面
- 使用 Minecraft 原生世界与存档；不依赖外部服务

---

## 2. 阶段状态总览

| 阶段 | 状态 | 事实说明 |
|---|---|---|
| P0 工程与基础数据 | ✅ 基础完成 | Fabric 入口、玩家档案、金币、技能经验、基础属性、PersistentState 和命令 |
| P1 玩家系统 | 🟡 基础已有 | 档案、属性和技能框架存在；统一属性来源、RPG 生命、技能成长效果和 HUD 未完成 |
| P2 物品与装备 | 🟡 基础已有 | JSON 物品定义、逻辑背包、装备、饰品包、Power、Tuning、Reforge 基础存在；物品实例、附魔、原版物品注册和完整 Tooltip 未完成 |
| P3 战斗与怪物 | 🟡 基础已有 | 伤害计算、怪物 JSON、独立怪物 HP、玩家受伤桥接和手动生成存在；掉落闭环、自然刷怪、AI 与完整战斗反馈未完成 |
| P4 成长与解锁 | 🟡 基础已有 | NPC、任务基础、对话和 GUI 框架存在；Collections、配方、Bestiary 和完整目标类型未完成 |
| P5 世界与地图 | 🟡 框架已有 | 区域、Warp、岛屿、NBT 结构导入、桥梁和传送路线已有；真实地图内容、私人岛完整玩法和地图保护未完成 |
| P6 生活职业 | ⬜ 未开始 | Mining、Farming、Fishing、Foraging 的完整循环未实现 |
| P7 自动化与经济扩展 | 🟡 经济基础已有 | Purse、Bank、商店已有；Minion、仓库扩展和单机市场模拟未完成 |
| P8 Slayer 与 Boss | ⬜ 未开始 | 尚无完整 Slayer 任务和 Boss 战斗闭环 |
| P9 Dungeon | ⬜ 未开始 | 尚无实例、房间、谜题、Secrets 和 Floor 流程 |
| P10 后期内容 | ⬜ 未开始 | Crimson Isle、Rift、高级 Mining 等 |
| PX 整合与发布 | ⬜ 未开始 | 全系统兼容、性能、平衡、存档迁移和发布流程 |

状态含义：
- ✅ 基础阶段目标已完成；不代表整个 Hypixel 对应玩法全部还原。
- 🟡 已有可复用实现，但仍有明确缺口。
- ⬜ 尚未开始实质实现。

---

## 3. 已有代码模块（按 P5.2 累计代码核对）

### 3.1 玩家、档案与成长基础

- `PlayerProfile`：UUID 玩家档案、Purse、Bank、技能经验、属性、逻辑背包及部分玩法进度。
- `SkyBlockWorldData`：世界级 `PersistentState`。
- `ProfileManager`：玩家档案获取、创建和保存管理。
- `PlayerStats`、`StatType`、`StatModifier`：属性定义和最终属性修正。
- `SkillType`：12 个内部技能 ID；它们是技术注册起点，不代表当前 Hypixel 所有技能玩法均已实现。
- `SkillDefinition`：当前仍使用临时 XP 曲线，不是最终 Hypixel 经验表。
- `CurrencyManager`、`BankManager`：Purse 和 Bank 的基础操作。

### 3.2 物品、装备与饰品

- `ItemDefinition`、`ItemRegistry`：从 JSON 加载物品定义。
- `PlayerInventory`：逻辑背包和装备槽数据。
- `EquipmentSlot`：Weapon、Helmet、Chestplate、Leggings、Boots、Accessory。
- `PowerDefinition`、`PowerRegistry`、`AccessoryPowerManager`：饰品 Power 基础。
- `ReforgeDefinition`、`ReforgeRegistry`、`ReforgeManager`：基础 Reforge 机制。
- 已有物品、装备、饰品和重铸相关命令及部分 GUI 页面。

**边界：** 当前 `ItemDefinition` 是物品定义，不等于具有独立强化数据的 `ItemInstance`。逻辑背包也不等于完整的 Minecraft 原版物品注册与渲染。

### 3.3 战斗与怪物

- `CombatMath`、`CombatManager`：基础伤害计算、暴击和防御减伤。
- `MobDefinition`、`MobRegistry`、`MobDropDefinition`：怪物定义和 JSON 数据。
- `SkyBlockMobHealthData`：登记怪物独立于原版最大生命值的持久化 HP。
- `PlayerEntityMixin`、`LivingEntityMixin`：玩家攻击与登记怪物攻击玩家的伤害桥接。
- 当前累计代码包含约 70 条怪物 JSON 定义；数据来源和可信度应以各 JSON 的 provenance 字段为准。
- `/skyblock mob spawn` 是手动测试生成，不代表自然刷怪系统已完成。

**边界：** 怪物 drops 数据已能描述，但不代表击杀后已完整执行掉落表。原版生物仍承担部分临时实体与 AI 职责。

### 3.4 经济、NPC、任务与 GUI

- `ShopRegistry`、`ShopService`：JSON 商店目录和基础购买/出售。
- `BankManager`：Bank 与 Purse 分离。
- `NpcRegistry`、`NpcManager`：NPC 定义、生成和交互基础。
- `QuestRegistry`、`QuestManager`：任务定义、接取、进度和奖励基础；当前目标类型有限。
- `GuiMenuManager`、`SkyBlockScreenHandler`、客户端 Screen：主菜单及 Profile、Bank、Quests、Shop、Equipment、Accessories 等基础页面。

**边界：** GUI 中有些页面仍是展示或命令入口，不代表所有交易、装备、任务交互都已经完成 GUI 化。

### 3.5 世界与地图

- `RegionRegistry`、`RegionManager`：区域定义、进入检测和发现记录。
- `WarpRegistry`、`WarpManager`：Warp 定义和传送。
- `IslandRegistry`、`IslandManager`：岛屿定义、测试岛生成与访问。
- `StructureRegistry`、`StructureImportManager`：原生结构 NBT 导入和放置。
- `ConnectionRegistry`、`ConnectionManager`：桥梁与传送路线定义、桥面生成和入口距离校验。

**边界：** 当前 starter island、marker、bridge、portal、outpost 都是项目测试 fixture，不是 Hypixel 地图坐标或官方地图资产。

---

## 4. 剩余工作按系统归类

### P1 玩家系统
- 统一属性来源层：基础、装备、饰品、技能、Buff、其他。
- 玩家 RPG 生命与原版生命同步策略。
- 技能等级增益、正式经验表和技能解锁。
- HUD 与状态展示。

### P2 物品与装备
- `ItemInstance` 和实例 NBT 序列化。
- Minecraft 原版 Item 注册、图标和物品交互。
- 附魔、冲突规则与效果结算。
- Tooltip、稀有度、属性和强化信息。
- 让现有 Accessory Power、Tuning、Reforge 迁移到稳定的物品实例模型。

### P3 战斗与怪物
- 击杀结算和真实掉落。
- 自然刷怪点、区域刷怪表和刷新规则。
- 怪物专属 AI、技能和特殊攻击。
- 伤害数字、血条、击杀提示和完整战斗反馈。

### P4 成长与解锁
- Collections 与采集累计。
- 配方注册、解锁和制作。
- 任务目标扩展：击杀、交付、采集、制作、区域、Boss 等。
- Bestiary 击杀记录和奖励。
- NPC 分支对话与交互 GUI。

### P5 世界与地图
- Hub 地图、区域划分、建筑、NPC、商店和传送点。
- Private Island 的玩家绑定、建造权限、边界、扩建和存档隔离。
- 地图资源来源、许可、版本、坐标变换和完整性检查。
- 更完整的桥梁、传送门表现与路线导航。

### P6-PX
具体任务和验收标准见 `DEV_TASKS_v0.4.md`。

---

## 5. 构建与运行验证记录

- 2026-11-27：P5.0 曾出现 4 个编译错误，后续已修复并推送。
- 2026-11-27：用户记录 P5.2 本地 Gradle 构建成功（BUILD SUCCESSFUL，JAR 约 261.8 KB）。
- 构建环境记录：Gradle 8.12.1、JDK 21。
- P5.2 的 `runClient` 和完整游戏内行为仍需测试。
- 以上构建记录是特定提交上的结果；后续代码修改后应重新构建。
- 本文不将未执行的测试标记为通过。

---

## 6. 数据与真实性原则

1. Hypixel 官方资料、官方 API、社区 Wiki 和项目自定义数据必须区分。
2. 每条外部数据尽可能保留来源 URL、获取时间、来源类型和可信度。
3. 社区 Wiki 数据不得标成官方数据。
4. `test_*`、`starter_*_fixture` 等测试内容不得冒充 Hypixel 正式物品、地图、NPC 或数值。
5. 真实地图资产需核验来源和再分发许可；不能因技术上能导入就默认可以公开分发。

---

## 7. 文档维护规则

- 本文只记录“代码现在有什么、验证到什么程度”。
- `DEV_TASKS_v0.4.md` 只记录“接下来要做什么、怎样验收”。
- 完成任务后先核对代码，再更新状态；不能仅凭类名或 JSON 文件存在就标记系统完成。
- 默认允许直接在 `main` 上开发。只有在改动风险、并行工作或回滚需求明显时才建立分支；分支不是每个阶段的硬性要求。
- 每次交付明确区分：代码已提交、静态检查、Gradle 构建通过、游戏内测试通过。
