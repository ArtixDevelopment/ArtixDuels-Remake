package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.AchievementManager;
import dev.artix.artixduels.models.Achievement;
import dev.artix.artixduels.models.AchievementProgress;
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
 * GUI para visualizar conquistas.
 */
public class AchievementGUI implements Listener {
    private final AchievementManager achievementManager;

    public AchievementGUI(AchievementManager achievementManager) {
        this.achievementManager = achievementManager;
    }

    /**
     * Abre o menu principal de conquistas.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lCONQUISTAS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        Set<String> categories = achievementManager.getCategories();
        int slot = 0;
        for (String category : categories) {
            if (slot >= 45) break;

            ItemStack categoryItem = createCategoryItem(category, player.getUniqueId());
            gui.setItem(slot, categoryItem);
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

    /**
     * Abre o menu de conquistas de uma categoria.
     */
    public void openCategoryMenu(Player player, String category) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&l" + category.toUpperCase());
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        List<Achievement> achievements = achievementManager.getAchievementsByCategory(category);
        Map<String, AchievementProgress> progress = achievementManager.getPlayerProgress(player.getUniqueId());
        
        int slot = 0;
        for (Achievement achievement : achievements) {
            if (slot >= 45) break;

            AchievementProgress achievementProgress = progress.get(achievement.getId());
            ItemStack achievementItem = createAchievementItem(achievement, achievementProgress);
            gui.setItem(slot, achievementItem);
            slot++;
        }

        // Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.contains("CONQUISTAS") && !title.contains("VITÓRIAS") && !title.contains("ELO") 
            && !title.contains("SEQUÊNCIAS")) {
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

        if (title.contains("CONQUISTAS") && !title.contains("VITÓRIAS") && !title.contains("ELO") 
            && !title.contains("SEQUÊNCIAS")) {
            if (displayName.contains("FECHAR")) {
                player.closeInventory();
            } else {
                String category = getCategoryFromLore(meta.getLore());
                if (category != null) {
                    openCategoryMenu(player, category);
                }
            }
        } else {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            }
        }
    }

    private String getCategoryFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("Categoria:")) {
                return ChatColor.stripColor(line).replace("Categoria: ", "").trim();
            }
        }
        return null;
    }

    private ItemStack createCategoryItem(String category, java.util.UUID playerId) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + category));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Categoria: " + category);
        
        List<Achievement> achievements = achievementManager.getAchievementsByCategory(category);
        Map<String, AchievementProgress> progress = achievementManager.getPlayerProgress(playerId);
        int unlocked = 0;
        for (Achievement achievement : achievements) {
            AchievementProgress achievementProgress = progress.get(achievement.getId());
            if (achievementProgress != null && achievementProgress.isUnlocked()) {
                unlocked++;
            }
        }
        
        lore.add("&7Progresso: &e" + unlocked + "/" + achievements.size());
        lore.add("&8&m                              ");
        lore.add("&7Clique para ver conquistas");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createAchievementItem(Achievement achievement, AchievementProgress progress) {
        Material material = Material.NETHER_STAR;
        try {
            material = Material.valueOf(achievement.getIcon());
        } catch (Exception e) {
            // Usar padrão
        }

        ItemStack item = new ItemStack(progress != null && progress.isUnlocked() ? material : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = achievement.getRarity().getDisplayName() + " " + achievement.getName();
        if (progress != null && progress.isUnlocked()) {
            displayName = "&a&l✓ " + displayName;
        } else {
            displayName = "&7&l✗ " + displayName;
        }
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7" + achievement.getDescription());
        lore.add("&8&m                              ");
        
        if (progress != null && progress.isUnlocked()) {
            lore.add("&a&lDESBLOQUEADO");
        } else {
            int currentProgress = progress != null ? progress.getProgress() : 0;
            double percentage = progress != null ? progress.getProgressPercentage(achievement) : 0.0;
            lore.add("&7Progresso: &e" + currentProgress + "/" + achievement.getTargetValue());
            lore.add("&7" + getProgressBar(percentage));
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

    private String getProgressBar(double percentage) {
        int bars = 20;
        int filled = (int) (bars * percentage / 100.0);
        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append("&7");
        for (int i = filled; i < bars; i++) {
            bar.append("█");
        }
        return bar.toString();
    }
}

