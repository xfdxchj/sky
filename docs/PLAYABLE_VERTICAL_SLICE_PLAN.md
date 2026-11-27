# 可游玩最小闭环（Vertical Slice）开发计划

> 本计划是当前开发优先级的执行入口。它不替代 P1–P10 的系统任务，而是从已有系统中选取最小范围，优先打通一次真实游玩流程。
> 技术目标：Minecraft 1.21.1 / Fabric / Java 21 / 单人世界。
> 内容目标：当前 Hypixel SkyBlock 风格的起步体验；未核实的数据继续标注为项目自定义 fixture，不冒充官方数据。

## 1. 阶段目标

玩家创建或进入世界后，可以：
1. 自动获得独立玩家档案；
2. 到达一个可识别的起始据点；
3. 从 NPC 接受一个简单目标；
4. 获得或购买一件武器/装备；
5. 前往附近的冒险区域，与怪物战斗；
6. 受到伤害、恢复生命并击杀怪物；
7. 获得掉落、金币和 Combat XP；
8. 回到据点交付/完成目标，获得奖励；
9. 退出并重新进入世界后，档案、物品、金币、经验和任务进度保持正确。

这是第一版可玩闭环，不代表完成完整 Hypixel Hub、正式区域地图或全部经济系统。

## 2. 开发原则

- Core 负责主流程；life-skills 和 skyblock_dungeons 仍是依赖 Core 的独立扩展，不把其玩法搬入 Core。
- 先复用已有 PlayerProfile、Inventory、Combat、Mob、Currency、NPC、Quest、GUI、Region、Warp、Private Island 代码。
- 不为了“看起来完整”而一次性实现所有系统；每个阶段必须能独立构建和验证。
- 在真实地图资产许可未确认前，使用明确标记的项目测试区域/结构 fixture；不伪装为 Hypixel 原版地图。
- 新物品与存档迁移涉及物品丢失风险，必须先通过本地构建和迁移测试。
- 不将“代码已提交”写成“可游玩”；必须分别记录 build、runClient 和游戏内验收。

## 3. 执行阶段

### VS-0：建立可运行基线（当前门槛）

目标：确认当前 main 的累计改动能构建并启动。
- [ ] 在最新 main 执行 ./gradlew build（Windows 使用 gradlew.bat build）。
- [ ] 修复所有 Core 编译错误。
- [ ] runClient 创建全新单人世界。
- [ ] 进入世界、退出世界、重新进入，确认档案创建与持久化。
- [ ] 单独记录 Core 构建结果；Life Skills 暂不作为 Core-only 启动的必需条件。

完成标准：最新 Core 可构建、可启动、玩家档案可保存和重载。没有这一步，不接入高风险的活动物品存档迁移。

### VS-1：起始据点与玩家入口

目标：玩家进入世界后知道自己在哪里、能做什么。
- [x] 首次加入时自动创建私人岛并传送至岛屿出生点（首版代码已提交；待构建与游戏内验收）。
- [ ] 将当前单一椭圆浮岛升级为 Hypixel 初始双岛布局：主岛（草地/树木/简单任务）与副岛（洞穴、Hub Portal、Jerry、Cobblestone Minion I）。
- [ ] 提供清晰的首次进入提示和基础状态入口。
- [ ] 至少能查看 Purse、Bank、主要属性、技能等级和当前目标。
- [ ] 起始据点至少有一个可交互 NPC 或等效入口。
- [ ] 将测试 fixture 与正式 Hypixel 地图数据明确区分。

完成标准：新玩家进入世界后无需手动输入一串调试命令，就能找到下一步目标。

### VS-2：物品获取与装备

目标：形成“获得装备 → 装备生效 → 属性变化”的闭环。
- [ ] 盘点现有测试武器、商店物品、装备槽与 GUI 的实际可用路径。
- [ ] 修复背包满时装备替换、移动失败和重复领取等事务边界。
- [ ] 为首个闭环提供少量测试装备，名称、数值和来源标记为项目 fixture。
- [ ] 验证装备属性进入统一 StatSource 管线，并能正确卸下/替换。
- [ ] 验证退出重进后装备和属性保持一致。

