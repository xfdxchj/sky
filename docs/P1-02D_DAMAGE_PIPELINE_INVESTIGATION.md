# P1-02D Minecraft 1.21.1 玩家伤害管线核查记录

> 日期：2026-11-27  
> 目标：Minecraft 1.21.1、Yarn 1.21.1+build.3、Fabric  
> 状态：公开 Javadoc 的方法与职责核对完成；项目实际 Loom mapped jar 的方法体/字节码尚未核验。本文不是伤害桥接实现，也不代表 P1-02D 已完成。

## 1. 已确认的 API

Yarn 1.21.1+build.3 的 `LivingEntity` Javadoc 可确认以下方法存在：

| 方法 | 可见性 | 描述/职责 | 选择器 |
|---|---|---|---|
| `damage(DamageSource, float)` | public | 进入实体受伤流程 | `damage(Lnet/minecraft/entity/damage/DamageSource;F)Z` |
| `applyArmorToDamage(DamageSource, float)` | protected | 原版护甲减伤 | `applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F` |
| `modifyAppliedDamage(DamageSource, float)` | protected | 调整最终应用的伤害；Javadoc 说明该方法处理附魔与 Resistance 效果 | `modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F` |
| `applyDamage(ServerWorld, DamageSource, float)` | protected | 应用伤害 | `applyDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)V` |

`DamageSource.isOf(RegistryKey<DamageType>)` 可用于精确识别 DamageType。当前分类器使用的 `OUT_OF_WORLD`、`GENERIC_KILL` 是类型键，不应与 Minecraft 的 armor-bypass 标签混为一谈。

参考：
- https://maven.fabricmc.net/docs/yarn-1.21.1+build.3/net/minecraft/entity/LivingEntity.html
- https://maven.fabricmc.net/docs/yarn-1.21.1+build.3/net/minecraft/entity/damage/DamageSource.html

## 2. 当前代码约束

- `LivingEntityMixin` 已在 `damage(DamageSource,float)` 的 HEAD 修改玩家收到的已登记 SkyBlock 怪物原始伤害；后续注入不得覆盖此逻辑。
- `PlayerEntityMixin` 已将 Core 管理的玩家近战攻击转交 `CombatManager` 并取消原版近战结算；不得让同一攻击再次走原版武器伤害。
- `PlayerHealthManager` 负责 RPG Health 与原版实体生命的比例同步；同步写入实体生命不能被再次解释成一笔 RPG 伤害。
- `PlayerDamageMath` 只负责 RPG Defense / True Defense 纯计算，目前没有 Minecraft 伤害入口调用它。

## 3. 为什么现在不能直接写入伤害 Mixin

仅凭 Javadoc 能确认方法存在和部分职责，不能确认 `damage` 方法体内的精确调用顺序、条件分支、盾牌/无敌帧处理位置、护甲耐久消耗位置，以及 `applyDamage` 前后的生命与 DamageTracker 更新顺序。

尤其不能直接在 `modifyAppliedDamage` 中乘 RPG Defense：如果原版护甲已在此前应用，这会形成原版护甲与 RPG Defense 的连续减伤。也不能在 `damage` HEAD 和 `applyDamage` 同时扣 RPG Health，否则会重复结算。

在没有项目实际 Loom 生成的 1.21.1+build.3 mapped jar / bytecode 前，不应猜测调用顺序并提交高风险 Mixin。

## 4. 下一步需要从本地构建环境取得的证据

在项目 Gradle 已解析依赖后，定位 Loom 生成的 Minecraft 1.21.1+build.3 mapped jar，使用 CFR / Vineflower / IntelliJ Decompiler 查看：

1. `LivingEntity.damage(DamageSource,float)` 完整方法体。
2. `applyArmorToDamage` 是否同时触发护甲耐久，及其调用条件。
3. `modifyAppliedDamage` 的实际调用位置。
4. `blockedByShield`、hurtTime / invulnerability、DamageTracker、吸收生命、`setHealth` 和死亡处理的相对顺序。
5. `applyDamage` 是否为唯一实际扣除实体生命的位置，及其子类覆写情况。

请将反编译后的相关方法体（或 mapped jar 路径与关键字节码片段）交给后续核对。不要只提供 Javadoc 方法列表。

## 5. 当前可接受的设计方向（尚未选定）

伤害桥必须在以下方案中经实际方法体核验后再选：

- **方案 A：在原版护甲减伤入口替换数值处理。** 必须确认是否能保留护甲耐久、盾牌、附魔与 Resistance 的独立行为；若一并跳过原版护甲处理，可能损失其他原版效果。
- **方案 B：保留原版护甲作为独立防御层，再叠加 RPG Defense。** 实现简单，但这不是“避免双重减伤”，只有在明确决定两套防御都应叠加时才可采用。
- **方案 C：将原版盔甲属性转换/折算进 RPG Defense，并取消其重复减伤。** 需要完整处理盔甲、韧性、附魔和药水，超出当前最小桥接范围。

首版目标仍是：玩家只扣一次 RPG HP；保留原版伤害反馈、盾牌、无敌帧、击退、DamageTracker 与死亡流程；普通伤害仅有一套明确的防御结算；True Damage 只由显式 SkyBlock 类型触发。

## 6. 状态

- 已完成：Yarn 1.21.1+build.3 Javadoc 方法签名与职责核对。
- 未完成：项目实际 Loom mapped jar / bytecode 方法体核验。
- 未完成：唯一减伤注入点选择。
- 未完成：玩家近战、登记怪物攻击和环境伤害桥接。
- 未执行：Gradle build、runClient、游戏内测试。
