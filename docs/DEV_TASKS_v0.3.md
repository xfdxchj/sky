# SkyBlock RPG 开发任务单 v0.3

> 基于 v0.2 开发任务单整理。本文件只包含开发任务，不记录进度状态。
> 当前进度见同目录的《SkyBlockRPG_开发状态_v0.3.md》。
> 仓库：https://github.com/xfdxchj/sky

---

## 1. 开发原则

1. 先检查现有代码，优先扩展，不重复创建已有系统。
2. 数据驱动，模块通过明确接口交互。
3. 每个阶段必须能独立编译、运行、验收。
4. 保持存档兼容，新增字段考虑默认值与迁移。
5. 不为了代码数量提前建立复杂架构。
6. 未经确认，不改变已有架构和已完成模块。

---

## 2. P0：工程与基础数据

任务：
- 建立 Fabric 工程结构、Mod 入口、命令注册、配置
- 建立世界级 `PersistentState` 存档
- 实现玩家 UUID 独立档案
- 实现金币增加 / 扣除 / 查询
- 实现技能经验累计、等级计算、进度计算
- 建立基础属性数据结构

---

## 3. P1：玩家系统完善

**目标：让档案、属性、技能、金币、生命形成可被其他模块调用的统一系统。**

### P1-01 属性统一计算接口
- 建立统一属性来源分层：基础 / 装备 / 饰品 / 技能 / Buff / 其他
- 提供唯一的“最终属性”查询接口，禁止模块各自修改最终属性
- 支持属性增减、上下限、临时 Buff
- 在现有 `PlayerStats` 上扩展，不重写

### P1-02 玩家 RPG 生命系统
- 建立玩家独立生命数据（当前生命 / 最大生命）
- 受伤、恢复、死亡、复活流程
- 与原版实体生命值做同步或适配，明确机制
- 玩家生命不应简单等于原版 20 点

### P1-03 技能系统完善
- 技能注册表、最大等级、经验表
- 技能等级增益（每级提供属性或效果）
- 技能奖励与等级解锁
- 技能经验来源接口
- 技能查询命令
- 初期技能：Combat、Mining、Farming、Foraging、Fishing、Enchanting、Alchemy、Taming

### P1-04 玩家档案管理
- 档案默认值初始化
- 进入 / 离开事件处理
- 档案读取与修改接口
- 保存时机管理、数据异常保护
- 存档版本号与迁移
- 暂时只支持单档案

### P1-05 HUD 基础界面
- 生命值、Defense、Mana / Intelligence、金币显示
- 基础状态信息
- 第一版不追求 Hypixel 还原度

### P1 验收
- 进入游戏自动拥有完整档案
- 属性可正确查询和计算
- 生命值独立管理
- 技能可加经验并升级，升级有实际效果
- HUD 显示玩家数据
- 退出重进数据正常
- 旧存档不因新增字段损坏

---

## 4. P2：物品与装备系统

**目标：建立完整的 RPG 装备体系。**

### P2-01 物品注册系统
- 自定义物品注册为 Minecraft 物品
- 物品 ID、类型、稀有度、基础属性
- 显示名称、描述、图标、模型
- 物品定义继续使用 JSON

### P2-02 物品数据模型
- 区分 `ItemDefinition` 与 `ItemInstance`
- 实例包含：Reforge、Enchantments、Stars、Upgrade Level、Extra Data
- 实例可序列化 / 反序列化
- 相同物品可有不同强化数据
- 物品数据不依赖显示名称判断

### P2-03 装备系统
- 装备位置：Helmet、Chestplate、Leggings、Boots、Weapon
- 后续扩展 Bow、Pickaxe、Axe、Drill
- 穿戴、卸下、属性计算、信息展示
- 耐久或特殊限制按最终设计决定

### P2-04 饰品系统
- Accessory 注册、Accessory Bag、饰品属性与效果
- 重复饰品规则、饰品升级、饰品组合效果
- 饰品不等同于原版盔甲槽

### P2-05 附魔与 Reforge
- 附魔数据结构、等级、效果、冲突规则
- Reforge 注册、属性加成、应用与移除
- 在现有 Reforge foundation 上扩展

### P2-06 物品界面
- Tooltip、稀有度颜色、属性展示、附魔展示、强化信息、装备对比

