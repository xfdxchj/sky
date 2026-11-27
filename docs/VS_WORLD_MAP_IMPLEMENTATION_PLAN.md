# 世界地图与岛屿还原计划（VS-World）

> 目标：以当前 Hypixel SkyBlock（2026 Hub 重制后）为参照，逐步补齐私人岛、Hub 主岛及其他可访问岛屿。
> 本文是地图与区域工作的执行计划，不代表任何外部地图已获得许可、已导入或已通过游戏验证。
> 项目目标版本：Minecraft 1.21.1 / Fabric / Java 21。

## 1. 先统一“主岛”的含义

本项目中区分两个概念，避免开发任务混淆：

- **私人岛主岛**：玩家出生的第一座浮岛，与副岛通过石砖桥连接。当前双岛生成器已写入源码，尚待本地构建与游戏内验收。
- **Hub 主岛（Hub Island）**：所有玩家可到达的公共中心区域，包含 Village、Bank、Auction House、Bazaar Alley、Mining District、Farm、Combat Settlement、Graveyard 等地点。当前只有 POI 目录和坐标参考，尚无实际 Hub 地形与建筑。

两者都属于 Core 世界内容，但私人岛是玩家独立空间，Hub 是公共区域；不能复用同一套生成/权限逻辑。

## 2. 目标地图范围

### 2.1 第一优先级：私人岛双岛

依据社区 Wiki 的 Private Island 初始布局描述，当前首版结构包括：

- 主岛：草地、泥土、石层、橡树和玩家出生点。
- 副岛：洞穴、火把、初始箱子、Jerry、Hub Portal 视觉占位、Cobblestone Minion I 展示底座。
- 两岛之间的石砖桥。

仍未完成：
- Portal 实际传送至 Hub。
- Minion 的生产、燃料、存储与升级逻辑。
- Jerry 的完整新手任务链。
- 岛屿建造范围、访客权限和保护边界的完整游戏验收。
- 与 Hypixel 原始结构的尺寸/细节逐项比对。

不得将 Portal 占位描述成已连通，也不得将 Minion 展示底座描述成可生产。

### 2.2 第二优先级：Hub 主岛（公共中心）

以当前 Hub 重制版本为基准，不使用 1.8 旧版坐标直接拼图。

推荐按可独立切片的区域实施：

1. Village：出生/集散中心、主广场、主道路和地标建筑。
2. Bank：银行建筑与 Banker NPC。
3. Auction House：拍卖行建筑与入口区域。
4. Bazaar Alley：Bazaar 建筑、街巷和相关 NPC。
5. Mining District：黑smith 周边与矿业入口。
6. Farm：农场入口和通往 Farming Islands 的路线。
7. Combat Settlement：通往 Graveyard 的方向与战斗区域入口。
8. Museum、Tavern、Rabbit House 等服务点。
9. Wilderness、Ruins、Mountain 等外围地形。

首个 Hub 可玩版本不要求所有区域同时完成。先完成 Village + Bank/Auction/Bazaar 的中心切片，再向外扩展。

### 2.3 第三优先级：首个冒险区

- Graveyard：作为 Hub 旁边首个战斗区，连接已有怪物、掉落、Combat XP 与任务系统。
- Crypts：在 Graveyard 主循环稳定后接入。
- 其他战斗区域按地图来源、玩法依赖和数据完整度排序。

### 2.4 后续独立岛屿

建议后续顺序：

1. Gold Mine
2. The Farming Islands（生活玩法由 skyblock_life 提供，Core 提供地图/区域/传送底座）
3. The Park（Foraging 玩法由 skyblock_life 提供）
4. Spider's Den
5. Deep Caverns
6. Dwarven Mines
7. Dungeon Hub 与 Catacombs（玩法由 skyblock_dungeons 提供，Core 提供世界入口和区域连接）
8. Crystal Hollows、Crimson Isle、Rift、Galatea 等后期区域

该顺序是开发优先级，不代表所有区域都属于首轮可玩闭环。

## 3. 地图资产策略

参考资源统一登记在 `HYPIXEL_REFERENCE_ASSET_CATALOG.md`。

### 3.1 当前候选资源

- Hub Modern Version：社区提供的 1.22 世界下载，视觉/布局参考优先级最高。
- Gold Mine、Farming Islands、The Park、Spider's Den、Dwarven Mines：社区目录中有现代版下载候选。
- Deep Caverns：当前目录只确认旧版参考。
- Private Island：暂未确认可靠的独立地图文件，继续按 Wiki 描述制作项目自有结构。

### 3.2 版本兼容

项目目标是 Minecraft 1.21.1，而当前 Hub 重制资产标注为 1.21.9+/1.22。禁止将新版本世界直接加载到 1.21.1，也禁止把外部世界存档覆盖到玩家世界。

地图导入必须经过：
1. 在匹配版本的隔离实例打开原始地图。
2. 记录下载来源、作者、版本、文件哈希和许可状态。
3. 按 Village、Bank 等功能区切片，而不是把整个世界塞进单个结构。
4. 将切片转换为项目目标版本支持的结构格式。
5. 对新版本方块/方块状态建立显式替换表；无法安全替换的内容保留在参考清单，不静默丢弃。
6. 为每个切片记录原点、目标锚点、旋转、包围盒和转换说明。
7. 在项目测试世界中预览，再执行导入。

