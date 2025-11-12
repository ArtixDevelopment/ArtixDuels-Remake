package dev.artix.artixduels.models;

import java.util.UUID;

/**
 * Progresso de um jogador em uma conquista.
 */
public class AchievementProgress {
    private UUID playerId;
    private String achievementId;
    private int progress;
    private boolean unlocked;
    private long unlockedAt;

    public AchievementProgress(UUID playerId, String achievementId) {
        this.playerId = playerId;
        this.achievementId = achievementId;
        this.progress = 0;
        this.unlocked = false;
        this.unlockedAt = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(String achievementId) {
        this.achievementId = achievementId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void addProgress(int amount) {
        this.progress += amount;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        if (unlocked && unlockedAt == 0) {
            this.unlockedAt = System.currentTimeMillis();
        }
    }

    public long getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(long unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public double getProgressPercentage(Achievement achievement) {
        if (achievement == null) return 0.0;
        return Math.min(100.0, (double) progress / achievement.getTargetValue() * 100.0);
    }
}

