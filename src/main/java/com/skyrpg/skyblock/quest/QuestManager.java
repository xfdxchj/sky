package com.skyrpg.skyblock.quest;

import com.skyrpg.skyblock.profile.PlayerProfile;
import com.skyrpg.skyblock.profile.ProfileManager;
import com.skyrpg.skyblock.skill.SkillManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class QuestManager {
    public enum Result { SUCCESS, UNKNOWN_QUEST, ALREADY_ACTIVE, ALREADY_COMPLETED, NOT_ACTIVE }
    private QuestManager() {}

    public static Result accept(ServerPlayerEntity player, String questId) {
        QuestDefinition quest = QuestRegistry.get(questId).orElse(null);
        if (quest == null) return Result.UNKNOWN_QUEST;
        PlayerProfile profile = ProfileManager.get(player);
        if (profile.isQuestCompleted(quest.id())) return Result.ALREADY_COMPLETED;
        if (profile.isQuestActive(quest.id())) return Result.ALREADY_ACTIVE;
        profile.acceptQuest(quest.id());
        ProfileManager.markChanged(player);
        return Result.SUCCESS;
    }

    public static Result abandon(ServerPlayerEntity player, String questId) {
        PlayerProfile profile = ProfileManager.get(player);
        if (!profile.isQuestActive(questId)) return Result.NOT_ACTIVE;
        profile.abandonQuest(questId);
        ProfileManager.markChanged(player);
        return Result.SUCCESS;
    }

    public static void onNpcTalk(ServerPlayerEntity player, String npcId) {
        PlayerProfile profile = ProfileManager.get(player);
        boolean changed = false;
        for (QuestDefinition quest : QuestRegistry.all()) {
            if (!quest.targetNpcId().equalsIgnoreCase(npcId) || !profile.isQuestActive(quest.id())) continue;
            int progress = profile.addQuestProgress(quest.id(), 1);
            changed = true;
            if (progress >= quest.requiredCount()) {
                profile.completeQuest(quest.id());
                boolean coinsGranted = quest.rewardCoins() == 0 || profile.addCoins(quest.rewardCoins());
                if (quest.rewardSkill() != null && quest.rewardSkillXp() > 0)
                    SkillManager.addExperience(player, quest.rewardSkill(), quest.rewardSkillXp());
                player.sendMessage(Text.literal("§aQuest completed: " + quest.title()), false);
                player.sendMessage(Text.literal("Rewards: "
                        + (coinsGranted ? quest.rewardCoins() + " coins" : "coin reward could not be added (Purse overflow)")
                        + (quest.rewardSkillXp() > 0 ? ", " + quest.rewardSkillXp() + " " + quest.rewardSkill().id() + " XP" : "")), false);
            } else {
                player.sendMessage(Text.literal("Quest progress: " + quest.title() + " (" + progress
                        + "/" + quest.requiredCount() + ")"), false);
            }
        }
        if (changed) ProfileManager.markChanged(player);
    }

    public static String status(PlayerProfile profile, QuestDefinition quest) {
        if (profile.isQuestCompleted(quest.id())) return "Completed";
        if (!profile.isQuestActive(quest.id())) return "Not accepted";
        return "In progress: " + profile.getQuestProgress(quest.id()) + "/" + quest.requiredCount();
    }
}
