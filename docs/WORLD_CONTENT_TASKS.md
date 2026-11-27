# SkyBlock RPG 世界内容开发任务书

> 面向接手开发的 AI / 开发者。本文件只覆盖 **SkyBlock RPG Core 的世界内容**（Hub、私人岛、区域）。
> 整理时间：2026-11-27

---

## 1. 项目是什么

**SkyBlock RPG Core**：Minecraft 1.21.1 / Fabric / Java 21 的单机 SkyBlock RPG Mod。

- 仓库：`https://github.com/xfdxchj/sky`
- 最新提交：`e3627cf`
- 本地最新克隆：`D:\游戏\_reference\SkyBlockRPG-latest`
- 本地工作副本：`D:\游戏\sky\SkyBlockRPG`
- 构建：Gradle 8.12.1 + JDK 21，`gradle build`

---

## 2. Core 现有的世界框架

包位置：`src/main/java/com/skyrpg/skyblock/`

| 包 | 作用 | 规模（约） |
|---|---|---|
| `region` | 区域注册、坐标、进入检测 | 137 行 |
| `warp` | 传送点、传送 | 119 行 |
| `island` | 岛屿生成、传送 | 433 行 |
| `structure` | NBT 结构导入 | 234 行 |
| `connection` | 地图连接 | 323 行 |
| `hub` | Hub 框架 | 224 行 |
| `npc` | NPC 与对话 | 179 行 |
| `quest` | 任务基础 | 149 行 |

数据位置：`src/main/resources/data/skyblockrpg/skyblock/`

当前测试数据量：
- `regions` 2、`warps` 3、`islands` 1、`structures` 1、`connections` 2、`npcs` 3、`quests` 1
- `mobs` 70、`items` 10、`shops` 1
- **没有 Hub 数据、没有地图文件**

也就是说：框架在，内容几乎为空。

---

## 3. 参考项目

克隆位置：`D:\游戏\_reference\HypixelRecreation`
（Swofty-Developments/HypixelRecreation，AGPL-3.0，Minestom 服务器端）

**注意：只能借鉴结构和数据组织，不能复制代码。** 它是服务器端 Minestom 架构，我们是 Fabric 客户端整合 mod，代码不能直接搬。

### 3.1 世界区域模块（路径前缀 `type.<区域>/`）

| 区域 | 规模 | 说明 |
|---|---|---|
| `type.island` | 27 文件 / 1248 行 | 私人岛 |
| `type.hub` | 180 文件 / 11437 行 | 主城 |
| `type.goldmine` | 10 文件 / 651 行 | 金矿 |
| `type.deepcaverns` | 9 文件 / 490 行 | 深洞 |
| `type.thepark` | 20 文件 / 2338 行 | 公园 |
| `type.theend` | 21 文件 / 1437 行 | 末地 |
| `type.garden` | 59 文件 / 6370 行 | 花园 |
| `type.dwarvenmines` | 55 文件 / 3493 行 | 矮人矿 |
| `type.spidersden` | 12 文件 / 935 行 | 蜘蛛巢 |
| `type.thefarmingislands` | 18 文件 / 774 行 | 农业岛 |
| `type.crimsonisle` | 2 文件 / 106 行 | 绯红岛 |
| `type.mainlobby` | 18 文件 / 792 行 | 主大厅 |
| `dungeons` | 83 文件 / 3339 行 | 地牢 |

### 3.2 世界相关数据
- `configuration/skyblock/` 下有区域坐标、NPC、任务等配置
- 项目还带 world 文件、结构、资源包（在 `configuration/` 和 release 资源里）

---

## 4. 开发任务

### 任务 1：Hub（优先）

目标：一个可用的 Hub，包含：
- 传送点（连接私人岛和其他区域）
- 少量 NPC（向导、银行家、商人）
- 商店
- 基础建筑/装饰
- 区域名称显示

实现方式：
- 用 Core 已有的 `hub`、`region`、`warp`、`npc`、`quest` 框架
- 数据放 `src/main/resources/data/skyblockrpg/skyblock/{hub,regions,warps,npcs,quests}/`
- 建筑可以用 NBT 结构（`structure` 包已有导入能力）或代码生成

参考：`D:\游戏\_reference\HypixelRecreation\type.hub`（只看结构，不复制）

### 任务 2：私人岛玩法

目标：在现有 `island` 基础上补：
- 玩家绑定岛屿
- 自由建造、边界保护
- 岛屿扩建接口
- 岛内传送点

参考：`D:\游戏\_reference\HypixelRecreation\type.island`

### 任务 3：区域扩展（逐个）

按顺序：
1. Gold Mine（`type.goldmine`）
2. Deep Caverns（`type.deepcaverns`）
3. The Park（`type.thepark`）
4. Garden（`type.garden`）
5. The End（`type.theend`）
6. 其他

每个区域需要：
- 区域定义（坐标、进入检测）
- 传送点
- 专属 NPC / 商店 / 怪物
- 专属机制

参考对应 `type.<区域>` 模块。

---

## 5. 约束

1. 只在 Core 仓库内开发，不新建第二个 Mod。
2. 不破坏现有公开 API（`com.skyrpg.skyblock.api`，当前 v3）。
3. 保持存档兼容，新增数据要能迁移。
4. 数据驱动：区域/传送/NPC/任务用 JSON，放 `data/skyblockrpg/skyblock/`。
5. 参考项目是 AGPL-3.0 的 Minestom 代码，只能看结构，不能复制。
6. 数值没有可靠来源时标注为占位，不要标成 Hypixel 官方数据。
7. 每阶段必须 `gradle build` 通过。
8. 完成后报告：新增/修改/删除文件、实际完成的功能、构建结果、是否运行验证、未完成内容。

---

## 6. 构建

```bash
cd D:\游戏\_reference\SkyBlockRPG-latest
gradle build
```

产物：`build/libs/skyblock-rpg-0.1.0-alpha.3.jar`

---

## 7. 验收

每个任务完成后：

- `gradle build` 成功
- 区域可以进入、传送正常
- NPC 可以交互
- 数据保存/读取正常
- 未破坏现有功能
