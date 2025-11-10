package dev.artix.artixduels.gui;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.managers.MenuManager;
import dev.artix.artixduels.managers.MessageManager;
import dev.artix.artixduels.models.Arena;
import dev.artix.artixduels.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

public class ConfigGUI implements Listener {
    private ArtixDuels plugin;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private MessageManager messageManager;
    private MenuManager menuManager;
    private Map<Player, String> editingKits;
    private Map<Player, String> editingArenas;

    public ConfigGUI(ArtixDuels plugin, KitManager kitManager, ArenaManager arenaManager, MessageManager messageManager, MenuManager menuManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
        this.messageManager = messageManager;
        this.menuManager = menuManager;
        this.editingKits = new HashMap<>();
        this.editingArenas = new HashMap<>();
    }

    public void openMainMenu(Player player) {
        MenuManager.MenuData menuData = menuManager.getMenu("main-config");
        if (menuData == null) {
            // Fallback para configuração antiga
            String title = ChatColor.translateAlternateColorCodes('&', messageManager.getMessageNoPrefix("gui.main-menu-title"));
            if (title.length() > 32) {
                title = ChatColor.translateAlternateColorCodes('&', "&6&lConfig");
            }
            Inventory gui = Bukkit.createInventory(null, 54, title);
            player.openInventory(gui);
            return;
        }

        String title = menuData.getTitle();
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, menuData.getSize(), title);
        
        // Preencher bordas do menu
        fillMenuBorders(gui);
        
        for (MenuManager.MenuItemData itemData : menuData.getItems()) {
            ItemStack item = menuManager.createMenuItem("main-config", itemData.getName());
            if (item != null && !itemData.getName().equals("border")) {
                gui.setItem(itemData.getSlot(), item);
            }
        }
        
