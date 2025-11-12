package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.gui.TournamentGUI;
import dev.artix.artixduels.managers.TournamentManager;
import dev.artix.artixduels.models.Tournament;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Comando para gerenciar torneios.
 */
public class TournamentCommand implements CommandExecutor {
    private final ArtixDuels plugin;
    private final TournamentManager tournamentManager;
    private final TournamentGUI tournamentGUI;

    public TournamentCommand(ArtixDuels plugin, TournamentManager tournamentManager, TournamentGUI tournamentGUI) {
        this.plugin = plugin;
        this.tournamentManager = tournamentManager;
        this.tournamentGUI = tournamentGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            tournamentGUI.openMainMenu(player);
            return true;
        }

        if (!player.hasPermission("artixduels.admin")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /tournament create <template-id>");
                    return true;
                }
                Tournament tournament = tournamentManager.createActiveTournament(args[1]);
                if (tournament != null) {
                    player.sendMessage("§aTorneio criado: §e" + tournament.getName());
                } else {
                    player.sendMessage("§cTemplate de torneio não encontrado!");
                }
                break;

            case "start":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /tournament start <tournament-id>");
                    return true;
                }
                tournamentManager.startTournament(args[1]);
                player.sendMessage("§aTorneio iniciado!");
                break;

            case "list":
                List<Tournament> activeTournaments = tournamentManager.getActiveTournaments();
                player.sendMessage("§6§lTorneios Ativos:");
                for (Tournament t : activeTournaments) {
                    player.sendMessage("§7- §e" + t.getName() + " §7(ID: §b" + t.getId() + "§7)");
                }
                break;

            default:
                tournamentGUI.openMainMenu(player);
                break;
        }

        return true;
    }
}

