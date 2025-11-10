package dev.artix.artixduels.database;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlatFileStatsDAO implements IStatsDAO {
    private ArtixDuels plugin;
    private File statsFolder;

    public FlatFileStatsDAO(ArtixDuels plugin) {
        this.plugin = plugin;
        this.statsFolder = new File(plugin.getDataFolder(), "stats");
        if (!statsFolder.exists()) {
            statsFolder.mkdirs();
        }
    }

    @Override
    public PlayerStats getPlayerStats(UUID playerId) {
        File playerFile = new File(statsFolder, playerId.toString() + ".yml");
        if (!playerFile.exists()) {
            return null;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        PlayerStats stats = new PlayerStats();
        stats.setPlayerId(playerId);
        stats.setPlayerName(config.getString("playerName", "Unknown"));
        stats.setWins(config.getInt("wins", 0));
        stats.setLosses(config.getInt("losses", 0));
        stats.setDraws(config.getInt("draws", 0));
        stats.setElo(config.getInt("elo", 1000));
        stats.setWinStreak(config.getInt("winStreak", 0));
        stats.setBestWinStreak(config.getInt("bestWinStreak", 0));
        stats.setXp(config.getInt("xp", 0));
        stats.setLevel(config.getInt("level", 1));

        Map<DuelMode, PlayerStats.ModeStats> modeStatsMap = new HashMap<>();
        if (config.contains("modeStats")) {
            for (DuelMode mode : DuelMode.values()) {
                String modePath = "modeStats." + mode.getName();
                if (config.contains(modePath)) {
                    PlayerStats.ModeStats modeStat = new PlayerStats.ModeStats();
                    modeStat.setWins(config.getInt(modePath + ".wins", 0));
                    modeStat.setLosses(config.getInt(modePath + ".losses", 0));
                    modeStat.setKills(config.getInt(modePath + ".kills", 0));
                    modeStatsMap.put(mode, modeStat);
                } else {
                    modeStatsMap.put(mode, new PlayerStats.ModeStats());
                }
            }
        } else {
            for (DuelMode mode : DuelMode.values()) {
                modeStatsMap.put(mode, new PlayerStats.ModeStats());
            }
        }
        stats.setModeStats(modeStatsMap);

        return stats;
    }

    @Override
    public void savePlayerStats(PlayerStats stats) {
        File playerFile = new File(statsFolder, stats.getPlayerId().toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        config.set("playerId", stats.getPlayerId().toString());
        config.set("playerName", stats.getPlayerName());
        config.set("wins", stats.getWins());
        config.set("losses", stats.getLosses());
        config.set("draws", stats.getDraws());
        config.set("elo", stats.getElo());
        config.set("winStreak", stats.getWinStreak());
        config.set("bestWinStreak", stats.getBestWinStreak());
        config.set("xp", stats.getXp());
        config.set("level", stats.getLevel());

        if (stats.getModeStats() != null) {
            for (Map.Entry<DuelMode, PlayerStats.ModeStats> entry : stats.getModeStats().entrySet()) {
                String modePath = "modeStats." + entry.getKey().getName();
                config.set(modePath + ".wins", entry.getValue().getWins());
                config.set(modePath + ".losses", entry.getValue().getLosses());
                config.set(modePath + ".kills", entry.getValue().getKills());
            }
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar estatísticas do jogador " + stats.getPlayerId() + ": " + e.getMessage());
        }
    }

    @Override
    public void createPlayerStats(PlayerStats stats) {
        savePlayerStats(stats);
    }
}

