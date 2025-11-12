package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.SuspiciousActivity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de anti-cheat integrado.
 */
public class AntiCheatManager implements Listener {
    private final ArtixDuels plugin;
    private boolean enabled;
    private boolean autoClickDetection;
    private boolean reachDetection;
    private boolean movementDetection;
    private boolean loggingEnabled;
    private boolean alertsEnabled;
    
    private Map<UUID, List<Long>> clickTimestamps;
    private Map<UUID, Integer> violationCounts;
    private Map<UUID, Location> lastLocations;
    private Map<UUID, Long> lastMoveTime;
    private List<SuspiciousActivity> suspiciousActivities;
    
    private int maxCPS;
    private double maxReach;
    private double maxSpeed;

    public AntiCheatManager(ArtixDuels plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.clickTimestamps = new HashMap<>();
        this.violationCounts = new HashMap<>();
        this.lastLocations = new HashMap<>();
        this.lastMoveTime = new HashMap<>();
        this.suspiciousActivities = new ArrayList<>();
        
        loadConfig(config);
        
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            startCleanupTask();
        }
    }

    private void loadConfig(FileConfiguration config) {
        if (!config.contains("anticheat")) {
            config.set("anticheat.enabled", false);
            config.set("anticheat.auto-click-detection", true);
            config.set("anticheat.reach-detection", true);
            config.set("anticheat.movement-detection", true);
            config.set("anticheat.logging-enabled", true);
            config.set("anticheat.alerts-enabled", true);
            config.set("anticheat.max-cps", 15);
            config.set("anticheat.max-reach", 3.5);
            config.set("anticheat.max-speed", 0.5);
            
            try {
                config.save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                plugin.getLogger().warning("Erro ao salvar config: " + e.getMessage());
            }
        }
        
        enabled = config.getBoolean("anticheat.enabled", false);
        autoClickDetection = config.getBoolean("anticheat.auto-click-detection", true);
        reachDetection = config.getBoolean("anticheat.reach-detection", true);
        movementDetection = config.getBoolean("anticheat.movement-detection", true);
        loggingEnabled = config.getBoolean("anticheat.logging-enabled", true);
        alertsEnabled = config.getBoolean("anticheat.alerts-enabled", true);
        maxCPS = config.getInt("anticheat.max-cps", 15);
        maxReach = config.getDouble("anticheat.max-reach", 3.5);
        maxSpeed = config.getDouble("anticheat.max-speed", 0.5);
    }

    public void reloadConfig(FileConfiguration config) {
        loadConfig(config);
        
        if (enabled) {
            try {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
            } catch (Exception e) {
                // Já registrado
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        saveConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setAutoClickDetection(boolean enabled) {
        this.autoClickDetection = enabled;
        saveConfig();
    }

    public void setReachDetection(boolean enabled) {
        this.reachDetection = enabled;
        saveConfig();
    }

    public void setMovementDetection(boolean enabled) {
        this.movementDetection = enabled;
        saveConfig();
    }

    public boolean isAutoClickDetectionEnabled() {
        return autoClickDetection;
    }

    public boolean isReachDetectionEnabled() {
        return reachDetection;
    }

    public boolean isMovementDetectionEnabled() {
        return movementDetection;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!enabled || !autoClickDetection) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        if (!isInDuel(player)) return;
        
        long now = System.currentTimeMillis();
        List<Long> clicks = clickTimestamps.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        clicks.add(now);
        
        clicks.removeIf(timestamp -> now - timestamp > 1000);
        
        int cps = clicks.size();
        if (cps > maxCPS) {
            recordViolation(player, SuspiciousActivity.ActivityType.AUTO_CLICK, 
                "CPS: " + cps + " (Máximo: " + maxCPS + ")");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!enabled || !reachDetection) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        if (!isInDuel(attacker) || !isInDuel(victim)) return;
        
        double distance = attacker.getLocation().distance(victim.getLocation());
        double reach = distance - 0.3;
        
        if (reach > maxReach) {
            recordViolation(attacker, SuspiciousActivity.ActivityType.REACH, 
                "Reach: " + String.format("%.2f", reach) + " (Máximo: " + maxReach + ")");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled || !movementDetection) return;
        
        Player player = event.getPlayer();
        if (!isInDuel(player)) return;
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (from == null || to == null) return;
        
        double distance = from.distance(to);
        long now = System.currentTimeMillis();
        Long lastMove = lastMoveTime.get(player.getUniqueId());
        
        if (lastMove != null) {
            long timeDiff = now - lastMove;
            if (timeDiff > 0) {
                double speed = distance / (timeDiff / 1000.0);
                
                if (speed > maxSpeed && !player.isFlying() && player.getLocation().getBlock().getRelative(0, -1, 0).getType().isSolid()) {
                    recordViolation(player, SuspiciousActivity.ActivityType.SUSPICIOUS_MOVEMENT, 
                        "Velocidade: " + String.format("%.2f", speed) + " (Máximo: " + maxSpeed + ")");
                }
            }
        }
        
        lastLocations.put(player.getUniqueId(), to);
        lastMoveTime.put(player.getUniqueId(), now);
    }

    private boolean isInDuel(Player player) {
        return plugin.getDuelManager().getPlayerDuel(player) != null;
    }

    private void recordViolation(Player player, SuspiciousActivity.ActivityType type, String description) {
        UUID playerId = player.getUniqueId();
        int violations = violationCounts.getOrDefault(playerId, 0) + 1;
        violationCounts.put(playerId, violations);
        
        SuspiciousActivity activity = new SuspiciousActivity(playerId, player.getName(), type, description);
        activity.setViolationLevel(violations);
        suspiciousActivities.add(activity);
        
        if (suspiciousActivities.size() > 1000) {
            suspiciousActivities.remove(0);
        }
        
        if (loggingEnabled) {
            plugin.getLogger().warning("[AntiCheat] " + player.getName() + " - " + type.getDisplayName() + ": " + description);
        }
        
        if (alertsEnabled && violations >= 3) {
            alertAdmins(player, type, description, violations);
        }
    }

    private void alertAdmins(Player player, SuspiciousActivity.ActivityType type, String description, int violations) {
        String message = "§c§l[ANTI-CHEAT] §c" + player.getName() + " §7- " + type.getDisplayName() + 
                        " §7(Violações: §c" + violations + "§7)";
        
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("artixduels.admin")) {
                admin.sendMessage(message);
            }
        }
    }

    public List<SuspiciousActivity> getSuspiciousActivities(UUID playerId) {
        List<SuspiciousActivity> activities = new ArrayList<>();
        for (SuspiciousActivity activity : suspiciousActivities) {
            if (activity.getPlayerId().equals(playerId)) {
                activities.add(activity);
            }
        }
        return activities;
    }

    public int getViolationCount(UUID playerId) {
        return violationCounts.getOrDefault(playerId, 0);
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            clickTimestamps.values().forEach(clicks -> clicks.removeIf(timestamp -> now - timestamp > 1000));
            
            if (suspiciousActivities.size() > 1000) {
                suspiciousActivities.subList(0, suspiciousActivities.size() - 1000).clear();
            }
        }, 0L, 20L * 60L);
    }

    private void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("anticheat.enabled", enabled);
        config.set("anticheat.auto-click-detection", autoClickDetection);
        config.set("anticheat.reach-detection", reachDetection);
        config.set("anticheat.movement-detection", movementDetection);
        config.set("anticheat.logging-enabled", loggingEnabled);
        config.set("anticheat.alerts-enabled", alertsEnabled);
        
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao salvar config: " + e.getMessage());
        }
    }
}

