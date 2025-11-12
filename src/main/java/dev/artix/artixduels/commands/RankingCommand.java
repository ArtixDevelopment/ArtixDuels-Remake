package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.RankingGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para abrir o menu de rankings.
 */
public class RankingCommand implements CommandExecutor {
    private final RankingGUI rankingGUI;

    public RankingCommand(RankingGUI rankingGUI) {
        this.rankingGUI = rankingGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;
        rankingGUI.openMainMenu(player);
        return true;
    }
}

