# P1-02 RPG 生命与战斗状态测试计划

> 状态：P1-02A/B/C 代码已提交，尚未完成本地构建与验收；本文件是验收标准，不代表测试已经执行。
> 对应设计：`P1-02_RPG_HEALTH_DESIGN.md`。

## A. 档案与迁移

1. 新玩家首次进入：Health=100、Health Regen=100、Defense=0、True Defense=0；最大 RPG Health 为 100，当前生命为 100。
2. schema 9 旧档案加载：当前生命迁移为加载后的最大生命，不丢失 Purse、Bank、技能、装备、饰品、任务和 StatEffect；保存后写入 schema 10 与 currentHealth。
3. 当前生命保存后退出重进：数值保持。
4. 当前生命为负数、NaN、正负无穷或超最大值：安全修复到合法范围。
5. 最大 Health 增加：当前绝对值不自动增加。
6. 最大 Health 降低：当前生命截断到新最大值。

## B. RPG / Minecraft 生命同步（P1-02B）

1. 首次加入：RPG Health 100/100 映射为原版 20/20。
2. 当前 RPG Health 为 50/100 时，原版生命显示为 10/20；重新加入后保持 10/20。
3. 原版受到普通伤害后，服务端同步按原版生命比例更新 RPG currentHealth，并在退出重进后保持。
4. 原版治疗后，RPG currentHealth 同比例恢复，不超过 maxHealth。
5. RPG maxHealth 增加或降低时，保留绝对 currentHealth；降低导致超上限时截断，并同步原版生命比例。
6. 通过档案侧接口改变 currentHealth 时，下一次服务端同步正确更新原版生命。
7. 死亡时 RPG currentHealth 变为 0；重生事件后恢复至最大 RPG Health。
8. 断线清除在线同步状态；重新加入从持久化档案重新初始化，不串用上次会话状态。
9. 多名玩家同时在线时 UUID 状态互不干扰。

## C. 属性与伤害公式

0. Health Regen=100 时，首版自然恢复公式按 `(MaxHealth / 100 + 1.5) * 1.0` 计算，默认每 2 秒结算一次（以较新的 Healing Wiki 页面为待验证参考）。

1. 默认 Health=100、Defense=0、True Defense=0。
2. Defense=100 时，普通伤害减半。
3. Defense=300 时，普通伤害为原始伤害的 25%。
4. Defense=0 时普通伤害不变。
5. True Defense 不影响普通伤害。
6. True Damage 只使用 True Defense；Defense 不参与。
7. BYPASS_RPG_DEFENSE 不受 Defense / True Defense 影响。
8. null DamageKind 按不应用 RPG 防御处理。
9. 非有限值和极端数值不会产生 NaN、负伤害或溢出。

## D. 原版伤害兼容

P1-02D-0 分类器用例（当前仅分类，不代表已接入伤害结算）：
- `OUT_OF_WORLD` → `BYPASS_RPG_DEFENSE`。
- `GENERIC_KILL` → `BYPASS_RPG_DEFENSE`，避免 `/kill` 被 RPG Defense 抵消。
- 普通玩家近战、普通怪物近战、摔落、火焰、熔岩、爆炸等 → `NORMAL`。
- Minecraft armor-bypass 标签本身不得映射为 `TRUE`。
- `TRUE` 只能由未来明确声明 SkyBlock True Damage 的来源产生；当前无默认 True Damage 来源。

分别测试：
- 玩家近战攻击。
- `OUT_OF_WORLD` / `GENERIC_KILL` 致死行为。
- 已登记 SkyBlock 怪物攻击玩家。
- 未登记原版怪物攻击玩家。
- 摔落、火焰、熔岩、窒息、溺水、仙人掌、爆炸、虚空、饥饿。
- 抗性效果、盾牌、无敌帧和吸收生命。

每项核验：RPG HP 只扣一次；Minecraft 受伤反馈正常；不会同时应用两套护甲减伤；死亡来源与原版一致。

## E. 恢复

1. 非战斗状态按配置间隔恢复。
2. 当前生命达到最大值后停止恢复。
3. 死亡状态不恢复。
4. 离线期间不补算离线恢复。
5. 世界暂停时不推进恢复计时。
6. 连续加载/卸载世界不重复注册恢复任务。

## E-1. Health Regen 服务端调度（P1-02E-0，待执行）

1. 新玩家 Health=100、Health Regen=100，等待 40 个世界游戏刻，RPG Health 增加 2.5。
2. MaxHealth=200、HealthRegen=100 时，每次恢复量为 3.5。
3. HealthRegen=200 时，恢复量按比例翻倍；HealthRegen=0 或负值时不恢复。
4. 当前生命低于最大值时恢复；恢复不会超过最大生命。
5. 满血时不产生无意义档案写入。
6. 死亡玩家不恢复；重生后重新开始 40 tick 计时，不立即额外回血。
7. 登录后不立即回血；等待完整 40 个世界游戏刻后才结算。
8. 维度切换后重新开始计时，不因新维度世界时间较大而瞬间补发。
9. 退出期间不补算离线恢复；重新登录重新计时。
10. 暂停世界时世界游戏时间不推进，因此不应累计恢复。
11. 多玩家计时互相独立；一名玩家的维度切换或重生不影响其他玩家。
12. 服务器停止后清理计时状态；重新启动后从登录时重新计时。

说明：P1-02E-0 当前源码使用 40 个世界游戏刻（约 2 秒游戏时间）作为首版间隔。该规则参考社区 Wiki 较新的 Healing 页面，尚待目标版本游戏内核验，不代表已验证与 Hypixel 当前线上实现完全一致。

## F. 死亡与重生

1. RPG HP 到 0 后进入原版死亡流程。
2. 死亡 tick 不会被同步逻辑补血。
3. 重生后恢复至最大 RPG Health。
4. 断线后重连不会把死亡玩家错误复活或把存活玩家置为 0。
5. 重生点遵循原版规则。
6. 死亡和重生不重复触发奖励或扣款。

## G. 回归

- Core-only 单人世界。
- Core + Life Skills API smoke-test Mod。
- Core + Dungeon 扩展（若该模组已有可运行测试版本）。
- 普通原版生物仍可正常受伤和死亡。
- 玩家攻击怪物、怪物攻击玩家、怪物独立 HP（P3）无回归。
- 保存、退出、重载和世界关闭后状态正确。

## H. 验收口径

只有完成 Gradle build、runClient 启动和上述约定的游戏内测试，才能将 P1-02 标记为已验收。代码提交或静态检查通过不等于构建和游戏测试通过。
