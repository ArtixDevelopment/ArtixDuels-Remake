package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.MessageManager;
import dev.artix.artixduels.managers.PlayerScoreboardPreferences;
import dev.artix.artixduels.models.DuelMode;
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

public class ScoreboardModeSelectionGUI implements Listener {
    private PlayerScoreboardPreferences preferences;
    private MessageManager messageManager;
    private dev.artix.artixduels.managers.ScoreboardManager scoreboardManager;

    public ScoreboardModeSelectionGUI(PlayerScoreboardPreferences preferences, MessageManager messageManager, dev.artix.artixduels.managers.ScoreboardManager scoreboardManager) {
        this.preferences = preferences;
        this.messageManager = messageManager;
        this.scoreboardManager = scoreboardManager;
    }

    public void openModeSelectionMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "&6&lSelecionar Modo do Scoreboard");
        
        dev.artix.artixduels.models.DuelMode activeMode = preferences.getPlayerActiveMode(player.getUniqueId());
        
        int slot = 0;
        for (DuelMode mode : DuelMode.values()) {
            if (slot >= 45) break;
            
            boolean isActive = mode.equals(activeMode);
            Material material = getMaterialForMode(mode);
            String displayName = (isActive ? "&a&l✓ " : "&7") + mode.getDisplayName();
            
            List<String> lore = new ArrayList<>();
            lore.add("&7Clique para selecionar");
            lore.add("&7este modo no scoreboard");
            if (isActive) {
                lore.add(" ");
                lore.add("&a✓ Modo ativo no scoreboard");
            }
            
            ItemStack modeItem = createMenuItem(material, displayName, lore.toArray(new String[0]));
            gui.setItem(slot, modeItem);
            slot++;
        }
        
        ItemStack closeItem = createMenuItem(Material.BARRIER, messageManager.getMessageNoPrefix("gui.close"), 
            "", "&7Clique para fechar");
        gui.setItem(49, closeItem);
        
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.contains("Selecionar Modos do Scoreboard")) {
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
        
        if (displayName.contains("Fechar")) {
            player.closeInventory();
            return;
        }
        
        // Selecionar modo específico
        DuelMode selectedMode = getModeFromDisplayName(displayName);
        if (selectedMode != null) {
            preferences.setPlayerActiveMode(player.getUniqueId(), selectedMode);
            java.util.Map<String, String> placeholders = createPlaceholderMap("mode", selectedMode.getDisplayName());
            player.sendMessage(messageManager.getMessage("scoreboard.mode-selected", placeholders));
            updatePlayerScoreboard(player);
            openModeSelectionMenu(player);
        }
    }

    private DuelMode getModeFromDisplayName(String displayName) {
        // Remove os símbolos de check
        String cleanName = displayName.replace("✓ ", "").trim();
        
        for (DuelMode mode : DuelMode.values()) {
            if (cleanName.contains(mode.getDisplayName()) || cleanName.contains(mode.getName())) {
                return mode;
            }
        }
        return null;
    }

    private Material getMaterialForMode(DuelMode mode) {
        switch (mode) {
            case BEDFIGHT:
                return Material.BED;
            case STICKFIGHT:
                return Material.STICK;
            case SOUP:
            case SOUPRECRAFT:
                return Material.MUSHROOM_SOUP;
            case GLADIATOR:
                return Material.IRON_SWORD;
            case FASTOB:
                return Material.DIAMOND_SWORD;
            case BOXING:
                return Material.LEATHER_HELMET;
            case FIREBALLFIGHT:
                return Material.FIREBALL;
            case SUMO:
                return Material.SLIME_BALL;
            case BATTLERUSH:
                return Material.BLAZE_POWDER;
            case TNTSUMO:
                return Material.TNT;
            default:
                return Material.BOOK;
        }
    }

    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            if (!line.isEmpty()) {
                loreList.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }

    private void updatePlayerScoreboard(org.bukkit.entity.Player player) {
        if (scoreboardManager != null) {
            // Verificar se o jogador está em um duelo
            org.bukkit.entity.Player onlinePlayer = org.bukkit.Bukkit.getPlayer(player.getUniqueId());
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                // Atualizar scoreboard do lobby
                scoreboardManager.removeScoreboard(onlinePlayer);
                scoreboardManager.createLobbyScoreboard(onlinePlayer);
            }
        }
    }

    private java.util.Map<String, String> createPlaceholderMap(String... pairs) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (i + 1 < pairs.length) {
                map.put(pairs[i], pairs[i + 1]);
            }
        }
        return map;
    }
}

