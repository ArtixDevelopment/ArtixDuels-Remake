package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.AdminDashboardGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para abrir o dashboard administrativo.
 */
public class AdminDashboardCommand implements CommandExecutor {
    private final AdminDashboardGUI adminDashboardGUI;

    public AdminDashboardCommand(AdminDashboardGUI adminDashboardGUI) {
        this.adminDashboardGUI = adminDashboardGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("artixduels.admin")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        adminDashboardGUI.openDashboard(player);
        return true;
    }
}

