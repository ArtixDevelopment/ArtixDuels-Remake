package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.CosmeticGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para abrir o menu de cosméticos.
 */
public class CosmeticCommand implements CommandExecutor {
    private final CosmeticGUI cosmeticGUI;

    public CosmeticCommand(CosmeticGUI cosmeticGUI) {
        this.cosmeticGUI = cosmeticGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;
        cosmeticGUI.openMainMenu(player);
        return true;
    }
}

