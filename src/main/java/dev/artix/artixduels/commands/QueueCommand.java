package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.gui.DuelModeSelectionGUI;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {
    private DuelManager duelManager;
    private DuelModeSelectionGUI duelModeSelectionGUI;

    public QueueCommand(ArtixDuels plugin, DuelManager duelManager, DuelModeSelectionGUI duelModeSelectionGUI) {
        this.duelManager = duelManager;
        this.duelModeSelectionGUI = duelModeSelectionGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (duelManager.isInDuel(player)) {
            player.sendMessage("§cVocê já está em um duelo!");
            return true;
        }

        if (args.length == 0) {
            duelModeSelectionGUI.openQueueMenu(player);
            return true;
        }

        String modeString = args[0].toUpperCase();
        DuelMode mode = DuelMode.fromString(modeString);
        if (mode == null) {
            player.sendMessage("§cModo inválido! Use §e/queue §cpara ver os modos disponíveis.");
            return true;
        }

        duelManager.addToMatchmaking(player, mode);
        return true;
    }
}