        player.openInventory(gui);
    }

    public void openKitsMenu(Player player) {
        MenuManager.MenuData menuData = menuManager.getMenu("kits");
        String title = menuData != null ? menuData.getTitle() : "&6&lKits";
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        int size = menuData != null ? menuData.getSize() : 54;
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Preencher bordas do menu
        fillMenuBorders(gui);
        
        int slot = 0;
        for (Map.Entry<String, Kit> entry : kitManager.getKits().entrySet()) {
            if (slot >= 45) break;
            
            ItemStack kitItem = createMenuItem(Material.DIAMOND_SWORD, "&a" + entry.getValue().getDisplayName(), 
                "&7Kit: &e" + entry.getKey(), "&7Clique para editar");
            gui.setItem(slot, kitItem);
            slot++;
        }
        
        // Adicionar itens do menu configurável
        if (menuData != null) {
            for (MenuManager.MenuItemData itemData : menuData.getItems()) {
                ItemStack item = menuManager.createMenuItem("kits", itemData.getName());
                if (item != null && !itemData.getName().equals("border")) {
                    gui.setItem(itemData.getSlot(), item);
                }
            }
        } else {
            // Fallback
            ItemStack createItem = createMenuItem(Material.EMERALD, messageManager.getMessageNoPrefix("gui.create-kit"), 
                "", "&7Clique para criar um novo kit");
            gui.setItem(45, createItem);
            
            ItemStack backItem = createMenuItem(Material.PAPER, messageManager.getMessageNoPrefix("gui.back"), 
                "", "&7Clique para voltar");
            gui.setItem(49, backItem);
        }
        
        player.openInventory(gui);
    }

    public void openArenasMenu(Player player) {
        MenuManager.MenuData menuData = menuManager.getMenu("arenas");
        String title = menuData != null ? menuData.getTitle() : "&6&lArenas";
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        int size = menuData != null ? menuData.getSize() : 54;
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Preencher bordas do menu
        fillMenuBorders(gui);
        
        int slot = 0;
        for (Map.Entry<String, Arena> entry : arenaManager.getArenas().entrySet()) {
            if (slot >= 45) break;
            
            String status = entry.getValue().isInUse() ? "&cEm Uso" : "&aDisponível";
            ItemStack arenaItem = createMenuItem(Material.GRASS, "&a" + entry.getKey(), 
                "&7Status: " + status, "&7Clique para editar");
            gui.setItem(slot, arenaItem);
            slot++;
        }
        
        // Adicionar itens do menu configurável
        if (menuData != null) {
            for (MenuManager.MenuItemData itemData : menuData.getItems()) {
                ItemStack item = menuManager.createMenuItem("arenas", itemData.getName());
                if (item != null && !itemData.getName().equals("border")) {
                    gui.setItem(itemData.getSlot(), item);
                }
            }
        } else {
            // Fallback
            ItemStack createItem = createMenuItem(Material.EMERALD, messageManager.getMessageNoPrefix("gui.create-arena"), 
                "", "&7Clique para criar uma nova arena");
            gui.setItem(45, createItem);
            
            ItemStack backItem = createMenuItem(Material.PAPER, messageManager.getMessageNoPrefix("gui.back"), 
                "", "&7Clique para voltar");
            gui.setItem(49, backItem);
        }
        
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.contains("Menu") && !title.contains("Kits") && !title.contains("Arenas") && 
            !title.contains("NPCs") && !title.contains("Modos")) {
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
        
        if (title.contains("Menu de Configuração")) {
            handleMainMenuClick(player, displayName);
        } else if (title.contains("Kits")) {
            handleKitsMenuClick(player, displayName, event.getSlot());
        } else if (title.contains("Arenas")) {
            handleArenasMenuClick(player, displayName, event.getSlot());
        }
    }

    private void handleMainMenuClick(Player player, String displayName) {
        if (displayName.contains("Kits")) {
            openKitsMenu(player);
        } else if (displayName.contains("Arenas")) {
            openArenasMenu(player);
        } else if (displayName.contains("NPCs")) {
            player.sendMessage(messageManager.getMessage("error.not-implemented"));
        } else if (displayName.contains("Modos")) {
            player.sendMessage(messageManager.getMessage("error.not-implemented"));
        } else if (displayName.contains("Recarregar")) {
            plugin.reloadConfig();
            plugin.reloadScoreboardConfig();
            plugin.reloadTablistConfig();
            plugin.reloadNPCsConfig();
            plugin.reloadMessagesConfig();
            plugin.reloadKitsConfig();
            player.sendMessage(messageManager.getMessage("admin.config-reloaded"));
            player.closeInventory();
        } else if (displayName.contains("Fechar")) {
            player.closeInventory();
        }
    }

    private void handleKitsMenuClick(Player player, String displayName, int slot) {
        if (slot == 45) {
            player.sendMessage(messageManager.getMessage("gui.create-kit-hint"));
            player.closeInventory();
        } else if (slot == 49) {
            openMainMenu(player);
        } else {
            String kitName = ChatColor.stripColor(displayName);
            editingKits.put(player, kitName);
            player.sendMessage(messageManager.getMessage("gui.editing-kit", 
                createPlaceholderMap("kit", kitName)));
            player.closeInventory();
        }
    }

    private void handleArenasMenuClick(Player player, String displayName, int slot) {
        if (slot == 45) {
            player.sendMessage(messageManager.getMessage("gui.create-arena-hint"));
            player.closeInventory();
        } else if (slot == 49) {
            openMainMenu(player);
        } else {
            String arenaName = ChatColor.stripColor(displayName);
            if (arenaManager.getArena(arenaName) != null) {
                editingArenas.put(player, arenaName);
                openArenaEditMenu(player, arenaName);
            }
        }
    }

    @EventHandler
    public void onArenaEditClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.contains("Editar Arena")) return;
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        String arenaName = title.replace("§6§lArena: ", "").replace(ChatColor.stripColor("§6§lArena: "), "");
        if (arenaName.equals(title)) {
            arenaName = title.replace("§6§l", "").replace(ChatColor.stripColor("§6§l"), "");
        }
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) return;
        
        ItemStack item = event.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        Location loc = player.getLocation();
        
        if (displayName.contains("Spawn Jogador 1")) {
            arena.setPlayer1Spawn(loc);
            arenaManager.saveArena(arenaName, arena);
            player.sendMessage(messageManager.getMessage("admin.spawn-set", 
                createPlaceholderMap("type", "Jogador 1", "arena", arenaName)));
            player.closeInventory();
        } else if (displayName.contains("Spawn Jogador 2")) {
            arena.setPlayer2Spawn(loc);
            arenaManager.saveArena(arenaName, arena);
            player.sendMessage(messageManager.getMessage("admin.spawn-set", 
                createPlaceholderMap("type", "Jogador 2", "arena", arenaName)));
            player.closeInventory();
        } else if (displayName.contains("Spawn Espectador")) {
            arena.setSpectatorSpawn(loc);
            arenaManager.saveArena(arenaName, arena);
            player.sendMessage(messageManager.getMessage("admin.spawn-set", 
                createPlaceholderMap("type", "Espectador", "arena", arenaName)));
            player.closeInventory();
        } else if (displayName.contains("Deletar")) {
            arenaManager.removeArena(arenaName);
            player.sendMessage(messageManager.getMessage("admin.arena-deleted", 
                createPlaceholderMap("arena", arenaName)));
            openArenasMenu(player);
        } else if (displayName.contains("Voltar")) {
            openArenasMenu(player);
        }
    }

    private void openArenaEditMenu(Player player, String arenaName) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lArena: " + arenaName);
        if (title.length() > 32) {
            title = ChatColor.translateAlternateColorCodes('&', "&6&l" + arenaName);
            if (title.length() > 32) {
                title = title.substring(0, 32);
            }
        }
        Inventory gui = Bukkit.createInventory(null, 27, title);
        
        ItemStack setSpawn1 = createMenuItem(Material.IRON_BOOTS, "&aDefinir Spawn Jogador 1", 
            "", "&7Clique para definir sua posição como spawn do jogador 1");
        gui.setItem(10, setSpawn1);
        
        ItemStack setSpawn2 = createMenuItem(Material.IRON_BOOTS, "&aDefinir Spawn Jogador 2", 
            "", "&7Clique para definir sua posição como spawn do jogador 2");
        gui.setItem(12, setSpawn2);
        
        ItemStack setSpectatorSpawn = createMenuItem(Material.ENDER_PEARL, "&aDefinir Spawn Espectador", 
            "", "&7Clique para definir sua posição como spawn de espectador");
        gui.setItem(14, setSpectatorSpawn);
        
        ItemStack deleteItem = createMenuItem(Material.STAINED_GLASS_PANE, "&cDeletar Arena", 
            "", "&7Clique para deletar esta arena");
        gui.setItem(16, deleteItem);
        
        ItemStack backItem = createMenuItem(Material.PAPER, messageManager.getMessageNoPrefix("gui.back"), 
            "", "&7Clique para voltar");
        gui.setItem(22, backItem);
        
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        editingKits.remove(player);
        editingArenas.remove(player);
    }

    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            if (!line.isEmpty()) {
                loreList.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }

    private Map<String, String> createPlaceholderMap(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (i + 1 < pairs.length) {
                map.put(pairs[i], pairs[i + 1]);
            }
        }
        return map;
    }

    public boolean isEditingKit(Player player) {
        return editingKits.containsKey(player);
    }

    public boolean isEditingArena(Player player) {
        return editingArenas.containsKey(player);
    }

    public String getEditingKit(Player player) {
        return editingKits.get(player);
    }

    public String getEditingArena(Player player) {
        return editingArenas.get(player);
    }

    private void fillMenuBorders(Inventory gui) {
        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);
    }
}

