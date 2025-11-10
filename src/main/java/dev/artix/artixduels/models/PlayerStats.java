package dev.artix.artixduels.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {
    private UUID playerId;
    private String playerName;
    private int wins;
    private int losses;
    private int draws;
    private int elo;
    private int winStreak;
    private int bestWinStreak;
    private int xp;
    private int level;
    private Map<DuelMode, ModeStats> modeStats;

    public PlayerStats(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
        this.elo = 1000;
        this.winStreak = 0;
        this.bestWinStreak = 0;
        this.xp = 0;
        this.level = 1;
        this.modeStats = new HashMap<>();
        for (DuelMode mode : DuelMode.values()) {
            modeStats.put(mode, new ModeStats());
        }
    }

    public PlayerStats() {
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public int getBestWinStreak() {
        return bestWinStreak;
    }

    public void setBestWinStreak(int bestWinStreak) {
        this.bestWinStreak = bestWinStreak;
    }

    public double getWinRate() {
        int total = wins + losses;
        if (total == 0) return 0.0;
        return (double) wins / total * 100;
    }

    public void addWin() {
        this.wins++;
        this.winStreak++;
        if (this.winStreak > this.bestWinStreak) {
            this.bestWinStreak = this.winStreak;
        }
    }

    public void addLoss() {
        this.losses++;
        this.winStreak = 0;
    }

    public void addDraw() {
        this.draws++;
    }

    public Map<DuelMode, ModeStats> getModeStats() {
        return modeStats;
    }

    public void setModeStats(Map<DuelMode, ModeStats> modeStats) {
        this.modeStats = modeStats;
    }

    public ModeStats getModeStats(DuelMode mode) {
        if (modeStats == null) {
            modeStats = new HashMap<>();
            for (DuelMode m : DuelMode.values()) {
                modeStats.put(m, new ModeStats());
            }
        }
        return modeStats.getOrDefault(mode, new ModeStats());
    }

    public void addModeWin(DuelMode mode) {
        getModeStats(mode).addWin();
    }

    public void addModeLoss(DuelMode mode) {
        getModeStats(mode).addLoss();
    }

    public void addModeKill(DuelMode mode) {
        getModeStats(mode).addKill();
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
        updateLevel();
    }

    public void addXp(int amount) {
        this.xp += amount;
        updateLevel();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private void updateLevel() {
        int newLevel = calculateLevel(xp);
        if (newLevel > level) {
            level = newLevel;
        }
    }

    private int calculateLevel(int xp) {
        return (xp / 100) + 1;
    }

    public static class ModeStats {
        private int wins;
        private int losses;
        private int kills;

        public ModeStats() {
            this.wins = 0;
            this.losses = 0;
            this.kills = 0;
        }

        public int getWins() {
            return wins;
        }

        public void setWins(int wins) {
            this.wins = wins;
        }

        public int getLosses() {
            return losses;
        }

        public void setLosses(int losses) {
            this.losses = losses;
        }

        public int getKills() {
            return kills;
        }

        public void setKills(int kills) {
            this.kills = kills;
        }

        public void addWin() {
            this.wins++;
        }

        public void addLoss() {
            this.losses++;
        }

        public void addKill() {
            this.kills++;
        }
    }
}

