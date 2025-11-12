package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.ReportManager;
import dev.artix.artixduels.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Comando para criar e gerenciar relatórios.
 */
public class ReportCommand implements CommandExecutor {
    private final ReportManager reportManager;

    public ReportCommand(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§6=== Sistema de Relatórios ===");
            player.sendMessage("§7Status: " + (reportManager.isEnabled() ? "§aAtivado" : "§cDesativado"));
            player.sendMessage("§7");
            player.sendMessage("§eUso: /report <jogador> <tipo> <motivo>");
            player.sendMessage("§7Tipos: CHEATING, BEHAVIOR, SPAM, OTHER");
            return true;
        }

        if (args[0].equalsIgnoreCase("list") && player.hasPermission("artixduels.admin")) {
            List<Report> pending = reportManager.getPendingReports();
            player.sendMessage("§6=== Relatórios Pendentes ===");
            if (pending.isEmpty()) {
                player.sendMessage("§7Nenhum relatório pendente.");
            } else {
                for (Report report : pending) {
                    player.sendMessage("§e" + report.getId() + " §7- §c" + report.getReportedName() + 
                                      " §7- §e" + report.getType().getDisplayName());
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("review") && player.hasPermission("artixduels.admin")) {
            if (args.length < 3) {
                player.sendMessage("§cUso: /report review <id> <accept|reject> [notas]");
                return true;
            }
            
            String reportId = args[1];
            String action = args[2].toLowerCase();
            String notes = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : "";
            
            Report report = reportManager.getReport(reportId);
            if (report == null) {
                player.sendMessage("§cRelatório não encontrado!");
                return true;
            }
            
            Report.ReportStatus status = action.equals("accept") ? 
                Report.ReportStatus.ACCEPTED : Report.ReportStatus.REJECTED;
            
            reportManager.reviewReport(reportId, player.getUniqueId().toString(), status, notes);
            player.sendMessage("§aRelatório " + (action.equals("accept") ? "aceito" : "rejeitado") + "!");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§cUso: /report <jogador> <tipo> <motivo>");
            return true;
        }

        Player reported = Bukkit.getPlayer(args[0]);
        if (reported == null || !reported.isOnline()) {
            player.sendMessage("§cJogador não encontrado ou offline!");
            return true;
        }

        if (reported.equals(player)) {
            player.sendMessage("§cVocê não pode se reportar!");
            return true;
        }

        String typeStr = args[1].toUpperCase();
        Report.ReportType type;
        try {
            type = Report.ReportType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cTipo inválido! Use: CHEATING, BEHAVIOR, SPAM, OTHER");
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        
        if (reportManager.createReport(player, reported, type, reason)) {
            player.sendMessage("§aRelatório enviado com sucesso!");
        }

        return true;
    }
}

