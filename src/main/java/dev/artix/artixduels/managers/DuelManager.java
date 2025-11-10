package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelManager {
    private ArtixDuels plugin;
    private Map<UUID, DuelRequest> pendingRequests;
    private Map<UUID, Duel> activeDuels;
    private Map<UUID, Duel> playerDuels;
    private Map<UUID, ItemStack[]> savedInventories;
    private Map<UUID, ItemStack[]> savedArmor;
    private Map<UUID, Location> savedLocations;
    private Queue<UUID> matchmakingQueue;
    private Map<DuelMode, Queue<UUID>> matchmakingQueuesByMode;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private RewardManager rewardManager;
    private BetManager betManager;
    private CooldownManager cooldownManager;
    private SpectatorManager spectatorManager;
    private dev.artix.artixduels.database.DuelHistoryDAO historyDAO;

    public DuelManager(ArtixDuels plugin, KitManager kitManager, ArenaManager arenaManager, StatsManager statsManager,
                       ScoreboardManager scoreboardManager, RewardManager rewardManager, BetManager betManager,
                       CooldownManager cooldownManager, SpectatorManager spectatorManager,
                       dev.artix.artixduels.database.DuelHistoryDAO historyDAO) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
        this.statsManager = statsManager;
        this.scoreboardManager = scoreboardManager;
        this.rewardManager = rewardManager;
        this.betManager = betManager;
        this.cooldownManager = cooldownManager;
        this.spectatorManager = spectatorManager;
        this.historyDAO = historyDAO;
        this.pendingRequests = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.playerDuels = new HashMap<>();
        this.savedInventories = new HashMap<>();
        this.savedArmor = new HashMap<>();
        this.savedLocations = new HashMap<>();
        this.matchmakingQueue = new LinkedList<>();
        this.matchmakingQueuesByMode = new HashMap<>();
        for (DuelMode mode : DuelMode.values()) {
            matchmakingQueuesByMode.put(mode, new LinkedList<>());
        }
    }

    public void sendDuelRequest(Player challenger, Player target, String kitName, String arenaName) {
        sendDuelRequest(challenger, target, kitName, arenaName, DuelMode.BEDFIGHT);
    }

    public void sendDuelRequest(Player challenger, Player target, String kitName, String arenaName, DuelMode mode) {
        if (isInDuel(challenger) || isInDuel(target)) {
            challenger.sendMessage("§cUm dos jogadores já está em um duelo!");
            return;
        }

        if (cooldownManager.isOnRequestCooldown(challenger.getUniqueId())) {
            long remaining = cooldownManager.getRemainingRequestCooldown(challenger.getUniqueId());
            challenger.sendMessage("§cAguarde §e" + remaining + " §csegundos antes de enviar outro convite!");
            return;
        }

        DuelRequest request = new DuelRequest(challenger, target, kitName, arenaName, mode);
        pendingRequests.put(target.getUniqueId(), request);
        cooldownManager.setRequestCooldown(challenger.getUniqueId());

        challenger.sendMessage("§aConvite de duelo enviado para §e" + target.getName() + "§a!");
        target.sendMessage("§e" + challenger.getName() + " §adesafiou você para um duelo!");
        target.sendMessage("§7Use §a/accept §7para aceitar ou §c/deny §7para recusar.");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(target.getUniqueId()) && 
                pendingRequests.get(target.getUniqueId()).equals(request)) {
                pendingRequests.remove(target.getUniqueId());
                challenger.sendMessage("§cO convite de duelo expirou.");
            }
        }, 6000L);
    }

    public void acceptDuelRequest(Player player) {
        DuelRequest request = pendingRequests.get(player.getUniqueId());
        if (request == null || request.isExpired()) {
            player.sendMessage("§cVocê não tem convites de duelo pendentes!");
            return;
        }

        Player challenger = Bukkit.getPlayer(request.getChallengerId());
        if (challenger == null || !challenger.isOnline()) {
            player.sendMessage("§cO jogador que te desafiou não está mais online!");
            pendingRequests.remove(player.getUniqueId());
            return;
        }

        pendingRequests.remove(player.getUniqueId());
        startDuel(challenger, player, request.getKitName(), request.getArenaName(), request.getMode());
    }

    public void denyDuelRequest(Player player) {
        DuelRequest request = pendingRequests.remove(player.getUniqueId());
        if (request == null) {
            player.sendMessage("§cVocê não tem convites de duelo pendentes!");
            return;
        }

        Player challenger = Bukkit.getPlayer(request.getChallengerId());
        if (challenger != null && challenger.isOnline()) {
            challenger.sendMessage("§c" + player.getName() + " recusou seu convite de duelo.");
        }
        player.sendMessage("§cVocê recusou o convite de duelo.");
    }

    public void startDuel(Player player1, Player player2, String kitName, String arenaName, DuelMode mode) {
        if (isInDuel(player1) || isInDuel(player2)) {
            return;
        }

        Kit kit = kitManager.getKit(kitName);
        Arena arena = arenaManager.getArena(arenaName);

        if (kit == null) {
            player1.sendMessage("§cKit não encontrado!");
            player2.sendMessage("§cKit não encontrado!");
            return;
        }

        if (arena == null) {
            arena = arenaManager.getAvailableArena();
            if (arena == null) {
                player1.sendMessage("§cNenhuma arena disponível!");
                player2.sendMessage("§cNenhuma arena disponível!");
                return;
            }
        }

        if (arena.isInUse()) {
            player1.sendMessage("§cA arena está em uso!");
            player2.sendMessage("§cA arena está em uso!");
            return;
        }

        arena.setInUse(true);
        Duel duel = new Duel(player1, player2, kitName, arenaName, mode);
        UUID duelId = UUID.randomUUID();
        activeDuels.put(duelId, duel);
        playerDuels.put(player1.getUniqueId(), duel);
        playerDuels.put(player2.getUniqueId(), duel);

        savePlayerInventory(player1);
        savePlayerInventory(player2);
        savePlayerLocation(player1);
        savePlayerLocation(player2);

        teleportPlayers(player1, player2, arena);
        giveKit(player1, kit);
        giveKit(player2, kit);

        scoreboardManager.createDuelScoreboard(player1, player2, duel);
        startCountdown(duelId, player1, player2);
    }

    private void savePlayerInventory(Player player) {
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents().clone());
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());
    }

    private void savePlayerLocation(Player player) {
        savedLocations.put(player.getUniqueId(), player.getLocation().clone());
    }

    private void teleportPlayers(Player player1, Player player2, Arena arena) {
        player1.teleport(arena.getPlayer1Spawn());
        player2.teleport(arena.getPlayer2Spawn());
    }

    private void giveKit(Player player, Kit kit) {
        player.getInventory().clear();
        player.getInventory().setContents(kit.getContents());
        player.getInventory().setArmorContents(kit.getArmor());
        player.updateInventory();
    }

    private void startCountdown(UUID duelId, Player player1, Player player2) {
        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {
                    player1.sendMessage("§eDuelo começando em §c" + countdown + "§e...");
                    player2.sendMessage("§eDuelo começando em §c" + countdown + "§e...");
                    countdown--;
                } else {
                    Duel duel = activeDuels.get(duelId);
                    if (duel != null) {
                        duel.setState(Duel.DuelState.FIGHTING);
                        player1.sendMessage("§a§lFIGHT!");
                        player2.sendMessage("§a§lFIGHT!");
                        scoreboardManager.updateDuelScoreboard(player1, duel);
                        scoreboardManager.updateDuelScoreboard(player2, duel);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void endDuel(UUID winnerId, UUID loserId, boolean draw) {
        Duel duel = playerDuels.get(winnerId);
        if (duel == null) {
            duel = playerDuels.get(loserId);
        }
        if (duel == null) return;

        Player winner = Bukkit.getPlayer(winnerId);
        Player loser = Bukkit.getPlayer(loserId);

        long duration = System.currentTimeMillis() - duel.getStartTime();

        if (draw) {
            if (winner != null) {
                winner.sendMessage("§eEmpate!");
                scoreboardManager.removeScoreboard(winner);
                scoreboardManager.createLobbyScoreboard(winner);
            }
            if (loser != null) {
                loser.sendMessage("§eEmpate!");
                scoreboardManager.removeScoreboard(loser);
                scoreboardManager.createLobbyScoreboard(loser);
            }
            statsManager.updateDrawStats(winnerId, loserId, duel.getMode());
        } else {
            if (winner != null) {
                winner.sendMessage("§a§lVITÓRIA!");
                rewardManager.giveWinRewards(winner);
                scoreboardManager.removeScoreboard(winner);
                scoreboardManager.createLobbyScoreboard(winner);
                cooldownManager.setDuelCooldown(winnerId);
            }
            if (loser != null) {
                loser.sendMessage("§c§lDERROTA!");
                rewardManager.giveLossRewards(loser);
                scoreboardManager.removeScoreboard(loser);
                scoreboardManager.createLobbyScoreboard(loser);
                cooldownManager.setDuelCooldown(loserId);
            }
            statsManager.updatePlayerStats(winnerId, loserId, duel.getMode());
            
            // Adicionar kill para o vencedor
            PlayerStats winnerStats = statsManager.getPlayerStats(winnerId, null);
            if (winnerStats != null) {
                winnerStats.addModeKill(duel.getMode());
                statsManager.savePlayerStats(winnerStats);
            }
            
            betManager.processBetResult(winnerId, loserId);
        }

        saveDuelHistory(duel, winnerId, loserId, draw, duration);
        spectatorManager.removeAllSpectators(duel);
        restorePlayers(winner, loser);
        cleanupDuel(duel);
    }

    private void saveDuelHistory(Duel duel, UUID winnerId, UUID loserId, boolean draw, long duration) {
        if (historyDAO == null) return;

        dev.artix.artixduels.models.DuelHistory history = new dev.artix.artixduels.models.DuelHistory();
        history.setPlayer1Id(duel.getPlayer1Id());
        history.setPlayer1Name(Bukkit.getOfflinePlayer(duel.getPlayer1Id()).getName());
        history.setPlayer2Id(duel.getPlayer2Id());
        history.setPlayer2Name(Bukkit.getOfflinePlayer(duel.getPlayer2Id()).getName());
        history.setWinnerId(draw ? null : winnerId);
        history.setKitName(duel.getKitName());
        history.setArenaName(duel.getArenaName());
        history.setTimestamp(System.currentTimeMillis());
        history.setDuration(duration);

        historyDAO.saveDuelHistory(history);
    }

    private void restorePlayers(Player player1, Player player2) {
        if (player1 != null) {
            restorePlayer(player1);
        }
        if (player2 != null) {
            restorePlayer(player2);
        }
    }

    private void restorePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (savedInventories.containsKey(playerId)) {
            player.getInventory().setContents(savedInventories.get(playerId));
            savedInventories.remove(playerId);
        }
        if (savedArmor.containsKey(playerId)) {
            player.getInventory().setArmorContents(savedArmor.get(playerId));
            savedArmor.remove(playerId);
        }
        if (savedLocations.containsKey(playerId)) {
            player.teleport(savedLocations.get(playerId));
            savedLocations.remove(playerId);
        }
        
        // Garantir que o item do perfil esteja presente
        org.bukkit.inventory.ItemStack profileItem = dev.artix.artixduels.listeners.ProfileItemListener.createProfileItem();
        player.getInventory().setItem(4, profileItem);
        
        player.updateInventory();
    }

    private void cleanupDuel(Duel duel) {
        Arena arena = arenaManager.getArena(duel.getArenaName());
        if (arena != null) {
            arena.setInUse(false);
        }

        UUID duelId = null;
        for (Map.Entry<UUID, Duel> entry : activeDuels.entrySet()) {
            if (entry.getValue().equals(duel)) {
                duelId = entry.getKey();
                break;
            }
        }

        if (duelId != null) {
            activeDuels.remove(duelId);
        }
        playerDuels.remove(duel.getPlayer1Id());
        playerDuels.remove(duel.getPlayer2Id());
    }

    public boolean isInDuel(Player player) {
        return playerDuels.containsKey(player.getUniqueId());
    }

    public Duel getPlayerDuel(Player player) {
        return playerDuels.get(player.getUniqueId());
    }

    public void addToMatchmaking(Player player) {
        addToMatchmaking(player, DuelMode.BEDFIGHT);
    }

    public void addToMatchmaking(Player player, DuelMode mode) {
        if (isInDuel(player)) {
            player.sendMessage("§cVocê já está em um duelo!");
            return;
        }

        Queue<UUID> queue = matchmakingQueuesByMode.get(mode);
        if (queue == null) {
            queue = new LinkedList<>();
            matchmakingQueuesByMode.put(mode, queue);
        }

        if (queue.contains(player.getUniqueId())) {
            player.sendMessage("§cVocê já está na fila de matchmaking para " + mode.getDisplayName() + "!");
            return;
        }

        queue.add(player.getUniqueId());
        player.sendMessage("§aVocê entrou na fila de matchmaking para §e" + mode.getDisplayName() + "§a!");

        if (queue.size() >= 2) {
            UUID player1Id = queue.poll();
            UUID player2Id = queue.poll();
            Player player1 = Bukkit.getPlayer(player1Id);
            Player player2 = Bukkit.getPlayer(player2Id);

            if (player1 != null && player2 != null && player1.isOnline() && player2.isOnline()) {
                Arena arena = arenaManager.getAvailableArena();
                String defaultKit = kitManager.getKits().keySet().iterator().next();
                if (arena != null && defaultKit != null) {
                    startDuel(player1, player2, defaultKit, arena.getName(), mode);
                }
            }
        }
    }

    public void removeFromMatchmaking(Player player) {
        for (Queue<UUID> queue : matchmakingQueuesByMode.values()) {
            if (queue.remove(player.getUniqueId())) {
                player.sendMessage("§cVocê saiu da fila de matchmaking.");
                return;
            }
        }
        if (matchmakingQueue.remove(player.getUniqueId())) {
            player.sendMessage("§cVocê saiu da fila de matchmaking.");
        }
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public BetManager getBetManager() {
        return betManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public int getActiveDuelsCount() {
        return activeDuels.size();
    }

    public int getMatchmakingQueueSize() {
        return matchmakingQueue.size();
    }

    public int getActiveDuelsCountByMode(DuelMode mode) {
        int count = 0;
        for (Duel duel : activeDuels.values()) {
            if (duel.getMode() == mode) {
                count++;
            }
        }
        return count;
    }

    public int getMatchmakingQueueSizeByMode(DuelMode mode) {
        Queue<UUID> queue = matchmakingQueuesByMode.get(mode);
        return queue != null ? queue.size() : 0;
    }
}

