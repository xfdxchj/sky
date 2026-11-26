# SkyBlock RPG 开发状态 v0.3

> 本文件记录开发进度状态，与《SkyBlockRPG_开发任务单_v0.3.md》配套。
> 整理时间：2026-11-27
> 仓库：https://github.com/xfdxchj/sky

---

## 1. 分支现状

| 分支 | 最新提交 | 内容 |
|---|---|---|
| `main` | `ce6a85b` | P3.1，稳定但落后，未合并后续 |
| `p3.2-data-driven-combat` | — | 数据驱动战斗 |
| `p3.3-skyblock-mob-health` | — | mob HP 持久化 |
| `p3.4*` / `p3.5` | — | mob 数据库 / 攻击 |
| `p4.0-economy-shop` | `3df6d52` | 商店系统 |
| `p4.1-bank-system` | `ab43243` | 银行 |
| `p4.2-region-warp-system` | — | 区域 / 传送 |
| `p4.3-npc-system` | — | NPC |
| `p4.4-dialogue-quest-foundation` | — | 对话 / 任务基础 |
| `p4.5-gui-foundation` | — | GUI 基础 |
| `p4.6-equipment-accessory-stats` | — | 装备 / 饰品属性 |
| `p4.7-accessory-power-system` | — | 饰品力量 |
| `p4.8-reforge-system` | — | 重铸 |
| `p5-island-map-foundation` | `906e03f` | 岛屿地图基础（含编译修复） |
| `p5.1-structure-import` | — | NBT 结构导入 |
| `p5.2-island-connections` | `c86692d` | 岛屿连接，**当前最完整** |

分支串联，`p5.2-island-connections` 包含以上全部提交。

---

## 2. 阶段状态总览

| 阶段 | 状态 | 说明 |
|---|---|---|
| P0 | ✅ 完成 | 工程、入口、命令、配置、存档 |
| P1 | ⚠️ 基础已有，需完善 | 缺统一属性接口、玩家 RPG 生命、技能等级增益、HUD |
| P2 | ⚠️ 基础已有，缺核心 | 缺 `ItemInstance`、附魔、Tooltip、物品实际注册 |
| P3 | ⚠️ 战斗已有，闭环缺 | 缺掉落、刷怪点、战斗反馈 |
| P4 | ⚠️ 部分 | 任务 / NPC / GUI 基础有；Collections、配方、Bestiary 无 |
| P5 | ⚠️ 框架已有，内容缺 | 区域 / 传送 / 岛屿 / 结构 / 连接有；Hub 地图内容、私人岛建造无 |
| P6 | ❌ 未开始 | 生活职业 |
| P7 | ⚠️ 部分 | 商店 / 银行有；Minion、Bazaar、仓库无 |
| P8 | ❌ 未开始 | Slayer / Boss |
| P9 | ❌ 未开始 | Dungeon |
| P10 | ❌ 未开始 | 后期内容 |
| PX | ❌ 未开始 | 整合发布 |

---

## 3. 已实现模块（按代码核对）

### 玩家与成长
- `profile/PlayerProfile`：UUID、金币、技能经验、属性、背包、档案版本
- `profile/SkyBlockWorldData`：`PersistentState` 世界级存档
- `profile/ProfileManager`：进出世界读取 / 创建档案
- `stats/PlayerStats`、`StatType`、`StatModifier`：基础属性 + 最终属性计算（加法 / 乘法 / 覆盖）
- `economy/CurrencyManager`、`BankManager`：金币与银行
- `skill/SkillType`、`SkillDefinition`、`SkillManager`：12 类技能、XP 曲线（占位曲线）

### 物品与装备
- `item/ItemDefinition`、`ItemRegistry`（从 `data/skyblockrpg/skyblock/items/*.json` 加载）
- `inventory/PlayerInventory`（36 格逻辑背包）、`EquipmentSlot`（6 个装备槽）
- `accessory/PowerRegistry`、`PowerDefinition`、`AccessoryPowerManager`
- `reforge/ReforgeRegistry`、`ReforgeDefinition`、`ReforgeManager`
- 命令：`items`、`item give`、`item info`、`inventory`、`equipment`、`equip`、`unequip`

### 战斗与怪物
- `combat/CombatManager`、`CombatMath`：伤害公式、暴击、防御减伤
- `combat/MobRegistry`、`MobDefinition`、`MobDropDefinition`、`SkyBlockMobHealthData`
- `mixin/LivingEntityMixin`、`mixin/PlayerEntityMixin`
- 70 个 mob JSON

### 世界与地图
- `region/*`：区域注册 / 管理
- `warp/*`：传送点 / 传送
- `island/*`：岛屿生成 / 传送
- `structure/*`：NBT 结构导入
- `connection/*`：地图连接
- `npc/*`：NPC + 对话
- `quest/*`：任务基础

### 界面
- `gui/GuiMenuManager`：主菜单 + profile / bank / quests / shop / equipment / accessories
- `gui/SkyBlockGui`、`SkyBlockScreenHandler`
- `client/SkyBlockGuiClient`、`SkyBlockHandledScreen`

---

## 4. 主要缺口

### P1
- 统一属性计算接口（现在各模块各自调用 `getFinalStat`，无来源分层）
- 玩家 RPG 生命系统（玩家 HP 仍用原版 health）
- 技能等级增益（升级无效果）
- HUD

### P2
- `ItemInstance`（现在物品只有 id + count）
- 附魔系统
- Tooltip / 稀有度颜色 / 属性展示
- 物品注册为 Minecraft 物品（现在游戏内不可见）

### P3
- 掉落系统（`CombatManager` 不处理 `drops`）
- 刷怪系统（只有手动 `/skyblock mob spawn`）
- 战斗反馈（只有 action bar 文本）

### P4
- Collections、配方、Bestiary
- 任务完整类型与追踪

### P5
- Hub 地图内容
- 私人岛建造 / 绑定 / 边界 / 扩建
- 完整地图导入工具链

### 工程
- `main` 未合并，仍停在 P3.1
- 版本号仍为 `0.1.0-alpha.3`
- 无 Gradle wrapper
- 运行时（`runClient`）未验证

---

## 5. 编译验证记录

- 2026-11-27：`p5-island-map-foundation` 曾出现 4 个编译错误（bank 命令链、GUI lore 类型、tick 事件、screen title），已修复并推送（`906e03f`）。
- 2026-11-27：`p5.2-island-connections` 实测编译通过（BUILD SUCCESSFUL，jar 261.8 KB）。
- 构建环境：Gradle 8.12.1，JDK 21，`GRADLE_USER_HOME=D:\游戏\.gradle-home`。
- 尚未执行 `runClient`，运行时行为未验证。
