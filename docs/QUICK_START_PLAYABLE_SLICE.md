# SkyBlock RPG — 首个可玩流程

> 当前为项目自有测试内容，不代表完整 Hypixel 复刻。目标环境：Minecraft 1.21.1 / Fabric。

## 开始游戏

1. 使用最新 Core 构建启动单人世界。
2. 首次进入会自动创建私人岛并传送到主岛。
3. 走到副岛，与 Jerry 交互。他会显示任务列表和冒险区域入口。

## 首轮流程

在 Jerry 的任务板接取第一项任务：

```text
/skyblock quest accept first_steps
```

再次与 Jerry 交互完成任务，获得 100 coins 和 10 Social XP。奖励金额按项目测试数值设置，刚好可以购买第一把训练剑。

购买并装备：

```text
/skyblock shop buy training_sword 1
/skyblock equip training_sword
/skyblock stats
```

接取战斗任务并前往 Graveyard：

```text
/skyblock quest accept clear_the_graveyard
/skyblock warp starter_graveyard
```

击杀 Graveyard 中的三只僵尸。每只已注册的 Graveyard Zombie 配置奖励为 1 coin、5 Combat XP，并有已知概率的 Rotten Flesh 掉落。完成任务额外获得 50 coins 和 25 Combat XP。

## 查看状态与返回私人岛

```text
/skyblock quest list
/skyblock inventory
/skyblock stats
/skyblock island home
```

退出并重新进入世界后，任务、金币、技能经验和 Core 逻辑背包应继续保留。

## 注意

- 首次服务器启动时生成的 Graveyard 是项目自有临时测试岛，坐标由项目数据定义；它不是从 Hypixel 地图 NBT 导入的正式区域。
- 当前任务板是聊天 + 命令交互；GUI 版本尚未完成。
- Graveyard 怪物在服务器启动时补足三只；本轮不包含同一运行期间的自动重生。
- 本文描述的是预期流程。只有在本地 Gradle build 和游戏内验收通过后，才能视为可正常游玩。
- 详细验收步骤见 [VS-3 / VS-4 战斗任务闭环测试计划](VS3_COMBAT_QUEST_SLICE_TEST_PLAN.md)。
