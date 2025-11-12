package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.gui.TrainingGUI;
import dev.artix.artixduels.managers.TrainingManager;
import dev.artix.artixduels.models.BotDifficulty;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.TrainingSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para gerenciar treinamentos.
 */
public class TrainingCommand implements CommandExecutor {
    private final ArtixDuels plugin;
    private final TrainingManager trainingManager;
    private final TrainingGUI trainingGUI;

    public TrainingCommand(ArtixDuels plugin, TrainingManager trainingManager, TrainingGUI trainingGUI) {
        this.plugin = plugin;
        this.trainingManager = trainingManager;
        this.trainingGUI = trainingGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            trainingGUI.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "stop":
                TrainingSession session = trainingManager.getSession(player.getUniqueId());
                if (session != null) {
                    trainingManager.stopTraining(player.getUniqueId());
                    player.sendMessage("§cTreinamento parado!");
                } else {
                    player.sendMessage("§cVocê não está em uma sessão de treinamento!");
                }
                break;

            case "start":
                if (args.length < 3) {
                    player.sendMessage("§cUso: /training start <dificuldade> <kit>");
                    player.sendMessage("§7Dificuldades: easy, medium, hard, expert");
                    return true;
                }
                try {
                    BotDifficulty difficulty = BotDifficulty.valueOf(args[1].toUpperCase());
                    String kitName = args[2];
                    String arenaName = plugin.getArenaManager().getAvailableArena() != null ? 
                                     plugin.getArenaManager().getAvailableArena().getName() : null;
                    TrainingSession newSession = trainingManager.startTraining(player, difficulty, kitName, arenaName, DuelMode.BEDFIGHT);
                    if (newSession != null) {
                        player.sendMessage("§aTreinamento iniciado!");
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cDificuldade inválida! Use: easy, medium, hard, expert");
                }
                break;

            case "stats":
                TrainingSession currentSession = trainingManager.getSession(player.getUniqueId());
                if (currentSession != null) {
                    TrainingSession.TrainingStats stats = currentSession.getStats();
                    player.sendMessage("§6§l=== ESTATÍSTICAS ===");
                    player.sendMessage("§7Acertos: §a" + stats.getPlayerHits() + " §7/ §c" + stats.getBotHits());
                    player.sendMessage("§7Eliminações: §a" + stats.getPlayerKills() + " §7/ §c" + stats.getBotKills());
                    player.sendMessage("§7Precisão: §b" + String.format("%.1f", stats.getPlayerAccuracy()) + "%");
                    player.sendMessage("§7Combos: §e" + stats.getPlayerCombos());
                } else {
                    player.sendMessage("§cVocê não está em uma sessão de treinamento!");
                }
                break;

            default:
                trainingGUI.openMainMenu(player);
                break;
        }

        return true;
    }
}

