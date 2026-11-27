package com.skyrpg.life.mining.handler;

import com.skyrpg.life.mining.MiningToolType;

/** Blocks that require a hoe (crops). */
public final class HoeMiningHandler implements MiningHandler {
    private final double strength;
    private final int miningPowerRequirement;

    public HoeMiningHandler(double strength, int miningPowerRequirement) {
        this.strength = strength;
        this.miningPowerRequirement = miningPowerRequirement;
    }

    @Override
    public boolean canToolBreak(MiningToolType tool) {
        return tool == MiningToolType.HOE;
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
        return "Hoe";
    }
}
