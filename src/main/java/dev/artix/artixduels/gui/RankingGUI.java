package dev.artix.artixduels.gui;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.MenuManager;
import dev.artix.artixduels.managers.RankingManager;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.PlayerStats;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI para exibir rankings e leaderboards.
 */
public class RankingGUI implements Listener {
    private final ArtixDuels plugin;
    private final RankingManager rankingManager;
    private final MenuManager menuManager;

    public RankingGUI(ArtixDuels plugin, RankingManager rankingManager, MenuManager menuManager) {
        this.plugin = plugin;
        this.rankingManager = rankingManager;
        this.menuManager = menuManager;
    }

    /**
     * Abre o menu principal de rankings.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lRANKINGS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Preencher bordas
        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        // Item: Ranking de ELO
        ItemStack eloItem = createRankingItem(Material.GOLD_INGOT, "&6&lRANKING DE ELO",
            "&7Veja os jogadores com maior ELO",
            "&7Clique para visualizar");
        gui.setItem(10, eloItem);

        // Item: Ranking de Vitórias
        ItemStack winsItem = createRankingItem(Material.DIAMOND_SWORD, "&a&lRANKING DE VITÓRIAS",
            "&7Veja os jogadores com mais vitórias",
            "&7Clique para visualizar");
        gui.setItem(12, winsItem);

        // Item: Ranking de Winrate
        ItemStack winrateItem = createRankingItem(Material.EMERALD, "&b&lRANKING DE WINRATE",
            "&7Veja os jogadores com melhor winrate",
            "&7Clique para visualizar");
        gui.setItem(14, winrateItem);

        // Item: Ranking de Win Streak
        ItemStack streakItem = createRankingItem(Material.BLAZE_POWDER, "&c&lRANKING DE STREAK",
            "&7Veja os jogadores com maior win streak",
            "&7Clique para visualizar");
        gui.setItem(16, streakItem);

        // Item: Ranking de XP
        ItemStack xpItem = createRankingItem(Material.EXP_BOTTLE, "&e&lRANKING DE XP",
            "&7Veja os jogadores com mais XP",
            "&7Clique para visualizar");
        gui.setItem(28, xpItem);

        // Item: Rankings por Modo
        ItemStack modeItem = createRankingItem(Material.BOOK, "&d&lRANKINGS POR MODO",
            "&7Veja rankings específicos de cada modo",
            "&7Clique para visualizar");
        gui.setItem(30, modeItem);

        // Item: Rankings por Período
        ItemStack periodItem = createRankingItem(Material.WATCH, "&6&lRANKINGS POR PERÍODO",
            "&7Veja rankings diários, semanais e mensais",
            "&7Clique para visualizar");
        gui.setItem(32, periodItem);

        // Item: Temporada Atual
        dev.artix.artixduels.models.Season season = rankingManager.getCurrentSeason();
        if (season != null) {
            ItemStack seasonItem = createRankingItem(Material.GOLD_BLOCK, "&6&lTEMPORADA " + season.getSeasonNumber(),
                "&7Temporada atual: " + season.getSeasonNumber(),
                "&7Início: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(season.getStartTime())),
                "&7Fim: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(season.getEndTime())),
                "&7Clique para ver ranking da temporada");
            gui.setItem(34, seasonItem);
        }

        // Item: Fechar
        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o ranking de um tipo específico.
     */
    public void openRanking(Player player, String type, String title) {
        List<RankingManager.RankingEntry> ranking = getRankingByType(type, 45);
        
        String guiTitle = ChatColor.translateAlternateColorCodes('&', title);
        if (guiTitle.length() > 32) {
            guiTitle = guiTitle.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, guiTitle);

        // Preencher bordas
        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        int slot = 0;
        for (RankingManager.RankingEntry entry : ranking) {
            if (slot >= 45) break;

            ItemStack head = createPlayerHead(entry.getPlayerName(), entry, slot + 1, type);
            gui.setItem(slot, head);
            slot++;
        }

        // Item: Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de rankings por modo.
     */
    public void openModeRankingMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lRANKINGS POR MODO");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Preencher bordas
        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        int slot = 0;
        for (DuelMode mode : DuelMode.values()) {
            if (slot >= 45) break;

            ItemStack modeItem = createModeRankingItem(mode);
            gui.setItem(slot, modeItem);
            slot++;
        }

        // Item: Voltar
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

        if (!title.contains("RANKING") && !title.contains("RANKINGS")) {
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

        if (title.contains("RANKINGS") && !title.contains("POR MODO")) {
            // Menu principal
            if (displayName.contains("ELO")) {
                openRanking(player, "elo", "&6&lRANKING DE ELO");
            } else if (displayName.contains("VITÓRIAS")) {
                openRanking(player, "wins", "&a&lRANKING DE VITÓRIAS");
            } else if (displayName.contains("WINRATE")) {
                openRanking(player, "winrate", "&b&lRANKING DE WINRATE");
            } else if (displayName.contains("STREAK")) {
                openRanking(player, "streak", "&c&lRANKING DE STREAK");
            } else if (displayName.contains("XP")) {
                openRanking(player, "xp", "&e&lRANKING DE XP");
            } else if (displayName.contains("POR MODO")) {
                openModeRankingMenu(player);
            } else if (displayName.contains("FECHAR") || displayName.contains("VOLTAR")) {
                player.closeInventory();
            }
        } else if (title.contains("POR MODO")) {
            // Menu de modos
            for (DuelMode mode : DuelMode.values()) {
                if (displayName.contains(mode.getDisplayName().toUpperCase())) {
                    openRanking(player, "mode_kills_" + mode.getName(), "&6&lRANKING: " + mode.getDisplayName());
                    break;
                }
            }
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            }
        } else {
            // Ranking específico
            if (displayName.contains("VOLTAR")) {
                if (title.contains("POR MODO")) {
                    openModeRankingMenu(player);
                } else {
                    openMainMenu(player);
                }
            }
        }
    }

