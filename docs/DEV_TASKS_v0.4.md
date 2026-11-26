# SkyBlock RPG 开发任务单 v0.4

> 本文件只定义后续开发目标和验收标准；当前实现事实见 [DEV_STATUS_v0.4.md](DEV_STATUS_v0.4.md)。
> 目标：尽可能还原当前 Hypixel SkyBlock 的核心系统，针对单人游戏做必要适配。
> 技术基线：Minecraft 1.21.1、Fabric、Java 21。
> 开发原则：先检查并复用现有实现，不重复造轮子；不把基础框架当成完整玩法。

---

## 1. 通用开发与验收规则

1. 默认可以直接在 `main` 上开发，不要求每个 P 阶段单独建分支。
2. 只有在功能跨度大、实验性强、需要并行或需要独立回滚时，才使用临时分支。
3. 开发前先核对当前代码与本任务单，确认缺口真实存在。
4. 数据优先 JSON / 数据驱动；核心状态由服务端权威管理。
5. 新增存档字段必须考虑旧存档默认值、迁移和异常数据。
6. 每个功能至少提供设计说明、测试步骤和失败边界。
7. 交付时分别报告：代码提交、静态检查、Gradle build、runClient / 游戏内测试；不得混写。
8. 官方数据、社区数据和项目 fixture 必须明确标注来源；禁止编造 Hypixel 数据。
9. 任何地图资产先核验来源与许可，再决定是否进入仓库。

### 阶段状态口径

- 已完成：该阶段定义的验收项已实现并完成约定测试。
- 基础完成：核心数据结构或服务已存在，但玩法闭环或测试仍缺。
- 未开始：尚无实质实现。

---

## 2. P0：工程与基础数据

**当前：基础完成。** 后续只处理维护问题，不重写。

已有范围：
- Fabric 工程、Mod 入口、命令注册。
- 世界级 PersistentState 和 UUID 玩家档案。
- Purse、技能经验、基础属性数据。
- Profile / stats / coins / skill 等基础命令。

后续维护：
- 版本化存档迁移。
- 数据损坏保护与保存边界。
- 构建配置和依赖维护。

---

## 2.1 M1：Core 扩展 API（Life Skills 接入）

**目标：** 让附属模组使用 Core 现有玩家、技能、属性、物品、经济和奖励系统，而不复制主体系统。

- [x] Namespaced 技能注册与 XP 曲线定义。
- [x] SkillAPI 注册、查询、等级和经验入口。
- [x] PlayerDataAPI 模块数据读写入口。
- [x] 旧技能存档键迁移至 `skyblockrpg:<skill>`。
- [x] 扩展模组缺席时保留其技能 XP 和模块数据。
- [x] StatAPI：读取 Core 最终属性；补齐 Farming Speed、Fishing Fortune、Foraging Speed / Fortune 等生活技能属性。
- [x] ItemAPI：读取跨命名空间数据包物品定义。
- [x] InventoryAPI：访问 Core 逻辑背包。
- [x] CurrencyAPI：访问 Purse / Bank。
- [x] RewardAPI：发放物品、金币和技能经验。
- [x] 更新 Life Skills 接口合约与测试计划。
- [ ] 用户本地 Gradle build 与 Core-only 游戏验证。
- [ ] 最小测试扩展 Mod 验证四技能注册、物品数据包加载和奖励流程。

**明确延期：** 任意 StatType 动态注册、属性修正写入 API、ItemInstance、原子多奖励事务、Quest Objective 和通用事件总线。不得把未发布接口写进 Life Skills 的编译依赖。

**验收：** 见 `CORE_API_v2_TEST_PLAN.md`.

## 3. P1：玩家系统完善

**目标：形成所有玩法模块可统一调用的玩家成长与属性基础。**

