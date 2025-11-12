package dev.artix.artixduels.models;

import java.util.UUID;

/**
 * Representa uma entrada no histórico de duelos.
 */
public class DuelHistoryEntry {
    private UUID duelId;
    private UUID opponentId;
    private String opponentName;
    private DuelMode mode;
    private String kitName;
    private String arenaName;
    private boolean won;
    private long timestamp;
    private int duration; // em segundos
    private int kills;
    private int deaths;

    public DuelHistoryEntry(UUID duelId, UUID opponentId, String opponentName, DuelMode mode,
                           String kitName, String arenaName, boolean won, long timestamp,
                           int duration, int kills, int deaths) {
        this.duelId = duelId;
        this.opponentId = opponentId;
        this.opponentName = opponentName;
        this.mode = mode;
        this.kitName = kitName;
        this.arenaName = arenaName;
        this.won = won;
        this.timestamp = timestamp;
        this.duration = duration;
        this.kills = kills;
        this.deaths = deaths;
    }

    public UUID getDuelId() {
        return duelId;
    }

    public UUID getOpponentId() {
        return opponentId;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public DuelMode getMode() {
        return mode;
    }

    public String getKitName() {
        return kitName;
    }

    public String getArenaName() {
        return arenaName;
    }

    public boolean isWon() {
        return won;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }
}

