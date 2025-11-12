package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.ChallengeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para abrir o menu de desafios.
 */
public class ChallengeCommand implements CommandExecutor {
    private final ChallengeGUI challengeGUI;

    public ChallengeCommand(ChallengeGUI challengeGUI) {
        this.challengeGUI = challengeGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;
        challengeGUI.openMainMenu(player);
        return true;
    }
}