### P1-01 属性来源管线
- 在现有 PlayerStats / StatModifier 上扩展。
- 区分 Base、Equipment、Accessory、Skill、Buff、Temporary、Other 来源。
- 统一最终属性查询和修正顺序。
- 支持上下限、持续时间和移除。
- 防止模块绕过管线直接改最终属性。

### P1-02 RPG 生命与战斗状态
- 玩家当前生命、最大生命和恢复。
- 受伤、死亡、复活与重生流程。
- 明确 RPG HP 与 Minecraft 原版生命、护甲、抗性的适配规则。
- 保留原版环境与非 SkyBlock 伤害的合理行为。

### P1-03 技能成长
- 正式技能定义、最大等级、经验表和版本化数据。
- 技能等级属性增益与解锁。
- XP 来源接口、奖励规则和防重复结算。
- 12 个现有 SkillType 仅作为内部 ID 起点；按当前 Hypixel 实际技能体系核对后确定首发范围。
- 替换临时线性 XP 曲线，不将占位曲线作为正式数据。

### P1-04 HUD 与状态界面
- HP、Defense、Mana / Intelligence、Purse 等基础展示。
- 技能等级和经验进度。
- 低频更新与客户端性能控制。

### P1 验收
- 各模块通过统一接口读取最终属性。
- 装备、饰品、技能和 Buff 的属性叠加符合规则。
- 玩家 RPG 生命与原版伤害流程不互相重复结算。
- 技能升级能产生实际效果。
- 退出重进后玩家状态正确保存。

---

## 4. P2：物品、实例与装备系统

**目标：把现有 ItemDefinition、逻辑背包、装备、Accessory Power、Tuning 和 Reforge foundation 整合为稳定物品体系。**

### P2-01 ItemInstance
- 明确区分静态 ItemDefinition 与动态 ItemInstance。
- 实例字段至少考虑：唯一实例 ID、Reforge、Enchantments、Stars、Upgrade Level、附加数据。
- 定义堆叠规则：有实例状态的物品不能错误合并。
- NBT 序列化、反序列化、旧数据迁移和复制安全。

### P2-02 Minecraft 物品表现
- 将 SkyBlock 物品映射到可实际获得的 Minecraft Item。
- 服务端校验物品身份和数据，不能仅凭显示名称识别。
- 图标、名称、Lore、稀有度颜色和 Tooltip。
- 防止玩家通过原版合成、铁砧或其他途径复制/篡改关键数据。

### P2-03 装备与属性
- 继续使用现有装备槽和装备逻辑。
- 装备、卸下、替换、背包满时的事务安全。
- 装备属性与 P1 属性来源管线对接。
- 后续扩展 Bow、Pickaxe、Axe、Drill 等专用槽位或工具类型。

### P2-04 饰品体系整合
- 保留现有 Accessory Bag、Accessory Power、Power、Tuning 基础。
- 将饰品升级线、重复规则、实例身份和属性贡献迁移到 ItemInstance。
- 补充 Enrichment、饰品效果和解锁规则时必须基于核实过的当前数据。

### P2-05 附魔与 Reforge
- 附魔定义、等级、冲突、效果和结算。
- 扩展现有 Reforge foundation：兼容性、稀有度、费用、Reforge Stone 和特殊效果。
- 附魔与 Reforge 不得重复叠加或绕过属性管线。

### P2 验收
- 两个相同定义的物品可保存不同实例强化状态。
- 物品可获得、移动、装备、卸下、存档和重载。
- Tooltip 展示与服务端真实数据一致。
- 背包满、替换装备、堆叠和迁移边界无物品丢失或复制。

---

## 5. P3：战斗与怪物闭环

**目标：把已有伤害、怪物注册、独立 HP 和 mob 数据补成可持续游玩的战斗循环。**

### P3-01 伤害管线
- 统一基础伤害、Strength、暴击、增伤、目标 Defense、最终伤害和击杀结算。
- 后续接入附魔、武器能力、Combat 等级、Ferocity 等机制。
- 区分玩家、怪物、环境和其他伤害来源，防止重复处理。

