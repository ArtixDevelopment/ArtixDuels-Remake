package dev.artix.artixduels.models;

import java.util.UUID;

/**
 * Representa qualificações de um jogador.
 */
public class Qualification {
    private UUID playerId;
    private int points;
    private int rank;
    private String tier;
    private long lastUpdate;

    public Qualification(UUID playerId) {
        this.playerId = playerId;
        this.points = 0;
        this.rank = 0;
        this.tier = "BRONZE";
        this.lastUpdate = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
        updateTier();
    }

    public void addPoints(int amount) {
        this.points += amount;
        updateTier();
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    private void updateTier() {
        if (points >= 5000) {
            tier = "DIAMOND";
        } else if (points >= 3000) {
            tier = "PLATINUM";
        } else if (points >= 2000) {
            tier = "GOLD";
        } else if (points >= 1000) {
            tier = "SILVER";
        } else {
            tier = "BRONZE";
        }
    }

    public boolean canParticipateInTournament(int requiredPoints) {
        return points >= requiredPoints;
    }
}

