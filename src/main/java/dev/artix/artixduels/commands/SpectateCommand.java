package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.SpectatorManager;
import dev.artix.artixduels.models.Duel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand implements CommandExecutor {
    private DuelManager duelManager;
    private SpectatorManager spectatorManager;

    public SpectateCommand(DuelManager duelManager, SpectatorManager spectatorManager) {
        this.duelManager = duelManager;
        this.spectatorManager = spectatorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;

        if (spectatorManager.isSpectator(player)) {
            spectatorManager.removeSpectator(player);
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUso: /spectate <jogador>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cJogador não encontrado!");
            return true;
        }

        Duel duel = duelManager.getPlayerDuel(target);
        if (duel == null) {
            player.sendMessage("§cO jogador não está em um duelo!");
            return true;
        }

        spectatorManager.addSpectator(player, duel);
        return true;
    }
}