### P3-02 怪物行为与生成
- 保留现有 MobDefinition / MobRegistry / Mob HP。
- 区域刷怪表、刷怪点、刷新数量、距离和重生时间。
- 按需要实现自定义实体或可配置 AI。
- 普通、远程、精英和特殊怪物分层实现。

### P3-03 掉落与奖励
- 将 MobDropDefinition 接入真实击杀结算。
- 固定、随机、概率、数量区间和条件掉落。
- Magic Find 等接口预留并按真实机制实现。
- 统一发放物品、金币、Combat XP，保证只结算一次。

### P3-04 战斗反馈
- 伤害数字、暴击、怪物 HP / 血条、击杀提示。
- Boss 血条接口。
- 伤害来源和异常状态提示。

### P3 验收
玩家可完成：获得装备 → 找到区域怪物 → 战斗 → 受伤与恢复 → 击杀 → 获得掉落和经验 → 存档重载。

---

## 6. P4：成长、收集、任务与 NPC

**目标：在已有 NPC、Quest、GUI 基础上，形成探索和长期成长循环。**

### P4-01 Collections
- 采集累计、等级、奖励、解锁条件和 UI。
- 先实现 Wheat、Cobblestone、Coal、Iron、Gold、Diamond、Rotten Flesh、Bone 等基础类别。
- 与 P6 采集事件对接；采集事件未完成前先做数据框架。

### P4-02 配方与制作
- 配方注册、材料需求、解锁条件、制作结果和经验。
- 服务端检查材料并以事务方式扣除与发放。
- 配方来源关联 Collection、NPC、任务等。

### P4-03 任务系统扩展
- 保留现有接取、完成、奖励和 talk_to_npc 基础。
- 增加击杀、收集/交付、采集、制作、到达区域、Boss 等目标。
- 任务链、前置条件、阶段目标、追踪和失败/放弃规则。
- GUI 对话选项与任务状态联动。

### P4-04 Bestiary
- 击杀计数、怪物等级/家族归档、阶段奖励。
- 与 MobRegistry 和击杀事件统一，不重复计数。
- Bestiary 信息页面。

### P4-05 NPC 与交互
- 从临时 villager stand-in 逐步升级为可配置外观、位置、交互和日程。
- 分支对话、点击选项、商店和任务入口。
- NPC 内容与地图坐标分离，方便地图调整。

### P4 验收
采集或击杀 → 记录 Collection / Bestiary → 解锁配方或任务 → 完成目标 → 发放奖励 → 存档正确。

---

## 7. P5：世界、地图与私人岛

**目标：把已有 Region、Warp、Island、Structure Import 和 Connection 框架变成可探索的 SkyBlock 世界。**

### P5.0 地图基础
- 维护 Region、Warp、Island 的 JSON 定义和坐标验证。
- 地图数据与 Java 系统分离。
- 测试 fixture 与真实地图数据严格区分。

### P5.1 结构资源导入
- 维护原生 NBT 结构导入、坐标、旋转、镜像和边界检查。
- 增加批量导入、结构清单、缺失资源检查和版本信息。
- 导入过程必须支持预检与失败保护。

### P5.2 岛屿连接
- 保留桥梁、传送路线和 Warp 关联。
- 后续完善桥面支撑、栏杆、视觉效果、交互入口和路线图。
- 所有连接端点必须与对应 Warp / Region 数据一致。

