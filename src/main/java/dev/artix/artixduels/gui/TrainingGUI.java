package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.TrainingManager;
import dev.artix.artixduels.models.BotDifficulty;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.TrainingSession;
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
 * GUI para visualizar e iniciar treinamentos.
 */
public class TrainingGUI implements Listener {
    private final TrainingManager trainingManager;

    public TrainingGUI(TrainingManager trainingManager) {
        this.trainingManager = trainingManager;
    }

    /**
     * Abre o menu principal de treinamento.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lTREINAMENTO");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        TrainingSession activeSession = trainingManager.getSession(player.getUniqueId());
        if (activeSession != null) {
            ItemStack activeItem = createActiveSessionItem(activeSession);
            gui.setItem(4, activeItem);
            
            ItemStack stopItem = createActionItem(Material.BARRIER, "&c&lPARAR TREINAMENTO",
                "&7Clique para parar o treinamento atual");
            gui.setItem(22, stopItem);
        } else {
            ItemStack easyItem = createDifficultyItem(BotDifficulty.EASY, "&a&lFÁCIL",
                "&7Ideal para iniciantes",
                "&7Precisão: &a30%",
                "&7Bloqueio: &a50%");
            gui.setItem(10, easyItem);

            ItemStack mediumItem = createDifficultyItem(BotDifficulty.MEDIUM, "&e&lMÉDIO",
                "&7Dificuldade intermediária",
                "&7Precisão: &e60%",
                "&7Bloqueio: &e70%");
            gui.setItem(12, mediumItem);

            ItemStack hardItem = createDifficultyItem(BotDifficulty.HARD, "&c&lDIFÍCIL",
                "&7Para jogadores experientes",
                "&7Precisão: &c80%",
                "&7Bloqueio: &c90%");
            gui.setItem(14, hardItem);

            ItemStack expertItem = createDifficultyItem(BotDifficulty.EXPERT, "&4&lEXPERT",
                "&7Máxima dificuldade",
                "&7Precisão: &495%",
                "&7Bloqueio: &4100%");
            gui.setItem(16, expertItem);
        }

        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de seleção de kit para treinamento.
     */
    public void openKitSelectionMenu(Player player, BotDifficulty difficulty) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lSELECIONAR KIT");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        dev.artix.artixduels.managers.KitManager kitManager = 
            ((dev.artix.artixduels.ArtixDuels) Bukkit.getPluginManager().getPlugin("ArtixDuels")).getKitManager();
        
        if (kitManager != null) {
            int slot = 0;
            for (String kitName : kitManager.getKits().keySet()) {
                if (slot >= 45) break;

                ItemStack kitItem = createKitItem(kitName, difficulty);
                gui.setItem(slot, kitItem);
                slot++;
            }
        }

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

        if (!title.contains("TREINAMENTO") && !title.contains("SELECIONAR KIT")) {
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

        if (title.contains("TREINAMENTO")) {
            if (displayName.contains("FECHAR")) {
                player.closeInventory();
            } else if (displayName.contains("PARAR")) {
                trainingManager.stopTraining(player.getUniqueId());
                player.closeInventory();
            } else if (displayName.contains("FÁCIL")) {
                openKitSelectionMenu(player, BotDifficulty.EASY);
            } else if (displayName.contains("MÉDIO")) {
                openKitSelectionMenu(player, BotDifficulty.MEDIUM);
            } else if (displayName.contains("DIFÍCIL")) {
                openKitSelectionMenu(player, BotDifficulty.HARD);
            } else if (displayName.contains("EXPERT")) {
                openKitSelectionMenu(player, BotDifficulty.EXPERT);
            }
        } else if (title.contains("SELECIONAR KIT")) {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            } else {
                String kitName = getKitNameFromLore(meta.getLore());
                String difficultyStr = getDifficultyFromLore(meta.getLore());
                if (kitName != null && difficultyStr != null) {
                    BotDifficulty difficulty = BotDifficulty.valueOf(difficultyStr);
                    dev.artix.artixduels.managers.ArenaManager arenaManager = 
                        ((dev.artix.artixduels.ArtixDuels) Bukkit.getPluginManager().getPlugin("ArtixDuels")).getArenaManager();
                    String arenaName = arenaManager != null ? arenaManager.getAvailableArena().getName() : null;
                    trainingManager.startTraining(player, difficulty, kitName, arenaName, DuelMode.BEDFIGHT);
                    player.closeInventory();
                }
            }
        }
    }

    private String getKitNameFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("Kit:")) {
                return ChatColor.stripColor(line).replace("Kit: ", "").trim();
            }
        }
        return null;
    }

    private String getDifficultyFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("Dificuldade:")) {
                String diff = ChatColor.stripColor(line).replace("Dificuldade: ", "").trim();
                return diff.toUpperCase();
            }
        }
        return null;
    }

    private ItemStack createDifficultyItem(BotDifficulty difficulty, String name, String... lore) {
        Material material = getMaterialForDifficulty(difficulty);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        loreList.add("&8&m                              ");
        loreList.add("&7Clique para selecionar");
        loreList.add("&7Dificuldade: " + difficulty.name());
        
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }

    private ItemStack createKitItem(String kitName, BotDifficulty difficulty) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + kitName));
        
        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Kit: " + kitName);
        lore.add("&7Dificuldade: " + difficulty.name());
        lore.add("&8&m                              ");
        lore.add("&7Clique para iniciar");
        lore.add("&8&m                              ");
        
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        
        return item;
    }

    private ItemStack createActiveSessionItem(TrainingSession session) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lTREINAMENTO ATIVO"));
        
        TrainingSession.TrainingStats stats = session.getStats();
        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Bot: &e" + session.getBot().getBotName());
        lore.add("&7Dificuldade: &b" + session.getBot().getDifficulty().getDisplayName());
        lore.add("&7Kit: &b" + session.getKitName());
        lore.add("&8&m                              ");
        lore.add("&7Acertos: &a" + stats.getPlayerHits() + " &7/ &c" + stats.getBotHits());
        lore.add("&7Eliminações: &a" + stats.getPlayerKills() + " &7/ &c" + stats.getBotKills());
        lore.add("&7Precisão: &b" + String.format("%.1f", stats.getPlayerAccuracy()) + "%");
        lore.add("&8&m                              ");
        
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        
        return item;
    }

    private ItemStack createActionItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }

    private Material getMaterialForDifficulty(BotDifficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return Material.WOOD_SWORD;
            case MEDIUM:
                return Material.STONE_SWORD;
            case HARD:
                return Material.IRON_SWORD;
            case EXPERT:
                return Material.DIAMOND_SWORD;
            default:
                return Material.STICK;
        }
    }
}

