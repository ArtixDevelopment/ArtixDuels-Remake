package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.database.IStatsDAO;
import dev.artix.artixduels.models.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gerenciador de rankings e leaderboards.
 * Fornece métodos para obter rankings por diferentes critérios.
 * Suporta rankings sazonais, por período, histórico e badges.
 */
public class RankingManager {
    private final ArtixDuels plugin;
    private final IStatsDAO statsDAO;
    private Map<String, List<RankingEntry>> cachedRankings;
    private Map<String, Map<RankingPeriod, List<RankingEntry>>> periodRankings;
    private Map<UUID, Map<String, RankingHistory>> playerHistory;
    private Season currentSeason;
    private long lastUpdate;
    private static final long CACHE_DURATION = 30000; // 30 segundos
    private static final long SEASON_DURATION = 2592000000L; // 30 dias em milissegundos
    private FileConfiguration seasonsConfig;
    private File seasonsFile;
    private FileConfiguration historyConfig;
    private File historyFile;

    public RankingManager(ArtixDuels plugin, IStatsDAO statsDAO) {
        this.plugin = plugin;
        this.statsDAO = statsDAO;
        this.cachedRankings = new HashMap<>();
        this.periodRankings = new HashMap<>();
        this.playerHistory = new HashMap<>();
        this.lastUpdate = 0;
        
        loadSeasonsConfig();
        loadHistoryConfig();
        initializeSeason();
        startSeasonResetTask();
    }

    /**
     * Obtém o ranking por ELO.
     * @param limit Limite de jogadores no ranking
     * @return Lista de entradas do ranking ordenadas por ELO
     */
    public List<RankingEntry> getEloRanking(int limit) {
        return getRanking("elo", limit);
    }

    /**
     * Obtém o ranking por vitórias.
     * @param limit Limite de jogadores no ranking
     * @return Lista de entradas do ranking ordenadas por vitórias
     */
    public List<RankingEntry> getWinsRanking(int limit) {
        return getRanking("wins", limit);
    }

    /**
     * Obtém o ranking por winrate.
     * @param limit Limite de jogadores no ranking
     * @return Lista de entradas do ranking ordenadas por winrate
     */
    public List<RankingEntry> getWinrateRanking(int limit) {
        return getRanking("winrate", limit);
    }

    /**
     * Obtém o ranking por win streak.
     * @param limit Limite de jogadores no ranking
     * @return Lista de entradas do ranking ordenadas por win streak
     */
    public List<RankingEntry> getStreakRanking(int limit) {
        return getRanking("streak", limit);
    }

    /**
     * Obtém o ranking por modo específico (kills).
     * @param mode Modo de duelo
     * @param limit Limite de jogadores no ranking
     * @return Lista de entradas do ranking ordenadas por kills no modo
     */
    public List<RankingEntry> getModeKillsRanking(DuelMode mode, int limit) {
        String key = "mode_kills_" + mode.getName();
        return getRanking(key, limit, mode);
    }

    /**
     * Obtém o ranking por modo específico (wins).
     * @param mode Modo de duelo
     * @param limit Limite de jogadores no ranking
     * @return Lista de entradas do ranking ordenadas por wins no modo
     */
    public List<RankingEntry> getModeWinsRanking(DuelMode mode, int limit) {
        String key = "mode_wins_" + mode.getName();
        return getRanking(key, limit, mode);
    }

    /**
     * Obtém o ranking por XP/Level.
     * @param limit Limite de jogadores no ranking
     * @return Lista de entradas do ranking ordenadas por XP
     */
    public List<RankingEntry> getXpRanking(int limit) {
        return getRanking("xp", limit);
    }

