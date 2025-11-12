package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Duel;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de métricas e análises.
 */
public class MetricsManager {
    private final ArtixDuels plugin;
    private final DuelManager duelManager;
    private final StatsManager statsManager;
    
    private List<Long> duelDurations;
    private Map<DuelMode, Integer> winsByMode;
    private Map<Integer, Integer> eloDistribution;
    private Map<Integer, Integer> activityByHour;
    private long totalDuels;
    private long totalPlayTime;

    public MetricsManager(ArtixDuels plugin, DuelManager duelManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.statsManager = statsManager;
        this.duelDurations = new ArrayList<>();
        this.winsByMode = new HashMap<>();
        this.eloDistribution = new HashMap<>();
        this.activityByHour = new HashMap<>();
        
        loadMetrics();
    }

    public void recordDuel(Duel duel, long duration) {
        totalDuels++;
        totalPlayTime += duration;
        duelDurations.add(duration);
        
        if (duelDurations.size() > 1000) {
            duelDurations.remove(0);
        }
        
        winsByMode.put(duel.getMode(), winsByMode.getOrDefault(duel.getMode(), 0) + 1);
        
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        activityByHour.put(hour, activityByHour.getOrDefault(hour, 0) + 1);
        
        saveMetrics();
    }

    public double getAverageDuelDuration() {
        if (duelDurations.isEmpty()) return 0.0;
        long sum = 0;
        for (Long duration : duelDurations) {
            sum += duration;
        }
        return (double) sum / duelDurations.size() / 1000.0; // em segundos
    }

    public double getWinRateByMode(DuelMode mode) {
        int wins = winsByMode.getOrDefault(mode, 0);
        long total = totalDuels;
        if (total == 0) return 0.0;
        return (double) wins / total * 100.0;
    }

    public Map<Integer, Integer> getEloDistribution() {
        return new HashMap<>(eloDistribution);
    }

    public int getPeakHour() {
        return activityByHour.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(0);
    }

    public long getTotalDuels() {
        return totalDuels;
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    private void loadMetrics() {
        File metricsFile = new File(plugin.getDataFolder(), "metrics.yml");
        if (!metricsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(metricsFile);
        totalDuels = config.getLong("total-duels", 0);
        totalPlayTime = config.getLong("total-play-time", 0);
    }

    private void saveMetrics() {
        File metricsFile = new File(plugin.getDataFolder(), "metrics.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(metricsFile);

        config.set("total-duels", totalDuels);
        config.set("total-play-time", totalPlayTime);
        config.set("average-duel-duration", getAverageDuelDuration());
        config.set("peak-hour", getPeakHour());

        try {
            config.save(metricsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar métricas: " + e.getMessage());
        }
    }
}

