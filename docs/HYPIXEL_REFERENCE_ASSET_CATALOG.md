# Hypixel SkyBlock 地图与数据参考资源目录

> 更新时间：2026-11-27
> 用途：为 SkyBlock RPG（Minecraft 1.21.1 / Fabric）建立可追溯的地图、结构、区域和游戏数据来源。
> 重要：本目录记录外部资源，不代表这些资源已下载、已导入或获得再分发许可。所有社区地图均须先检查来源说明、许可和版本。

## 1. 地图资源（优先级）

### 1.1 Hub Island

| 资源 | 版本/状态 | 用途 | 地址 |
|---|---|---|---|
| 社区维护的全区域世界下载目录 | 2026-11-17 更新；含 Hub 1.8 与现代版 1.22（用户已确认地图在实际使用中基本可兼容项目目标版本） | 首选完整地图参考；现代版最接近当前重制 Hub | https://hypixel.net/threads/updated-hypixel-skyblock-world-downloads-for-every-single-island.5140617/ |
| Hub Modern Version | 1.22 RAR，社区上传 | 当前 Hub 建筑与地形参考；用户反馈 1.22 地图基本没有不兼容多方块，可优先按直接读取/导入路线处理，仍需项目内扫描确认 | https://www.mediafire.com/file/aybs2t2t0l7snkk/The+Hub+(Modern+Version).rar/file |
| Hub + Carnival | 1.22 RAR，社区上传 | 含 Carnival 的 Hub 变体 | https://www.mediafire.com/file/4iyjvqzslwmqcos/The+Hub+(incl.+Carnival).rar/file |
| Hub 1.8 archive | 旧版 | 仅作历史建筑/布局参考，不作为当前版本基准 | https://www.mediafire.com/file/02wd269n5mdz1zw/The_Hub.zip/file |
| 1.8.9 结束前 Hub 归档 | Planet Minecraft 社区存档，发布于 2026-03-09 | 历史参考；页面说明地图归档于 1.21.10 | https://www.planetminecraft.com/project/hypixel-skyblock-hub-last-day-of-1-8-9-archive-2026-02-24/ |

### 1.2 首个可玩闭环相关区域

以下均来自同一社区世界下载目录。优先使用现代版；目录中未提供现代版的区域，只能作为旧版参考，需评估转换损失。

| 区域 | 版本 | 下载 |
|---|---|---|
| Gold Mine | 1.22 Modern | https://www.mediafire.com/file/q056fbxwqirmon8/Gold+Mine+(Modern+Version).rar/file |
| Gold Mine | 1.8 | https://www.mediafire.com/file/szo8chwmbgaubt5/The_Gold_Mine.zip/file |
| The Farming Islands | 1.22 Modern | https://www.mediafire.com/file/4aazu2csz64u5ax/The+Farming+Islands+(Modern+Version).rar/file |
| The Farming Islands | 1.8 | https://www.mediafire.com/file/8xvbs7s2ji5q3th/The_Farming_Islands.zip/file |
| The Park | 1.22 Modern | https://www.mediafire.com/file/lvxci5sd7rpcppt/The+Park+(Modern+Version).rar/file |
| The Spider's Den | 1.22 Modern | https://www.mediafire.com/file/dvp6aspx8ekzyy5/Spider's+Den+(Modern+Version).rar/file |
| Deep Caverns | 旧版目录 | https://www.mediafire.com/file/cj5re7ft12h9oem/The_Deep_Caverns.zip/file |
| Dwarven Mines | 1.22 Modern | https://www.mediafire.com/file/6voe059rsserb9v/Dwarven+Mines+(Modern+Version).rar/file |
| Dungeon Hub | 1.22 Modern | https://www.mediafire.com/file/5i8zmasc0slhdbt/Dungeon+Hub+(Modern+Version).rar/file |
| Catacombs Entrance | 社区随机生成地图 | https://www.mediafire.com/file/sniarjp9sd6my7c/%5BDungeons%5D_The_Entrance.zip/file |
| Catacombs Floor I | 社区随机生成地图 | https://www.mediafire.com/file/ko46o1oxwl2wtqo/%5BDungeons%5D_Floor_I.zip/file |

