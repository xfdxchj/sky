package com.skyrpg.skyblock.npc;

import java.util.List;
import java.util.Locale;

/** Data-driven NPC identity, placement constraints and interaction behavior. */
public record NpcDefinition(
        String id, String displayName, String entityType, String requiredRegionId,
        double interactionRadius, List<String> dialogue, String action, String actionTarget) {
    public NpcDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("NPC id cannot be blank");
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        entityType = entityType == null || entityType.isBlank() ? "minecraft:villager" : entityType;
        requiredRegionId = requiredRegionId == null ? "" : requiredRegionId;
        if (!Double.isFinite(interactionRadius) || interactionRadius <= 0 || interactionRadius > 16)
            throw new IllegalArgumentException("NPC interaction_radius must be > 0 and <= 16");
        dialogue = dialogue == null ? List.of() : List.copyOf(dialogue);
        action = action == null ? "none" : action.toLowerCase(Locale.ROOT);
        if (!List.of("none", "bank", "shop", "warp").contains(action))
            throw new IllegalArgumentException("Unsupported NPC action: " + action);
        actionTarget = actionTarget == null ? "" : actionTarget;
        if (action.equals("warp") && actionTarget.isBlank())
            throw new IllegalArgumentException("Warp NPC action requires action_target");
    }
}
