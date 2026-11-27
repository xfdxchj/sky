package com.skyrpg.life.mining;

import com.skyrpg.life.common.drop.DropTable;
import com.skyrpg.life.mining.handler.MiningHandler;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * A block with a mining handler, fortune category, base XP and drop table.
 *
 * <p>Strength, power and XP values are placeholder development data, not verified Hypixel values.</p>
 */
public record MineableBlock(
        Identifier blockId,
        MiningHandler handler,
        MiningBlockClassification classification,
        double baseXp,
        DropTable drops) {

    public MineableBlock {
        if (blockId == null) {
            throw new IllegalArgumentException("blockId is required");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler is required");
        }
        if (classification == null) {
            classification = MiningBlockClassification.NONE;
        }
        baseXp = Double.isFinite(baseXp) ? Math.max(0.0, baseXp) : 0.0;
        if (drops == null) {
            drops = new DropTable(List.of());
        }
    }
}
