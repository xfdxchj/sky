# P2-01 ItemInstance 测试计划

> 状态：待实现；测试必须先覆盖旧版 PlayerInventory NBT。

## A. 实例身份与状态
1. 两个相同 definitionId 的装备获得不同 UUID。
2. 分别修改两个实例的 Reforge、Enchantments、Stars、Upgrade Level，状态互不影响。
3. 同一实例 ID 不得在两个容器位置重复出现。
4. 未知 definitionId、非法 UUID、负 count、超最大堆叠数、非法星级/升级等级、非法附魔等级和过大 customData 被拒绝或隔离。
5. 普通无状态堆叠可合并；任一实例带状态后不可合并。
6. 普通堆叠拆分后两个堆叠具有不同 UUID，数量之和保持不变。

## B. 旧档案迁移
1. 仅含旧 items map 的档案迁移后各 itemId 总数完全一致。
2. 旧 equipment 每个槽位迁移成独立 count=1 实例，装备槽指向该实例。
3. 旧 accessoryBag 的数量、顺序和重复饰品保留。
4. 旧 reforges 正确迁移到对应装备实例。
5. 缺少新字段的 schema 10 及更早档案均可加载。
6. 同一档案保存、重载多次后 instanceId 稳定，不重复生成实例。
7. 未知物品、超容量和损坏条目进入可审计暂存/警告路径，不静默丢失。
8. 迁移后的档案重新保存后，旧格式不再被重复导入。

## C. Inventory 原子操作
1. 添加/移除普通堆叠时容量和数量正确。
2. 背包满时装备替换失败，原装备和待装备物品均保留。
3. 装备成功时物品从背包移至槽位，实例 ID 不变。
4. 卸下失败时装备槽保持不变。
5. Accessory Bag 放入/取出失败不丢失物品；重复饰品仍按现有升级线规则计算。
6. Reforge 只跟随目标实例，不会因同槽位替换污染新装备。
7. 任何失败操作前后物品总量、实例 ID 集合和容器引用保持不变。

## D. API 回归
1. API v3 旧 InventoryAPI.getCount/add/remove 签名与行为继续可用。
2. RewardAPI.giveItem 仍能添加无状态扩展材料。
3. Life Skills 的 4 个 namespaced 技能、模块数据和物品奖励不受实例迁移影响。
4. Core-only 启动不依赖 Life Skills。
5. Core + Life Skills 可同时加载，扩展仍只依赖公开 API。
