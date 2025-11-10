package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.ScoreboardModeSelectionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScoreboardCommand implements CommandExecutor {
    private ScoreboardModeSelectionGUI scoreboardModeSelectionGUI;

    public ScoreboardCommand(ScoreboardModeSelectionGUI scoreboardModeSelectionGUI) {
        this.scoreboardModeSelectionGUI = scoreboardModeSelectionGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        scoreboardModeSelectionGUI.openModeSelectionMenu(player);
        return true;
    }
}

