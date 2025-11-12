package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.AchievementGUI;
import dev.artix.artixduels.managers.AchievementManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para visualizar conquistas.
 */
public class AchievementCommand implements CommandExecutor {
    private final AchievementManager achievementManager;
    private final AchievementGUI achievementGUI;

    public AchievementCommand(AchievementManager achievementManager, AchievementGUI achievementGUI) {
        this.achievementManager = achievementManager;
        this.achievementGUI = achievementGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;
        achievementGUI.openMainMenu(player);
        return true;
    }
}

