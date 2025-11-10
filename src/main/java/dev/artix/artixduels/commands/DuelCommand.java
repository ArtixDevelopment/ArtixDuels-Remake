package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.gui.DuelModeSelectionGUI;
import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {
    private DuelManager duelManager;
    private DuelModeSelectionGUI modeSelectionGUI;

    public DuelCommand(ArtixDuels plugin, DuelManager duelManager, KitManager kitManager, ArenaManager arenaManager, DuelModeSelectionGUI modeSelectionGUI) {
        this.duelManager = duelManager;
        this.modeSelectionGUI = modeSelectionGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cUso: /duelo <jogador>");
            player.sendMessage("§cUso: /duelo queue [modo] (entrar na fila de matchmaking)");
            return true;
        }

        if (args[0].equalsIgnoreCase("queue")) {
            DuelMode mode = args.length > 1 ? DuelMode.fromString(args[1].toUpperCase()) : DuelMode.BEDFIGHT;
            if (mode == null) {
                player.sendMessage("§cModo de duelo inválido! Modos disponíveis: " + getAvailableModes());
                return true;
            }
            duelManager.addToMatchmaking(player, mode);
            return true;
        }

        String targetName = args[0];
        modeSelectionGUI.openModeSelectionMenu(player, targetName);
        return true;
    }

    private String getAvailableModes() {
        StringBuilder modes = new StringBuilder();
        for (DuelMode mode : DuelMode.values()) {
            if (modes.length() > 0) modes.append(", ");
            modes.append(mode.getName());
        }
        return modes.toString();
    }
}

