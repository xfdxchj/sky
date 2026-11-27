package com.skyrpg.life.mining.handler;

import com.skyrpg.life.mining.MiningToolType;

/** Blocks that require a pickaxe (stone, ores, dwarven metals). */
public final class PickaxeMiningHandler implements MiningHandler {
    private final double strength;
    private final int miningPowerRequirement;

    public PickaxeMiningHandler(double strength, int miningPowerRequirement) {
        this.strength = strength;
        this.miningPowerRequirement = miningPowerRequirement;
    }

    @Override
    public boolean canToolBreak(MiningToolType tool) {
        return tool == MiningToolType.PICKAXE;
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
        return "Pickaxe";
    }
}