    /**
     * Obtém a posição de um jogador no ranking.
     * @param playerId UUID do jogador
     * @param type Tipo de ranking
     * @return Posição no ranking (1-based) ou -1 se não encontrado
     */
    public int getPlayerRank(UUID playerId, String type) {
        List<RankingEntry> ranking = getRanking(type, 1000);
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getPlayerId().equals(playerId)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Obtém um ranking genérico.
     */
    private List<RankingEntry> getRanking(String type, int limit) {
        return getRanking(type, limit, null);
    }

    private List<RankingEntry> getRanking(String type, int limit, DuelMode mode) {
        // Verificar cache
        long currentTime = System.currentTimeMillis();
        if (cachedRankings.containsKey(type) && (currentTime - lastUpdate) < CACHE_DURATION) {
            List<RankingEntry> cached = cachedRankings.get(type);
            return cached.stream().limit(limit).collect(Collectors.toList());
        }

        // Buscar todos os stats do banco de dados
        List<PlayerStats> allStats = statsDAO.getAllPlayerStats();
        List<RankingEntry> entries = new ArrayList<>();

        for (PlayerStats stats : allStats) {
            if (stats == null) continue;

            double value = 0;
            String playerName = stats.getPlayerName();
            
            // Obter nome atualizado se o jogador estiver online
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(stats.getPlayerId());
            if (offlinePlayer != null && offlinePlayer.getName() != null) {
                playerName = offlinePlayer.getName();
            }

            switch (type) {
                case "elo":
                    value = stats.getElo();
                    break;
                case "wins":
                    value = stats.getWins();
                    break;
                case "winrate":
                    value = stats.getWinRate();
                    break;
                case "streak":
                    value = stats.getBestWinStreak();
                    break;
                case "xp":
                    value = stats.getXp();
                    break;
                default:
                    if (type.startsWith("mode_kills_") && mode != null) {
                        value = stats.getModeStats(mode).getKills();
                    } else if (type.startsWith("mode_wins_") && mode != null) {
                        value = stats.getModeStats(mode).getWins();
                    }
                    break;
            }

            entries.add(new RankingEntry(stats.getPlayerId(), playerName, value, stats));
        }

        // Ordenar por valor (decrescente)
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Atualizar cache
        cachedRankings.put(type, entries);
        lastUpdate = currentTime;

        return entries.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Obtém ranking por período.
     */
    public List<RankingEntry> getRankingByPeriod(String type, RankingPeriod period, int limit) {
        if (!periodRankings.containsKey(type)) {
            periodRankings.put(type, new HashMap<>());
        }
        
        Map<RankingPeriod, List<RankingEntry>> typeRankings = periodRankings.get(type);
        if (!typeRankings.containsKey(period)) {
            typeRankings.put(period, new ArrayList<>());
        }
        
        List<RankingEntry> ranking = typeRankings.get(period);
        if (ranking.isEmpty() || (System.currentTimeMillis() - lastUpdate) > CACHE_DURATION) {
            ranking = calculatePeriodRanking(type, period, limit);
            typeRankings.put(period, ranking);
        }
        
        return ranking.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Calcula ranking para um período específico.
     */
    private List<RankingEntry> calculatePeriodRanking(String type, RankingPeriod period, int limit) {
        long periodStart = getPeriodStart(period);
        List<PlayerStats> allStats = statsDAO.getAllPlayerStats();
        List<RankingEntry> entries = new ArrayList<>();

        for (PlayerStats stats : allStats) {
            if (stats == null) continue;
            
            double value = getStatsValueForPeriod(stats, type, periodStart);
            if (value <= 0) continue;

            String playerName = stats.getPlayerName();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(stats.getPlayerId());
            if (offlinePlayer != null && offlinePlayer.getName() != null) {
                playerName = offlinePlayer.getName();
            }

            entries.add(new RankingEntry(stats.getPlayerId(), playerName, value, stats));
        }

        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return entries;
    }

    /**
     * Obtém o início do período.
     */
    private long getPeriodStart(RankingPeriod period) {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);

        switch (period) {
            case DAILY:
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            case WEEKLY:
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            case MONTHLY:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            case SEASONAL:
                return currentSeason != null ? currentSeason.getStartTime() : now;
            default:
                return 0;
        }
    }

    /**
     * Obtém valor de stats para um período.
     */
    private double getStatsValueForPeriod(PlayerStats stats, String type, long periodStart) {
        switch (type) {
            case "elo":
                return stats.getElo();
            case "wins":
                return stats.getWins();
            case "winrate":
                return stats.getWinRate();
            case "streak":
                return stats.getBestWinStreak();
            case "xp":
                return stats.getXp();
            default:
                return 0;
        }
    }

    /**
     * Obtém badge de ranking para uma posição.
     */
    public RankingBadge getBadgeForPosition(int position) {
        return RankingBadge.getBadgeForPosition(position);
    }

    /**
     * Registra posição no histórico.
     */
    public void recordRankingPosition(UUID playerId, String rankingType, int position) {
        if (!playerHistory.containsKey(playerId)) {
            playerHistory.put(playerId, new HashMap<>());
        }

        Map<String, RankingHistory> playerHistories = playerHistory.get(playerId);
        if (!playerHistories.containsKey(rankingType)) {
            playerHistories.put(rankingType, new RankingHistory(playerId, rankingType));
        }

        RankingHistory history = playerHistories.get(rankingType);
        history.addEntry(position, System.currentTimeMillis());
        
        saveHistoryConfig();
    }

    /**
     * Obtém histórico de ranking de um jogador.
     */
    public RankingHistory getPlayerRankingHistory(UUID playerId, String rankingType) {
        if (!playerHistory.containsKey(playerId)) {
            return null;
        }
        return playerHistory.get(playerId).get(rankingType);
    }

    /**
     * Obtém a temporada atual.
     */
    public Season getCurrentSeason() {
        return currentSeason;
    }

    /**
     * Inicializa ou carrega temporada.
     */
    private void initializeSeason() {
        if (seasonsConfig.contains("current_season")) {
            ConfigurationSection seasonSection = seasonsConfig.getConfigurationSection("current_season");
            if (seasonSection != null) {
                int seasonNumber = seasonSection.getInt("number", 1);
                long startTime = seasonSection.getLong("start_time", System.currentTimeMillis());
                long endTime = seasonSection.getLong("end_time", startTime + SEASON_DURATION);
                currentSeason = new Season(seasonNumber, startTime, endTime);
                
                if (seasonSection.contains("top_player_id")) {
                    currentSeason.setTopPlayerId(UUID.fromString(seasonSection.getString("top_player_id")));
                    currentSeason.setTopPlayerName(seasonSection.getString("top_player_name", ""));
                }
            }
        } else {
            currentSeason = new Season(1, System.currentTimeMillis(), System.currentTimeMillis() + SEASON_DURATION);
            saveSeasonsConfig();
        }
    }

    /**
     * Reseta temporada e distribui recompensas.
     */
    public void resetSeason() {
        if (currentSeason == null) {
            return;
        }

        distributeSeasonRewards();
        
        int nextSeasonNumber = currentSeason.getSeasonNumber() + 1;
        currentSeason = new Season(nextSeasonNumber, System.currentTimeMillis(), 
                                   System.currentTimeMillis() + SEASON_DURATION);
        
        saveSeasonsConfig();
        clearCache();
        
        plugin.getLogger().info("Temporada " + (nextSeasonNumber - 1) + " finalizada! Nova temporada " + 
                              nextSeasonNumber + " iniciada.");
    }

    /**
     * Distribui recompensas da temporada.
     */
    private void distributeSeasonRewards() {
        List<RankingEntry> topPlayers = getEloRanking(100);
        
        for (int i = 0; i < Math.min(topPlayers.size(), 100); i++) {
            RankingEntry entry = topPlayers.get(i);
            Player player = Bukkit.getPlayer(entry.getPlayerId());
            
            if (player != null && player.isOnline()) {
                RankingBadge badge = getBadgeForPosition(i + 1);
                giveRankingReward(player, i + 1, badge);
            }
        }
    }

    /**
     * Dá recompensa por posição no ranking.
     */
    public void giveRankingReward(Player player, int position, RankingBadge badge) {
        dev.artix.artixduels.managers.RewardManager rewardManager = plugin.getRewardManager();
        if (rewardManager == null) return;

        if (position == 1) {
            rewardManager.giveMoney(player, 10000.0);
            player.sendMessage("§6§l[RECOMPENSA] §eVocê recebeu §a$10.000 §epor estar em #1 no ranking!");
        } else if (position <= 10) {
            rewardManager.giveMoney(player, 5000.0);
            player.sendMessage("§e§l[RECOMPENSA] §eVocê recebeu §a$5.000 §epor estar no Top 10!");
        } else if (position <= 100) {
            rewardManager.giveMoney(player, 1000.0);
            player.sendMessage("§b§l[RECOMPENSA] §eVocê recebeu §a$1.000 §epor estar no Top 100!");
        }
    }

    /**
     * Carrega configuração de temporadas.
     */
    private void loadSeasonsConfig() {
        seasonsFile = new File(plugin.getDataFolder(), "seasons.yml");
        if (!seasonsFile.exists()) {
            try {
                seasonsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao criar seasons.yml: " + e.getMessage());
            }
        }
        seasonsConfig = YamlConfiguration.loadConfiguration(seasonsFile);
    }

    /**
     * Salva configuração de temporadas.
     */
    private void saveSeasonsConfig() {
        if (currentSeason != null) {
            ConfigurationSection seasonSection = seasonsConfig.createSection("current_season");
            seasonSection.set("number", currentSeason.getSeasonNumber());
            seasonSection.set("start_time", currentSeason.getStartTime());
            seasonSection.set("end_time", currentSeason.getEndTime());
            if (currentSeason.getTopPlayerId() != null) {
                seasonSection.set("top_player_id", currentSeason.getTopPlayerId().toString());
                seasonSection.set("top_player_name", currentSeason.getTopPlayerName());
            }
        }
        
        try {
            seasonsConfig.save(seasonsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar seasons.yml: " + e.getMessage());
        }
    }

    /**
     * Carrega histórico de rankings.
     */
    private void loadHistoryConfig() {
        historyFile = new File(plugin.getDataFolder(), "ranking_history.yml");
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao criar ranking_history.yml: " + e.getMessage());
            }
        }
        historyConfig = YamlConfiguration.loadConfiguration(historyFile);
        
        if (historyConfig.contains("history")) {
            ConfigurationSection historySection = historyConfig.getConfigurationSection("history");
            if (historySection != null) {
                for (String playerIdStr : historySection.getKeys(false)) {
                    UUID playerId = UUID.fromString(playerIdStr);
                    ConfigurationSection playerSection = historySection.getConfigurationSection(playerIdStr);
                    if (playerSection != null) {
                        Map<String, RankingHistory> playerHistories = new HashMap<>();
                        for (String rankingType : playerSection.getKeys(false)) {
                            RankingHistory history = new RankingHistory(playerId, rankingType);
                            List<String> entries = playerSection.getStringList(rankingType);
                            for (String entryStr : entries) {
                                String[] parts = entryStr.split(":");
                                if (parts.length == 2) {
                                    int position = Integer.parseInt(parts[0]);
                                    long timestamp = Long.parseLong(parts[1]);
                                    history.addEntry(position, timestamp);
                                }
                            }
                            playerHistories.put(rankingType, history);
                        }
                        playerHistory.put(playerId, playerHistories);
                    }
                }
            }
        }
    }

    /**
     * Salva histórico de rankings.
     */
    private void saveHistoryConfig() {
        ConfigurationSection historySection = historyConfig.createSection("history");
        
        for (Map.Entry<UUID, Map<String, RankingHistory>> playerEntry : playerHistory.entrySet()) {
            ConfigurationSection playerSection = historySection.createSection(playerEntry.getKey().toString());
            
            for (Map.Entry<String, RankingHistory> historyEntry : playerEntry.getValue().entrySet()) {
                List<String> entries = new ArrayList<>();
                for (RankingHistory.RankingHistoryEntry entry : historyEntry.getValue().getHistory()) {
                    entries.add(entry.getPosition() + ":" + entry.getTimestamp());
                }
                playerSection.set(historyEntry.getKey(), entries);
            }
        }
        
        try {
            historyConfig.save(historyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar ranking_history.yml: " + e.getMessage());
        }
    }

    /**
     * Inicia tarefa de reset de temporada.
     */
    private void startSeasonResetTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentSeason != null && currentSeason.isExpired()) {
                resetSeason();
            }
        }, 0L, 72000L); // Verifica a cada hora
    }

    /**
     * Limpa o cache de rankings.
     */
    public void clearCache() {
        cachedRankings.clear();
        periodRankings.clear();
        lastUpdate = 0;
    }

    /**
     * Classe interna para representar uma entrada no ranking.
     */
    public static class RankingEntry {
        private final UUID playerId;
        private final String playerName;
        private final double value;
        private final PlayerStats stats;

        public RankingEntry(UUID playerId, String playerName, double value, PlayerStats stats) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.value = value;
            this.stats = stats;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public String getPlayerName() {
            return playerName;
        }

        public double getValue() {
            return value;
        }

        public PlayerStats getStats() {
            return stats;
        }
    }
}

