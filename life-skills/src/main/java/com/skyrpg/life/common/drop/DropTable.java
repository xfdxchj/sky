package com.skyrpg.life.common.drop;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/** A simple chance-based drop table. Fortune/mechanic bonuses are applied by callers, not here. */
public final class DropTable {
    private final List<DropEntry> entries;

    public DropTable(List<DropEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public List<DropResult> roll(Random random) {
        List<DropResult> results = new ArrayList<>();
        for (DropEntry entry : entries) {
            if (entry.chanceKnown() && random.nextDouble() >= entry.chance()) {
                continue;
            }
            int span = entry.maxCount() - entry.minCount();
            int count = entry.minCount() + (span > 0 ? random.nextInt(span + 1) : 0);
            if (count > 0) {
                results.add(new DropResult(entry.itemId(), count));
            }
        }
        return results;
    }

    public List<DropEntry> entries() {
        return entries;
    }

    /** One rolled item stack: a namespaced item id plus a count. */
    public record DropResult(Identifier itemId, int count) {}
}
