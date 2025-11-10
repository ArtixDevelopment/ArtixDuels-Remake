package dev.artix.artixduels.listeners;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.TablistManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TablistListener implements Listener {
    private final TablistManager tablistManager;
    private final ArtixDuels plugin;

    public TablistListener(TablistManager tablistManager, ArtixDuels plugin) {
        this.tablistManager = tablistManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (tablistManager != null && tablistManager.isEnabled()) {
            // Aguardar alguns ticks para garantir que o jogador está totalmente conectado
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                tablistManager.updateTablist(event.getPlayer());
                tablistManager.updateAllTablists();
            }, 10L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (tablistManager != null) {
            tablistManager.removePlayerTablist(event.getPlayer());
        }
    }
}