依赖：VS-0。ItemInstance 活动档案接入前，不能假设实例强化状态已持久化；若首版沿用旧 Inventory，必须明确记录其限制，不能把它当成最终物品系统。

### VS-3：战斗、击杀与奖励

目标：形成一次完整且可重复的战斗循环。
- [ ] 选定一个可稳定到达的测试冒险区域。
- [ ] 为该区域配置有限数量的怪物生成点/生成命令入口；首版可先用受控生成，不要求完整自然刷怪。
- [ ] 验证玩家造成伤害、怪物 HP、玩家受伤、RPG Health 和恢复行为。
- [ ] 接通 MobDropDefinition 的击杀结算。
- [ ] 击杀只结算一次：物品、金币、Combat XP 不重复发放。
- [ ] 验证死亡、重生、怪物消失和服务器重启等边界。
- [ ] 添加最基本的血量/伤害/击杀反馈，至少让玩家能判断战斗结果。

依赖：VS-0、VS-2。P1-02D 原版伤害桥接仍按用户此前决定暂缓；在伤害边界没有通过验证前，不把 RPG 防御宣称为完整兼容。

### VS-4：NPC、任务与商店串联

目标：玩家能从据点接到目标，出发完成，再回来领取奖励。
- [ ] 选择现有 QuestManager 支持的目标类型，先做一个最简单的击杀或交付任务。
- [ ] NPC 对话/交互能显示任务状态并接取、提交。
- [ ] 商店能购买闭环所需的基础装备/补给。
- [ ] 任务奖励通过统一 Reward/Currency 路径发放。
- [ ] 任务进度、领取状态和奖励防重复在存档重载后保持正确。
- [ ] 至少一个 GUI 路径完整可用；命令保留作调试，不作为唯一玩家入口。

依赖：VS-2、VS-3。若现有 Quest Objective 不支持击杀目标，先增加最小必要目标类型，不提前实现通用任务编辑器。

### VS-5：世界路线与私人岛

目标：把据点、冒险区域和玩家私人空间连接起来。
- [ ] 明确起始据点、冒险区域和私人岛的实际坐标/传送入口。
- [ ] 验证 Warp、Region 与 NPC/怪物位置一致。
- [ ] 私人岛创建、回家、重启持久化和基本建造权限通过游戏测试。
- [ ] 为测试区域提供可重复生成/清理方式，避免污染玩家长期世界。
- [ ] 后续再替换/扩展为经过来源与许可核验的 Hypixel 地图资产。

依赖：VS-0 至 VS-4。首个闭环允许使用项目测试地图，不以完整 Hub 地形作为前置条件。

### VS-6：首轮整体验收

- [ ] 干净世界从创建到完成首个任务，无需开发者命令介入。
- [ ] 装备、属性、战斗、掉落、金币、经验、任务奖励均只结算一次。
- [ ] 退出重进后状态正确。
- [ ] 没有已知严重物品复制/丢失、金币复制、任务重复领奖问题。
- [ ] Core-only 与安装 Life Skills 的启动/依赖关系分别验证。
- [ ] 记录已知限制，形成首个可玩版本说明。

## 4. 当前优先顺序

1. VS-0：用户先在本地构建并启动最新 Core 基线。
2. VS-1：审查出生点、起始入口和玩家状态可见性。
3. VS-2：完成最小装备获取/装备闭环。
4. VS-3：优先打通击杀与真实奖励结算。
5. VS-4：把 NPC、任务、商店串成一次冒险流程。
6. VS-5：补齐起始据点、冒险区和私人岛之间的世界连接。
7. VS-6：整体验收。

## 5. 明确不纳入第一版闭环

- 完整 Hypixel Hub 地形与全部 NPC；
- 完整自然刷怪和所有怪物专属 AI；
- 全部正式 Hypixel 物品、附魔、Reforge、Pets；
- Collections 全量内容、配方大全、Bestiary 全量内容；
- Minion、Bazaar、Auction House 的完整模拟；
- Slayer、Boss、Dungeon；
- Crimson Isle、Rift、Crystal Hollows 等后期区域；
- Life Skills 的完整 GUI、地图和全部职业内容。

