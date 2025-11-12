package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.NotificationManager;
import dev.artix.artixduels.gui.NotificationSettingsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para configurar notificações.
 */
public class NotificationSettingsCommand implements CommandExecutor {
    private final NotificationSettingsGUI settingsGUI;

    public NotificationSettingsCommand(NotificationManager notificationManager) {
        this.settingsGUI = new NotificationSettingsGUI(notificationManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;
        settingsGUI.openSettings(player);
        return true;
    }
}

