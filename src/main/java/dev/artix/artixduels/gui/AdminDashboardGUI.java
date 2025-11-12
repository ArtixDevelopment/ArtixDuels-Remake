package dev.artix.artixduels.gui;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.MetricsManager;
import dev.artix.artixduels.managers.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard administrativo.
 */
public class AdminDashboardGUI implements Listener {
    private final ArtixDuels plugin;
    private final MetricsManager metricsManager;
    private final DuelManager duelManager;
    private final StatsManager statsManager;

    public AdminDashboardGUI(ArtixDuels plugin, MetricsManager metricsManager, 
                            DuelManager duelManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.metricsManager = metricsManager;
        this.duelManager = duelManager;
        this.statsManager = statsManager;
    }

    public void openDashboard(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lDASHBOARD ADMIN");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        // Estatísticas do servidor
        gui.setItem(10, createStatItem(Material.DIAMOND_SWORD, "&aDuelos Ativos", 
            "&7" + duelManager.getActiveDuelsCount()));
        gui.setItem(11, createStatItem(Material.SIGN, "&eJogadores na Fila", 
            "&7" + duelManager.getMatchmakingQueueSize()));
        gui.setItem(12, createStatItem(Material.GOLD_INGOT, "&6Total de Duelos", 
            "&7" + metricsManager.getTotalDuels()));
        gui.setItem(13, createStatItem(Material.WATCH, "&bTempo Médio", 
            "&7" + String.format("%.1f", metricsManager.getAverageDuelDuration()) + "s"));
        gui.setItem(14, createStatItem(Material.WATCH, "&dHora de Pico", 
            "&7" + metricsManager.getPeakHour() + ":00"));

        // Fechar
        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.contains("DASHBOARD")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());

        if (displayName.contains("FECHAR")) {
            player.closeInventory();
        }
    }

    private ItemStack createStatItem(Material material, String name, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Valor: &e" + value);
        lore.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }
}

