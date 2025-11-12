package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Achievement;
import dev.artix.artixduels.models.AchievementProgress;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de conquistas.
 */
public class AchievementManager {
    private final ArtixDuels plugin;
    private final StatsManager statsManager;
    private final RewardManager rewardManager;
    private final NotificationManager notificationManager;
    private Map<String, Achievement> achievements;
    private Map<UUID, Map<String, AchievementProgress>> playerProgress;
    private Map<String, List<Achievement>> achievementsByCategory;

    public AchievementManager(ArtixDuels plugin, StatsManager statsManager, 
                             RewardManager rewardManager, NotificationManager notificationManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.rewardManager = rewardManager;
        this.notificationManager = notificationManager;
        this.achievements = new HashMap<>();
        this.playerProgress = new HashMap<>();
        this.achievementsByCategory = new HashMap<>();
        
        loadAchievements();
        loadPlayerProgress();
    }

    /**
     * Carrega conquistas do arquivo de configuração.
     */
    private void loadAchievements() {
        File achievementsFile = new File(plugin.getDataFolder(), "achievements.yml");
        if (!achievementsFile.exists()) {
            createDefaultAchievements(achievementsFile);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(achievementsFile);
        if (config.contains("achievements")) {
            for (String achievementId : config.getConfigurationSection("achievements").getKeys(false)) {
                String path = "achievements." + achievementId;
                String name = config.getString(path + ".name");
                String description = config.getString(path + ".description");
                String category = config.getString(path + ".category", "Geral");
                String rarityStr = config.getString(path + ".rarity", "COMMON");
                Achievement.AchievementRarity rarity = Achievement.AchievementRarity.valueOf(rarityStr.toUpperCase());
                int targetValue = config.getInt(path + ".target-value", 1);
                String targetType = config.getString(path + ".target-type", "wins");
                List<String> rewards = config.getStringList(path + ".rewards");
                String icon = config.getString(path + ".icon", "STAR");

                Achievement achievement = new Achievement(achievementId, name, description, category, rarity, targetValue, targetType);
                achievement.setRewards(rewards);
                achievement.setIcon(icon);
                achievements.put(achievementId, achievement);

                achievementsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(achievement);
            }
        }
    }

    /**
     * Cria conquistas padrão.
     */
    private void createDefaultAchievements(File file) {
        try {
            file.createNewFile();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Conquistas de Vitórias
            config.set("achievements.first_win.name", "Primeira Vitória");
            config.set("achievements.first_win.description", "Ganhe seu primeiro duelo");
            config.set("achievements.first_win.category", "Vitórias");
            config.set("achievements.first_win.rarity", "COMMON");
            config.set("achievements.first_win.target-value", 1);
            config.set("achievements.first_win.target-type", "wins");

            config.set("achievements.win_10.name", "Vencedor");
            config.set("achievements.win_10.description", "Ganhe 10 duelos");
            config.set("achievements.win_10.category", "Vitórias");
            config.set("achievements.win_10.rarity", "COMMON");
            config.set("achievements.win_10.target-value", 10);
            config.set("achievements.win_10.target-type", "wins");

            config.set("achievements.win_100.name", "Mestre dos Duelos");
            config.set("achievements.win_100.description", "Ganhe 100 duelos");
            config.set("achievements.win_100.category", "Vitórias");
            config.set("achievements.win_100.rarity", "RARE");
            config.set("achievements.win_100.target-value", 100);
            config.set("achievements.win_100.target-type", "wins");

            // Conquistas de ELO
            config.set("achievements.elo_1500.name", "Competitivo");
            config.set("achievements.elo_1500.description", "Alcance 1500 de ELO");
            config.set("achievements.elo_1500.category", "ELO");
            config.set("achievements.elo_1500.rarity", "RARE");
            config.set("achievements.elo_1500.target-value", 1500);
            config.set("achievements.elo_1500.target-type", "elo");

            config.set("achievements.elo_2000.name", "Elite");
            config.set("achievements.elo_2000.description", "Alcance 2000 de ELO");
            config.set("achievements.elo_2000.category", "ELO");
            config.set("achievements.elo_2000.rarity", "EPIC");
            config.set("achievements.elo_2000.target-value", 2000);
            config.set("achievements.elo_2000.target-type", "elo");

            // Conquistas de Streak
            config.set("achievements.streak_5.name", "Em Chamas");
            config.set("achievements.streak_5.description", "Alcance uma sequência de 5 vitórias");
            config.set("achievements.streak_5.category", "Sequências");
            config.set("achievements.streak_5.rarity", "COMMON");
            config.set("achievements.streak_5.target-value", 5);
            config.set("achievements.streak_5.target-type", "streak");

            config.set("achievements.streak_10.name", "Invencível");
            config.set("achievements.streak_10.description", "Alcance uma sequência de 10 vitórias");
            config.set("achievements.streak_10.category", "Sequências");
            config.set("achievements.streak_10.rarity", "RARE");
            config.set("achievements.streak_10.target-value", 10);
            config.set("achievements.streak_10.target-type", "streak");

            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao criar arquivo de conquistas: " + e.getMessage());
        }
    }

    /**
     * Atualiza progresso de conquistas de um jogador.
     */
    public void updateProgress(UUID playerId, String targetType, int amount) {
        PlayerStats stats = statsManager.getPlayerStats(playerId, null);
        if (stats == null) return;

        Map<String, AchievementProgress> progress = playerProgress.computeIfAbsent(playerId, k -> new HashMap<>());

        for (Achievement achievement : achievements.values()) {
            if (!achievement.getTargetType().equals(targetType)) continue;

            AchievementProgress achievementProgress = progress.computeIfAbsent(achievement.getId(), 
                k -> new AchievementProgress(playerId, achievement.getId()));

            if (achievementProgress.isUnlocked()) continue;

            int currentValue = getCurrentValue(stats, targetType);
            achievementProgress.setProgress(currentValue);

            if (currentValue >= achievement.getTargetValue() && !achievementProgress.isUnlocked()) {
                unlockAchievement(playerId, achievement, achievementProgress);
            }
        }
    }

    /**
     * Obtém valor atual baseado no tipo.
     */
    private int getCurrentValue(PlayerStats stats, String targetType) {
        switch (targetType.toLowerCase()) {
            case "wins":
                return stats.getWins();
            case "losses":
                return stats.getLosses();
            case "elo":
                return stats.getElo();
            case "streak":
                return stats.getBestWinStreak();
            case "xp":
                return stats.getXp();
            case "level":
                return stats.getLevel();
            default:
                return 0;
        }
    }

    /**
     * Desbloqueia uma conquista.
     */
    private void unlockAchievement(UUID playerId, Achievement achievement, AchievementProgress progress) {
        progress.setUnlocked(true);
        progress.setUnlockedAt(System.currentTimeMillis());

        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            // Notificação
            notificationManager.sendAchievementNotification(player, achievement);

            // Recompensas
            if (achievement.getRewards() != null) {
                for (String reward : achievement.getRewards()) {
                    try {
                        if (reward.startsWith("money:")) {
                            double amount = Double.parseDouble(reward.substring(6));
                            rewardManager.giveMoney(player, amount);
                        } else if (reward.startsWith("command:")) {
                            String command = reward.substring(8).replace("%player%", player.getName());
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                        } else if (reward.startsWith("xp:")) {
                            int xp = Integer.parseInt(reward.substring(3));
                            PlayerStats stats = statsManager.getPlayerStats(player);
                            if (stats != null) {
                                stats.addXp(xp);
                                statsManager.savePlayerStats(stats);
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erro ao dar recompensa: " + reward);
                    }
                }
            }
        }

        savePlayerProgress(playerId);
    }

    /**
     * Obtém progresso de conquistas de um jogador.
     */
    public Map<String, AchievementProgress> getPlayerProgress(UUID playerId) {
        return playerProgress.computeIfAbsent(playerId, k -> new HashMap<>());
    }

    /**
     * Obtém todas as conquistas.
     */
    public Map<String, Achievement> getAchievements() {
        return achievements;
    }

    /**
     * Obtém conquistas por categoria.
     */
    public List<Achievement> getAchievementsByCategory(String category) {
        return achievementsByCategory.getOrDefault(category, new ArrayList<>());
    }

    /**
     * Obtém todas as categorias.
     */
    public Set<String> getCategories() {
        return achievementsByCategory.keySet();
    }

    /**
     * Carrega progresso dos jogadores.
     */
    private void loadPlayerProgress() {
        File progressFile = new File(plugin.getDataFolder(), "achievement_progress.yml");
        if (!progressFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(progressFile);
        if (config.contains("progress")) {
            for (String playerIdStr : config.getConfigurationSection("progress").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    Map<String, AchievementProgress> progress = new HashMap<>();

                    if (config.contains("progress." + playerIdStr)) {
                        for (String achievementId : config.getConfigurationSection("progress." + playerIdStr).getKeys(false)) {
                            String path = "progress." + playerIdStr + "." + achievementId;
                            int progressValue = config.getInt(path + ".progress", 0);
                            boolean unlocked = config.getBoolean(path + ".unlocked", false);
                            long unlockedAt = config.getLong(path + ".unlocked-at", 0);

                            AchievementProgress achievementProgress = new AchievementProgress(playerId, achievementId);
                            achievementProgress.setProgress(progressValue);
                            achievementProgress.setUnlocked(unlocked);
                            achievementProgress.setUnlockedAt(unlockedAt);
                            progress.put(achievementId, achievementProgress);
                        }
                    }

                    playerProgress.put(playerId, progress);
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }
    }

    /**
     * Salva progresso de um jogador.
     */
    private void savePlayerProgress(UUID playerId) {
        File progressFile = new File(plugin.getDataFolder(), "achievement_progress.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(progressFile);

        Map<String, AchievementProgress> progress = playerProgress.get(playerId);
        if (progress != null) {
            for (Map.Entry<String, AchievementProgress> entry : progress.entrySet()) {
                String path = "progress." + playerId.toString() + "." + entry.getKey();
                config.set(path + ".progress", entry.getValue().getProgress());
                config.set(path + ".unlocked", entry.getValue().isUnlocked());
                config.set(path + ".unlocked-at", entry.getValue().getUnlockedAt());
            }
        }

        try {
            config.save(progressFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar progresso de conquistas: " + e.getMessage());
        }
    }

    /**
     * Salva todo o progresso.
     */
    public void saveAllProgress() {
        for (UUID playerId : playerProgress.keySet()) {
            savePlayerProgress(playerId);
        }
    }
}