### P5.3 Hub 地图
- [x] 建立 Hub POI 数据模型、注册表和查询命令。
- [x] 收录 25 个 Hub 区域、服务点和 NPC 参考坐标，覆盖 Village、Bank、Auction House、Bazaar Alley、Mining District、Farm、Combat Settlement、Graveyard、Mountain、Ruins、Foraging Camp、Wilderness 等；每条记录保留社区 Wiki 来源。
- [ ] 选择有明确使用与再分发许可的当前 Hub 地图资产；未确认许可前不复制第三方世界文件。
- [ ] 核对 Minecraft 版本差异，决定将当前 Hypixel 地图资产适配到项目 1.21.1，还是调整项目目标版本。
- [ ] 导入 Hub 地形、功能建筑和结构资源。
- [ ] 测量真实 Region 边界，建立进入提示与解锁规则。
- [ ] 配置 NPC 实体位置、服务动作、商店、Warp 和 Connection。
- 地图坐标、NPC 配置和玩法逻辑保持分离。

### P5.4 Private Island
- 玩家与岛屿绑定、创建、访问和删除策略。
- 独立岛屿存档或可靠的空间分区方案。
- 建造权限、保护边界、岛屿等级、扩建和访客权限。
- Minion 放置与岛屿规则对接。

### P5.5 其他区域与地图工具链
- 首批区域按依赖和资产可用性排序：Gold Mine、Deep Caverns、The Park、Dungeon Hub 等。
- 后续再扩展 Spider's Den、The End、Dwarven Mines、Crystal Hollows、Crimson Isle、Galatea 等。
- 地图导入工具：坐标变换、结构清单、版本差异、资源完整性和来源记录。

### P5 验收
- 可在 Hub、Private Island 和已导入区域间移动。
- Region、Warp、NPC、怪物和资源点坐标一致。
- 私人岛归属、权限、建造和存档互不串档。
- 地图资源缺失或导入失败时不会破坏已有世界。

---

### P5.4 Private Island foundation

**代码基础已提交，等待本地构建与游戏测试。**

- [x] 每个玩家 UUID 一个私人岛屿记录。
- [x] 世界级 PersistentState 保存归属与位置。
- [x] 天空网格分配与空域预检。
- [x] 创建、回家和信息命令。
- [x] 初始非所有者方块破坏/标准方块交互保护。
- [ ] 单人世界创建、回家、重启持久化测试。
- [ ] 双玩家/LAN 所有权保护测试。
- [ ] 验证不同种子、世界边界和已占用天空区域的失败行为。
- [ ] 后续增加访客权限、扩建、岛屿等级、Minion 和完整保护。

详细测试见 `P5.4_PRIVATE_ISLAND_TEST_PLAN.md`。

## 8. P6：生活职业

**目标：形成非战斗资源生产与成长循环。**

### P6-01 Mining
- 矿物、矿点刷新、Mining XP、Mining Fortune / Speed、工具、Collection 和配方。
- 后续接入 Dwarven Mines、Crystal Hollows 等区域机制。

### P6-02 Farming
- Garden / 农田、作物、生长、收获、Farming XP、Farming Fortune、工具和订单。
- 作物采集事件接入 Collection。

### P6-03 Fishing
- 钓鱼、鱼类定义、钓点、Fishing XP、Fishing Speed、稀有鱼和特殊事件。

### P6-04 Foraging
- 树木、木材采集、Foraging XP、工具和 Collection。
- 根据当前 Hypixel 版本核对 Foraging 机制与地图内容。

### P6 验收
每个职业至少形成：工具/条件 → 采集操作 → 资源结算 → XP → 等级成长 → 解锁内容 → 存档。

---

## 9. P7：Minion、仓库与单机经济扩展

**已有基础：** Purse、Bank、基础商店和买卖结算已经存在。本阶段不重复建设基础金币和普通商店。

### P7-01 Minion
- Minion 类型、等级、放置、工作范围、生产周期和储存。
- Fuel、Upgrade、离线生产结算。
- 时间戳结算，避免每 tick 处理所有 Minion。
- 与岛屿权限、Collection 和资源价格对接。

### P7-02 仓库与背包
- Personal Storage、Backpack、Ender Chest 扩展。
- Accessory Bag 与现有系统统一。
- 容量、分页、移动事务和存档兼容。

