package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.DuelManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand implements CommandExecutor {
    private DuelManager duelManager;

    public DenyCommand(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        duelManager.denyDuelRequest(player);
        return true;
    }
}

