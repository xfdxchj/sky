# P2-01 ItemInstance 设计

> 状态：P2-01A 数据模型源码已提交；尚未完成本地构建与单元/游戏测试。
> 基线：main @ e2ee2b3（2026-11-27）；Core API v3；Minecraft 1.21.1 / Fabric / Java 21。
> 目标：在不破坏现有物品定义、逻辑背包和 Life Skills API v3 的前提下，引入可持久化、可独立强化的物品实例。

## 1. 当前问题

当前 Core 的 `ItemDefinition` 是静态定义；`PlayerInventory` 以 `Map<String, Integer>` 汇总物品数量，装备和 Accessory Bag 只保存定义 ID，Reforge 另存在装备槽映射中。

因此两个同 ID 装备无法分别保存 Reforge、附魔、星级或升级状态。当前数据结构也无法表达唯一实例身份、实例拆分/合并与实例级装备迁移。

Life Skills 已作为独立模组提交，并通过 Core API v3 使用技能、属性、奖励、物品定义和逻辑背包接口。P2-01 不得要求 Life Skills 访问 Core 内部类，也不得破坏现有 API v3 方法签名。

## 2. 核心模型

### 2.1 ItemDefinition

继续作为只读静态定义，负责：
- namespaced item ID、名称、稀有度、类型、基础属性和 Lore；
- 最大堆叠数、装备槽、是否可重铸等静态规则；
- 不保存任何玩家实例状态。

### 2.2 ItemInstance

新增玩家拥有的动态物品实例，至少包含：
- `UUID instanceId`：实例/堆栈唯一标识；
- `Identifier definitionId`：关联 ItemDefinition；
- `int count`：当前堆叠数量；
- `String reforgeId`：可选实例重铸；
- `Map<Identifier, Integer> enchantments`：附魔 ID 与等级；
- `int stars`：星级；
- `int upgradeLevel`：升级阶段；
- `NbtCompound customData`：命名空间隔离的扩展数据。

实例构造与加载必须校验：定义存在、count 在 1..maxStackSize、数值有限且非负、附魔等级合法、扩展 NBT 有大小上限。未知定义不能被静默改写成其他物品。

### 2.3 堆叠规则

- 无实例强化状态的物品允许堆叠，数量不得超过定义最大堆叠数。
- 含 Reforge、Enchantments、Stars、Upgrade Level 或非空 customData 的物品视为有状态实例，count 必须为 1。
- 有状态实例不得与其他实例合并。
- 拆分普通堆叠时，为新堆叠分配新的 UUID；合并时保留目标堆叠 UUID。
- 对即将强化的普通堆叠，必须先拆出 count=1 的新实例，再应用实例状态。

### 2.4 Inventory 持有关系

Inventory 统一持有 ItemInstance/堆叠条目；装备槽和 Accessory Bag 只引用 instanceId，不复制物品数据。
- 同一 instanceId 在背包、装备槽、Accessory Bag 中只能出现一次。
- 装备、卸下、饰品放入/取出均为原子移动：校验目标容量与规则后一次性提交。
- Reforge 从装备槽独立 Map 迁移到 ItemInstance；旧装备槽 Reforge 在迁移时写入对应装备实例。
- 现有装备槽类型和 Accessory Power/Tuning 规则暂时保留，先改为通过实例引用解析 ItemDefinition。

## 3. 旧存档迁移

升级必须无损：
1. 读取旧 `items: Map<itemId,count>`，按定义最大堆叠数拆成若干 ItemInstance；每个堆叠生成 UUID。
2. 读取旧 equipment 中的 itemId，为每个装备创建独立 count=1 实例并绑定原装备槽。
3. 读取旧 accessoryBag 中的 itemId，为每个饰品创建独立实例并保留原顺序。
4. 读取旧 reforges 槽位映射，将合法 Reforge 写入对应装备实例。
5. 对重复槽位、未知物品、超容量和损坏记录采用可审计的迁移策略；不得静默丢弃。优先将无法放入的物品保留在迁移暂存区并记录警告。
6. 新格式写入后保留 schema/version 标记；重复加载不得重复生成新实例 ID。

## 4. API 兼容

- 不改变 API v3 现有 `InventoryAPI.getCount/add/remove`、`ItemAPI.get/getAll`、`RewardAPI` 方法签名。
- 旧 API 继续按 definitionId 汇总数量和执行普通无状态堆叠物品操作。
- 新增实例 API 时使用 API-owned DTO（例如 ItemInstanceView），不向扩展暴露内部可变对象或 PlayerInventory。
- 新 API 不得允许扩展伪造 instanceId、修改 Core 保留命名空间数据或绕过实例校验。
- 本阶段不要求 Life Skills 改用实例 API；现有四技能和资源奖励继续可用。

## 5. 实施拆分

### P2-01A：数据模型与规则
新增 ItemInstance、堆叠条目/引用模型、实例状态校验与堆叠兼容规则；先不替换 PlayerInventory 持久化。

### P2-01B：档案持久化与迁移
新增版本化 NBT 格式；旧 items/equipment/accessoryBag/reforges 一次性迁移；保留未知扩展数据。

### P2-01C：Inventory 事务与兼容层
将 add/remove/equip/unequip/accessory 操作迁移到实例存储；提供按 definitionId 聚合的旧查询接口；保证失败回滚无丢失/复制。

### P2-01D：实例查询 API
在 Core API v3 中以向后兼容方式增加只读实例 DTO 和必要查询；修改 API marker 仅在出现不兼容变更时进行，单纯新增兼容方法不擅自提升主版本。

## 6. 非目标

本任务不实现 Minecraft 原版 Item 注册、图标、Tooltip、附魔完整效果、Reforge Stone、星级材料消耗、交易 GUI、拍卖行或掉落表。这些分别留在 P2-02/P2-05/P7。

## 7. 风险与原则

- 任何迁移都必须先有旧档案 fixture 和往返测试。
- 不允许同时保留两份可独立修改的物品真相来源。
- 任何装备/饰品移动先校验、后提交；不依赖“失败后再尽力补回”。
- API v3 的扩展数据、技能 XP、资源物品奖励必须在 Core-only 与 Core + Life Skills 两种环境保持兼容。
- 所有物品来源数据仍须标注官方、社区 Wiki、项目 fixture 或未知，不得把测试数据写成 Hypixel 正式内容。


## 8. P2-01A 当前实现边界（2026-11-27）

已新增 `com.skyrpg.skyblock.item.ItemInstance`：

- 不可变实例对象：UUID、namespaced definition ID、数量、Reforge、Enchantments、Stars、Upgrade Level、扩展 NBT。
- `create` / `restore` 工厂与 `validateAgainst` 定义级校验；旧 Core 无命名空间 ID 按 `skyblockrpg:` 解析。
- 状态化实例强制 count=1；无状态堆叠才允许拆分/合并。
- 拆分时原 UUID 留给余量，新堆叠生成 UUID；合并保留目标 UUID。
- 实例状态更新返回新对象并保留 UUID；NBT 输入/输出均防御性复制。
- 对星级、附魔等级、附魔数量、扩展 NBT 深度/节点/键长/字符串长度设定结构限制。

本阶段刻意不修改 `PlayerInventory`、`PlayerProfile` 或存档 schema；实例尚未进入玩家档案，也未改变 API v3 旧接口。扩展 NBT 的精确序列化字节上限将在 P2-01B 的 NBT 编解码层落实。

**待验证：** Java 21 / Loom 编译、实例规则测试、ItemRegistry 加载后 definition 校验，以及后续 P2-01B 的真实 NBT 往返。
