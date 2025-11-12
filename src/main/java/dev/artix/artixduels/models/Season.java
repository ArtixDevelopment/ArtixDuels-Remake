package dev.artix.artixduels.models;

import java.util.Map;
import java.util.UUID;

/**
 * Representa uma temporada competitiva.
 */
public class Season {
    private String id;
    private String name;
    private long startTime;
    private long endTime;
    private boolean active;
    private Map<UUID, Integer> seasonElo;
    private Map<UUID, Integer> seasonWins;
    private Map<String, Object> rewards;

    public Season(String id, String name, long startTime, long endTime) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = false;
    }

    public Season(int seasonNumber, long startTime2, long endTime2) {
        //TODO Auto-generated constructor stub
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<UUID, Integer> getSeasonElo() {
        return seasonElo;
    }

    public void setSeasonElo(Map<UUID, Integer> seasonElo) {
        this.seasonElo = seasonElo;
    }

    public Map<UUID, Integer> getSeasonWins() {
        return seasonWins;
    }

    public void setSeasonWins(Map<UUID, Integer> seasonWins) {
        this.seasonWins = seasonWins;
    }

    public Map<String, Object> getRewards() {
        return rewards;
    }

    public void setRewards(Map<String, Object> rewards) {
        this.rewards = rewards;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > endTime;
    }

    public void setTopPlayerId(UUID fromString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTopPlayerId'");
    }

    public void setTopPlayerName(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTopPlayerName'");
    }

    public int getSeasonNumber() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSeasonNumber'");
    }

    public Object getTopPlayerId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTopPlayerId'");
    }

    public Object getTopPlayerName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTopPlayerName'");
    }
}
