# P1-02 RPG 生命与战斗状态设计

> 状态：设计基线，尚未实现。
> 目标版本：Minecraft 1.21.1 / Fabric / Java 21。
> 上游任务：P1 玩家系统完善。不要将 P1-02 改写为 P0 或重新编号。

## 1. 目标与边界

建立 Core 统一管理的玩家 RPG 生命系统，使 Health、Defense、True Defense 和 Health Regen 真正参与战斗，同时保留 Minecraft 的实体受伤反馈、死亡、重生和环境交互。

本任务只处理玩家，不重写怪物独立 HP（P3 现有实现），也不在此阶段实现完整 HUD、护甲装备属性换算、药水/附魔全套 Hypixel 机制。

## 2. 已核对的代码现状

- `StatType` 已含 `HEALTH`、`DEFENSE`、`TRUE_DEFENSE`。
- `PlayerStats` 当前只初始化 CRIT_CHANCE=30、CRIT_DAMAGE=50；HEALTH / DEFENSE / TRUE_DEFENSE / HEALTH_REGEN 未设默认值。
- `PlayerProfile` schema 为 9，持久化玩家经济、技能、物品、任务和 StatEffect，但没有当前生命字段。
- `LivingEntityMixin` 当前只替换登记怪物对玩家的攻击数值。
- `PlayerEntityMixin` 当前只处理 Core 武器的玩家近战攻击。
- `MobDefinition` 有 damage / defense，没有 trueDamage 类型字段。
- `CombatMath` 的目标 Defense 减伤公式已经采用 `100 / (100 + defense)`，但这不是玩家受伤流程。

## 3. 参考规则与来源口径

当前 Hypixel SkyBlock 社区 Wiki 的 Stats 页面记录：
- Health 基础值 100。
- Defense 基础值 0。
- True Defense 基础值 0。
- Health Regen 基础值 100。当前社区 Wiki 的 Health Regen 页面仍写 1 秒间隔，但较新的 Healing 页面（2026-10-20 编辑）记载自然恢复间隔为 2 秒；本项目以较新的 Healing 页面作为首版参考。

Defense 的基础减伤：
```
damageMultiplier = 100 / (100 + defense)
finalDamage = rawDamage * damageMultiplier
```
等价于减伤率 `defense / (defense + 100)`。True Defense 只作用于明确标记的 True Damage，普通 Defense 不参与该类伤害。

数据来源标记：以上 Hypixel 数值属于社区 Wiki 记录，不是 Mojang 或 Hypixel 官方 API 的直接字段。正式配置必须保留来源 URL、检索日期和来源类型。

## 4. 生命单位与 Minecraft 映射

Core 采用 RPG Health 单位：
- 新玩家最大 RPG Health = 100。
- RPG Health 可以由装备、饰品、技能和效果增加。
- 当前生命始终限制在 `0..maxHealth`。
- 最大生命降低时，当前生命按明确规则收敛：默认采用绝对值保留并向新最大值截断，不按比例自动补血。
- Minecraft 原版玩家最大生命维持 20 个 health points（10 颗心）作为显示/实体兼容层；Core 将 RPG HP 映射到原版生命比例：
  `minecraftHealth = 20 * currentRpgHealth / maxRpgHealth`。
- RPG 生命系统开启时，不允许其他 Core 逻辑把原版 20 点生命误当成 20 RPG HP。
- 吸收生命（Absorption）暂不计入 RPG Health；需要单独制定兼容规则后再实现。

该映射避免把 RPG Health=100 直接写入 Minecraft 最大生命属性，也避免修改原版属性上限。

## 5. 受伤结算原则

### 5.1 统一入口

服务端玩家的受伤统一经过 Core 伤害协调器。先识别来源，再确定伤害类别、Defense 规则、原版兼容规则和最终 RPG HP 损失。客户端不拥有权威生命值。

### 5.2 普通伤害

- 普通 SkyBlock Damage 先应用 RPG Defense：`raw * 100 / (100 + Defense)`。
- Defense 非负且有限；极端值必须安全钳制，避免除零、NaN 和溢出。
- 普通玩家盔甲不能再对同一笔伤害执行第二次原版护甲减伤，否则会双重减伤。
- 原版护甲耐久、盔甲附魔、抗性药水等是否保留其附加效果，必须通过明确的 DamageSource 分类和测试决定；不可简单取消整条原版 damage 流程。

### 5.3 True Damage

- 只有显式标记为 True Damage 的伤害才应用 True Defense。
- 不得仅凭“伤害绕过原版护甲”的标记就自动认定它是 Hypixel True Damage；Minecraft 的伤害标签与 Hypixel True Damage 不是同一概念。
- 在显式伤害类别完成前，现有伤害源保持普通伤害语义。
- 后续可为 MobDefinition / 攻击定义增加可选 damageType 字段；旧 JSON 缺字段时默认 NORMAL。

