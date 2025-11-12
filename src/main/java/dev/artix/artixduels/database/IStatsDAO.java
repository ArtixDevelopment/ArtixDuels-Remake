package dev.artix.artixduels.database;

import dev.artix.artixduels.models.PlayerStats;

import java.util.List;
import java.util.UUID;

public interface IStatsDAO {
    PlayerStats getPlayerStats(UUID playerId);
    void savePlayerStats(PlayerStats stats);
    void createPlayerStats(PlayerStats stats);
    List<PlayerStats> getAllPlayerStats();
}

