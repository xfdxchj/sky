# Hub Modern 地图接入与切片作业

> 当前策略：优先直接使用用户确认基本兼容的 1.22 Hub 地图，不预设需要整图降版本。
> 项目目标仍为 Minecraft 1.21.1。任何实际不兼容项以隔离测试结果为准。
> 本文不表示地图已进入仓库，也不表示社区地图已获得再分发许可。

## 1. 当前候选地图

- 名称：The Hub (Modern Version)
- 来源页面：https://www.mediafire.com/file/aybs2t2t0l7snkk/The+Hub+(Modern+Version).rar/file
- 来源目录：https://hypixel.net/threads/updated-hypixel-skyblock-world-downloads-for-every-single-island.5140617/
- 声明版本：1.22（社区上传信息）
- 目标：Minecraft 1.21.1 / Fabric
- 当前兼容状态：用户实测反馈基本可用；项目隔离实例尚未完成独立扫描
- 再分发许可：待核验；不得将原始地图文件提交到公开仓库

## 2. 原始地图保护

1. 下载后保留原始 RAR，不在原件上操作。
2. 复制解压到单独目录，记录文件大小、SHA-256、下载日期和来源。
3. 不要将地图文件放进当前主存档的 saves 目录后直接打开。
4. 建立独立 Minecraft 1.21.1 实例和临时测试世界目录。
5. 首次打开只做读取兼容性检查；不要保存覆盖原始副本。若 Minecraft 提示升级/转换世界，先取消并备份，再决定是否继续。

## 3. 兼容性检查清单

在 1.21.1 隔离实例中记录：
- 世界是否能正常进入，是否出现 DataFixer / missing registry / unknown block 警告。
- 所有维度是否可加载。
- 方块 ID 与 BlockState 是否出现未知值或被替换。
- 方块实体是否保留：箱子、告示牌、旗帜、容器、命令方块等。
- 实体是否保留；注意 Core 当前结构导入器默认不放置结构内实体，NPC 应由 NPC Registry 单独生成。
- 数据包、函数、战利品表和自定义维度是否存在缺失。
- 关键地标的建筑轮廓、道路、装饰方块是否发生明显变化。

如果以上检查通过，就直接使用地图，不做无意义的全图转换。若只有少量不兼容方块，则先记录具体 ID/状态，再做局部替换，不进行全局盲替换。

## 4. 切片范围建议

第一批不要只切 Village 中央建筑。每个切片都应保留道路、广场、建筑周边和地形过渡，避免区域之间出现断口。

| Slice ID | 内容 | 优先级 |
|---|---|---:|
| hub_village_core | Village 主广场、出生/集散点、周边道路与地标建筑 | 1 |
| hub_bank | Bank 建筑及周边道路衔接 | 2 |
| hub_auction_house | Auction House 建筑及周边道路衔接 | 2 |
| hub_bazaar_alley | Bazaar Alley 街巷、建筑与入口区域 | 2 |
| hub_mining_district | Mining District 建筑、道路和矿区入口 | 3 |
| hub_farm_gate | Farm 入口及通往 Farming Islands 的连接区域 | 3 |
| hub_combat_settlement | Combat Settlement 与 Graveyard 方向的道路/地形过渡 | 3 |
| hub_graveyard | Graveyard 完整地形、墓地与入口 | 4 |

实际切片边界必须从地图测量，不能用 Wiki 的 POI 单点坐标代替包围盒。

## 5. 与 Core P5.1 结构导入器的衔接

Core 已有原生 Minecraft Structure NBT 导入器：
- NBT 放置路径：data/<namespace>/structures/<path>.nbt
- manifest 路径：data/skyblockrpg/skyblock/structures/*.json
- 命令：/skyblock structure list|info|paste
- 当前单个结构的包围盒体积上限：250,000 blocks
- 当前策略：目标包围盒必须全部为空；不覆盖现有地形
- 当前实体放置关闭；NPC 需由 Core NPC 系统单独布置
- 结构 manifest 支持 dimension、origin、rotation、mirror 和来源/许可字段

因此切片应控制在 250,000 体积以内。超大区域拆成多个不重叠、边界明确的子切片；不要把整张 Hub 当作一个结构。

### Manifest 示例（坐标仅为占位，严禁直接使用）

```json
{
  "id": "hub_village_core",
  "name": "Hub Village Core",
  "template": "skyblockrpg:hub/village_core",
  "dimension": "minecraft:overworld",
  "origin_x": 0,
  "origin_y": 80,
  "origin_z": 0,
  "rotation": "none",
  "mirror": "none",
  "max_blocks": 250000,
  "source": "Community Hub Modern Version map",
  "source_url": "https://www.mediafire.com/file/aybs2t2t0l7snkk/The+Hub+(Modern+Version).rar/file",
  "license": "pending review",
  "test_fixture": false
}
```

坐标必须根据实际地图源坐标、目标锚点和切片尺寸填写。正式 manifest 不得保留示例原点。

## 6. 导入顺序

1. 原始地图审计与坐标总览。
2. Village Core 切片导出、manifest 登记、测试世界预览。
3. Bank / Auction House / Bazaar 切片。
4. 道路与 Mining District / Farm / Combat Settlement 连接区域。
5. Graveyard 切片。
6. 将 POI 坐标从“社区 Wiki 参考坐标”更新为“地图测量坐标”，同时保留原始来源字段。
7. 按地图测量结果建立 Region cuboid、Warp 锚点、NPC 实体位置和连接图。

## 7. 目前需要的输入

真正生成 NBT 切片和填写准确 manifest，需要拿到实际解压后的地图文件（或至少提供地图所在目录/压缩包）。当前仓库只有下载来源链接和结构导入代码，没有地图二进制，因此不能诚实地给出切片坐标、体积、哈希或声称已经导入。

收到地图文件后，第一步不是马上贴进游戏，而是先产出：
- 地图维度与区块范围清单；
- Hub 主要建筑/区域的坐标包围盒；
- 不兼容方块与方块实体报告；
- 首批切片 manifest 草案。