这些内容仍保留在原 P 阶段路线图中，但不阻塞第一个可玩闭环。

## 6. 首轮代码审查结果（2026-11-27）

以下是对当前 main 源码与数据文件的静态审查，不代表构建或游戏内验证：

- 已有 starter island fixture：可通过岛屿生成逻辑放置浮岛。2026-11-27 新增首次加入自动创建私人岛并传送的流程，源码已提交，尚未构建或游戏验证。当前生成几何仍是单一椭圆浮岛，不等于 Hypixel 初始双岛布局。
- 已有 starter_test_area Region fixture，范围为 Overworld 的 X/Z -64 至 64；它目前是区域数据，不是已建成的冒险地图。
- 已有训练武器、铁甲、药水/补给类测试物品和 General Store 数据；价格明确来自 project_test_values。
- 已有 MobDefinition 的奖励与 drops 数据结构；CombatManager 当前击杀结算发放金币和 Combat XP，但尚未执行 MobDropDefinition 的物品掉落。
- 已有 NPC 标签识别与交互；NPC 需要世界中存在带正确命令标签的实体，当前入口代码未显示自动布置完整 NPC 场景。
- 已有基础 GUI 菜单；商店页面目前是目录展示，购买仍提示使用命令；NPC 商店交互也以聊天信息/命令提示为主。
- QuestDefinition 当前只接受 talk_to_npc；首个战斗任务需要新增最小 kill_mob 目标与可靠的击杀事件结算。
- ItemInstance 与 NBT codec 已存在，但尚未接入 PlayerInventory 活动存档；首轮装备闭环暂时必须基于旧逻辑背包，或等 ItemInstance 迁移通过构建与迁移验收后再切换。

据此，VS-1 的第一项实际工作应是明确并实现首次进入的起始落点/据点入口，而不是先堆 NPC 数量或地图 POI。VS-3 的关键缺口是掉落执行与一次性击杀结算；VS-4 的关键缺口是击杀任务目标和可交互的 GUI 交易路径。


## 7. 外部地图与参考资料

地图和数据来源统一登记在 [HYPIXEL_REFERENCE_ASSET_CATALOG.md](HYPIXEL_REFERENCE_ASSET_CATALOG.md)。当前优先级：
1. Hub Modern Version：作为当前 Hub 视觉/结构参考，先在隔离实例检查；地图版本 1.21.9+/1.22，与项目 1.21.1 不兼容风险尚未解决。
2. Hub Village 与 Bank/商店周边：首个起始据点切片。
3. Graveyard：首个冒险区域切片。
4. Private Island：暂无已确认的官方独立地图下载，依据 Wiki 初始布局描述制作项目自有结构。

任何外部地图均不得直接覆盖玩家世界；先记录来源、版本、许可、哈希和转换说明，再切片导入。完整地图未实际导入前，VS-1/VS-3 仍不得标记为完成。


## 8. VS-1 首次进入流程（2026-11-27）

- `SkyBlockRPG` 的 JOIN 回调在玩家档案加载后检查 `PrivateIslandManager.get(player)`。
- 没有私人岛记录时调用 `PrivateIslandManager.create(player)`；创建成功后调用 `visitHome(player)`，并显示首次进入提示。
- 已有私人岛记录的玩家不会在每次登录时被强制传送，保留其当前所在位置。
- 创建失败或回家失败时向玩家显示原因并记录服务端错误日志，不写入“已成功传送”的假状态。
- 该改动复用现有天空网格分配、空域预检、浮岛生成和私人岛持久化，不覆盖玩家世界原有方块。
- 代码提交：`837ccee`。当前仅为源码提交；未执行 Gradle build、runClient 或游戏内验收。
- 后续还原目标：依据 `HYPIXEL_REFERENCE_ASSET_CATALOG.md` 的 Wiki 初始布局资料，将单岛升级为主岛+副岛，并分别生成树木、洞穴、箱子初始物资、Jerry、Hub Portal 和 Cobblestone Minion I。以上实体/物资应由玩法数据生成，不写死在结构 NBT 中。


