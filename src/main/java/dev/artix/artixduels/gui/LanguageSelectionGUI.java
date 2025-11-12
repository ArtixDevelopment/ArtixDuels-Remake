package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.LanguageManager;
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
 * GUI para seleção de idioma.
 */
public class LanguageSelectionGUI implements Listener {
    private final LanguageManager languageManager;

    public LanguageSelectionGUI(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    /**
     * Abre o menu de seleção de idioma.
     */
    public void openLanguageMenu(Player player) {
        String title = languageManager.getMessageNoPrefix(player, "gui.language.title", new java.util.HashMap<>());
        if (title.isEmpty()) {
            title = ChatColor.translateAlternateColorCodes('&', "&6&lIDIOMA");
        }
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 27, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        String currentLang = languageManager.getPlayerLanguage(player.getUniqueId());

        // Português
        ItemStack ptItem = createLanguageItem(Material.BANNER, "&6&lPortuguês", "pt", currentLang.equals("pt"));
        gui.setItem(10, ptItem);

        // Inglês
        ItemStack enItem = createLanguageItem(Material.BANNER, "&b&lEnglish", "en", currentLang.equals("en"));
        gui.setItem(12, enItem);

        // Espanhol
        ItemStack esItem = createLanguageItem(Material.BANNER, "&e&lEspañol", "es", currentLang.equals("es"));
        gui.setItem(14, esItem);

        // Francês
        ItemStack frItem = createLanguageItem(Material.BANNER, "&d&lFrançais", "fr", currentLang.equals("fr"));
        gui.setItem(16, frItem);

        // Alemão
        ItemStack deItem = createLanguageItem(Material.BANNER, "&c&lDeutsch", "de", currentLang.equals("de"));
        gui.setItem(22, deItem);

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

        if (!title.contains("IDIOMA") && !title.contains("LANGUAGE") && !title.contains("IDIOMA")) {
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

        if (displayName.contains("FECHAR") || displayName.contains("CLOSE")) {
            player.closeInventory();
            return;
        }

        // Extrair código do idioma do lore
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (String line : lore) {
                if (line.contains("Code:")) {
                    String langCode = ChatColor.stripColor(line).replace("Code: ", "").trim();
                    languageManager.setPlayerLanguage(player.getUniqueId(), langCode);
                    languageManager.savePlayerLanguages();
                    
                    java.util.HashMap<String, String> placeholders = new java.util.HashMap<>();
                    placeholders.put("language", languageManager.getLanguageName(langCode));
                    String message = languageManager.getMessage(player, "messages.language.changed", placeholders);
                    if (message.isEmpty()) {
                        message = "&aIdioma alterado para: &e" + languageManager.getLanguageName(langCode);
                    }
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    player.closeInventory();
                    return;
                }
            }
        }
    }

    private ItemStack createLanguageItem(Material material, String name, String langCode, boolean isSelected) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Código: &b" + langCode.toUpperCase());
        lore.add("&7Nome: &e" + languageManager.getLanguageName(langCode));
        lore.add("&8&m                              ");
        if (isSelected) {
            lore.add("&a&l✓ SELECIONADO");
        } else {
            lore.add("&7Clique para selecionar");
        }
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

