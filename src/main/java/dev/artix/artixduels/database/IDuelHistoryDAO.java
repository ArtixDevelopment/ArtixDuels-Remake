package dev.artix.artixduels.database;

import dev.artix.artixduels.models.DuelHistory;

import java.util.List;
import java.util.UUID;

public interface IDuelHistoryDAO {
    void saveDuelHistory(DuelHistory history);
    List<DuelHistory> getPlayerHistory(UUID playerId, int limit);
}

