package dev.artix.artixduels.listeners;

import dev.artix.artixduels.managers.TablistManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TablistListener implements Listener {
    private TablistManager tablistManager;

    public TablistListener(TablistManager tablistManager) {
        this.tablistManager = tablistManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (tablistManager != null && tablistManager.isEnabled()) {
            tablistManager.updateTablist(event.getPlayer());
            tablistManager.updateAllTablists();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (tablistManager != null) {
            tablistManager.removePlayerTablist(event.getPlayer());
        }
    }
}