## 9. VS-1 私人岛双岛布局首版（2026-11-27）

本轮源码提交将新玩家首次创建的单一椭圆浮岛升级为项目自有的双岛起始布局：

- 主岛：草方块/泥土/石头浮岛、橡树、出生点。
- 副岛：独立草方块浮岛、简易洞穴、火把和初始箱子。
- 初始箱子：熔岩桶×1、水桶×1、骨粉×3、泥土×5、草方块×7、圆石×8；使用 Minecraft 原版物品作为首轮可操作内容。
- Jerry：以无敌、静音、禁用 AI 的村民实体生成，绑定 Core NPC 定义 jerry，可触发现有 NPC 交互逻辑。
- Hub Portal：黑曜石框与紫色玻璃的装饰性占位，尚未连接 Hub。
- Cobblestone Minion：目前只有展示底座，不是可工作的 Minion；待 Core Minion 系统实现后接入。
- 两岛在现有私人岛 64 格直径保护范围内生成，主岛出生点偏移到主岛中心；首次创建仍只写入一条玩家所有权记录。
- 生成器只在空气位置放置结构块；既有地形不会被覆盖。洞穴开挖仅作用于新生成的副岛内部。

相关代码：
- PrivateIslandStarterLayout.java
- PrivateIslandManager.java
- data/skyblockrpg/skyblock/npcs/jerry.json

提交：
- 2a1a2fb 双岛布局生成器
- fe46106 私人岛创建流程接入双岛生成
- 905db4f Jerry NPC 定义

以上均为源码提交，尚未执行本轮改动后的 Gradle build / runClient / 游戏内验收。请重点验证双岛是否完整生成、箱子库存是否正确、Jerry 能否交互，以及保护范围是否覆盖两个岛。


## 10. VS-3 / VS-4 首个战斗任务闭环实现（2026-11-28）

本轮已提交以下源码与数据，状态为“已写入仓库，待本地构建和游戏内验收”：

- Quest objective 新增 `kill_mob`，通过 `target_mob_id` 绑定 MobRegistry 定义；旧的 `talk_to_npc` 数据保持兼容。
- QuestRegistry 加载时校验 NPC / Mob 目标存在，且启动顺序为 MobRegistry → NpcRegistry → QuestRegistry。
- CombatManager 在玩家击杀已注册怪物后推进击杀任务，并调用物品掉落结算。
- 物品掉落仅发放概率已知、且 Core ItemRegistry 已注册的 `item` 类型；未知概率不发放，不把未知概率当作 0% 或 100%。
- 新增 Core 逻辑物品 `rotten_flesh`，用于 Graveyard Zombie 的已知掉落。
- 新增项目自有 `StarterGraveyardManager`，在服务器启动时于 Overworld (512, 220, 512) 附近生成测试冒险浮岛，并确保启动时存在最多三只带有 `graveyard_zombie` 标签的僵尸。
- 新增 `starter_graveyard` Region 与 Warp。
- Jerry 的任务板会显示任务目标、接取命令和 Graveyard 路线。
- 新增 `docs/VS3_COMBAT_QUEST_SLICE_TEST_PLAN.md`，列出构建、全新世界、击杀结算、重启持久化和边界验收步骤。

新增项目任务数据：
- `first_steps`：与 Jerry 对话，奖励 100 coins、10 Social XP。
- `clear_the_graveyard`：击杀 3 只 Graveyard Zombie，奖励 50 coins、25 Combat XP。

重要限制：
- Starter Graveyard 是临时项目测试区域，不是 Hypixel 原版地图。
- 僵尸目前只在服务器启动时补足；同一次运行期间击杀后不会自动重生。
- Jerry 任务板仍是聊天 + 命令交互，不是最终 Quest GUI。
- P1-02D 原版伤害桥接仍按此前决定暂缓。
- 本轮未执行 Gradle build、runClient 或游戏内验收，不能标记 VS-3 / VS-4 完成。

下一步：
1. 用户本地构建并验证本轮代码。
2. 根据反馈修复构建或运行问题。
3. 再做 Jerry 任务接取 GUI、冒险区内怪物重生，以及真实地图切片替换。