### 3.3 许可边界

社区世界下载不等于允许公开再分发。未确认许可和作者授权前：
- 不把完整世界、schematic 或 NBT 地图二进制提交到公开仓库。
- 仓库只保存导入工具、manifest、坐标索引、替换表和说明。
- 用户本地持有的原始地图由本地导入流程读取。
- 不将社区重制项目的地图称为 Hypixel 官方原始地图。

## 4. Core 地图架构要求

继续复用现有系统，不新建平行的 Region/Warp/NPC 管理器：

- `HubRegistry`：POI 元数据、父子区域和来源记录。
- `RegionRegistry` / `RegionManager`：实际区域边界和玩家所在区域判定。
- `WarpRegistry`：跨区域/岛屿传送目标。
- `ConnectionRegistry`：区域入口、出口和世界连接关系。
- `StructureRegistry` / P5.1 导入管线：地图切片与结构生成。
- `NpcRegistry`：NPC 定义与实体交互。
- `QuestRegistry`：新手引导、区域解锁和任务进度。
- 生活/地牢玩法由扩展模组注册内容，不让 Core 直接依赖扩展模组。

POI 坐标不能自动等同于 Region 边界；区域边界必须由地图切片包围盒或人工核验得出。

## 5. 阶段任务（VS-World）

### VS-World-0：当前代码基线验收
- [ ] 对最新 main 执行 `gradlew.bat build`。
- [ ] 启动 runClient，创建全新世界。
- [ ] 验证私人岛双岛、桥、洞穴、箱子和 Jerry。
- [ ] 验证已有世界重进不会重复生成岛屿结构。
- [ ] 验证保护范围和出生点。

### VS-World-1：私人岛入口闭环
- [ ] Jerry 对话正常触发。
- [ ] 新手任务从接取、进度到领奖完整运行。
- [ ] Hub Portal 传送到 Hub Village。
- [ ] Hub 提供返回私人岛的入口/命令。
- [ ] 传送前检查目标区块、地面和安全落点。

### VS-World-2：Hub Village 切片
- [ ] 确认现代 Hub 资源在隔离实例中可读取。
- [ ] 完成 Village 切片的许可、版本、哈希和坐标记录。
- [ ] 导出并转换为 1.21.1 可用结构。
- [ ] 建立安全预览/导入流程，不能覆盖玩家已有建筑。
- [ ] 在测试世界放置 Village，核验道路、广场、建筑和高度。
- [ ] 定义 Village 的实际 Region 边界和出生/传送锚点。

### VS-World-3：Hub 服务区
- [ ] Bank 建筑与 Banker NPC。
- [ ] Auction House 建筑与入口 NPC/交互点。
- [ ] Bazaar Alley 建筑与 Bazaar 入口。
- [ ] Mining District、Farm、Combat Settlement 的道路连接。
- [ ] NPC 实体坐标以导入地图和社区 Wiki 双重核对，不直接照搬旧坐标。

### VS-World-4：Graveyard 冒险区
- [ ] 从 Hub 地图切片或可靠参考构建 Graveyard。
- [ ] 设置 Region、Warp 和怪物生成点。
- [ ] 接入至少一条可完成的击杀任务。
- [ ] 验证掉落、金币、Combat XP 和任务进度只结算一次。

### VS-World-5：其他岛屿
- [ ] 按 Gold Mine、Farming Islands、The Park、Spider's Den 等顺序逐岛接入。
- [ ] 每座岛都有独立地图 manifest、Region、Warp 和入口条件。
- [ ] 生活玩法由 skyblock_life 注册，地牢玩法由 skyblock_dungeons 注册。
- [ ] 未完成的区域入口显示锁定/未开放状态，不传送到空坐标。

## 6. 每个地图切片的 Manifest

建议统一采用以下字段：

```json
{
  "asset_id": "hub_village_modern",
  "display_name": "Hub Village",
  "source_url": "https://example.invalid/source",
  "source_type": "community_world_download",
  "source_game_version": "1.22",
  "target_minecraft_version": "1.21.1",
  "license_status": "pending_review",
  "file_sha256": "",
  "source_origin": [0, 0, 0],
  "target_origin": [0, 80, 0],
  "rotation": 0,
  "bounding_box": {
    "min": [0, 0, 0],
    "max": [0, 0, 0]
  },
  "conversion_notes": [],
  "in_game_verified": false
}
```

上例仅为字段模板，坐标与来源必须由实际地图测量填写，不能照抄示例值。

## 7. 当前状态边界

截至本计划建立时：
- 私人岛双岛布局：源码已提交，等待最新本地构建和游戏验收。
- Hub POI：已有 25 条坐标/地点参考数据；不是已建成的 Hub。
- Hub 地形/建筑：尚未导入。
- Graveyard 地形：尚未导入。
- 地图资产：已登记候选来源，未将其视为已下载、已转换或已获再分发许可。
- Portal、Minion：当前私人岛上仍是占位展示，不是完整玩法。

## 8. 当前下一步

先完成 VS-World-0 的本地构建和私人岛游戏验收。通过后，优先实现 Jerry → Hub Portal → Hub Village 的最小路线；同时开始检查 Hub Modern Version 的隔离打开与 Village 切片流程。不要先生成一整张凭空拼凑的 Hub。
