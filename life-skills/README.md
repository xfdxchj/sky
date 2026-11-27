# SkyBlock RPG Life Skills

SkyBlock RPG 的附属生活玩法模组（Mining / Fishing / Farming / Foraging）。

## 状态

- 版本：`0.1.0-alpha.1`
- 环境：Minecraft 1.21.1 / Fabric / Java 21
- 依赖：`skyblockrpg` Core（API v3）
- 当前：四技能核心玩法 + 数据驱动已实现，未做 GUI/图鉴（需 Core API 或客户端支持）

详细状态见 [docs/DEV_STATUS.md](docs/DEV_STATUS.md)。

## 功能

- 四技能注册与等级奖励
- Mining：28 个方块、工具处理器、自定义挖掘时间
- Foraging：8 种树、整树砍伐、衰败、再生、树礼物
- Farming：11 种作物
- Fishing：21 种海怪、18 种奖杯鱼、10 种鱼
- 数据驱动：内容在 `data/skyblock_life/skyblock/`

## 命令

- `/skyblock_life skills`
- `/skyblock_life stats`
- `/skyblock_life fortune`

## 构建

1. 把 Core 生产 jar 放到 `libs/skyblock-rpg-0.1.0-alpha.3.jar`
2. `gradle build`

产物：`build/libs/skyblock-life-0.1.0-alpha.1.jar`
