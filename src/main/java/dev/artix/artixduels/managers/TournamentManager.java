package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.Tournament;
import dev.artix.artixduels.models.TournamentMatch;
import dev.artix.artixduels.models.TournamentParticipant;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Gerenciador de torneios.
 */
public class TournamentManager {
    private final ArtixDuels plugin;
    private final DuelManager duelManager;
    private final StatsManager statsManager;
    private FileConfiguration tournamentsConfig;
    private File tournamentsFile;
    private Map<String, Tournament> tournaments;
    private Map<String, Tournament> activeTournaments;
    private Map<UUID, String> playerTournaments;

    public TournamentManager(ArtixDuels plugin, DuelManager duelManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.statsManager = statsManager;
        this.tournaments = new HashMap<>();
        this.activeTournaments = new HashMap<>();
        this.playerTournaments = new HashMap<>();
        
        loadTournamentsConfig();
        loadTournaments();
    }

    private void loadTournamentsConfig() {
        tournamentsFile = new File(plugin.getDataFolder(), "tournaments.yml");
        if (!tournamentsFile.exists()) {
            plugin.saveResource("tournaments.yml", false);
        }
        tournamentsConfig = YamlConfiguration.loadConfiguration(tournamentsFile);
    }

    private void loadTournaments() {
        tournaments.clear();
        
        if (tournamentsConfig.contains("tournaments")) {
            ConfigurationSection tournamentsSection = tournamentsConfig.getConfigurationSection("tournaments");
            if (tournamentsSection != null) {
                for (String key : tournamentsSection.getKeys(false)) {
                    ConfigurationSection tournamentSection = tournamentsSection.getConfigurationSection(key);
                    if (tournamentSection != null) {
                        Tournament tournament = Tournament.fromConfig(tournamentSection);
                        tournaments.put(tournament.getId(), tournament);
                    }
                }
            }
        }
        
        plugin.getLogger().info("Carregados " + tournaments.size() + " torneios.");
    }

    public void reload() {
        loadTournamentsConfig();
        loadTournaments();
    }

    /**
     * Cria um novo torneio ativo.
     */
    public Tournament createActiveTournament(String tournamentId) {
        Tournament template = tournaments.get(tournamentId);
        if (template == null) {
            return null;
        }

        Tournament activeTournament = new Tournament(
            tournamentId + "_" + System.currentTimeMillis(),
            template.getName(),
            template.getDescription(),
            template.getType(),
            template.getMaxParticipants(),
            template.getMinParticipants(),
            template.getMode(),
            template.getKit()
        );
        activeTournament.setRewards(template.getRewards());

        activeTournaments.put(activeTournament.getId(), activeTournament);
        return activeTournament;
    }

    /**
     * Registra um jogador em um torneio.
     */
    public boolean registerPlayer(Player player, String tournamentId) {
        Tournament tournament = activeTournaments.get(tournamentId);
        if (tournament == null) {
            player.sendMessage("§cTorneio não encontrado!");
            return false;
        }

        if (tournament.getState() != Tournament.TournamentState.REGISTRATION) {
            player.sendMessage("§cAs inscrições para este torneio estão fechadas!");
            return false;
        }

        if (tournament.isFull()) {
            player.sendMessage("§cO torneio está cheio!");
            return false;
        }

        if (tournament.isParticipant(player.getUniqueId())) {
            player.sendMessage("§cVocê já está inscrito neste torneio!");
            return false;
        }

        if (duelManager.isInDuel(player)) {
            player.sendMessage("§cVocê não pode se inscrever enquanto está em um duelo!");
            return false;
        }

        tournament.addParticipant(player.getUniqueId());
        playerTournaments.put(player.getUniqueId(), tournamentId);
        player.sendMessage("§aVocê se inscreveu no torneio: §e" + tournament.getName() + "§a!");
        player.sendMessage("§7Participantes: §b" + tournament.getParticipants().size() + "§7/§b" + tournament.getMaxParticipants());

        return true;
    }

    /**
     * Remove um jogador de um torneio.
     */
    public boolean unregisterPlayer(Player player, String tournamentId) {
        Tournament tournament = activeTournaments.get(tournamentId);
        if (tournament == null) {
            return false;
        }

        if (tournament.getState() != Tournament.TournamentState.REGISTRATION) {
            player.sendMessage("§cNão é possível cancelar a inscrição após o início do torneio!");
            return false;
        }

        if (!tournament.isParticipant(player.getUniqueId())) {
            return false;
        }

        tournament.removeParticipant(player.getUniqueId());
        playerTournaments.remove(player.getUniqueId());
        player.sendMessage("§cVocê cancelou sua inscrição no torneio.");

        return true;
    }

