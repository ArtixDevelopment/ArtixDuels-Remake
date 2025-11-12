package dev.artix.artixduels.models;

import java.util.*;

/**
 * Representa um replay de duelo.
 */
public class Replay {
    private UUID replayId;
    private UUID player1Id;
    private String player1Name;
    private UUID player2Id;
    private String player2Name;
    private String kitName;
    private String arenaName;
    private DuelMode mode;
    private long startTime;
    private long endTime;
    private long duration;
    private UUID winnerId;
    private String winnerName;
    private List<ReplayFrame> frames;
    private Map<String, Object> metadata;

    public Replay(UUID player1Id, String player1Name, UUID player2Id, String player2Name,
                  String kitName, String arenaName, DuelMode mode) {
        this.replayId = UUID.randomUUID();
        this.player1Id = player1Id;
        this.player1Name = player1Name;
        this.player2Id = player2Id;
        this.player2Name = player2Name;
        this.kitName = kitName;
        this.arenaName = arenaName;
        this.mode = mode;
        this.startTime = System.currentTimeMillis();
        this.frames = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public UUID getReplayId() {
        return replayId;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public String getKitName() {
        return kitName;
    }

    public String getArenaName() {
        return arenaName;
    }

    public DuelMode getMode() {
        return mode;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.duration = endTime - startTime;
    }

    public long getDuration() {
        return duration;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinner(UUID winnerId, String winnerName) {
        this.winnerId = winnerId;
        this.winnerName = winnerName;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public List<ReplayFrame> getFrames() {
        return frames;
    }

    public void addFrame(ReplayFrame frame) {
        frames.add(frame);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public int getTotalFrames() {
        return frames.size();
    }
}

