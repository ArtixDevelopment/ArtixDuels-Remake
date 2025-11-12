package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.TournamentManager;
import dev.artix.artixduels.models.Tournament;
import dev.artix.artixduels.models.TournamentMatch;
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
import java.util.UUID;

/**
 * GUI para visualizar e gerenciar torneios.
 */
public class TournamentGUI implements Listener {
    private final TournamentManager tournamentManager;

    public TournamentGUI(TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;
    }

    /**
     * Abre o menu principal de torneios.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lTORNEIOS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        List<Tournament> activeTournaments = tournamentManager.getActiveTournaments();
        
        int slot = 0;
        for (Tournament tournament : activeTournaments) {
            if (slot >= 45) break;

            boolean isParticipant = tournament.isParticipant(player.getUniqueId());
            ItemStack tournamentItem = createTournamentItem(tournament, isParticipant);
            gui.setItem(slot, tournamentItem);
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
     * Abre o menu de detalhes de um torneio.
     */
    public void openTournamentDetails(Player player, Tournament tournament) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&l" + tournament.getName());
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        boolean isParticipant = tournament.isParticipant(player.getUniqueId());
        String statusColor = getStatusColor(tournament.getState());
        String statusText = getStatusText(tournament.getState());

        ItemStack infoItem = createInfoItem(tournament, isParticipant, statusColor, statusText);
        gui.setItem(4, infoItem);

        if (tournament.getState() == Tournament.TournamentState.REGISTRATION) {
            if (!isParticipant && !tournament.isFull()) {
                ItemStack registerItem = createActionItem(Material.EMERALD, "&a&lINSCREVER-SE",
                    "&7Clique para se inscrever no torneio");
                gui.setItem(20, registerItem);
            } else if (isParticipant) {
                ItemStack unregisterItem = createActionItem(Material.REDSTONE, "&c&lCANCELAR INSCRIÇÃO",
                    "&7Clique para cancelar sua inscrição");
                gui.setItem(20, unregisterItem);
            }
        }

        if (tournament.getState() == Tournament.TournamentState.IN_PROGRESS) {
            ItemStack bracketItem = createActionItem(Material.PAPER, "&e&lVER BRACKETS",
                "&7Clique para ver os brackets do torneio");
            gui.setItem(22, bracketItem);
        }

        ItemStack participantsItem = createParticipantsItem(tournament);
        gui.setItem(24, participantsItem);

        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de brackets.
     */
    public void openBracketsMenu(Player player, Tournament tournament) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lBRACKETS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        Map<Integer, List<TournamentMatch>> brackets = tournament.getBrackets();
        int currentRound = tournament.getCurrentRound();
        List<TournamentMatch> currentRoundMatches = brackets.get(currentRound);