    private List<RankingManager.RankingEntry> getRankingByType(String type, int limit) {
        if (type.equals("elo")) {
            return rankingManager.getEloRanking(limit);
        } else if (type.equals("wins")) {
            return rankingManager.getWinsRanking(limit);
        } else if (type.equals("winrate")) {
            return rankingManager.getWinrateRanking(limit);
        } else if (type.equals("streak")) {
            return rankingManager.getStreakRanking(limit);
        } else if (type.equals("xp")) {
            return rankingManager.getXpRanking(limit);
        } else if (type.startsWith("mode_kills_")) {
            String modeName = type.replace("mode_kills_", "");
            DuelMode mode = DuelMode.fromString(modeName);
            if (mode != null) {
                return rankingManager.getModeKillsRanking(mode, limit);
            }
        } else if (type.startsWith("mode_wins_")) {
            String modeName = type.replace("mode_wins_", "");
            DuelMode mode = DuelMode.fromString(modeName);
            if (mode != null) {
                return rankingManager.getModeWinsRanking(mode, limit);
            }
        }
        return new ArrayList<>();
    }

    private ItemStack createRankingItem(Material material, String name, String... lore) {
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

    private ItemStack createPlayerHead(String playerName, RankingManager.RankingEntry entry, int position, String type) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            "&6#" + position + " &e" + playerName));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        
        if (type.equals("elo")) {
            lore.add("&7ELO: &6" + (int) entry.getValue());
        } else if (type.equals("wins")) {
            lore.add("&7Vitórias: &a" + (int) entry.getValue());
        } else if (type.equals("winrate")) {
            lore.add("&7Winrate: &b" + String.format("%.2f", entry.getValue()) + "%");
        } else if (type.equals("streak")) {
            lore.add("&7Streak: &c" + (int) entry.getValue());
        } else if (type.equals("xp")) {
            lore.add("&7XP: &e" + (int) entry.getValue());
        } else if (type.startsWith("mode_")) {
            lore.add("&7Valor: &b" + (int) entry.getValue());
        }

        PlayerStats stats = entry.getStats();
        if (stats != null) {
            lore.add("&8&m                              ");
            lore.add("&7ELO: &6" + stats.getElo());
            lore.add("&7Vitórias: &a" + stats.getWins());
            lore.add("&7Derrotas: &c" + stats.getLosses());
            lore.add("&7Winrate: &b" + String.format("%.2f", stats.getWinRate()) + "%");
        }

        lore.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        head.setItemMeta(meta);

        return head;
    }

    private ItemStack createModeRankingItem(DuelMode mode) {
        Material material = getMaterialForMode(mode);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + mode.getDisplayName().toUpperCase()));
        
        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Clique para ver o ranking");
        lore.add("&7de kills neste modo");
        lore.add("&8&m                              ");
        
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        
        return item;
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
}