### P2 验收
- 能注册并获得自定义装备
- 装备可穿戴 / 卸下并影响属性加成
- 物品动态数据可保存
- 重进世界装备数据不丢失
- Tooltip 正确展示

---

## 5. P3：战斗与怪物系统

**目标：建立完整的基础战斗循环。**

### P3-01 伤害计算系统
- 统一伤害流程：武器基础伤害 → Strength → 暴击 → 增伤 → 目标防御 → 最终伤害 → 击杀
- 伤害事件、击杀事件
- 所有战斗统一调用该接口
- 伤害显示

### P3-02 怪物系统
- 自定义怪物注册、属性、等级、HP、伤害、防御
- 移动与攻击行为
- 怪物刷新点
- 初期 3 种：普通近战、远程、精英

### P3-03 掉落系统
- 固定掉落、随机掉落、概率、数量区间
- Magic Find 接口
- 特殊掉落条件
- 在现有 `MobDropDefinition` 数据上实现实际掉落逻辑

### P3-04 战斗反馈
- 伤害数字、暴击提示、怪物血条
- Boss 血条基础框架
- 击杀提示、战斗音效

### P3 验收
玩家可以：获得武器 → 攻击怪物 → 造成属性相关伤害 → 被怪攻击 → 击杀 → 获得掉落 → 获得 Combat XP → 存档保存。

---

## 6. P4：成长与解锁系统

**目标：建立长期成长循环。**

### P4-01 Collections
- Collection 注册、采集累计、等级、解锁、奖励、UI
- 初期：Wheat、Cobblestone、Coal、Iron、Gold、Diamond、Rotten Flesh、Bone

### P4-02 配方系统
- 配方注册、材料需求、解锁条件、制作操作、结果、经验奖励

### P4-03 任务系统
- 任务类型：击杀怪物、收集物品、采集资源、与 NPC 交谈、到达区域、制作物品、击败 Boss
- 任务接取、追踪、完成、奖励

### P4-04 Bestiary
- 怪物击杀记录、怪物等级、Bestiary 经验、奖励、怪物信息界面

### P4 验收
- 采集增加 Collection
- Collection 解锁配方
- 配方可制作物品
- 任务可追踪和完成
- Bestiary 记录击杀

> 依赖提醒：P4 的“采集”依赖 P6 的职业循环，先做框架、内容等 P6 就位。

---

## 7. P5：世界与地图系统

**目标：将游戏逻辑与 SkyBlock 风格地图结合。**

### P5-01 区域管理
- 区域注册、ID、坐标范围、进入检测、名称显示、区域属性、权限与特殊规则

### P5-02 传送系统
- 传送点注册、区域入口 / 出口、传送命令、传送菜单、冷却、条件

### P5-03 Hub
- 导入 Hub 地图、NPC 位置、商店位置、传送点、功能建筑、区域显示、基础任务

### P5-04 私人岛
- 初始岛屿生成、玩家绑定、自由建造、数据管理、边界、扩建接口、Minion 放置区域

### P5-05 地图导入工具链
- 地图版本检查、区域提取、结构转换、NBT 模板、坐标记录、区域 JSON 配置、完整度检查

第一批地图：Hub、Private Island、Gold Mine、Deep Caverns、The Park、Dungeon Hub。
后续：Dwarven Mines、Crystal Hollows、Spider's Den、The End、Farming Islands、Crimson Isle、Galatea、Backwater Bayou、Lotus Atoll。

### P5 验收
- 私人岛可前往 Hub
- 传送进入其他区域
- 区域名称与信息正确
- 地图正常加载
- 不同区域可用不同怪物 / 资源 / 规则
- 私人岛独立保存

---

## 8. P6：生活职业系统

**目标：建立非战斗玩法。**

### P6-01 Mining
- 自定义矿物、刷新点、Mining XP、Mining Fortune / Speed、特殊采矿、Collection、工具、配方

### P6-02 Farming
- Garden、种植、生长、收获、Farming XP、Farming Fortune、工具、作物订单、Collection

### P6-03 Fishing
- 钓鱼系统、鱼类注册、特殊鱼、稀有掉落、Fishing XP、Fishing Speed、特殊钓点

### P6-04 Foraging
- 树木种类、木材采集、Foraging XP、特殊工具、Collection

