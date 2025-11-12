package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.StatsDashboardGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para abrir o dashboard de estatísticas.
 */
public class DashboardCommand implements CommandExecutor {
    private final StatsDashboardGUI dashboardGUI;

    public DashboardCommand(StatsDashboardGUI dashboardGUI) {
        this.dashboardGUI = dashboardGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;
        dashboardGUI.openDashboard(player);
        return true;
    }
}