        if (currentRoundMatches != null) {
            int slot = 0;
            for (TournamentMatch match : currentRoundMatches) {
                if (slot >= 45) break;

                ItemStack matchItem = createMatchItem(match, currentRound);
                gui.setItem(slot, matchItem);
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

        if (!title.contains("TORNEIO") && !title.contains("BRACKETS")) {
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

        if (title.equals(ChatColor.translateAlternateColorCodes('&', "&6&lTORNEIOS"))) {
            if (displayName.contains("FECHAR")) {
                player.closeInventory();
            } else {
                String tournamentId = getTournamentIdFromLore(meta.getLore());
                if (tournamentId != null) {
                    Tournament tournament = tournamentManager.getActiveTournaments().stream()
                        .filter(t -> t.getId().equals(tournamentId))
                        .findFirst()
                        .orElse(null);
                    if (tournament != null) {
                        openTournamentDetails(player, tournament);
                    }
                }
            }
        } else if (title.contains("BRACKETS")) {
            if (displayName.contains("VOLTAR")) {
                String tournamentId = getTournamentIdFromLore(meta.getLore());
                if (tournamentId != null) {
                    Tournament tournament = tournamentManager.getActiveTournaments().stream()
                        .filter(t -> t.getId().equals(tournamentId))
                        .findFirst()
                        .orElse(null);
                    if (tournament != null) {
                        openTournamentDetails(player, tournament);
                    }
                }
            }
        } else {
            if (displayName.contains("INSCREVER")) {
                String tournamentId = getTournamentIdFromLore(meta.getLore());
                if (tournamentId != null) {
                    tournamentManager.registerPlayer(player, tournamentId);
                    Tournament tournament = tournamentManager.getActiveTournaments().stream()
                        .filter(t -> t.getId().equals(tournamentId))
                        .findFirst()
                        .orElse(null);
                    if (tournament != null) {
                        openTournamentDetails(player, tournament);
                    }
                }
            } else if (displayName.contains("CANCELAR")) {
                String tournamentId = getTournamentIdFromLore(meta.getLore());
                if (tournamentId != null) {
                    tournamentManager.unregisterPlayer(player, tournamentId);
                    Tournament tournament = tournamentManager.getActiveTournaments().stream()
                        .filter(t -> t.getId().equals(tournamentId))
                        .findFirst()
                        .orElse(null);
                    if (tournament != null) {
                        openTournamentDetails(player, tournament);
                    }
                }
            } else if (displayName.contains("BRACKETS")) {
                String tournamentId = getTournamentIdFromLore(meta.getLore());
                if (tournamentId != null) {
                    Tournament tournament = tournamentManager.getActiveTournaments().stream()
                        .filter(t -> t.getId().equals(tournamentId))
                        .findFirst()
                        .orElse(null);
                    if (tournament != null) {
                        openBracketsMenu(player, tournament);
                    }
                }
            } else if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            }
        }
    }

    private String getTournamentIdFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("ID:")) {
                return ChatColor.stripColor(line).replace("ID: ", "").trim();
            }
        }
        return null;
    }

    private String getStatusColor(Tournament.TournamentState state) {
        switch (state) {
            case REGISTRATION: return "&e";
            case STARTING: return "&6";
            case IN_PROGRESS: return "&a";
            case FINISHED: return "&7";
            case CANCELLED: return "&c";
            default: return "&7";
        }
    }

    private String getStatusText(Tournament.TournamentState state) {
        switch (state) {
            case REGISTRATION: return "Inscrições Abertas";
            case STARTING: return "Iniciando";
            case IN_PROGRESS: return "Em Andamento";
            case FINISHED: return "Finalizado";
            case CANCELLED: return "Cancelado";
            default: return "Desconhecido";
        }
    }

    private ItemStack createTournamentItem(Tournament tournament, boolean isParticipant) {
        Material material = tournament.getState() == Tournament.TournamentState.IN_PROGRESS ? 
            Material.DIAMOND_SWORD : Material.GOLD_INGOT;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String statusColor = getStatusColor(tournament.getState());
        String participantPrefix = isParticipant ? "&a&l[INSCRITO] " : "";
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            participantPrefix + statusColor + tournament.getName()));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7" + tournament.getDescription());
        lore.add("&8&m                              ");
        lore.add("&7Estado: " + statusColor + getStatusText(tournament.getState()));
        lore.add("&7Participantes: &b" + tournament.getParticipants().size() + 
                "&7/&b" + tournament.getMaxParticipants());
        lore.add("&7Modo: &e" + tournament.getMode());
        lore.add("&8&m                              ");
        lore.add("&7Clique para ver detalhes");
        lore.add("&8&m                              ");
        lore.add("&7ID: " + tournament.getId());

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createInfoItem(Tournament tournament, boolean isParticipant, 
                                     String statusColor, String statusText) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            "&6&l" + tournament.getName()));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7" + tournament.getDescription());
        lore.add("&8&m                              ");
        lore.add("&7Estado: " + statusColor + statusText);
        lore.add("&7Participantes: &b" + tournament.getParticipants().size() + 
                "&7/&b" + tournament.getMaxParticipants());
        lore.add("&7Modo: &e" + tournament.getMode());
        lore.add("&7Tipo: &b" + tournament.getType().getName());
        if (isParticipant) {
            lore.add("&8&m                              ");
            lore.add("&a&l✓ Você está inscrito neste torneio");
        }
        lore.add("&8&m                              ");
        lore.add("&7ID: " + tournament.getId());

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

    private ItemStack createParticipantsItem(Tournament tournament) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lPARTICIPANTES"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Total: &b" + tournament.getParticipants().size());
        lore.add("&8&m                              ");
        
        int count = 0;
        for (UUID participantId : tournament.getParticipants()) {
            if (count >= 10) {
                lore.add("&7... e mais " + (tournament.getParticipants().size() - count) + " jogadores");
                break;
            }
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(participantId);
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Desconhecido";
            lore.add("&7- &e" + playerName);
            count++;
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

    private ItemStack createMatchItem(TournamentMatch match, int round) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        String player1Name = "Aguardando";
        String player2Name = "Aguardando";
        
        if (match.getPlayer1Id() != null) {
            org.bukkit.OfflinePlayer p1 = Bukkit.getOfflinePlayer(match.getPlayer1Id());
            player1Name = p1.getName() != null ? p1.getName() : "Desconhecido";
        }
        
        if (match.getPlayer2Id() != null) {
            org.bukkit.OfflinePlayer p2 = Bukkit.getOfflinePlayer(match.getPlayer2Id());
            player2Name = p2.getName() != null ? p2.getName() : "Desconhecido";
        }

        String statusColor = match.getState() == TournamentMatch.MatchState.FINISHED ? "&a" : 
                            match.getState() == TournamentMatch.MatchState.IN_PROGRESS ? "&e" : "&7";
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            statusColor + "Rodada " + round + " - Partida " + match.getMatchNumber()));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7" + player1Name + " &7vs &7" + player2Name);
        lore.add("&8&m                              ");
        
        if (match.getState() == TournamentMatch.MatchState.FINISHED && match.getWinnerId() != null) {
            org.bukkit.OfflinePlayer winner = Bukkit.getOfflinePlayer(match.getWinnerId());
            String winnerName = winner.getName() != null ? winner.getName() : "Desconhecido";
            lore.add("&a&lVencedor: &e" + winnerName);
        } else {
            lore.add("&7Estado: " + statusColor + match.getState().getName());
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

