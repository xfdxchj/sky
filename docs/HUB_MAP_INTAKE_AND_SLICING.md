# Hub Modern 地图接入与切片作业

## 0. 当前自动切片工具

已新增 [Hub NBT 切片器说明](HUB_MAP_SLICER.md) 与 `tools/slice_structure_nbt.py`。
工具按固定三维网格切片，保留 palette、state 索引和每个方块携带的 NBT，
并生成包含每片源偏移/尺寸/计数的 JSON 索引。默认建议 96×64×96，
对应当前 Hub 尺寸的 48 片。切片器的合成 NBT 往返与坐标守恒测试已接入
Fabric Build 工作流。

注意：这只是 NBT 文件切片能力，尚不等于游戏内整图批量导入。Core 目前
仍逐个结构执行“目标包围盒必须全为空气”的预检；批量导入管理器需要先
对所有切片整体预检，再分批放置。真实 Hub NBT 的切片结果仍需在本地
仓库执行命令生成，切片文件不提交到公开仓库。

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

## 5. 初步世界坐标预设（非实测）

为提前准备世界布局，暂定 Hub 目标规划原点为 Overworld `(0, 80, 0)`。这只是规划锚点，不代表原地图实际坐标，也不意味着整个 Hub 的尺寸已经确定。正式导入前必须根据地图测量结果确定源包围盒、目标偏移和落地高度。

Core 当前私人岛分配器使用基准 `(8192, 8192)`、64 列、512 格间距、表面 Y=270。Hub 的暂定原点与私人岛首个分配区相距较远；仍须用 Hub 完整包围盒检查是否与私人岛网格或未来岛屿区域冲突。其他岛屿不提前编造绝对坐标，先建立相对连接关系。

## 6. 与 Core P5.1 结构导入器的衔接

Core 已有原生 Minecraft Structure NBT 导入器：
- NBT 放置路径：data/<namespace>/structures/<path>.nbt
- manifest 路径：data/skyblockrpg/skyblock/structures/*.json
- 命令：/skyblock structure list|info|paste
- 当前单个结构的包围盒体积上限：4,000,000 blocks（代码硬上限；manifest 默认 250,000；实际导入仍应按内存、tick 与目标世界压力控制切片规模）
- 当前策略：目标包围盒必须全部为空；不覆盖现有地形
- 当前实体放置关闭；NPC 需由 Core NPC 系统单独布置
- 结构 manifest 支持 dimension、origin、rotation、mirror 和来源/许可字段

因此切片的硬上限是 4,000,000 体积，但这不是推荐切片大小。建议仍按功能区拆分，避免一次导入整张 Hub；超大区域拆成多个不重叠、边界明确的子切片。

**当前导入器的重要限制：** `StructureImportManager` 要求整个目标包围盒均为空气，并且默认忽略结构实体。这意味着不能把切片直接贴到普通已生成地形上。拿到地图后，需要先判断该地图是否适合作为新世界的基础存档；若要把建筑合并进现有世界，则需准备受控空域/预清理区，或另行设计明确的 replace policy。不得默认覆盖地形或玩家建筑。

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

## 7. 导入顺序

1. 原始地图审计与坐标总览。
2. Village Core 切片导出、manifest 登记、测试世界预览。
3. Bank / Auction House / Bazaar 切片。
4. 道路与 Mining District / Farm / Combat Settlement 连接区域。
5. Graveyard 切片。
6. 将 POI 坐标从“社区 Wiki 参考坐标”更新为“地图测量坐标”，同时保留原始来源字段。
7. 按地图测量结果建立 Region cuboid、Warp 锚点、NPC 实体位置和连接图。

## 8. 已上传结构资产与本地审计

仓库现已收到 12 个结构 NBT 文件（提交：`568d50d7bcafebc47dfbd9507ce24b974c85bbe8`）：

- `worlds/structures/the_hub_incl_carnival.nbt` — Hub（含 Carnival）
- `worlds/structures/gold_mine_modern_version.nbt`、`worlds/structures/gold_mine.nbt` — Gold Mine 两个版本
- `worlds/structures/the_deep_caverns.nbt` — Deep Caverns
- `worlds/structures/dwarven_mines_modern_version.nbt` — Dwarven Mines
- `worlds/structures/spiders_den_modern_version.nbt` — Spider's Den
- `worlds/structures/the_end_modern_version.nbt` — The End
- `worlds/structures/the_farming_islands_modern_version.nbt` — Farming Islands
- `worlds/structures/jerrys_workshop.nbt` — Jerry's Workshop
- `worlds/structures/dungeon_hub_modern_version.nbt` — Dungeon Hub
- `worlds/structures/floor_vii_world_download.nbt` — Floor VII
- `worlds/structures/galatea.nbt` — Galatea

已新增不依赖第三方 Python 包的只读审计脚本：`tools/inspect_structure_nbt.py`。在仓库根目录运行：

```powershell
python tools/inspect_structure_nbt.py worlds/structures --output reports/structure_audit.json
```

脚本支持 gzip/zlib/未压缩 NBT，输出结构尺寸、包围盒体积、DataVersion、单/多 palette 方块状态计数、方块实体数、实体类型、方块坐标范围，以及超出结构 size 的方块坐标数量；不会修改 NBT。多 palette 结构会分别按每套 palette 统计，`invalid_state_indices` 以第一套 palette 为主，同时在 `palette_variants` 中保留每套 palette 的独立校验结果。注意：GitHub 文件浏览器/连接器无法解码这些二进制 NBT，必须在本地仓库运行脚本才能得到真实测量结果。

**当前已确认的是 12 个结构 NBT 已提交；尚未确认它们的实际尺寸、坐标、方块统计和游戏内兼容性。** 不能把这些结构文件误称为完整可运行的世界存档，也不能在审计前假设 Hub NBT 的坐标就是目标世界坐标。

## 9. Hub 坐标对齐审计（2026-11-28）

已根据 Structure Audit artifact 完成 NBT 结构事实核验；尺寸为 384×157×336，方块条目 1,184,911，实体与方块实体均为 0，NBT 相对坐标范围为 (0,0,0) 至 (383,156,335)。**这不包含原始世界绝对坐标或玩家出生锚点**，所以暂定原点 `(0,80,0)` 仍然只是规划占位，禁止用于正式生成或首次进入传送。

详细的坐标对齐方法、锚点验证要求与运行时恢复限制见 [Hub Coordinate Alignment Audit](HUB_COORDINATE_ALIGNMENT_AUDIT.md)。在获得同版本原始世界或可验证的坐标对应点前，Hub 自动组装和首次进入路由保持关闭。

## 10. 审计完成后需要产出的结果

真正生成准确 manifest 前，先产出：
- 地图维度与区块范围清单；
- Hub 主要建筑/区域的坐标包围盒；
- 不兼容方块与方块实体报告；
- 首批切片 manifest 草案。

审计后可进一步生成便于人工审阅的摘要：

```powershell
python tools/summarize_structure_audit.py reports/structure_audit.json --markdown reports/structure_audit_summary.md
```

摘要会列出各结构的尺寸、包围盒体积、方块/方块实体/实体数量，以及 palette 索引错误和越界坐标提示。它不会推断地图内 POI 的真实名称或坐标，也不会自动生成正式 manifest。GitHub Actions 在 NBT 或审计工具更新时会自动运行，并将 JSON 原始报告与 Markdown 摘要作为 `structure-audit` artifact 保存。
