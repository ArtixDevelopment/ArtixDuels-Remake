package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.NotificationManager;
import dev.artix.artixduels.models.NotificationPreferences;
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
 * GUI para configurar preferências de notificação.
 */
public class NotificationSettingsGUI implements Listener {
    private final NotificationManager notificationManager;

    public NotificationSettingsGUI(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    /**
     * Abre o menu de configurações de notificação.
     */
    public void openSettings(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lNOTIFICAÇÕES");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 27, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        NotificationPreferences prefs = notificationManager.getPreferences(player.getUniqueId());

        // Som
        ItemStack soundItem = createToggleItem(Material.NOTE_BLOCK, "&6&lSom",
            prefs.isSoundEnabled(), "&7Ative ou desative notificações", "&7com som");
        gui.setItem(10, soundItem);

        // Partículas
        ItemStack particleItem = createToggleItem(Material.FIREWORK, "&6&lPartículas",
            prefs.isParticlesEnabled(), "&7Ative ou desative notificações", "&7com partículas");
        gui.setItem(12, particleItem);

        // Título
        ItemStack titleItem = createToggleItem(Material.SIGN, "&6&lTítulo",
            prefs.isTitleEnabled(), "&7Ative ou desative notificações", "&7de título (title/subtitle)");
        gui.setItem(14, titleItem);

        // Actionbar
        ItemStack actionbarItem = createToggleItem(Material.PAPER, "&6&lActionbar",
            prefs.isActionbarEnabled(), "&7Ative ou desative notificações", "&7na actionbar");
        gui.setItem(16, actionbarItem);

        // Chat
        ItemStack chatItem = createToggleItem(Material.BOOK, "&6&lChat",
            prefs.isChatEnabled(), "&7Ative ou desative notificações", "&7no chat");
        gui.setItem(22, chatItem);

        // Fechar
        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(18, closeItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.contains("NOTIFICAÇÕES")) {
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
            return;
        }

        NotificationPreferences prefs = notificationManager.getPreferences(player.getUniqueId());

        if (displayName.contains("Som")) {
            prefs.setSoundEnabled(!prefs.isSoundEnabled());
            player.sendMessage("§7Som: " + (prefs.isSoundEnabled() ? "§aAtivado" : "§cDesativado"));
        } else if (displayName.contains("Partículas")) {
            prefs.setParticlesEnabled(!prefs.isParticlesEnabled());
            player.sendMessage("§7Partículas: " + (prefs.isParticlesEnabled() ? "§aAtivado" : "§cDesativado"));
        } else if (displayName.contains("Título")) {
            prefs.setTitleEnabled(!prefs.isTitleEnabled());
            player.sendMessage("§7Título: " + (prefs.isTitleEnabled() ? "§aAtivado" : "§cDesativado"));
        } else if (displayName.contains("Actionbar")) {
            prefs.setActionbarEnabled(!prefs.isActionbarEnabled());
            player.sendMessage("§7Actionbar: " + (prefs.isActionbarEnabled() ? "§aAtivado" : "§cDesativado"));
        } else if (displayName.contains("Chat")) {
            prefs.setChatEnabled(!prefs.isChatEnabled());
            player.sendMessage("§7Chat: " + (prefs.isChatEnabled() ? "§aAtivado" : "§cDesativado"));
        }

        notificationManager.setPreferences(player.getUniqueId(), prefs);
        openSettings(player);
    }

    private ItemStack createToggleItem(Material material, String name, boolean enabled, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> loreList = new ArrayList<>();
        loreList.add("&8&m                              ");
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        loreList.add("&8&m                              ");
        loreList.add(enabled ? "&a&lATIVADO" : "&c&lDESATIVADO");
        loreList.add("&7Clique para " + (enabled ? "desativar" : "ativar"));
        loreList.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : loreList) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }
}

