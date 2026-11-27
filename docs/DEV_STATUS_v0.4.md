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
| 默认分支 `main` | 已整合 P0 至 P5.3 当前提交的累计功能代码（P5.3 目前为 Hub POI 目录基础） |
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
| P1 玩家系统 | 🟡 基础已有 | 属性来源、BUFF 生命周期、SKILL 等级奖励框架已提交待测；RPG 生命、正式技能数据和 HUD 未完成 |
| P2 物品与装备 | 🟡 基础已有 | JSON 物品定义、逻辑背包、装备、饰品包、Power、Tuning、Reforge 基础存在；物品实例、附魔、原版物品注册和完整 Tooltip 未完成 |
| P3 战斗与怪物 | 🟡 基础已有 | 伤害计算、怪物 JSON、独立怪物 HP、玩家受伤桥接和手动生成存在；掉落闭环、自然刷怪、AI 与完整战斗反馈未完成 |
| P4 成长与解锁 | 🟡 基础已有 | NPC、任务基础、对话和 GUI 框架存在；Collections、配方、Bestiary 和完整目标类型未完成 |
| P5 世界与地图 | 🟡 框架与目录已有 | 区域、Warp、岛屿、NBT 结构导入、桥梁和传送路线已有；P5.3 已建立 25 个有来源的 Hub POI 坐标记录与查询命令；真实地形、建筑、NPC 实体布置、区域边界和私人岛完整玩法仍未完成 |
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
- `PlayerStats`、`StatType`、`StatModifier`、`StatSource`：属性定义、来源分类和最终属性修正。
- `StatEffect`、`StatEffectManager`：BUFF / TEMPORARY / OTHER 效果应用、刷新、主动移除、持久化与过期清理。
- `StatEffectAPI`（API v3）：为外部扩展开放命名空间隔离的 BUFF / TEMPORARY / OTHER 写入与移除入口。
- `SkillType`：12 个内部技能 ID；它们是技术注册起点，不代表当前 Hypixel 所有技能玩法均已实现。
- `SkillDefinition`：支持扩展注册 namespaced 技能；内置/示例 XP 曲线仍是临时数据，不是最终 Hypixel 经验表。
- `SkillStatReward`、`SkillStatRewardRegistry`：按明确等级阈值注册精确属性奖励；PlayerProfile 按当前技能等级派生 SKILL 来源修正，不从 XP 曲线推导奖励。
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
- `HubLocationDefinition`、`HubRegistry`：25 个有来源的 Hub 地点/NPC 参考坐标；`HubCommands` 提供列表、详情、定位和最近点查询。

**边界：** 当前 starter island、marker、bridge、portal、outpost 都是项目测试 fixture，不是 Hypixel 地图坐标或官方地图资产。

---

### 3.7 P5.4 Private Island foundation（代码已提交，待本地验证）

- 新增 `PrivateIslandRecord`、`PrivateIslandWorldData` 和 `PrivateIslandManager`。
- 每个玩家按 UUID 拥有一个持久化私人岛屿记录；岛屿位置通过 Overworld 天空网格分配。
- 新增 `/skyblock island create`、`/skyblock island home`、`/skyblock island private info`。
- 创建前执行目标空域预检，生成失败时不写入所有权记录。
- 增加非所有者的方块破坏和标准方块交互/相邻放置路径拦截。
- 设计与测试计划见 `P5.4_PRIVATE_ISLAND_DESIGN.md`、`P5.4_PRIVATE_ISLAND_TEST_PLAN.md`。

**限制：** 当前是私人岛屿基础设施，不包含访客权限、扩建、岛屿等级、Minion、GUI，也未覆盖爆炸、活塞、流体及所有第三方模组交互路径。没有执行 Gradle build 或游戏内验证。

### 3.6 Core 扩展 API v3（源码已提交，待本地验证）

