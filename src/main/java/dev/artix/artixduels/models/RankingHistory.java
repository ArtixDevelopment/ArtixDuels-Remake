package dev.artix.artixduels.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Armazena o histórico de posições de um jogador no ranking.
 */
public class RankingHistory {
    private UUID playerId;
    private String rankingType;
    private List<RankingHistoryEntry> history;

    public RankingHistory(UUID playerId, String rankingType) {
        this.playerId = playerId;
        this.rankingType = rankingType;
        this.history = new ArrayList<>();
    }

    public void addEntry(int position, long timestamp) {
        history.add(new RankingHistoryEntry(position, timestamp));
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getRankingType() {
        return rankingType;
    }

    public List<RankingHistoryEntry> getHistory() {
        return history;
    }

    public RankingHistoryEntry getLatestEntry() {
        if (history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }

    public static class RankingHistoryEntry {
        private int position;
        private long timestamp;

        public RankingHistoryEntry(int position, long timestamp) {
            this.position = position;
            this.timestamp = timestamp;
        }

        public int getPosition() {
            return position;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}

