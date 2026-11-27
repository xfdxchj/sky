package com.skyrpg.life.skill;

import net.minecraft.util.Identifier;

/** Stable identifiers for the four life skills. No enum: skills are addressed by Identifier. */
public final class LifeSkills {
    public static final Identifier MINING = Identifier.of("skyblock_life", "mining");
    public static final Identifier FISHING = Identifier.of("skyblock_life", "fishing");
    public static final Identifier FARMING = Identifier.of("skyblock_life", "farming");
    public static final Identifier FORAGING = Identifier.of("skyblock_life", "foraging");

    private LifeSkills() {}
}
