package com.skyrpg.life.mining.handler;

import com.skyrpg.life.mining.MiningToolType;

/** Defines which tools can break a block, its strength and its breaking power requirement. */
public interface MiningHandler {
    boolean canToolBreak(MiningToolType tool);

    /** Vanilla-like block strength used for break-time calculation. */
    double strength();

    int miningPowerRequirement();

    String handlerName();
}