- 保留 API v1 的 `SkillAPI` 与 `PlayerDataAPI`，不改变既有方法签名。
- `CoreApi.API_VERSION` 更新为 3。
- 新增 API-owned `SkillDefinition` / `ItemView` DTO，避免扩展代码依赖内部 `SkillRegistration` / `ItemDefinition`。
- 新增只读 `StatAPI`：查询 Core 最终属性管线；补充生活技能所需的 `FARMING_SPEED`、`FISHING_FORTUNE`、`FORAGING_SPEED`、`FORAGING_FORTUNE`。
- 新增 `ItemAPI`：查询 Core 与扩展数据包中的 API-owned `ItemView`。
- 新增 `SkillDefinition`、`ItemView` DTO，避免扩展模组直接依赖 Core 的内部注册表数据类。
- 对外部玩家数据保留命名空间隔离，禁止扩展写入 `skyblockrpg` 保留命名空间；技能查询接口补齐空值保护。
- 加入物品最大堆叠数与属性数值校验，避免非法 JSON 进入背包计算。
- `ItemRegistry` 现在扫描所有命名空间下的 `data/<namespace>/skyblock/items/*.json`，扩展物品使用 namespaced ID；保留 Core 旧 ID 查询兼容。
- 新增 `InventoryAPI`：通过 Core 逻辑背包安全查询、增加和移除物品。
- 新增 `CurrencyAPI`：统一访问 Purse / Bank。
- 新增 `RewardAPI`：提供物品、金币和技能经验奖励入口。
- 新增 `StatEffectAPI`：外部模块使用同命名空间的 effect/source ID 写入或移除 BUFF、TEMPORARY、OTHER 修正。
- `SkillAPI.registerStatReward`：扩展为自身已注册的技能定义明确等级属性奖励；达到阈值后由 Core 派生为 `StatSource.SKILL`。不使用 XP 曲线推算任何奖励数值。
- 生活模组接口合约、外部模组依赖说明和独立 API smoke-test 工程见 `CORE_API_v3.md`、`CORE_API_v3_TEST_PLAN.md`（v2 历史文档保留）、`EXTERNAL_MODULE_SETUP.md`、`examples/life-skills-api-smoke/`、`LIFE_SKILLS_MOD_SPEC.md`。

**明确未包含：** 动态注册任意 StatType、直接写入 BASE / EQUIPMENT / ACCESSORY / SKILL 来源、ItemInstance、原子多奖励事务、Quest Objective 和通用 Core 事件总线。BUFF / TEMPORARY / OTHER 写入由 StatEffectAPI 限定开放。Life Skills 不得假设这些接口存在。所有新增 API 目前仅完成源码提交，尚未由本地 Gradle build 或游戏内测试验证。

## 4. 剩余工作按系统归类