### P7-03 Bazaar 单机模拟
- 商品、买入/卖出订单、数量、价格和手续费规则。
- 使用 NPC / 系统订单模拟市场，不依赖真实在线玩家。
- 明确市场价格生成和刷新规则，避免随机价格破坏经济。

### P7-04 Auction House 单机化
- NPC 委托、系统挂牌和模拟竞价/固定价格购买。
- 物品实例数据必须完整保留。
- 防止重复领取、重复扣款和交易状态不一致。

### P7-05 经济平衡与记录
- 交易记录、金币来源/消耗统计、价格配置和防重复结算。
- 商店、Minion、Bazaar、Auction House 使用统一交易服务。

### P7 验收
Minion 可持续生产；仓库可存取；市场订单可创建、成交和结算；所有金币与物品变动可追踪且不会重复发放。

---

## 10. P8：Slayer 与 Boss

### P8-01 Slayer 基础
- Slayer NPC、任务接取、指定怪物击杀、Slayer XP、等级、Boss 召唤和奖励。

### P8-02 Boss 框架
- 阶段、技能、特殊机制、血条、掉落和结算。
- 与 P3 伤害、怪物、掉落和 P4 Bestiary 共用接口。

### P8-03 内容顺序
Revenant Horror → Tarantula Broodfather → Sven Packmaster → Voidgloom Seraph → Inferno Demonlord。

### P8 验收
接取任务 → 击杀指定怪物 → 召唤 Boss → 完成战斗 → 正确结算 XP、掉落和奖励。

---

## 11. P9：Dungeon

### P9-01 实例管理
- 创建、进入、退出、实例状态、难度、进度和结算。
- 单人实例清理和异常退出恢复。

### P9-02 房间系统
- Room Registry、模板、连接、生成、边界和 Boss 房间。
- 房间布局与结构导入工具链对接。

### P9-03 Dungeon 玩法
- 普通/精英怪、Secrets、Puzzle、Trap、Mini Boss、Boss、计时和评分。

### P9-04 Dungeon 装备与职业
- Dungeon Item、Stars、Class、属性缩放和升级。

### P9-05 Floor
Entrance → Floor I–VII。
单人模式必须重新平衡怪物、Boss、机关、评分和奖励，不能直接照搬多人模式数值。

### P9 验收
进入实例 → 生成房间 → 完成挑战 → 击败 Boss → 计分 → 发奖 → 清理实例。

---

## 12. P10：后期内容

按核心系统成熟度逐步推进，不在基础循环未完成时提前堆砌大量后期数据。

- Advanced Mining：Dwarven Mines、Crystal Hollows、Commission、Heart of the Mountain。
- Crimson Isle：Faction、区域任务、Kuudra 和高级装备。
- Rift：独立机制、货币、区域、任务和装备。
- 高级养成：Pets、Pet Items、高级 Accessories、装备强化和高级附魔。
- 活动与特殊玩法：Dragon、特殊 Boss、节日活动和限时奖励。
- 其他当前版本新增区域与系统。

---

## 13. PX：整合、平衡与发布

### PX-01 系统整合
统一玩家、属性、装备、战斗、技能、Collection、经济、Minion、地图、Slayer 和 Dungeon 接口。

### PX-02 存档与迁移
正常保存/读取、异常退出、大量物品实例、Minion 离线结算、Dungeon 清理、版本迁移和备份恢复。

### PX-03 性能
实体与刷怪数量、Minion 数量、区域加载、结构生成、Dungeon 生成、HUD 更新和存档频率。

### PX-04 平衡
装备曲线、怪物强度、金币速度、掉落概率、技能升级速度、Boss 难度和职业收益。

### PX-05 发布
Mod 构建、资源整理、配置、默认存档、安装说明、版本说明、更新日志和备份迁移。

### PX 验收
干净环境可构建和启动；核心循环完整；旧存档可迁移；无已知严重复制/丢失漏洞；性能与平衡达到可长期游玩标准。
