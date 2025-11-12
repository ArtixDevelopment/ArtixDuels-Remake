package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.TitleManager;
import dev.artix.artixduels.models.Title;
import dev.artix.artixduels.models.TitleProgress;
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
import java.util.Map;
import java.util.Set;

/**
 * GUI para gerenciar títulos.
 */
public class TitleGUI implements Listener {
    private final TitleManager titleManager;

    public TitleGUI(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    /**
     * Abre o menu de títulos.
     */
    public void openTitleMenu(Player player) {
        String menuTitle = ChatColor.translateAlternateColorCodes('&', "&6&lTÍTULOS");
        if (menuTitle.length() > 32) {
            menuTitle = menuTitle.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, menuTitle);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        Map<String, Title> titles = titleManager.getTitles();
        Set<String> unlocked = titleManager.getUnlockedTitles(player.getUniqueId());
        String activeTitle = titleManager.getActiveTitle(player.getUniqueId());
        Map<String, TitleProgress> progress = titleManager.getTitleProgress(player.getUniqueId());
        
        int slot = 0;
        for (Map.Entry<String, Title> entry : titles.entrySet()) {
            if (slot >= 45) break;

            Title title = entry.getValue();
            boolean isUnlocked = unlocked.contains(entry.getKey());
            boolean isActive = entry.getKey().equals(activeTitle);
            TitleProgress titleProgress = progress.get(entry.getKey());
            
            ItemStack titleItem = createTitleItem(title, isUnlocked, isActive, titleProgress);
            gui.setItem(slot, titleItem);
            slot++;
        }

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
        String menuTitle = event.getView().getTitle();

        if (!menuTitle.contains("TÍTULOS")) {
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
        } else {
            String titleId = getTitleIdFromLore(meta.getLore());
            if (titleId != null) {
                Set<String> unlocked = titleManager.getUnlockedTitles(player.getUniqueId());
                if (unlocked.contains(titleId)) {
                    String activeTitle = titleManager.getActiveTitle(player.getUniqueId());
                    if (titleId.equals(activeTitle)) {
                        titleManager.removeActiveTitle(player.getUniqueId());
                        player.sendMessage("§cTítulo desativado!");
                    } else {
                        titleManager.setActiveTitle(player.getUniqueId(), titleId);
                        Title title = titleManager.getTitles().get(titleId);
                        player.sendMessage("§aTítulo ativado: " + title.getDisplayName());
                    }
                    openTitleMenu(player);
                } else {
                    player.sendMessage("§cVocê ainda não desbloqueou este título!");
                }
            }
        }
    }

    private String getTitleIdFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("ID:")) {
                return ChatColor.stripColor(line).replace("ID: ", "").trim();
            }
        }
        return null;
    }

    private ItemStack createTitleItem(Title title, boolean isUnlocked, boolean isActive, TitleProgress progress) {
        Material material = Material.NAME_TAG;
        try {
            material = Material.valueOf(title.getBadgeIcon());
        } catch (Exception e) {
            // Usar padrão
        }

        ItemStack item = new ItemStack(isUnlocked ? material : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = title.getRarity().getDisplayName() + " " + title.getName();
        if (isActive) {
            displayName = "&a&l[ATIVO] " + displayName;
        } else if (isUnlocked) {
            displayName = "&a&l✓ " + displayName;
        } else {
            displayName = "&7&l✗ " + displayName;
        }
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7ID: " + title.getId());
        lore.add("&7" + title.getDescription());
        lore.add("&8&m                              ");
        
        if (isUnlocked) {
            lore.add("&a&lDESBLOQUEADO");
            if (isActive) {
                lore.add("&e&lATIVO");
                lore.add("&7Clique para desativar");
            } else {
                lore.add("&7Clique para ativar");
            }
        } else {
            lore.add("&c&lBLOQUEADO");
            if (progress != null && title.getRequirements() != null) {
                lore.add("&7Requisitos:");
                for (String requirement : title.getRequirements()) {
                    String[] parts = requirement.split(":");
                    if (parts.length == 2) {
                        String reqType = parts[0];
                        int reqValue = Integer.parseInt(parts[1]);
                        int currentValue = progress.getRequirementProgress(reqType);
                        String status = currentValue >= reqValue ? "&a" : "&c";
                        lore.add(" &7- " + status + reqType + ": " + currentValue + "/" + reqValue);
                    }
                }
            }
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

