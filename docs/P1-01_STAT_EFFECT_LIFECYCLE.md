# P1-01 属性效果生命周期

## 目标

在既有 `StatSource` / `StatModifier` / `PlayerStats` 管线之上，提供服务端权威的 BUFF、TEMPORARY、OTHER 属性效果管理。装备、Reforge、饰品、Power 和 Tuning 仍由原有系统派生，不复制到效果存档。

## 数据模型

每个效果包含：

- `id`：玩家档案内稳定的效果 ID，长度 1–128。
- `sourceId`：用于诊断和追踪的来源标识。
- `source`：仅允许 BUFF、TEMPORARY、OTHER。
- `stat`、`amount`、`operation`：复用现有属性修正模型。
- `expiresAtGameTime`：Overworld 游戏刻绝对过期时间；`-1` 表示不自动过期。

效果保存在玩家 UUID 档案的 `statEffects` NBT 列表中。旧档案缺少该字段时按空列表迁移，数据版本提升到 9。

## 生命周期规则

1. `StatEffectManager.apply(...)` 只能在服务端调用；不接受 BASE、EQUIPMENT、ACCESSORY、SKILL 来源，防止效果管理器覆盖其他系统职责。
2. durationTicks 必须为正数；`-1` 表示永久；`0` 和小于 `-1` 的值拒绝。
3. 相同 effect ID 再次应用时替换旧效果，视为刷新；不同 ID 可以并存并叠加。
4. `remove(...)` 按 effect ID 主动移除。
5. 每个服务端 tick 以 Overworld 的 game time 清理到期效果；过期时标记档案 dirty。
6. 服务器关闭期间世界游戏刻不推进，因此效果时长暂停；重开后从持久化的绝对游戏刻继续。
7. 玩家离线期间不执行在线 tick 清理，但再次加入后会在服务端 tick 中清理；离线期间若世界仍在运行，时间仍会推进。
8. 效果只参与最终属性修正，不改写基础属性，也不直接操作 Minecraft 原版生命/护甲。

## API 形态

- `StatEffectManager.apply(player, effectId, sourceId, source, stat, amount, operation, durationTicks)`
- `StatEffectManager.remove(player, effectId)`
- `PlayerProfile.getStatEffects()` 返回不可变快照。
- `PlayerProfile.getStatModifiers()` 将有效存档效果合并到统一属性修正快照。

目前这是 Core 内部接口。外部模块写入属性效果的公共 API 尚未开放；生活模组不得直接依赖该内部 Manager。

## 尚未实现

- SKILL 来源的等级奖励映射。当前 XP 曲线为临时占位，不得据此生成虚构属性奖励。
- 效果上限、免疫、驱散、抗性、图标、提示和 HUD。
- 不同效果间的唯一性组、叠层上限、乘区优先级和条件触发。
- Dungeon 专属效果与副本实例时间语义。

## 验收

见 `P1-01_STAT_EFFECT_TEST_PLAN.md`。