### P1 玩家系统
- 属性来源基础与现有装备体系迁移已提交：新增 `StatSource`（BASE、EQUIPMENT、ACCESSORY、SKILL、BUFF、TEMPORARY、OTHER），`StatModifier` 增加来源字段并保留四参数兼容构造。
- 装备词条与 Reforge 明确标记为 EQUIPMENT；饰品词条、Power、Tuning 明确标记为 ACCESSORY。
- `PlayerStats` 支持按来源筛选计算；`PlayerProfile.getStatModifiers()` 提供统一修正快照，`getFinalStat(type, includedSources)` 可按来源过滤。
- 旧四参数构造仍按 sourceId 前缀推断来源，未知前缀归为 OTHER；新代码应显式传入 StatSource。
- BUFF、TEMPORARY、OTHER 已接入效果生命周期：效果按 ID 刷新/替换，不同 ID 可叠加；支持主动移除和 Overworld 游戏刻过期；效果存入玩家档案 NBT，旧档案缺字段时按空列表迁移。
- 当前效果管理器拒绝 BASE、EQUIPMENT、ACCESSORY、SKILL 来源；SKILL 来源通过独立等级奖励注册表派生。扩展可为自己注册的技能定义明确阈值与属性值；Core 不从临时 XP 曲线推导奖励。基础属性仍由 PlayerStats 独立保存。
- P1-02 RPG 生命系统：P1-02A 当前生命档案与 schema 10 迁移、P1-02B 原版生命同步管理器、P1-02C DamageKind / PlayerDamageMath 纯计算层已提交；P1-02D 正在分步实施，当前已完成 DamageSource 分类器（OUT_OF_WORLD / GENERIC_KILL 绕过 RPG Defense，其余默认 NORMAL）和 Yarn 1.21.1+build.3 Javadoc 方法签名/职责核对，但尚未取得项目实际 Loom mapped jar 核验方法体与调用顺序，也未接入实体伤害流程。P1-02E Health Regen 与 P1-02F 边界完善未开始。代码待用户本地构建与游戏验收。具体见 `P1-02_RPG_HEALTH_DESIGN.md`、`P1-02_RPG_HEALTH_TEST_PLAN.md` 和 `P1-02D_DAMAGE_PIPELINE_INVESTIGATION.md`。
- 技能等级增益、正式经验表和技能解锁。
- HUD 与状态展示。
- 属性来源测试计划：`P1-01_STAT_PIPELINE_TEST_PLAN.md`；效果生命周期设计与测试：`P1-01_STAT_EFFECT_LIFECYCLE.md`、`P1-01_STAT_EFFECT_TEST_PLAN.md`；Core API v3 测试：`CORE_API_v3_TEST_PLAN.md`。

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
- P5.3 已完成 Hub POI 数据模型、25 条有来源的坐标记录和列表/详情/定位/最近点命令；这只是地图数据基础。
- Hub 真实地形与建筑结构、Region 实际边界、NPC 实体放置、商店与 Warp 绑定仍未完成。
- Private Island 的玩家绑定、建造权限、边界、扩建和存档隔离仍未完成。
- 地图资源来源、许可、版本、坐标变换和完整性检查仍需完善。
- 更完整的桥梁、传送门表现与路线导航。

### P6-PX
具体任务和验收标准见 `DEV_TASKS_v0.4.md`。

---

## 5. 构建与运行验证记录

- 2026-11-27：P5.0 曾出现 4 个编译错误，后续已修复并推送。
- 2026-11-27：用户记录 P5.2 本地 Gradle 构建成功（BUILD SUCCESSFUL，JAR 约 261.8 KB）。
- 构建环境记录：Gradle 8.12.1、JDK 21。
- P5.2 的 `runClient` 和完整游戏内行为仍需测试。
- P5.3 尚未由用户本地 Gradle build 或游戏内测试。
- 2026-11-27：P1-01 已完成现有装备/饰品/Power/Tuning/Reforge 来源标记与统一修正快照，并提交 BUFF / TEMPORARY / OTHER 效果生命周期、NBT 持久化、SKILL 等级奖励派生与 Core API v3；未执行 Gradle build 或游戏内测试。
- 2026-11-27：P1-02 设计、测试计划和 P1-02A（当前 RPG Health 字段、schema 10 持久化迁移、Health / Health Regen / Defense / True Defense 基准）已提交；未执行 Gradle build 或游戏内测试。
- 2026-11-27：P1-02B 服务端生命同步桥已提交，包含登录初始化、原版生命变化回写、档案生命变化下发、最大生命变化处理、重生满血和断线清理；未执行 Gradle build 或游戏内测试。
- 2026-11-27：P1-02C DamageKind 与 PlayerDamageMath 纯计算层已提交，区分普通伤害、True Damage 和绕过 RPG 防御的伤害；尚未接入实体伤害流程，未执行构建或公式测试。
- 2026-11-27：P1-02D-0 PlayerDamageClassifier 已提交，明确 OUT_OF_WORLD / GENERIC_KILL 绕过 RPG Defense，其他来源默认 NORMAL；仅完成分类器，不代表伤害桥接已实现。未执行 Gradle build 或游戏测试。
- 2026-11-27：P1-02D-1 已核对 Yarn 1.21.1+build.3 Javadoc 的 damage / applyArmorToDamage / modifyAppliedDamage / applyDamage 方法签名与职责，并提交 `P1-02D_DAMAGE_PIPELINE_INVESTIGATION.md`。Javadoc 不提供完整方法体，项目实际 Loom mapped jar 的字节码调用顺序仍待核验；未提交高风险伤害 Mixin，未执行 Gradle build 或游戏测试。
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