    /**
     * Inicia um torneio.
     */
    public void startTournament(String tournamentId) {
        Tournament tournament = activeTournaments.get(tournamentId);
        if (tournament == null) {
            return;
        }

        if (!tournament.hasMinParticipants()) {
            Bukkit.broadcastMessage("§cO torneio " + tournament.getName() + " foi cancelado por falta de participantes!");
            tournament.setState(Tournament.TournamentState.CANCELLED);
            return;
        }

        tournament.setState(Tournament.TournamentState.STARTING);
        tournament.setStartTime(System.currentTimeMillis());

        generateBrackets(tournament);

        Bukkit.broadcastMessage("§6§l[TORNEIO] §eO torneio " + tournament.getName() + " começou!");
        Bukkit.broadcastMessage("§7Participantes: §b" + tournament.getParticipants().size());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tournament.setState(Tournament.TournamentState.IN_PROGRESS);
            startNextRound(tournament);
        }, 100L);
    }

    /**
     * Gera os brackets do torneio.
     */
    private void generateBrackets(Tournament tournament) {
        List<UUID> participants = new ArrayList<>(tournament.getParticipants());
        Collections.shuffle(participants);

        Map<Integer, List<TournamentMatch>> brackets = new HashMap<>();

        int round = 1;
        List<TournamentMatch> currentRoundMatches = new ArrayList<>();
        int matchNumber = 1;

        for (int i = 0; i < participants.size(); i += 2) {
            if (i + 1 < participants.size()) {
                TournamentMatch match = new TournamentMatch(participants.get(i), participants.get(i + 1), round, matchNumber++);
                currentRoundMatches.add(match);
            } else {
                TournamentMatch bye = new TournamentMatch(participants.get(i), null, round, matchNumber++);
                bye.setState(TournamentMatch.MatchState.BYE);
                bye.setWinner(participants.get(i));
                currentRoundMatches.add(bye);
            }
        }

        brackets.put(round, currentRoundMatches);
        tournament.setBrackets(brackets);
        tournament.setCurrentRound(round);
    }

    /**
     * Inicia a próxima rodada do torneio.
     */
    private void startNextRound(Tournament tournament) {
        List<TournamentMatch> currentRoundMatches = tournament.getBrackets().get(tournament.getCurrentRound());
        if (currentRoundMatches == null || currentRoundMatches.isEmpty()) {
            finishTournament(tournament);
            return;
        }

        for (TournamentMatch match : currentRoundMatches) {
            if (match.getState() == TournamentMatch.MatchState.PENDING) {
                Player player1 = Bukkit.getPlayer(match.getPlayer1Id());
                Player player2 = match.getPlayer2Id() != null ? Bukkit.getPlayer(match.getPlayer2Id()) : null;

                if (player1 != null && player1.isOnline() && player2 != null && player2.isOnline()) {
                    match.setState(TournamentMatch.MatchState.IN_PROGRESS);
                    DuelMode mode = DuelMode.fromString(tournament.getMode());
                    duelManager.startDuel(player1, player2, tournament.getKit(), null, mode);
                } else if (player1 != null && player1.isOnline() && player2 == null) {
                    match.setWinner(player1.getUniqueId());
                }
            }
        }
    }

    /**
     * Processa o resultado de uma partida do torneio.
     */
    public void processMatchResult(String tournamentId, UUID winnerId, UUID loserId) {
        Tournament tournament = activeTournaments.get(tournamentId);
        if (tournament == null) {
            return;
        }

        List<TournamentMatch> currentRoundMatches = tournament.getBrackets().get(tournament.getCurrentRound());
        if (currentRoundMatches == null) {
            return;
        }

        for (TournamentMatch match : currentRoundMatches) {
            if (match.isPlayerInMatch(winnerId) && match.isPlayerInMatch(loserId) &&
                match.getState() == TournamentMatch.MatchState.IN_PROGRESS) {
                match.setWinner(winnerId);

                TournamentParticipant winnerData = tournament.getParticipantData(winnerId);
                TournamentParticipant loserData = tournament.getParticipantData(loserId);
                if (winnerData != null) {
                    winnerData.addWin();
                }
                if (loserData != null) {
                    loserData.addLoss();
                    loserData.setEliminated(true);
                }

                checkRoundComplete(tournament);
                return;
            }
        }
    }

    /**
     * Verifica se a rodada está completa e avança para a próxima.
     */
    private void checkRoundComplete(Tournament tournament) {
        List<TournamentMatch> currentRoundMatches = tournament.getBrackets().get(tournament.getCurrentRound());
        if (currentRoundMatches == null) {
            return;
        }

        boolean allFinished = true;
        for (TournamentMatch match : currentRoundMatches) {
            if (match.getState() != TournamentMatch.MatchState.FINISHED &&
                match.getState() != TournamentMatch.MatchState.BYE) {
                allFinished = false;
                break;
            }
        }

        if (allFinished) {
            advanceToNextRound(tournament);
        }
    }

    /**
     * Avança para a próxima rodada.
     */
    private void advanceToNextRound(Tournament tournament) {
        List<TournamentMatch> currentRoundMatches = tournament.getBrackets().get(tournament.getCurrentRound());
        List<UUID> winners = new ArrayList<>();

        for (TournamentMatch match : currentRoundMatches) {
            if (match.getWinnerId() != null) {
                winners.add(match.getWinnerId());
            }
        }

        if (winners.size() == 1) {
            finishTournament(tournament, winners.get(0));
            return;
        }

        int nextRound = tournament.getCurrentRound() + 1;
        List<TournamentMatch> nextRoundMatches = new ArrayList<>();
        int matchNumber = 1;

        for (int i = 0; i < winners.size(); i += 2) {
            if (i + 1 < winners.size()) {
                TournamentMatch match = new TournamentMatch(winners.get(i), winners.get(i + 1), nextRound, matchNumber++);
                nextRoundMatches.add(match);
            } else {
                TournamentMatch bye = new TournamentMatch(winners.get(i), null, nextRound, matchNumber++);
                bye.setState(TournamentMatch.MatchState.BYE);
                bye.setWinner(winners.get(i));
                nextRoundMatches.add(bye);
            }
        }

        tournament.getBrackets().put(nextRound, nextRoundMatches);
        tournament.setCurrentRound(nextRound);

        Bukkit.broadcastMessage("§6§l[TORNEIO] §eRodada " + nextRound + " do torneio " + tournament.getName() + " começou!");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startNextRound(tournament);
        }, 100L);
    }

    /**
     * Finaliza o torneio.
     */
    private void finishTournament(Tournament tournament) {
        finishTournament(tournament, null);
    }

    private void finishTournament(Tournament tournament, UUID winnerId) {
        tournament.setState(Tournament.TournamentState.FINISHED);
        tournament.setEndTime(System.currentTimeMillis());

        if (winnerId != null) {
            Player winner = Bukkit.getPlayer(winnerId);
            if (winner != null && winner.isOnline()) {
                winner.sendMessage("§6§l[TORNEIO] §eParabéns! Você venceu o torneio " + tournament.getName() + "!");
                giveTournamentRewards(winner, tournament);
            }

            Bukkit.broadcastMessage("§6§l[TORNEIO] §e" + (winner != null ? winner.getName() : "Jogador") + 
                                   " venceu o torneio " + tournament.getName() + "!");
        }

        activeTournaments.remove(tournament.getId());
        for (UUID participantId : tournament.getParticipants()) {
            playerTournaments.remove(participantId);
        }
    }

    /**
     * Dá as recompensas do torneio.
     */
    private void giveTournamentRewards(Player winner, Tournament tournament) {
        Map<String, Object> rewards = tournament.getRewards();
        
        if (rewards.containsKey("money")) {
            double money = ((Number) rewards.get("money")).doubleValue();
            dev.artix.artixduels.ArtixDuels artixDuels = (dev.artix.artixduels.ArtixDuels) plugin;
            dev.artix.artixduels.managers.RewardManager rewardManager = artixDuels.getRewardManager();
            if (rewardManager != null) {
                rewardManager.giveMoney(winner, money);
            }
        }
        
        if (rewards.containsKey("xp")) {
            int xp = ((Number) rewards.get("xp")).intValue();
            dev.artix.artixduels.models.PlayerStats stats = statsManager.getPlayerStats(winner);
            if (stats != null) {
                stats.addXp(xp);
                statsManager.savePlayerStats(stats);
            }
        }
    }

    /**
     * Obtém o torneio ativo de um jogador.
     */
    public Tournament getPlayerTournament(UUID playerId) {
        String tournamentId = playerTournaments.get(playerId);
        if (tournamentId == null) {
            return null;
        }
        return activeTournaments.get(tournamentId);
    }

    /**
     * Obtém todos os torneios ativos.
     */
    public List<Tournament> getActiveTournaments() {
        return new ArrayList<>(activeTournaments.values());
    }

    /**
     * Obtém todos os templates de torneios.
     */
    public List<Tournament> getTournamentTemplates() {
        return new ArrayList<>(tournaments.values());
    }
}

