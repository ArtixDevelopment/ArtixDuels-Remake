package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.BattlePass;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de passe de batalha.
 */
public class BattlePassManager {
    private final ArtixDuels plugin;
    private final RewardManager rewardManager;
    private BattlePass currentBattlePass;
    private Map<UUID, BattlePass.BattlePassProgress> playerProgress;

    public BattlePassManager(ArtixDuels plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.playerProgress = new HashMap<>();
        
        loadBattlePass();
    }

    public void addBattlePassXP(UUID playerId, int xp) {
        if (currentBattlePass == null || !currentBattlePass.isActive()) return;
        
        BattlePass.BattlePassProgress progress = playerProgress.computeIfAbsent(playerId, 
            k -> new BattlePass.BattlePassProgress(playerId));
        
        int oldLevel = progress.getLevel();
        progress.addXp(xp);
        
        if (progress.getLevel() > oldLevel) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage("§6§l[PASSE DE BATALHA] §eVocê subiu para o nível " + progress.getLevel() + "!");
            }
        }
        
        saveBattlePass();
    }

    public void claimReward(Player player, int level) {
        if (currentBattlePass == null) return;
        
        BattlePass.BattlePassProgress progress = playerProgress.get(player.getUniqueId());
        if (progress == null || progress.getLevel() < level) {
            player.sendMessage("§cVocê ainda não alcançou este nível!");
            return;
        }
        
        if (progress.getClaimedRewards().getOrDefault(level, false)) {
            player.sendMessage("§cVocê já reivindicou esta recompensa!");
            return;
        }
        
        BattlePass.BattlePassReward reward = currentBattlePass.getFreeRewards().get(level);
        if (reward != null) {
            giveReward(player, reward);
        }
        
        if (progress.isPremium()) {
            BattlePass.BattlePassReward premiumReward = currentBattlePass.getPremiumRewards().get(level);
            if (premiumReward != null) {
                giveReward(player, premiumReward);
            }
        }
        
        progress.getClaimedRewards().put(level, true);
        saveBattlePass();
    }

    private void giveReward(Player player, BattlePass.BattlePassReward reward) {
        String type = reward.getType();
        Map<String, Object> data = reward.getData();
        
        if (type.equals("money")) {
            double amount = ((Number) data.get("amount")).doubleValue();
            rewardManager.giveMoney(player, amount);
        } else if (type.equals("item")) {
            // Implementar distribuição de itens
        }
    }

    public BattlePass getCurrentBattlePass() {
        return currentBattlePass;
    }

    public BattlePass.BattlePassProgress getPlayerProgress(UUID playerId) {
        return playerProgress.computeIfAbsent(playerId, k -> new BattlePass.BattlePassProgress(playerId));
    }

    private void loadBattlePass() {
        File battlePassFile = new File(plugin.getDataFolder(), "battlepass.yml");
        if (!battlePassFile.exists()) {
            createDefaultBattlePass();
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(battlePassFile);
        // Carregar battle pass do arquivo
    }

    private void createDefaultBattlePass() {
        long now = System.currentTimeMillis();
        long duration = 30L * 24L * 60L * 60L * 1000L; // 30 dias
        currentBattlePass = new BattlePass("bp_1", "Passe de Batalha 1", now, now + duration, 100);
    }

    private void saveBattlePass() {
        File battlePassFile = new File(plugin.getDataFolder(), "battlepass.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(battlePassFile);

        if (currentBattlePass != null) {
            config.set("current.id", currentBattlePass.getId());
            config.set("current.name", currentBattlePass.getName());
            config.set("current.start-time", currentBattlePass.getStartTime());
            config.set("current.end-time", currentBattlePass.getEndTime());
        }

        try {
            config.save(battlePassFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar battle pass: " + e.getMessage());
        }
    }
}

