package dev.artix.artixduels.models;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Representa um festival.
 */
public class Festival {
    private String id;
    private String name;
    private String description;
    private long startTime;
    private long endTime;
    private boolean active;
    private List<String> challenges;
    private Map<String, Object> rewards;
    private Map<UUID, FestivalProgress> playerProgress;
    private String theme;

    public Festival(String id, String name, long startTime, long endTime) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active && System.currentTimeMillis() >= startTime && System.currentTimeMillis() <= endTime;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<String> challenges) {
        this.challenges = challenges;
    }

    public Map<String, Object> getRewards() {
        return rewards;
    }

    public void setRewards(Map<String, Object> rewards) {
        this.rewards = rewards;
    }

    public Map<UUID, FestivalProgress> getPlayerProgress() {
        return playerProgress;
    }

    public void setPlayerProgress(Map<UUID, FestivalProgress> playerProgress) {
        this.playerProgress = playerProgress;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public static class FestivalProgress {
        private UUID playerId;
        private int points;
        private Map<String, Integer> challengeProgress;
        private int rank;

        public FestivalProgress(UUID playerId) {
            this.playerId = playerId;
            this.points = 0;
            this.challengeProgress = new java.util.HashMap<>();
            this.rank = 0;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public void addPoints(int amount) {
            this.points += amount;
        }

        public Map<String, Integer> getChallengeProgress() {
            return challengeProgress;
        }

        public void setChallengeProgress(Map<String, Integer> challengeProgress) {
            this.challengeProgress = challengeProgress;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }
}