### P6 验收
四种职业都能形成：采集 / 操作 → 获得资源 → 增加经验 → 提升等级 → 解锁内容。

---

## 9. P7：自动化与经济系统

**目标：形成长期资源生产与交易循环。**

### P7-01 Minion
- Minion 注册、类型、等级、放置、生产、储存
- Fuel、Upgrade、离线生产结算、UI
- 初期：Cobblestone、Wheat、Coal、Iron
- 用时间戳结算，不要每 tick 处理

### P7-02 商店
- NPC 商店、购买 / 出售、金币结算、价格配置

### P7-03 经济系统
- 货币管理、交易记录、价格配置、出售限制、物品价值、防重复结算

### P7-04 仓库
- Personal Storage、Ender Chest 扩展、Backpack、Accessory Bag、Minion Storage

### P7-05 Bazaar / 拍卖行（单机化）
- Bazaar 商品、买入 / 卖出订单、价格与数量
- 拍卖行改为 NPC 动态市场
- 单机不需要真实玩家交易，采用 NPC 订单和模拟市场

### P7 验收
- Minion 持续生产，离线正确结算
- 商店可买可卖，金币准确
- 仓库可保存物品
- 无重复发放 / 扣款

---

## 10. P8：Slayer 与 Boss

**目标：建立中期战斗挑战。**

### P8-01 Slayer 基础框架
- Slayer NPC、任务接取、指定怪物击杀、Slayer XP、等级、Boss 召唤、击杀奖励

### P8-02 Boss 系统
- Boss 属性、阶段、技能、特殊机制、血条、掉落、结算

### P8-03 Slayer 内容（按优先级）
Revenant Horror → Tarantula Broodfather → Sven Packmaster → Voidgloom Seraph → Inferno Demonlord。

### P8 验收
接取任务 → 击杀指定怪 → 召唤 Boss → 完成战斗 → 获得奖励。

---

## 11. P9：Dungeon

**目标：建立完整的单人 Dungeon 体验。**

### P9-01 实例管理
- 创建 / 进入 / 退出实例、状态、进度、难度、结算

### P9-02 房间系统
- Room Registry、模板、连接、随机生成、类型、边界、Boss 房间

### P9-03 玩法
- 普通怪、精英怪、Secrets、Puzzle、Trap、Mini Boss、Boss、计时、评分、奖励

### P9-04 装备
- Dungeon Item、Stars、Class、属性、升级

### P9-05 Floor
Entrance → Floor I ~ VII。
**单人必须重新平衡怪物、Boss、机关、评分，不能直接用多人数值。**

### P9 验收
进入 → 随机生成房间 → 完成挑战 → 击败 Boss → 正确计分 → 获得奖励 → 实例清理。

---

## 12. P10：后期内容

**目标：逐步接近当前 Hypixel SkyBlock 的主要内容规模。**

### 12.1 高级 Mining
Dwarven Mines、Crystal Hollows、Commission、Heart of the Mountain、高级矿物、装备、配方。

### 12.2 Crimson Isle
区域探索、特殊怪物、Faction、高级任务、Kuudra、高级装备。

### 12.3 Rift
独立机制、货币、特殊装备、区域、专属任务。

### 12.4 高级养成
高级 Pets、Pet Level、Pet Item、高级 Accessories、装备强化、高级附魔、高级 Reforge、装备收集。

### 12.5 活动与特殊玩法
Dragon、特殊 Boss、节日活动、限时任务、特殊奖励。

### P10 验收
主要后期区域可进入，核心玩法可运行，装备与成长数据兼容现有系统。

---

## 13. PX：整体整合与发布

### PX-01 系统整合
检查玩家、属性、装备、战斗、技能、Collection、经济、Minion、地图、Dungeon、Slayer，确保统一数据接口。

### PX-02 存档测试
正常保存 / 读取、异常退出、大量物品、多词条、Minion 离线、Dungeon 清理、版本迁移。

### PX-03 性能测试
实体数量、Minion 数量、区域加载、Dungeon 生成、HUD 更新、大量物品实例、存档写入频率。

### PX-04 游戏平衡
装备曲线、怪物强度、金币速度、掉落概率、技能升级速度、Boss 难度、职业收益。

### PX-05 发布
Mod 构建、资源整理、配置整理、默认存档、安装说明、版本说明、更新日志、备份迁移。