完整列表（含 Crystal Hollows、Backwater Bayou、Lotus Atoll、Galatea、End、Crimson Isle、各层 Catacombs）见上方社区目录。它们不是当前最小闭环的前置资源。

### 1.3 可研究的完整重制项目

- SkyblockCreation-World：小规模 Hypixel SkyBlock 风格重制，公开 GitHub 世界存档，含 region、data、entities 和 datapack。
  - 仓库：https://github.com/SadanMasterMode/SkyblockCreation-World
  - 定位：研究世界结构组织、数据包与地图分区；不是 Hypixel 原始地图，也不能直接视为可嵌入的结构资产。
- HyblixelSkyBlock：Minestom 的 SkyBlock 重制代码库，含 Hub、Island、The Farming Islands 等模块。
  - 仓库：https://github.com/Hyblixel/HyblixelSkyBlock
  - 定位：参考系统拆分、区域与 NPC 数据建模；不是 Fabric Mod，也不是可直接复制的地图文件。
- HypixelRecreation：多游戏模式重制项目，含 SkyBlock 等模块。
  - 仓库：https://github.com/Swofty-Developments/HypixelRecreation
  - 定位：仅用于架构/数据参考，需逐文件核对许可证和来源。

## 2. 官方与社区数据来源

### 2.1 Hypixel 官方 API

- 官方文档：https://api.hypixel.net/
- OpenAPI/开发者入口：https://developer.hypixel.net/
- 官方公开静态资源包含 SkyBlock Items、Collections、Skills、Election 等。
- 官方 API 的 SkyBlock 玩家物品/库存数据可包含 Base64 编码的 gzip NBT；这不是世界地图下载接口。
- API Key、速率限制与服务条款必须遵守。不要把密钥提交到仓库。

首批适合纳入 Core 的官方数据：
1. 物品基础定义（ID、名称、材质、稀有度、基础属性、NPC 售价等 API 实际提供字段）。
2. 技能类型与经验需求。
3. Collections 定义与层级。
4. 后续再评估 Election 等动态数据是否适合离线模拟。

### 2.2 Wiki / 社区位置数据

- Hub 总览与子区域：https://hypixelskyblock.minecraft.wiki/w/The_Hub
- Locations 总览：https://hypixelskyblock.minecraft.wiki/w/Locations
- Graveyard：https://hypixelskyblock.minecraft.wiki/w/Graveyard
- Bazaar Alley：https://hypixelskyblock.minecraft.wiki/w/Bazaar_Alley
- NPC Hub 坐标旧表：https://hypixel-skyblock.fandom.com/wiki/NPC/List/Hub

注意：
- Wiki 数据属于社区整理，不是官方 API 保证的数据。
- 2026-02-24 Hub 已进行重大重制，旧 NPC 坐标表不能直接套用到新 Hub。
- 当前 Wiki 页面可提供子区域、NPC、怪物、任务和坐标；每条数据应保留 source_url、retrieved_at、confidence。
- 例：Graveyard 当前页面列出坐标约 -99, 71, -54，并列出 Graveyard Zombie、Zombie Villager；应与导入的地图实际坐标核对。

## 3. 地图导入到 Fabric 1.21.1 的建议管线

不要直接把外部世界存档覆盖玩家世界。推荐流程：

1. **原始归档层**：用户本地保存下载的 ZIP/RAR，记录来源 URL、下载时间、文件哈希、声明的 Minecraft 版本。
2. **隔离检查层**：在独立 Minecraft 实例打开地图，确认区块、方块、实体、方块实体、数据包和资源包；不在主开发世界测试。
3. **兼容性扫描层**：用户已确认该 1.22 地图在实际使用中基本可兼容，且不涉及大量不兼容多方块。不要预设必须整图降版本。先尝试在项目目标版本的隔离实例直接打开副本；扫描方块 ID、方块状态、方块实体、实体、数据包与维度数据。只有扫描发现不兼容项时，才对相关区块/切片做定点替换或转换。
4. **结构切片层**：先保留整张地图作为只读基准，再按功能区域导出结构（Hub Village、Bank/Auction/Bazaar、Graveyard 等）。不要把整个 Hub 塞进单个结构；切片应保留相邻道路与过渡区，避免拼接断层。
5. **坐标与锚点层**：每个切片记录 source_origin、target_origin、rotation、bounding_box、dimension、source_version、conversion_notes。
6. **数据关联层**：NPC、Region、Warp、Mob Spawn、Quest 不要从地图几何自动猜测；结合 Wiki/API/人工核验，分别保存数据文件。
7. **许可与来源层**：技术兼容与再分发许可是两件事。未经许可，不将完整地图二进制直接提交到公开仓库；仓库只存导入脚本、元数据、坐标索引和用户本地导入说明。

