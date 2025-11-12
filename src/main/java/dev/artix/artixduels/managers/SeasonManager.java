package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.PlayerStats;
import dev.artix.artixduels.models.Season;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de temporadas (seasons).
 */
public class SeasonManager {
    private final ArtixDuels plugin;
    private final StatsManager statsManager;
    private final RewardManager rewardManager;
    private Map<String, Season> seasons;
    private Season currentSeason;
    private long seasonDuration;

    public SeasonManager(ArtixDuels plugin, StatsManager statsManager, RewardManager rewardManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.rewardManager = rewardManager;
        this.seasons = new HashMap<>();
        this.seasonDuration = 30L * 24L * 60L * 60L * 1000L; // 30 dias em millis
        
        loadSeasons();
        checkCurrentSeason();
    }

    private void loadSeasons() {
        File seasonsFile = new File(plugin.getDataFolder(), "seasons.yml");
        if (!seasonsFile.exists()) {
            createDefaultSeason();
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(seasonsFile);
        if (config.contains("seasons")) {
            for (String seasonId : config.getConfigurationSection("seasons").getKeys(false)) {
                String path = "seasons." + seasonId;
                String name = config.getString(path + ".name");
                long startTime = config.getLong(path + ".start-time");
                long endTime = config.getLong(path + ".end-time");
                boolean active = config.getBoolean(path + ".active", false);

                Season season = new Season(seasonId, name, startTime, endTime);
                season.setActive(active);
                seasons.put(seasonId, season);
            }
        }
    }

    private void createDefaultSeason() {
        long now = System.currentTimeMillis();
        Season season = new Season("season_1", "Temporada 1", now, now + seasonDuration);
        season.setActive(true);
        seasons.put("season_1", season);
        currentSeason = season;
        saveSeasons();
    }

    private void checkCurrentSeason() {
        for (Season season : seasons.values()) {
            if (season.isActive() && !season.isExpired()) {
                currentSeason = season;
                return;
            }
        }
        
        // Criar nova temporada se não houver ativa
        createNewSeason();
    }

    private void createNewSeason() {
        String newId = "season_" + (seasons.size() + 1);
        long now = System.currentTimeMillis();
        Season season = new Season(newId, "Temporada " + (seasons.size() + 1), now, now + seasonDuration);
        season.setActive(true);
        
        // Desativar temporada anterior
        if (currentSeason != null) {
            currentSeason.setActive(false);
            distributeSeasonRewards(currentSeason);
        }
        
        seasons.put(newId, season);
        currentSeason = season;
        resetSeasonElo();
        saveSeasons();
    }

    private void resetSeasonElo() {
        // Reset ELO de todos os jogadores para a temporada
        // Implementação específica pode variar
    }

    private void distributeSeasonRewards(Season season) {
        // Distribuir recompensas baseadas no ranking da temporada
        // Implementação específica pode variar
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public void updateSeasonStats(UUID playerId, boolean won) {
        if (currentSeason == null) return;
        
        PlayerStats stats = statsManager.getPlayerStats(playerId, null);
        if (stats == null) return;

        Map<UUID, Integer> seasonWins = currentSeason.getSeasonWins();
        if (seasonWins == null) {
            seasonWins = new HashMap<>();
            currentSeason.setSeasonWins(seasonWins);
        }
        
        if (won) {
            seasonWins.put(playerId, seasonWins.getOrDefault(playerId, 0) + 1);
        }
        
        Map<UUID, Integer> seasonElo = currentSeason.getSeasonElo();
        if (seasonElo == null) {
            seasonElo = new HashMap<>();
            currentSeason.setSeasonElo(seasonElo);
        }
        seasonElo.put(playerId, stats.getElo());
        
        saveSeasons();
    }

    private void saveSeasons() {
        File seasonsFile = new File(plugin.getDataFolder(), "seasons.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(seasonsFile);

        for (Map.Entry<String, Season> entry : seasons.entrySet()) {
            String path = "seasons." + entry.getKey();
            Season season = entry.getValue();
            config.set(path + ".name", season.getName());
            config.set(path + ".start-time", season.getStartTime());
            config.set(path + ".end-time", season.getEndTime());
            config.set(path + ".active", season.isActive());
        }

        try {
            config.save(seasonsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar temporadas: " + e.getMessage());
        }
    }
}

