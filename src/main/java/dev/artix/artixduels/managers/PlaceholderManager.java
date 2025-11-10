package dev.artix.artixduels.managers;

import dev.artix.artixduels.models.DuelMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderManager {
    private DuelManager duelManager;
    private StatsManager statsManager;

    public PlaceholderManager(DuelManager duelManager, StatsManager statsManager) {
        this.duelManager = duelManager;
        this.statsManager = statsManager;
    }

    public String processPlaceholders(String text, Player player, DuelMode mode) {
        return processPlaceholders(text, player, null, mode);
    }

    public String processPlaceholders(String text, Player player, Player opponent, DuelMode mode) {
        Map<String, String> placeholders = new HashMap<>();
        
        // Placeholders gerais
        if (player != null) {
            placeholders.put("player", player.getName());
            placeholders.put("ping", String.valueOf(getPing(player)));
        }
        
        if (opponent != null) {
            placeholders.put("opponent", opponent.getName());
            placeholders.put("opponentping", String.valueOf(getPing(opponent)));
        }
        
        // Placeholders por modo
        if (mode != null) {
            placeholders.put("mode", mode.getDisplayName());
            placeholders.put("mode-name", mode.getName());
            placeholders.put("players-in-duel-mode", String.valueOf(duelManager.getActiveDuelsCountByMode(mode) * 2));
            placeholders.put("players-in-queue-mode", String.valueOf(duelManager.getMatchmakingQueueSizeByMode(mode)));
            placeholders.put("active-duels-mode", String.valueOf(duelManager.getActiveDuelsCountByMode(mode)));
            placeholders.put("total-players-mode", String.valueOf(
                duelManager.getActiveDuelsCountByMode(mode) * 2 + duelManager.getMatchmakingQueueSizeByMode(mode)));
        }
        
        // Placeholders gerais de duelo
        placeholders.put("players-in-duel", String.valueOf(duelManager.getActiveDuelsCount() * 2));
        placeholders.put("players-in-queue", String.valueOf(duelManager.getMatchmakingQueueSize()));
        placeholders.put("active-duels", String.valueOf(duelManager.getActiveDuelsCount()));
        placeholders.put("total-players", String.valueOf(
            duelManager.getActiveDuelsCount() * 2 + duelManager.getMatchmakingQueueSize()));
        placeholders.put("online-players", String.valueOf(
            org.bukkit.Bukkit.getOnlinePlayers().size()));
        
        // Placeholders de estatísticas
        if (player != null) {
            dev.artix.artixduels.models.PlayerStats stats = statsManager.getPlayerStats(player);
            if (stats != null) {
                placeholders.put("elo", String.valueOf(stats.getElo()));
                placeholders.put("wins", String.valueOf(stats.getWins()));
                placeholders.put("losses", String.valueOf(stats.getLosses()));
                placeholders.put("draws", String.valueOf(stats.getDraws()));
                placeholders.put("winrate", String.format("%.2f", stats.getWinRate()));
                placeholders.put("winstreak", String.valueOf(stats.getWinStreak()));
                placeholders.put("beststreak", String.valueOf(stats.getBestWinStreak()));
                
                // Placeholders por modo específico
                for (DuelMode duelMode : DuelMode.values()) {
                    String modeName = duelMode.getName().toLowerCase();
                    dev.artix.artixduels.models.PlayerStats.ModeStats modeStats = stats.getModeStats(duelMode);
                    
                    placeholders.put(modeName + "-wins", String.valueOf(modeStats.getWins()));
                    placeholders.put(modeName + "-losses", String.valueOf(modeStats.getLosses()));
                    placeholders.put(modeName + "-kills", String.valueOf(modeStats.getKills()));
                    placeholders.put(modeName + "-draws", String.valueOf(0)); // Por enquanto 0
                    
                    // Placeholders com nome completo do modo
                    String modeDisplayName = duelMode.getDisplayName().toLowerCase().replace(" ", "");
                    placeholders.put(modeDisplayName + "-wins", String.valueOf(modeStats.getWins()));
                    placeholders.put(modeDisplayName + "-losses", String.valueOf(modeStats.getLosses()));
                    placeholders.put(modeDisplayName + "-kills", String.valueOf(modeStats.getKills()));
                }
            }
        }
        
        // Processar placeholders
        String processed = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processed = processed.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return processed;
    }

    private int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return 0;
        }
    }
}

