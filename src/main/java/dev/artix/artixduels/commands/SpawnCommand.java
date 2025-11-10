package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.listeners.ProfileItemListener;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private ArtixDuels plugin;

    public SpawnCommand(ArtixDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        Location lobbySpawn = plugin.getLobbySpawn();
        if (lobbySpawn == null) {
            player.sendMessage("§cSpawn do lobby não foi definido! Use §e/setspawn lobby §cpara definir.");
            return true;
        }

        if (plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage("§cVocê não pode usar este comando durante um duelo!");
            return true;
        }

        plugin.getDuelManager().removeFromMatchmaking(player);
        player.teleport(lobbySpawn);
        player.sendMessage("§aVocê foi teleportado para o lobby!");
        
        ProfileItemListener profileItemListener = plugin.getProfileItemListener();
        if (profileItemListener != null) {
            profileItemListener.giveHotbarItems(player);
        }
        
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().createLobbyScoreboard(player);
        }
        
        return true;
    }
}