## 4. 对 Vertical Slice 的资源选择

第一轮只需要：
- Hub Village / Bank / General Store 周边：作为起始据点。
- Graveyard：作为首个冒险区，怪物与目标明确。
- Private Island：作为个人起始空间；若没有可用原始地图，依据公开描述制作项目自有简化起始岛，包含 Cobblestone Minion I 展示位与 Hub Portal 的视觉/功能占位。

地图优先级：
1. Hub Modern Version（先确认 1.21.1 导入可行性）。
2. Graveyard（从 Hub 地图切片，不单独拼造）。
3. Private Island（当前公开下载目录没有确认到可靠的独立官方地图下载，先以自有结构实现，并明确标记）。
4. 其余区域按可玩闭环推进，不提前导入所有大地图。

## 5. 资源验收记录模板

每个导入资源必须记录：
- asset_id
- source_url
- source_type（official_api / community_world_download / wiki / project_fixture）
- source_game_version
- target_minecraft_version
- license_or_permission_status
- file_hash
- imported_format（world / schematic / structure / data）
- bounding_box
- source_origin / target_origin
- conversion_tool_and_version
- conversion_notes
- in_game_verified（true/false）


## 6. Private Island 初始布局参考

目前没有在上述世界下载目录中确认到可信的、独立的官方 Private Island 世界文件。Wiki 对首次创建档案时的初始状态有较明确描述，因此第一版可以按公开描述制作自有结构，而不是臆造一个大浮岛：

- 玩家首次出生在两个浮岛之一。
- 第一座小岛：草方块、泥土、石头和一棵橡树；承载最初的简单任务。
- 第二座岛：Hub Portal、Jerry、Cobblestone Minion I，以及带火把和箱子的小洞穴。
- 初始箱子内容：Lava Bucket、Water Bucket、3 Bone Meal、5 Dirt、7 Grass、8 Cobblestone。
- 初始 Cobblestone Minion I 有 20 Cobblestone。
- Private Island 默认可建造区域为 161×161（后续可升级）。

参考：
- https://hypixelskyblock.minecraft.wiki/w/Private_Island
- https://hypixelskyblock.minecraft.wiki/w/Minions

以上是 Wiki 社区资料整理，不是官方可下载结构文件。实现时请在自有结构定义中保留来源字段，并将 NPC、箱子内容、Minion 作为玩法数据单独生成，避免把实体和方块实体写死在结构模板中。

## 7. 当前 Hub 版本选择注意事项

Wiki 记录 Hub 于 2026-02-24（0.24.2）进行重大重制，并与其他岛屿升级至 Minecraft 1.21.9+。因此：
- 2026-11 的社区下载目录中 Hub Modern Version 是最值得优先检查的候选。
- 1.8 Hub 归档不是当前 Hub，只适合历史布局和建筑参考。
- 本项目 Minecraft 1.21.1 低于该重制地图标注的 1.21.9+ 基线，但用户已反馈实际地图基本没有不兼容多方块。应以隔离实例的直接打开与扫描结果为准，不再默认必须降版本转换；仍禁止将原始地图直接覆盖玩家世界。
- 优先在项目 1.21.1 的隔离实例中直接打开地图副本并做兼容性扫描。若可正常读取，则直接按 Village、Bank/Auction/Bazaar、Combat Settlement/Graveyard 等功能区切片；只有遇到实际不兼容的方块状态、方块实体或数据格式时，才建立显式替换表。
- 如果不能可靠转换，则先使用地图作为坐标/视觉参考，在 1.21.1 用自有结构重建必要区域；不要静默丢方块或数据。
