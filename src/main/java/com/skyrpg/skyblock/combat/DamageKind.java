package com.skyrpg.skyblock.combat;

/** Explicit RPG damage semantics; Minecraft damage tags are not equivalent to these kinds. */
public enum DamageKind {
    /** Uses the player's ordinary Defense stat. */
    NORMAL,
    /** Uses True Defense instead of ordinary Defense. */
    TRUE,
    /** Bypasses RPG Defense calculations entirely (for example, configured void damage). */
    BYPASS_RPG_DEFENSE
}
