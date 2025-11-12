package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.ReplayManager;
import dev.artix.artixduels.models.Replay;
import dev.artix.artixduels.models.ReplaySession;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * GUI para visualizar e gerenciar replays.
 */
public class ReplayGUI implements Listener {
    private final ReplayManager replayManager;

    public ReplayGUI(ReplayManager replayManager) {
        this.replayManager = replayManager;
    }

    /**
     * Abre o menu principal de replays.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lREPLAYS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        List<Replay> replays = replayManager.getSavedReplays();
        int slot = 0;
        for (Replay replay : replays) {
            if (slot >= 45) break;

            ItemStack replayItem = createReplayItem(replay);
            gui.setItem(slot, replayItem);
            slot++;
        }

        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de controles de replay.
     */
    public void openReplayControls(Player player, ReplaySession session) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lCONTROLES");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 27, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        ReplaySession.ReplayState state = session.getState();
        
        ItemStack playPauseItem;
        if (state == ReplaySession.ReplayState.PLAYING) {
            playPauseItem = createControlItem(Material.REDSTONE, "&c&lPAUSAR", "&7Clique para pausar");
        } else {
            playPauseItem = createControlItem(Material.EMERALD, "&a&lREPRODUZIR", "&7Clique para reproduzir");
        }
        gui.setItem(10, playPauseItem);

        ItemStack stopItem = createControlItem(Material.BARRIER, "&c&lPARAR", "&7Clique para parar");
        gui.setItem(12, stopItem);

        ItemStack speedItem = createControlItem(Material.REDSTONE_TORCH_ON, 
            "&e&lVELOCIDADE: &b" + String.format("%.2fx", session.getPlaybackSpeed()),
            "&7Clique para alterar velocidade");
        gui.setItem(14, speedItem);

        ItemStack cameraItem;
        if (session.isFreeCamera()) {
            cameraItem = createControlItem(Material.EYE_OF_ENDER, "&b&lCÂMERA LIVRE: &aATIVA",
                "&7Clique para desativar");
        } else {
            cameraItem = createControlItem(Material.EYE_OF_ENDER, "&b&lCÂMERA LIVRE: &cINATIVA",
                "&7Clique para ativar");
        }
        gui.setItem(16, cameraItem);

        ItemStack infoItem = createReplayInfoItem(session.getReplay(), session);
        gui.setItem(4, infoItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.contains("REPLAY") && !title.contains("CONTROLES")) {
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

        if (title.contains("REPLAYS")) {
            if (displayName.contains("FECHAR")) {
                player.closeInventory();
            } else {
                String replayIdStr = getReplayIdFromLore(meta.getLore());
                if (replayIdStr != null) {
                    try {
                        java.util.UUID replayId = java.util.UUID.fromString(replayIdStr);
                        ReplaySession session = replayManager.startPlayback(player, replayId);
                        if (session != null) {
                            openReplayControls(player, session);
                        } else {
                            player.sendMessage("§cErro ao iniciar replay!");
                        }
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§cID de replay inválido!");
                    }
                }
            }
        } else if (title.contains("CONTROLES")) {
            ReplaySession session = replayManager.getSession(player.getUniqueId());
            if (session == null) {
                player.closeInventory();
                return;
            }

            if (displayName.contains("REPRODUZIR")) {
                session.setState(ReplaySession.ReplayState.PLAYING);
                player.sendMessage("§aReplay reproduzindo...");
            } else if (displayName.contains("PAUSAR")) {
                session.setState(ReplaySession.ReplayState.PAUSED);
                player.sendMessage("§eReplay pausado.");
            } else if (displayName.contains("PARAR")) {
                replayManager.stopPlayback(player.getUniqueId());
                player.closeInventory();
                player.sendMessage("§cReplay parado.");
            } else if (displayName.contains("VELOCIDADE")) {
                double currentSpeed = session.getPlaybackSpeed();
                double newSpeed = currentSpeed >= 4.0 ? 0.25 : currentSpeed * 2.0;
                session.setPlaybackSpeed(newSpeed);
                player.sendMessage("§eVelocidade alterada para §b" + String.format("%.2fx", newSpeed));
                openReplayControls(player, session);
            } else if (displayName.contains("CÂMERA")) {
                session.setFreeCamera(!session.isFreeCamera());
                player.sendMessage(session.isFreeCamera() ? 
                    "§aCâmera livre ativada!" : "§cCâmera livre desativada!");
                openReplayControls(player, session);
            }
        }
    }

    private String getReplayIdFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("ID:")) {
                return ChatColor.stripColor(line).replace("ID: ", "").trim();
            }
        }
        return null;
    }

    private ItemStack createReplayItem(Replay replay) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        String winnerText = replay.getWinnerName() != null ? 
            "&a" + replay.getWinnerName() : "&7Empate";
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            "&6&l" + replay.getPlayer1Name() + " &7vs &6&l" + replay.getPlayer2Name()));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Modo: &e" + replay.getMode().getDisplayName());
        lore.add("&7Kit: &b" + replay.getKitName());
        lore.add("&7Arena: &b" + replay.getArenaName());
        lore.add("&7Vencedor: " + winnerText);
        lore.add("&7Duração: &b" + formatDuration(replay.getDuration()));
        lore.add("&7Frames: &b" + replay.getTotalFrames());
        lore.add("&7Data: &b" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(replay.getStartTime())));
        lore.add("&8&m                              ");
        lore.add("&7Clique para assistir");
        lore.add("&8&m                              ");
        lore.add("&7ID: " + replay.getReplayId().toString());

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createControlItem(Material material, String name, String... lore) {
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

    private ItemStack createReplayInfoItem(Replay replay, ReplaySession session) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lINFORMAÇÕES"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Frame: &b" + (session.getCurrentFrame() + 1) + "&7/&b" + replay.getTotalFrames());
        lore.add("&7Progresso: &b" + String.format("%.1f", 
            (double) session.getCurrentFrame() / replay.getTotalFrames() * 100) + "%");
        lore.add("&7Velocidade: &b" + String.format("%.2fx", session.getPlaybackSpeed()));
        lore.add("&7Estado: &b" + session.getState().name());
        lore.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}

