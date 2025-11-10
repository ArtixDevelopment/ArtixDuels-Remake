package dev.artix.artixduels.gui;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.MessageManager;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DuelModeSelectionGUI implements Listener {
    private DuelManager duelManager;
    private KitManager kitManager;
    private MessageManager messageManager;
    private Map<UUID, String> pendingChallenges;

    public DuelModeSelectionGUI(ArtixDuels plugin, DuelManager duelManager, KitManager kitManager, ArenaManager arenaManager, MessageManager messageManager) {
        this.duelManager = duelManager;
        this.kitManager = kitManager;
        this.messageManager = messageManager;
        this.pendingChallenges = new HashMap<>();
    }

    public void openModeSelectionMenu(Player challenger, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            challenger.sendMessage(messageManager.getMessage("error.player-not-found"));
            return;
        }

        if (target.equals(challenger)) {
            challenger.sendMessage(messageManager.getMessage("error.cannot-challenge-self"));
            return;
        }

        pendingChallenges.put(challenger.getUniqueId(), targetName);

        Inventory gui = Bukkit.createInventory(null, 54, "&6&lSelecione o Modo de Duelo");
        
        int slot = 0;
        for (DuelMode mode : DuelMode.values()) {
            if (slot >= 45) break;
            
            ItemStack modeItem = createModeItem(mode);
            gui.setItem(slot, modeItem);
            slot++;
        }
        
        ItemStack closeItem = createMenuItem(Material.BARRIER, messageManager.getMessageNoPrefix("gui.close"), 
            "", "&7Clique para fechar");
        gui.setItem(49, closeItem);
        
        challenger.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.contains("Selecione o Modo de Duelo")) {
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
            pendingChallenges.remove(player.getUniqueId());
            return;
        }
        
        String targetName = pendingChallenges.get(player.getUniqueId());
        if (targetName == null) {
            player.closeInventory();
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(messageManager.getMessage("error.player-not-found"));
            player.closeInventory();
            pendingChallenges.remove(player.getUniqueId());
            return;
        }
        
        DuelMode selectedMode = getModeFromDisplayName(displayName);
        if (selectedMode == null) {
            return;
        }
        
        player.closeInventory();
        pendingChallenges.remove(player.getUniqueId());
        
        String defaultKit = getDefaultKit();
        if (defaultKit == null) {
            player.sendMessage(messageManager.getMessage("error.kit-not-found"));
            return;
        }
        
        duelManager.sendDuelRequest(player, target, defaultKit, null, selectedMode);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            String title = event.getView().getTitle();
            
            if (title.contains("Selecione o Modo de Duelo")) {
                pendingChallenges.remove(player.getUniqueId());
            }
        }
    }

    private ItemStack createModeItem(DuelMode mode) {
        Material material = getMaterialForMode(mode);
        String displayName = "&6&l" + mode.getDisplayName();
        
        List<String> lore = new ArrayList<>();
        lore.add("&7Clique para desafiar com");
        lore.add("&7o modo &e" + mode.getDisplayName());
        
        return createMenuItem(material, displayName, lore.toArray(new String[0]));
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

    private DuelMode getModeFromDisplayName(String displayName) {
        for (DuelMode mode : DuelMode.values()) {
            if (displayName.contains(mode.getDisplayName())) {
                return mode;
            }
        }
        return null;
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

    private String getDefaultKit() {
        if (kitManager.getKits().isEmpty()) {
            return null;
        }
        return kitManager.getKits().keySet().iterator().next();
    }
}

