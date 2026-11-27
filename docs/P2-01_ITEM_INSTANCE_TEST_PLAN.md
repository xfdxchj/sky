# P2-01 ItemInstance 测试计划

> 状态：P2-01A 源码已提交；测试尚未执行。旧版 PlayerInventory NBT 迁移测试留待 P2-01B。

## A. 实例身份与状态（P2-01A）

1. `ItemInstance.create(definition, count)` 为每个新堆叠生成非空 UUID，并保留正确 definitionId。
2. 两个相同 definitionId 的装备获得不同 UUID。
3. `validateAgainst` 拒绝 definitionId 不匹配、超过 maxStackSize、不可重铸定义却带 Reforge 的实例。
4. count 必须在 1..64；stars 必须在 0..10；upgradeLevel 不得为负；附魔等级必须在 1..255，附魔数量不得超过 64。
5. customData 的嵌套深度、节点数、键长和字符串长度超限时拒绝。
6. `customData()` 返回副本；修改返回值不得改变原实例。
7. 对实例应用 Reforge、Enchantments、Stars、Upgrade Level、customData 后，原实例不变，新实例保留原 UUID。
8. 两个相同 definitionId 的装备分别修改强化状态，状态互不影响。
9. 普通无状态堆叠可合并；任一实例带状态后不可合并。
10. 普通堆叠拆分后两个堆叠具有不同 UUID，数量之和保持不变；余量保留原 UUID，新拆出堆叠使用新 UUID。
11. 合并后目标 UUID 保留，源实例由调用方从容器移除。
12. count>1 的堆叠尝试加入任意实例状态必须失败，不得悄悄减少物品数量。
13. split 对有状态实例必须失败；amount<=0 或 amount>=count 必须失败。
14. 未知 definitionId、非法 UUID、负 count、超最大堆叠数、非法星级/升级等级、非法附魔等级和过大 customData 被拒绝或隔离。

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
