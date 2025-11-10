package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.StatsManager;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class StatsCommand implements CommandExecutor {
    private StatsManager statsManager;

    public StatsCommand(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cJogador não encontrado!");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cUso: /stats <jogador>");
                return true;
            }
            target = (Player) sender;
        }

        PlayerStats stats = statsManager.getPlayerStats(target);
        DecimalFormat df = new DecimalFormat("#.##");

        sender.sendMessage("§6=== Estatísticas de §e" + stats.getPlayerName() + " §6===");
        sender.sendMessage("§7Vitórias: §a" + stats.getWins());
        sender.sendMessage("§7Derrotas: §c" + stats.getLosses());
        sender.sendMessage("§7Empates: §e" + stats.getDraws());
        sender.sendMessage("§7Win Rate: §b" + df.format(stats.getWinRate()) + "%");
        sender.sendMessage("§7Elo: §d" + stats.getElo());
        sender.sendMessage("§7Sequência de Vitórias: §a" + stats.getWinStreak());
        sender.sendMessage("§7Melhor Sequência: §a" + stats.getBestWinStreak());

        return true;
    }
}

