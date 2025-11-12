package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Challenge;
import dev.artix.artixduels.models.ChallengeProgress;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gerenciador de desafios diários e semanais.
 */
public class ChallengeManager {
    private final ArtixDuels plugin;
    private final StatsManager statsManager;
    private final RewardManager rewardManager;
    private FileConfiguration challengesConfig;
    private File challengesFile;
    private Map<String, Challenge> challenges;
    private Map<UUID, Map<String, ChallengeProgress>> playerProgress;
    private long lastDailyReset;
    private long lastWeeklyReset;

    public ChallengeManager(ArtixDuels plugin, StatsManager statsManager, RewardManager rewardManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.rewardManager = rewardManager;
        this.challenges = new HashMap<>();
        this.playerProgress = new HashMap<>();
        this.lastDailyReset = System.currentTimeMillis();
        this.lastWeeklyReset = System.currentTimeMillis();
        
        loadChallengesConfig();
        loadChallenges();
        startResetTask();
    }

    private void loadChallengesConfig() {
        challengesFile = new File(plugin.getDataFolder(), "challenges.yml");
        if (!challengesFile.exists()) {
            plugin.saveResource("challenges.yml", false);
        }
        challengesConfig = YamlConfiguration.loadConfiguration(challengesFile);
    }

    private void loadChallenges() {
        challenges.clear();
        
        if (challengesConfig.contains("challenges")) {
            ConfigurationSection challengesSection = challengesConfig.getConfigurationSection("challenges");
            if (challengesSection != null) {
                for (String key : challengesSection.getKeys(false)) {
                    ConfigurationSection challengeSection = challengesSection.getConfigurationSection(key);
                    if (challengeSection != null) {
                        Challenge challenge = Challenge.fromConfig(challengeSection);
                        challenges.put(challenge.getId(), challenge);
                    }
                }
            }
        }
        
        plugin.getLogger().info("Carregados " + challenges.size() + " desafios.");
    }

    public void reload() {
        loadChallengesConfig();
        loadChallenges();
    }