### 5.4 环境与原版伤害

需要逐项定义并测试：摔落、火焰/熔岩、窒息、溺水、仙人掌、爆炸、虚空、饥饿、状态效果、玩家攻击、怪物攻击。
- 原版来源必须仍能触发 Minecraft 的受伤反馈、击退、伤害追踪与死亡处理。
- 虚空等原版致死机制不得因 RPG Defense 而变成可无限存活。
- 无敌帧和盾牌行为不得被重复应用。
- 对不适合 RPG Defense 的来源（例如明确的 VOID / OUT_OF_WORLD）应在伤害分类表中明确标记为 BYPASS_RPG_DEFENSE，而不是散落硬编码判断。

## 6. 当前生命与持久化

- 在 PlayerProfile 增加当前 RPG Health 持久化字段，并提升 schema 版本。
- 新档案默认当前生命等于最大生命。
- 旧档案迁移：没有该字段时，加载为当前最大生命，不让老玩家上线即残血。
- 加载后先计算最终最大 Health，再将 currentHealth 截断到新上限。
- 每次真实 HP 变化时更新档案；避免每 tick 无意义写入。
- 玩家离线期间不进行离线回血；世界暂停时不推进恢复计时。
- 生命字段异常（负数、NaN、无穷大）按安全默认值修复。

## 7. 恢复、死亡与重生

### 恢复
- Health Regen 基础值为 100，新增共享属性标识 `HEALTH_REGEN`。
- 当前社区 Wiki 的自然恢复公式：`(MaxHealth / 100 + 1.5) * HealthRegen / 100`。
- 当前较新的 Healing 页面记载自然恢复间隔为 2 秒；较旧的 Health Regen 页面仍记载 1 秒且标注内容过时。首版采用 2 秒作为待游戏验证的配置默认值。
- Health Regen 采用服务端定时恢复，不在每 tick 创建临时对象或反复保存。
- 首版按固定 2 秒间隔执行上述自然恢复公式；战斗中/战斗外规则、特殊区域和 Vitality / Mending 不在本阶段扩展。
- 最大生命变化、死亡、重生、维度切换时重算或重置恢复状态。

### 死亡
- RPG HP 到 0 时将 Minecraft 实体生命同步到 0，让原版死亡流程负责死亡动画、死亡屏幕和重生请求。
- 死亡时不允许在同一 tick 的同步逻辑中把 HP 自动补回。
- 不在 P1-02 自行实现掉落惩罚、Coins 丢失或墓碑；这些规则需另立任务并有当前版本依据。

### 重生
- 重生后恢复到最大 RPG Health（首版规则）。
- 清除死亡前的短时受伤/恢复计时状态。
- 复活点继续使用 Minecraft 原版规则；未来 SkyBlock Island / Hub spawn 再接入传送规则。

## 8. Minecraft 1.21.1 技术实现约束

Yarn 1.21.1+build.3 文档中，LivingEntity 提供 `damage(DamageSource,float)`、`applyArmorToDamage`、`modifyAppliedDamage` 和 `applyDamage` 等不同层次。实现前必须对照当前 Loom/Yarn 映射和实际字节码确认调用顺序。

建议：
1. 在服务端玩家专用路径识别伤害来源。
2. 在不破坏原版 invulnerability、盾牌、hurtTime、damage tracker 和死亡流程的前提下，调整传入原版流程的伤害量，或在受控位置替换 RPG HP 损失。
3. 仅针对 ServerPlayerEntity；不影响普通怪物、动物和其他 LivingEntity。
4. 避免同时在 damage HEAD 与 applyDamage 再次扣 RPG HP。
5. 用同步保护标记防止 Core 自己同步实体生命时递归触发 RPG 伤害逻辑。
6. 任何 Mixin 注入点都必须有针对 1.21.1+build.3 的测试与启动验证。

## 9. 实施拆分

- P1-02A：PlayerProfile currentHealth 字段、schema 迁移、最大生命读取与夹取；不改伤害。
- P1-02B：玩家实体生命同步与登录/重生/最大生命变化生命周期。
- P1-02C：DamageKind / 伤害来源分类与 Defense / True Defense 计算单元测试。
- P1-02D：原版伤害桥接，先支持玩家近战和已登记怪物攻击；再扩展环境伤害。
- P1-02E：Health Regen 服务端调度。
- P1-02F：死亡、重生、断线和维度切换边界测试。
- 每个子任务完成后先本地 build，再进入下一个子任务；未通过不推进下一段。

## 10. 不在本任务范围

- 完整装备护甲换算、附魔、宠物、药水、吸收生命。
- 完整 HUD / Action Bar / Boss Bar。
- Hypixel 全部怪物攻击类型和 Boss 特殊伤害。
- 玩家死亡惩罚、金币丢失、墓碑与掉落规则。
- 生活职业和地牢模组自身的伤害逻辑。
