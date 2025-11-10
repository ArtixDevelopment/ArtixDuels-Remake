package dev.artix.artixduels.models;

import org.bukkit.entity.Player;

import java.util.UUID;

public class DuelRequest {
    private UUID challengerId;
    private UUID targetId;
    private String kitName;
    private String arenaName;
    private DuelMode mode;
    private long timestamp;
    private boolean expired;

    public DuelRequest(Player challenger, Player target, String kitName, String arenaName, DuelMode mode) {
        this.challengerId = challenger.getUniqueId();
        this.targetId = target.getUniqueId();
        this.kitName = kitName;
        this.arenaName = arenaName;
        this.mode = mode;
        this.timestamp = System.currentTimeMillis();
        this.expired = false;
    }

    public UUID getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(UUID challengerId) {
        this.challengerId = challengerId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    public String getArenaName() {
        return arenaName;
    }

    public void setArenaName(String arenaName) {
        this.arenaName = arenaName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isExpired(long timeout) {
        return System.currentTimeMillis() - timestamp > timeout;
    }

    public DuelMode getMode() {
        return mode;
    }

    public void setMode(DuelMode mode) {
        this.mode = mode;
    }
}

