package dev.artix.artixduels.listeners;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.gui.ScoreboardModeSelectionGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProfileItemListener implements Listener {
    private ScoreboardModeSelectionGUI scoreboardModeSelectionGUI;
    private static final int PROFILE_ITEM_SLOT = 4; // Slot 4 (quinta posição, índice 4)

    public ProfileItemListener(ArtixDuels plugin, ScoreboardModeSelectionGUI scoreboardModeSelectionGUI) {
        this.scoreboardModeSelectionGUI = scoreboardModeSelectionGUI;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveProfileItem(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String displayName = meta.getDisplayName();
        if (displayName.contains("Perfil") || displayName.contains("Profile")) {
            event.setCancelled(true);
            scoreboardModeSelectionGUI.openModeSelectionMenu(player);
        }
    }

    public void giveProfileItem(Player player) {
        ItemStack profileItem = createProfileItem();
        
        // Dar o item no slot 4 (quinta posição)
        player.getInventory().setItem(PROFILE_ITEM_SLOT, profileItem);
    }
    
    public static ItemStack createProfileItem() {
        ItemStack profileItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = profileItem.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lPerfil &7(Clique Direito)"));
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Configure seu perfil"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7e selecione o modo do scoreboard"));
        meta.setLore(lore);
        
        profileItem.setItemMeta(meta);
        return profileItem;
    }
}

