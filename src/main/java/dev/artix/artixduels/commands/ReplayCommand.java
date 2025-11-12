package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.gui.ReplayGUI;
import dev.artix.artixduels.managers.ReplayManager;
import dev.artix.artixduels.models.Replay;
import dev.artix.artixduels.models.ReplaySession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Comando para gerenciar replays.
 */
public class ReplayCommand implements CommandExecutor {
    private final ArtixDuels plugin;
    private final ReplayManager replayManager;
    private final ReplayGUI replayGUI;

    public ReplayCommand(ArtixDuels plugin, ReplayManager replayManager, ReplayGUI replayGUI) {
        this.plugin = plugin;
        this.replayManager = replayManager;
        this.replayGUI = replayGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            replayGUI.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "play":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /replay play <replay-id>");
                    return true;
                }
                try {
                    UUID replayId = UUID.fromString(args[1]);
                    ReplaySession session = replayManager.startPlayback(player, replayId);
                    if (session != null) {
                        replayGUI.openReplayControls(player, session);
                        player.sendMessage("§aReplay iniciado!");
                    } else {
                        player.sendMessage("§cReplay não encontrado!");
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cID de replay inválido!");
                }
                break;

            case "stop":
                ReplaySession session = replayManager.getSession(player.getUniqueId());
                if (session != null) {
                    replayManager.stopPlayback(player.getUniqueId());
                    player.sendMessage("§cReplay parado.");
                } else {
                    player.sendMessage("§cVocê não está assistindo nenhum replay!");
                }
                break;

            case "list":
                java.util.List<Replay> replays = replayManager.getSavedReplays();
                player.sendMessage("§6§lReplays Disponíveis:");
                for (Replay replay : replays) {
                    player.sendMessage("§7- §e" + replay.getPlayer1Name() + " vs " + replay.getPlayer2Name() + 
                                     " §7(ID: §b" + replay.getReplayId() + "§7)");
                }
                break;

            case "delete":
                if (!player.hasPermission("artixduels.admin")) {
                    player.sendMessage("§cVocê não tem permissão para usar este comando!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUso: /replay delete <replay-id>");
                    return true;
                }
                try {
                    UUID replayId = UUID.fromString(args[1]);
                    if (replayManager.deleteReplay(replayId)) {
                        player.sendMessage("§aReplay deletado!");
                    } else {
                        player.sendMessage("§cReplay não encontrado!");
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cID de replay inválido!");
                }
                break;

            default:
                replayGUI.openMainMenu(player);
                break;
        }

        return true;
    }
}

