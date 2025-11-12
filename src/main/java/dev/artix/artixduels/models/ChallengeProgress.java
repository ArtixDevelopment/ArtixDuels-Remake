package dev.artix.artixduels.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa o progresso de um jogador em um desafio.
 */
public class ChallengeProgress {
    private UUID playerId;
    private String challengeId;
    private int progress;
    private boolean completed;
    private long completedAt;
    private Map<String, Object> metadata;

    public ChallengeProgress(UUID playerId, String challengeId) {
        this.playerId = playerId;
        this.challengeId = challengeId;
        this.progress = 0;
        this.completed = false;
        this.completedAt = 0;
        this.metadata = new HashMap<>();
    }

    public ChallengeProgress(UUID playerId, String challengeId, int progress, 
                           boolean completed, long completedAt, Map<String, Object> metadata) {
        this.playerId = playerId;
        this.challengeId = challengeId;
        this.progress = progress;
        this.completed = completed;
        this.completedAt = completedAt;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getChallengeId() {
        return challengeId;
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && completedAt == 0) {
            this.completedAt = System.currentTimeMillis();
        }
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }
}

