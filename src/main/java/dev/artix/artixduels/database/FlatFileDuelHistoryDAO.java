package dev.artix.artixduels.database;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.DuelHistory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlatFileDuelHistoryDAO implements IDuelHistoryDAO {
    private ArtixDuels plugin;
    private File historyFile;

    public FlatFileDuelHistoryDAO(ArtixDuels plugin) {
        this.plugin = plugin;
        this.historyFile = new File(plugin.getDataFolder(), "duel_history.yml");
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao criar arquivo de histórico: " + e.getMessage());
            }
        }
    }

    @Override
    public void saveDuelHistory(DuelHistory history) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(historyFile);
        
        List<String> historyList = config.getStringList("history");
        if (historyList == null) {
            historyList = new ArrayList<>();
        }

        String historyEntry = String.format(
            "%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
            history.getPlayer1Id().toString(),
            history.getPlayer1Name(),
            history.getPlayer2Id().toString(),
            history.getPlayer2Name(),
            history.getWinnerId() != null ? history.getWinnerId().toString() : "null",
            history.getKitName(),
            history.getArenaName(),
            history.getTimestamp(),
            history.getDuration()
        );

        historyList.add(historyEntry);
        config.set("history", historyList);

        try {
            config.save(historyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar histórico de duelo: " + e.getMessage());
        }
    }

    @Override
    public List<DuelHistory> getPlayerHistory(UUID playerId, int limit) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(historyFile);
        List<String> historyList = config.getStringList("history");
        
        if (historyList == null || historyList.isEmpty()) {
            return new ArrayList<>();
        }

        List<DuelHistory> playerHistory = new ArrayList<>();
        int count = 0;

        for (int i = historyList.size() - 1; i >= 0 && count < limit; i--) {
            String entry = historyList.get(i);
            String[] parts = entry.split("\\|");
            
            if (parts.length < 9) continue;

            UUID player1Id = UUID.fromString(parts[0]);
            UUID player2Id = UUID.fromString(parts[2]);
            
            if (player1Id.equals(playerId) || player2Id.equals(playerId)) {
                DuelHistory history = new DuelHistory();
                history.setPlayer1Id(player1Id);
                history.setPlayer1Name(parts[1]);
                history.setPlayer2Id(player2Id);
                history.setPlayer2Name(parts[3]);
                history.setWinnerId(parts[4].equals("null") ? null : UUID.fromString(parts[4]));
                history.setKitName(parts[5]);
                history.setArenaName(parts[6]);
                history.setTimestamp(Long.parseLong(parts[7]));
                history.setDuration(Long.parseLong(parts[8]));
                
                playerHistory.add(history);
                count++;
            }
        }

        return playerHistory;
    }
}

