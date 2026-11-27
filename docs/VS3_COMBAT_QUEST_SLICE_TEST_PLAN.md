# VS-3 / VS-4 — 首个战斗任务闭环测试计划

> 本阶段新增的是项目自有测试冒险区，不是 Hypixel 原版 Graveyard 地图。真实地图审计完成后再替换地形和坐标。

## 本轮代码范围

- Quest objective 新增 `kill_mob`，使用 `target_mob_id` 指向 MobRegistry 中的定义。
- QuestRegistry 在加载时验证目标 NPC / Mob 是否存在；MobRegistry 在 QuestRegistry 之前加载。
- CombatManager 在已注册怪物被玩家击杀时推进对应任务。
- 击杀奖励会尝试发放已知概率、且已在 Core ItemRegistry 注册的物品；未知概率不会被当成 0% 或 100%。
- 新增 Core 材料 `rotten_flesh`，使 Graveyard Zombie 的已知掉落可以进入逻辑背包。
- 新增 `StarterGraveyardManager`：首次服务器启动时在 Overworld (512, 220, 512) 附近生成项目自有浮岛测试区，并确保最多存在三只带有 `graveyard_zombie` 标签的僵尸。
- 新增 `starter_graveyard` Region 与 Warp。
- Jerry 作为私人岛上的任务板，显示任务目标、接取命令和 Graveyard 路线。

## 本地构建

在仓库根目录执行：

```powershell
.\gradlew.bat build
```

构建失败时先停止游戏内验收，提交完整错误日志。

## 全新世界验收

1. 使用最新 main 创建全新单人世界。
2. 确认首次加入自动创建双岛私人岛并传送。
3. 找到副岛上的 Jerry，与其交互。
4. Jerry 应列出 `first_steps` 与 `clear_the_graveyard`，并显示：
   - `/skyblock quest accept first_steps`
   - `/skyblock quest accept clear_the_graveyard`
   - `/skyblock warp starter_graveyard`
5. 接取 `first_steps`，再次与 Jerry 交互，应完成一次且只完成一次，获得 100 coins 与 10 Social XP。
6. 接取 `clear_the_graveyard`，传送至 Graveyard。
7. 确认 Graveyard 区域有三只带有 `skyblockrpg_mob_graveyard_zombie` 标签的可攻击僵尸。
8. 装备 Core 训练武器，击杀三只僵尸。
9. 每次击杀应只结算一次：
   - 1 coin；
   - 5 Combat XP；
   - 已知概率的 Core 物品掉落（Graveyard Zombie 的 Rotten Flesh）；
   - 任务进度 +1。
10. 第三次击杀后任务完成，奖励 50 coins 与 25 Combat XP。
11. 使用 `/skyblock quest list` 确认两个任务状态；使用 `/skyblock inventory` 或现有背包查看入口确认 Rotten Flesh 数量。
12. 退出并重进世界，确认任务状态、金币、技能经验和逻辑背包保持。

## 边界测试

- 未接取任务前击杀，不应增加任务进度。
- 已完成任务后再次击杀，不应重复发任务奖励。
- 同一击杀不能重复增加进度或奖励。
- 未知概率的掉落不发放。
- ItemRegistry 未注册的掉落不进入背包，也不应伪装成已发放。
- 背包满时击杀仍正常结算金币、经验和任务；物品掉落应明确提示未收取。
- 已有 Graveyard 标记时重启服务器，不应再次生成整座岛或重复刷满三只僵尸。
- 其他玩家世界已有非空气地形时，生成器不得覆盖标记位置以外的现有方块。

## 已知限制 / 待办

- 目前 Graveyard 是项目自有测试岛，不是地图审计后的 Hypixel 原版区域。
- 怪物当前在服务器启动时确保生成；死亡后不会在同一运行周期自动重生。
- Jerry 的任务板使用聊天和命令入口，尚未升级为交互式 Quest GUI。
- 本轮没有改动原版伤害桥接 P1-02D；RPG Combat 仍按现有自定义攻击管线工作。
- 结构 NBT 审计和真实 Hub / Graveyard 切片导入仍并行推进。
- 所有代码提交均需经过本地 Gradle build 与游戏内验收后，才能标记为可玩完成。
