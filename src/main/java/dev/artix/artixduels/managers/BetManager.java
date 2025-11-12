package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.DuelRequest;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BetManager {
    private ArtixDuels plugin;
    private Map<UUID, Double> pendingBets;
    private Map<UUID, DuelRequest> betRequests;
    private boolean bettingEnabled;
    private double minBet;
    private double maxBet;

    public BetManager(ArtixDuels plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.pendingBets = new HashMap<>();
        this.betRequests = new HashMap<>();
        loadConfig(config);
    }

    private void loadConfig(FileConfiguration config) {
        bettingEnabled = config.getBoolean("betting.enabled", false);
        minBet = config.getDouble("betting.min-bet", 0.0);
        maxBet = config.getDouble("betting.max-bet", 10000.0);
    }

    public void reload(FileConfiguration config) {
        loadConfig(config);
    }

    public boolean createBet(Player challenger, Player target, double amount) {
        if (!bettingEnabled) {
            challenger.sendMessage("§cSistema de apostas está desabilitado!");
            return false;
        }

        if (amount < minBet || amount > maxBet) {
            challenger.sendMessage("§cValor da aposta deve estar entre §e" + minBet + " §ce §e" + maxBet + "§c!");
            return false;
        }

        if (!hasEnoughMoney(challenger, amount)) {
            challenger.sendMessage("§cVocê não tem dinheiro suficiente!");
            return false;
        }

        if (!hasEnoughMoney(target, amount)) {
            challenger.sendMessage("§cO jogador não tem dinheiro suficiente para apostar!");
            return false;
        }

        pendingBets.put(challenger.getUniqueId(), amount);
        challenger.sendMessage("§aAposta de §e" + amount + " §acriada! Aguardando confirmação do oponente.");
        target.sendMessage("§e" + challenger.getName() + " §adesafiou você para um duelo com aposta de §e" + amount + "§a!");
        target.sendMessage("§7Use §a/accept §7para aceitar ou §c/deny §7para recusar.");

        return true;
    }

    public void acceptBet(Player player, DuelRequest request) {
        Double betAmount = pendingBets.get(request.getChallengerId());
        if (betAmount == null) {
            return;
        }

        if (!hasEnoughMoney(player, betAmount)) {
            player.sendMessage("§cVocê não tem dinheiro suficiente para aceitar a aposta!");
            return;
        }

        takeMoney(player, betAmount);
        Player challenger = Bukkit.getPlayer(request.getChallengerId());
        if (challenger != null) {
            takeMoney(challenger, betAmount);
        }

        betRequests.put(request.getChallengerId(), request);
        player.sendMessage("§aAposta aceita! O duelo começará em breve.");
    }

    public void processBetResult(UUID winnerId, UUID loserId) {
        for (Map.Entry<UUID, DuelRequest> entry : betRequests.entrySet()) {
            DuelRequest request = entry.getValue();
            if (request.getChallengerId().equals(winnerId) || request.getChallengerId().equals(loserId) ||
                request.getTargetId().equals(winnerId) || request.getTargetId().equals(loserId)) {

                Double betAmount = pendingBets.get(request.getChallengerId());
                if (betAmount == null) continue;

                Player winner = Bukkit.getPlayer(winnerId);
                if (winner != null) {
                    double totalReward = betAmount * 2;
                    giveMoney(winner, totalReward);
                    winner.sendMessage("§aVocê ganhou §e" + totalReward + " §ana aposta!");
                }

                pendingBets.remove(request.getChallengerId());
                betRequests.remove(request.getChallengerId());
                break;
            }
        }
    }

    public void cancelBet(UUID playerId) {
        pendingBets.remove(playerId);
        betRequests.remove(playerId);
    }

    private boolean hasEnoughMoney(Player player, double amount) {
        try {
            dev.artix.artixduels.utils.IntegrationManager integrationManager = plugin.getIntegrationManager();
            if (integrationManager != null && integrationManager.isVaultEnabled()) {
                Object economy = integrationManager.getVaultEconomy();
                if (economy != null) {
                    Class<?> economyClass = economy.getClass().getInterfaces()[0];
                    return (Boolean) economyClass.getMethod("has", Player.class, double.class).invoke(economy, player, amount);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao verificar dinheiro de " + player.getName() + ": " + e.getMessage());
        }
        return false;
    }

    private void takeMoney(Player player, double amount) {
        try {
            dev.artix.artixduels.utils.IntegrationManager integrationManager = plugin.getIntegrationManager();
            if (integrationManager != null && integrationManager.isVaultEnabled()) {
                Object economy = integrationManager.getVaultEconomy();
                if (economy != null) {
                    Class<?> economyClass = economy.getClass().getInterfaces()[0];
                    economyClass.getMethod("withdrawPlayer", Player.class, double.class).invoke(economy, player, amount);
                    plugin.getLogger().fine("Retirado " + amount + " de " + player.getName() + " via Vault.");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao retirar dinheiro de " + player.getName() + ": " + e.getMessage());
        }
    }

    private void giveMoney(Player player, double amount) {
        try {
            dev.artix.artixduels.utils.IntegrationManager integrationManager = plugin.getIntegrationManager();
            if (integrationManager != null && integrationManager.isVaultEnabled()) {
                Object economy = integrationManager.getVaultEconomy();
                if (economy != null) {
                    Class<?> economyClass = economy.getClass().getInterfaces()[0];
                    economyClass.getMethod("depositPlayer", Player.class, double.class).invoke(economy, player, amount);
                    plugin.getLogger().fine("Depositado " + amount + " para " + player.getName() + " via Vault.");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao dar dinheiro para " + player.getName() + ": " + e.getMessage());
        }
    }

    public boolean isBettingEnabled() {
        return bettingEnabled;
    }

    public double getBetAmount(UUID playerId) {
        return pendingBets.getOrDefault(playerId, 0.0);
    }
}

