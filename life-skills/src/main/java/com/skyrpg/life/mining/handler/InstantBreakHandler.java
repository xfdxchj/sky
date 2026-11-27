package com.skyrpg.life.mining.handler;

import com.skyrpg.life.mining.MiningToolType;

/** Blocks that break instantly with any tool or hand (crops). */
public final class InstantBreakHandler implements MiningHandler {
    @Override
    public boolean canToolBreak(MiningToolType tool) {
        return true;
    }

    @Override
    public double strength() {
        return 0;
    }

    @Override
    public int miningPowerRequirement() {
        return 0;
    }

    @Override
    public String handlerName() {
        return "Instant";
    }
}
