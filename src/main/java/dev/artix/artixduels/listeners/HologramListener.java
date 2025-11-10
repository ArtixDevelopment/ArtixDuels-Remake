package dev.artix.artixduels.listeners;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.HologramSystemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class HologramListener implements Listener {
    private ArtixDuels plugin;

    public HologramListener(ArtixDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof org.bukkit.entity.ArmorStand)) {
            return;
        }

        Player player = event.getPlayer();
        org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) event.getRightClicked();

        HologramSystemManager hologramManager = plugin.getHologramSystemManager();
        if (hologramManager == null) {
            return;
        }

        String hologramName = hologramManager.getHologramAt(stand);
        if (hologramName != null) {
            event.setCancelled(true);
            hologramManager.nextPage(player, hologramName);
        }
    }
}

