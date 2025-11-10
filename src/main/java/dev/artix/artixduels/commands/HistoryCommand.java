package dev.artix.artixduels.commands;

import dev.artix.artixduels.database.IDuelHistoryDAO;
import dev.artix.artixduels.models.DuelHistory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryCommand implements CommandExecutor {
    private IDuelHistoryDAO historyDAO;

    public HistoryCommand(IDuelHistoryDAO historyDAO) {
        this.historyDAO = historyDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        List<DuelHistory> history = historyDAO.getPlayerHistory(player.getUniqueId(), 10);

        if (history.isEmpty()) {
            player.sendMessage("§cVocê não tem histórico de duelos!");
            return true;
        }

        player.sendMessage("§6=== Histórico de Duelos ===");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (int i = 0; i < Math.min(history.size(), 10); i++) {
            DuelHistory duel = history.get(i);
            String opponent = duel.getPlayer1Id().equals(player.getUniqueId()) 
                ? duel.getPlayer2Name() 
                : duel.getPlayer1Name();
            
            String result;
            if (duel.getWinnerId() == null) {
                result = "§eEmpate";
            } else if (duel.getWinnerId().equals(player.getUniqueId())) {
                result = "§aVitória";
            } else {
                result = "§cDerrota";
            }

            String date = sdf.format(new Date(duel.getTimestamp()));
            long duration = duel.getDuration() / 1000;

            player.sendMessage("§7" + (i + 1) + ". §e" + opponent + " §7- " + result);
            player.sendMessage("   §7Kit: §f" + duel.getKitName() + " §7| Arena: §f" + duel.getArenaName());
            player.sendMessage("   §7Duração: §f" + duration + "s §7| Data: §f" + date);
        }

        return true;
    }
}

