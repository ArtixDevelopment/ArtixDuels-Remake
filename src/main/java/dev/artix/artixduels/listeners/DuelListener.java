package dev.artix.artixduels.listeners;

import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.models.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class DuelListener implements Listener {
    private DuelManager duelManager;

    public DuelListener(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Duel duel = duelManager.getPlayerDuel(player);
        
        if (duel == null || duel.getState() != Duel.DuelState.FIGHTING) {
            return;
        }

        Player killer = player.getKiller();
        if (killer != null && duel.isPlayer(killer.getUniqueId())) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            duelManager.endDuel(killer.getUniqueId(), player.getUniqueId(), false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Duel duel = duelManager.getPlayerDuel(player);
        
        if (duel != null) {
            UUID opponentId = duel.getOpponent(player.getUniqueId());
            if (opponentId != null) {
                Player opponent = org.bukkit.Bukkit.getPlayer(opponentId);
                if (opponent != null) {
                    duelManager.endDuel(opponentId, player.getUniqueId(), false);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Duel duel = duelManager.getPlayerDuel(player);
        
        if (duel != null && duel.getState() == Duel.DuelState.COUNTDOWN) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Duel duel = duelManager.getPlayerDuel(player);
        
        if (duel != null && duel.getState() == Duel.DuelState.COUNTDOWN) {
            event.setCancelled(true);
        }
    }
}