    /**
     * Obtém todos os desafios de um tipo específico.
     */
    public List<Challenge> getChallenges(Challenge.ChallengeType type) {
        return challenges.values().stream()
                .filter(c -> c.getType() == type)
                .sorted(Comparator.comparingInt(Challenge::getPriority).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtém o progresso de um jogador em um desafio.
     */
    public ChallengeProgress getProgress(UUID playerId, String challengeId) {
        Map<String, ChallengeProgress> progressMap = playerProgress.computeIfAbsent(playerId, k -> new HashMap<>());
        return progressMap.computeIfAbsent(challengeId, k -> new ChallengeProgress(playerId, challengeId));
    }

    /**
     * Atualiza o progresso de um jogador em um desafio.
     */
    public void updateProgress(UUID playerId, Challenge.ChallengeObjective objective, int amount, DuelMode mode) {
        PlayerStats stats = statsManager.getPlayerStats(playerId, null);
        if (stats == null) return;

        for (Challenge challenge : challenges.values()) {
            if (challenge.getObjective() != objective) continue;
            if (challenge.getObjective() == Challenge.ChallengeObjective.WIN_DUELS_MODE && 
                challenge.getMetadata("mode") != null) {
                String modeName = (String) challenge.getMetadata("mode");
                if (!modeName.equals(mode.getName())) continue;
            }
            if (challenge.getObjective() == Challenge.ChallengeObjective.GET_KILLS_MODE && 
                challenge.getMetadata("mode") != null) {
                String modeName = (String) challenge.getMetadata("mode");
                if (!modeName.equals(mode.getName())) continue;
            }

            ChallengeProgress progress = getProgress(playerId, challenge.getId());
            if (progress.isCompleted()) continue;

            int oldProgress = progress.getProgress();
            progress.addProgress(amount);

            if (progress.getProgress() >= challenge.getTarget() && !progress.isCompleted()) {
                progress.setCompleted(true);
                completeChallenge(Bukkit.getPlayer(playerId), challenge, progress);
            }
        }
    }

    /**
     * Completa um desafio e dá as recompensas.
     */
    private void completeChallenge(Player player, Challenge challenge, ChallengeProgress progress) {
        if (player == null || !player.isOnline()) return;

        progress.setCompleted(true);
        giveRewards(player, challenge);

        player.sendMessage("§a§l[DESAFIO] §aVocê completou o desafio: §e" + challenge.getName() + "§a!");
        player.sendMessage("§7Recompensas recebidas!");

        plugin.getLogger().info("Jogador " + player.getName() + " completou o desafio " + challenge.getId());
    }

    /**
     * Dá as recompensas de um desafio.
     */
    private void giveRewards(Player player, Challenge challenge) {
        Map<String, Object> rewards = challenge.getRewards();
        
        if (rewards.containsKey("money")) {
            double money = ((Number) rewards.get("money")).doubleValue();
            rewardManager.giveMoney(player, money);
        }
        
        if (rewards.containsKey("xp")) {
            int xp = ((Number) rewards.get("xp")).intValue();
            PlayerStats stats = statsManager.getPlayerStats(player);
            if (stats != null) {
                stats.addXp(xp);
                statsManager.savePlayerStats(stats);
            }
        }
        
        if (rewards.containsKey("items")) {
            // Implementar distribuição de itens se necessário
        }
    }

    /**
     * Obtém todos os desafios ativos de um jogador.
     */
    public List<Challenge> getActiveChallenges(UUID playerId, Challenge.ChallengeType type) {
        List<Challenge> allChallenges = getChallenges(type);
        List<Challenge> activeChallenges = new ArrayList<>();
        
        for (Challenge challenge : allChallenges) {
            ChallengeProgress progress = getProgress(playerId, challenge.getId());
            if (!progress.isCompleted()) {
                activeChallenges.add(challenge);
            }
        }
        
        return activeChallenges;
    }

    /**
     * Reseta desafios diários.
     */
    public void resetDailyChallenges() {
        for (UUID playerId : new HashSet<>(playerProgress.keySet())) {
            Map<String, ChallengeProgress> progressMap = playerProgress.get(playerId);
            if (progressMap == null) continue;
            
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, ChallengeProgress> entry : progressMap.entrySet()) {
                Challenge challenge = challenges.get(entry.getKey());
                if (challenge != null && challenge.getResetType() == Challenge.ChallengeResetType.DAILY) {
                    toRemove.add(entry.getKey());
                }
            }
            
            for (String challengeId : toRemove) {
                progressMap.remove(challengeId);
            }
        }
        
        lastDailyReset = System.currentTimeMillis();
        plugin.getLogger().info("Desafios diários resetados.");
    }

    /**
     * Reseta desafios semanais.
     */
    public void resetWeeklyChallenges() {
        for (UUID playerId : new HashSet<>(playerProgress.keySet())) {
            Map<String, ChallengeProgress> progressMap = playerProgress.get(playerId);
            if (progressMap == null) continue;
            
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, ChallengeProgress> entry : progressMap.entrySet()) {
                Challenge challenge = challenges.get(entry.getKey());
                if (challenge != null && challenge.getResetType() == Challenge.ChallengeResetType.WEEKLY) {
                    toRemove.add(entry.getKey());
                }
            }
            
            for (String challengeId : toRemove) {
                progressMap.remove(challengeId);
            }
        }
        
        lastWeeklyReset = System.currentTimeMillis();
        plugin.getLogger().info("Desafios semanais resetados.");
    }

    /**
     * Inicia a task de reset automático.
     */
    private void startResetTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
            // Reset diário (a cada 24 horas)
            if (currentTime - lastDailyReset >= 86400000L) {
                resetDailyChallenges();
            }
            
            // Reset semanal (a cada 7 dias)
            if (currentTime - lastWeeklyReset >= 604800000L) {
                resetWeeklyChallenges();
            }
        }, 0L, 3600000L); // Verifica a cada hora
    }

    /**
     * Salva o progresso de um jogador.
     */
    public void saveProgress(UUID playerId) {
        // Implementar salvamento em arquivo ou banco de dados se necessário
    }

    /**
     * Carrega o progresso de um jogador.
     */
    public void loadProgress(UUID playerId) {
        // Implementar carregamento de arquivo ou banco de dados se necessário
    }
}

