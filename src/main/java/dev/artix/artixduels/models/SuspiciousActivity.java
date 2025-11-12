package dev.artix.artixduels.models;

import java.util.UUID;

/**
 * Representa uma atividade suspeita detectada.
 */
public class SuspiciousActivity {
    private UUID playerId;
    private String playerName;
    private ActivityType type;
    private String description;
    private long timestamp;
    private int violationLevel;
    private String evidence;

    public enum ActivityType {
        AUTO_CLICK("Auto-Click"),
        REACH("Reach"),
        SUSPICIOUS_MOVEMENT("Movimento Suspeito"),
        FLY("Fly"),
        SPEED("Speed");

        private String displayName;

        ActivityType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public SuspiciousActivity(UUID playerId, String playerName, ActivityType type, String description) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.type = type;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
        this.violationLevel = 1;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public ActivityType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getViolationLevel() {
        return violationLevel;
    }

    public void setViolationLevel(int violationLevel) {
        this.violationLevel = violationLevel;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }
}

