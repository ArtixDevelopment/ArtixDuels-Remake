package dev.artix.artixduels.managers;

import dev.artix.artixduels.database.StatsDAO;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {
    private StatsDAO statsDAO;
    private Map<UUID, PlayerStats> cachedStats;

    public StatsManager(StatsDAO statsDAO) {
        this.statsDAO = statsDAO;
        this.cachedStats = new HashMap<>();
    }

    public PlayerStats getPlayerStats(Player player) {
        return getPlayerStats(player.getUniqueId(), player.getName());
    }

    public PlayerStats getPlayerStats(UUID playerId, String playerName) {
        PlayerStats stats = cachedStats.get(playerId);
        if (stats == null) {
            stats = statsDAO.getPlayerStats(playerId);
            if (stats == null) {
                String name = playerName != null ? playerName : "Unknown";
                stats = new PlayerStats(playerId, name);
                statsDAO.createPlayerStats(stats);
            }
            cachedStats.put(playerId, stats);
        } else if (playerName != null && !playerName.equals(stats.getPlayerName())) {
            stats.setPlayerName(playerName);
        }
        return stats;
    }

    public void savePlayerStats(PlayerStats stats) {
        statsDAO.savePlayerStats(stats);
        cachedStats.put(stats.getPlayerId(), stats);
    }

    public void updatePlayerStats(UUID winnerId, UUID loserId) {
        updatePlayerStats(winnerId, loserId, null);
    }

    public void updatePlayerStats(UUID winnerId, UUID loserId, dev.artix.artixduels.models.DuelMode mode) {
        PlayerStats winnerStats = getPlayerStats(winnerId, null);
        PlayerStats loserStats = getPlayerStats(loserId, null);

        winnerStats.addWin();
        if (mode != null) {
            winnerStats.addModeWin(mode);
        }
        loserStats.addLoss();
        if (mode != null) {
            loserStats.addModeLoss(mode);
        }

        int winnerElo = calculateElo(winnerStats.getElo(), loserStats.getElo(), true);
        int loserElo = calculateElo(loserStats.getElo(), winnerStats.getElo(), false);

        winnerStats.setElo(winnerElo);
        loserStats.setElo(loserElo);

        savePlayerStats(winnerStats);
        savePlayerStats(loserStats);
    }

    public void updateDrawStats(UUID player1Id, UUID player2Id) {
        updateDrawStats(player1Id, player2Id, null);
    }

    public void updateDrawStats(UUID player1Id, UUID player2Id, dev.artix.artixduels.models.DuelMode mode) {
        PlayerStats stats1 = getPlayerStats(player1Id, null);
        PlayerStats stats2 = getPlayerStats(player2Id, null);

        stats1.addDraw();
        stats2.addDraw();
        // Por enquanto não salvamos draws por modo, mas podemos adicionar depois

        savePlayerStats(stats1);
        savePlayerStats(stats2);
    }

    private int calculateElo(int playerElo, int opponentElo, boolean won) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, (opponentElo - playerElo) / 400.0));
        int k = 32;
        int actualScore = won ? 1 : 0;
        return (int) (playerElo + k * (actualScore - expectedScore));
    }

    public void removeCachedStats(UUID playerId) {
        cachedStats.remove(playerId);
    }
}

