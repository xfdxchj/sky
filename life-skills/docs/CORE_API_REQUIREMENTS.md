# SkyBlock RPG Life Skills — Core API 状态

> 对应 Core API v2（`CORE_API_v2.md`）。
> 本文件记录 Life Skills 需要的接口是否已经由 Core 提供。

## 已提供（Core API v2）

| 能力 | 接口 | 状态 |
|---|---|---|
| 技能动态注册 | `SkillAPI.registerSkill(SkillRegistration)` | ✅ |
| 技能经验/等级/进度查询 | `SkillAPI.getExperience/getLevel/getProgress` | ✅ |
| 技能经验发放 | `SkillAPI.addExperience` | ✅ |
| 玩家扩展数据持久化 | `PlayerDataAPI.get/set/remove` | ✅ |
| 属性只读查询 | `StatAPI.getFinal/getBase/getSupportedStats` | ✅ |
| 物品定义查询 | `ItemAPI.get/getAll` | ✅ |
| 逻辑背包增删查 | `InventoryAPI.getCount/add/remove` | ✅ |
| 金币/银行 | `CurrencyAPI` | ✅ |
| 奖励门面 | `RewardAPI` | ✅ |
| 方块破坏/交互/实体事件 | Fabric 服务端回调（Core 不提供事件总线） | ✅ 用 Fabric |

## 未提供（v2 明确不包括）

- 运行时注册新的 `StatType`
- 属性修正来源写入 API
- `ItemInstance` / 物品 NBT 接口
- 原子多奖励事务
- 任务目标回调 API
- 通用 Core 玩法事件总线
- Core 拥有的 Collection 框架

这些不阻塞 L1 技能注册接入；做 P2/P4 时需要先向 Core 提需求。

## 当前接入方式

- 编译期：`compileOnly files("libs/skyblock-rpg-0.1.0-alpha.3.jar")`
- 运行期：`fabric.mod.json` 声明 `depends.skyblockrpg = "*"`
- 启动时检查 `CoreApi.API_VERSION >= 2`，再注册四种技能
