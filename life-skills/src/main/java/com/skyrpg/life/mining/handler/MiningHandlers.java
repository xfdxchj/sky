package com.skyrpg.life.mining.handler;

/** Factory for mining handlers. */
public final class MiningHandlers {
    private MiningHandlers() {}

    public static MiningHandler pickaxe(double strength, int miningPower) {
        return new PickaxeMiningHandler(strength, miningPower);
    }

    public static MiningHandler axe(double strength, int miningPower) {
        return new AxeMiningHandler(strength, miningPower);
    }

    public static MiningHandler hoe(double strength, int miningPower) {
        return new HoeMiningHandler(strength, miningPower);
    }

    public static MiningHandler instant() {
        return new InstantBreakHandler();
    }
}
