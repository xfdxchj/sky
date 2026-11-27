package com.skyrpg.life.mining.handler;

import com.skyrpg.life.mining.MiningToolType;

/** Blocks that require an axe (logs). */
public final class AxeMiningHandler implements MiningHandler {
    private final double strength;
    private final int miningPowerRequirement;

    public AxeMiningHandler(double strength, int miningPowerRequirement) {
        this.strength = strength;
        this.miningPowerRequirement = miningPowerRequirement;
    }

    @Override
    public boolean canToolBreak(MiningToolType tool) {
        return tool == MiningToolType.AXE;
    }

    @Override
    public double strength() {
        return strength;
    }

    @Override
    public int miningPowerRequirement() {
        return miningPowerRequirement;
    }

    @Override
    public String handlerName() {
        return "Axe";
    }
}
