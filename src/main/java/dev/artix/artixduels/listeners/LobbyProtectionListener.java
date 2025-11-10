package dev.artix.artixduels.listeners;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.DuelManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LobbyProtectionListener implements Listener {
    private ArtixDuels plugin;
    private DuelManager duelManager;

    public LobbyProtectionListener(ArtixDuels plugin, DuelManager duelManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        
        if (duelManager.isInDuel(player)) {
            return;
        }

        Location lobbySpawn = plugin.getLobbySpawn();
        if (lobbySpawn == null) {
            return;
        }

        Location playerLoc = player.getLocation();
        if (playerLoc.getWorld() != lobbySpawn.getWorld()) {
            return;
        }

        double distance = playerLoc.distance(lobbySpawn);
        if (distance <= 100) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        
        if (duelManager.isInDuel(player) || duelManager.isInDuel(damager)) {
            return;
        }

        Location lobbySpawn = plugin.getLobbySpawn();
        if (lobbySpawn == null) {
            return;
        }

        Location playerLoc = player.getLocation();
        Location damagerLoc = damager.getLocation();
        
        if (playerLoc.getWorld() != lobbySpawn.getWorld() || damagerLoc.getWorld() != lobbySpawn.getWorld()) {
            return;
        }

        double playerDistance = playerLoc.distance(lobbySpawn);
        double damagerDistance = damagerLoc.distance(lobbySpawn);
        
        if (playerDistance <= 100 || damagerDistance <= 100) {
            event.setCancelled(true);
        }
    }
}

