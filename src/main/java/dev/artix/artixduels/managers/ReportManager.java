package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de relatórios.
 */
public class ReportManager {
    private final ArtixDuels plugin;
    private boolean enabled;
    private boolean autoPunishment;
    private int autoPunishmentThreshold;
    private Map<String, Report> reports;
    private Map<UUID, List<String>> playerReports;
    private Map<UUID, Long> reportCooldowns;

    public ReportManager(ArtixDuels plugin) {
        this.plugin = plugin;
        this.reports = new HashMap<>();
        this.playerReports = new HashMap<>();
        this.reportCooldowns = new HashMap<>();
        
        loadConfig();
        loadReports();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("reports")) {
            config.set("reports.enabled", true);
            config.set("reports.auto-punishment", false);
            config.set("reports.auto-punishment-threshold", 5);
            config.set("reports.cooldown-seconds", 300);
            
            try {
                config.save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                plugin.getLogger().warning("Erro ao salvar config: " + e.getMessage());
            }
        }
        
        enabled = config.getBoolean("reports.enabled", true);
        autoPunishment = config.getBoolean("reports.auto-punishment", false);
        autoPunishmentThreshold = config.getInt("reports.auto-punishment-threshold", 5);
    }

    public void reloadConfig() {
        loadConfig();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        saveConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setAutoPunishment(boolean enabled) {
        this.autoPunishment = enabled;
        saveConfig();
    }

    public void setAutoPunishmentThreshold(int threshold) {
        this.autoPunishmentThreshold = threshold;
        saveConfig();
    }

    public boolean createReport(Player reporter, Player reported, Report.ReportType type, String reason) {
        if (!enabled) {
            reporter.sendMessage("§cO sistema de relatórios está desativado!");
            return false;
        }
        
        UUID reporterId = reporter.getUniqueId();
        UUID reportedId = reported.getUniqueId();
        
        if (reporterId.equals(reportedId)) {
            reporter.sendMessage("§cVocê não pode se reportar!");
            return false;
        }
        
        if (isOnCooldown(reporterId)) {
            long cooldown = getCooldownTime(reporterId);
            reporter.sendMessage("§cAguarde " + (cooldown / 1000) + " segundos antes de reportar novamente!");
            return false;
        }
        
        String reportId = UUID.randomUUID().toString().substring(0, 8);
        Report report = new Report(reportId, reporterId, reporter.getName(), reportedId, reported.getName(), type, reason);
        reports.put(reportId, report);
        
        playerReports.computeIfAbsent(reportedId, k -> new ArrayList<>()).add(reportId);
        setCooldown(reporterId);
        
        reporter.sendMessage("§aRelatório criado com sucesso! ID: §e" + reportId);
        
        alertAdmins(report);
        checkAutoPunishment(reportedId);
        saveReports();
        
        return true;
    }

    public void reviewReport(String reportId, String reviewerId, Report.ReportStatus status, String notes) {
        Report report = reports.get(reportId);
        if (report == null) return;
        
        report.setStatus(status);
        report.setReviewerId(reviewerId);
        report.setReviewNotes(notes);
        
        if (status == Report.ReportStatus.ACCEPTED && autoPunishment) {
            Player reported = Bukkit.getPlayer(report.getReportedId());
            if (reported != null && reported.isOnline()) {
                punishPlayer(reported, report.getType());
            }
        }
        
        saveReports();
    }

    private void checkAutoPunishment(UUID reportedId) {
        if (!autoPunishment) return;
        
        List<String> playerReportIds = playerReports.get(reportedId);
        if (playerReportIds == null) return;
        
        int acceptedReports = 0;
        for (String reportId : playerReportIds) {
            Report report = reports.get(reportId);
            if (report != null && report.getStatus() == Report.ReportStatus.ACCEPTED) {
                acceptedReports++;
            }
        }
        
        if (acceptedReports >= autoPunishmentThreshold) {
            Player reported = Bukkit.getPlayer(reportedId);
            if (reported != null && reported.isOnline()) {
                punishPlayer(reported, Report.ReportType.CHEATING);
            }
        }
    }

    private void punishPlayer(Player player, Report.ReportType type) {
        String command = plugin.getConfig().getString("reports.punishment-command", "ban %player% 7d Punição automática");
        command = command.replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private void alertAdmins(Report report) {
        String message = "§c§l[RELATÓRIO] §7Novo relatório: §e" + report.getId() + 
                        " §7| Reportado: §c" + report.getReportedName() + 
                        " §7| Tipo: §e" + report.getType().getDisplayName();
        
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("artixduels.admin")) {
                admin.sendMessage(message);
            }
        }
    }

    public List<Report> getReports(UUID playerId) {
        List<String> reportIds = playerReports.get(playerId);
        if (reportIds == null) return new ArrayList<>();
        
        List<Report> playerReports = new ArrayList<>();
        for (String reportId : reportIds) {
            Report report = reports.get(reportId);
            if (report != null) {
                playerReports.add(report);
            }
        }
        return playerReports;
    }

    public List<Report> getPendingReports() {
        List<Report> pending = new ArrayList<>();
        for (Report report : reports.values()) {
            if (report.getStatus() == Report.ReportStatus.PENDING) {
                pending.add(report);
            }
        }
        return pending;
    }

    public Report getReport(String reportId) {
        return reports.get(reportId);
    }

    private boolean isOnCooldown(UUID playerId) {
        Long cooldown = reportCooldowns.get(playerId);
        if (cooldown == null) return false;
        return System.currentTimeMillis() < cooldown;
    }

    private long getCooldownTime(UUID playerId) {
        Long cooldown = reportCooldowns.get(playerId);
        if (cooldown == null) return 0;
        return Math.max(0, cooldown - System.currentTimeMillis());
    }

    private void setCooldown(UUID playerId) {
        int cooldownSeconds = plugin.getConfig().getInt("reports.cooldown-seconds", 300);
        reportCooldowns.put(playerId, System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }

    private void loadReports() {
        File reportsFile = new File(plugin.getDataFolder(), "reports.yml");
        if (!reportsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(reportsFile);
        if (config.contains("reports")) {
            for (String reportId : config.getConfigurationSection("reports").getKeys(false)) {
                String path = "reports." + reportId;
                String reporterIdStr = config.getString(path + ".reporter-id");
                String reporterName = config.getString(path + ".reporter-name");
                String reportedIdStr = config.getString(path + ".reported-id");
                String reportedName = config.getString(path + ".reported-name");
                String typeStr = config.getString(path + ".type");
                String reason = config.getString(path + ".reason");
                String statusStr = config.getString(path + ".status", "PENDING");
                String reviewerId = config.getString(path + ".reviewer-id");
                String reviewNotes = config.getString(path + ".review-notes");
                
                try {
                    UUID reporterId = UUID.fromString(reporterIdStr);
                    UUID reportedId = UUID.fromString(reportedIdStr);
                    Report.ReportType type = Report.ReportType.valueOf(typeStr);
                    Report.ReportStatus status = Report.ReportStatus.valueOf(statusStr);
                    
                    Report report = new Report(reportId, reporterId, reporterName, reportedId, reportedName, type, reason);
                    report.setStatus(status);
                    report.setReviewerId(reviewerId);
                    report.setReviewNotes(reviewNotes);
                    reports.put(reportId, report);
                    
                    playerReports.computeIfAbsent(reportedId, k -> new ArrayList<>()).add(reportId);
                } catch (Exception e) {
                    plugin.getLogger().warning("Erro ao carregar relatório " + reportId + ": " + e.getMessage());
                }
            }
        }
    }

    private void saveReports() {
        File reportsFile = new File(plugin.getDataFolder(), "reports.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(reportsFile);

        for (Map.Entry<String, Report> entry : reports.entrySet()) {
            String path = "reports." + entry.getKey();
            Report report = entry.getValue();
            config.set(path + ".reporter-id", report.getReporterId().toString());
            config.set(path + ".reporter-name", report.getReporterName());
            config.set(path + ".reported-id", report.getReportedId().toString());
            config.set(path + ".reported-name", report.getReportedName());
            config.set(path + ".type", report.getType().toString());
            config.set(path + ".reason", report.getReason());
            config.set(path + ".status", report.getStatus().toString());
            config.set(path + ".reviewer-id", report.getReviewerId());
            config.set(path + ".review-notes", report.getReviewNotes());
            config.set(path + ".timestamp", report.getTimestamp());
        }

        try {
            config.save(reportsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar relatórios: " + e.getMessage());
        }
    }

    private void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("reports.enabled", enabled);
        config.set("reports.auto-punishment", autoPunishment);
        config.set("reports.auto-punishment-threshold", autoPunishmentThreshold);
        
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao salvar config: " + e.getMessage());
        }
    }
}

