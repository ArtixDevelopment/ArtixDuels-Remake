package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Arena;
import dev.artix.artixduels.models.ArenaEditSession;
import dev.artix.artixduels.models.ArenaTemplate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Editor visual de arenas.
 */
public class ArenaEditor {
    private final ArtixDuels plugin;
    private final ArenaManager arenaManager;
    private Map<UUID, ArenaEditSession> editSessions;
    private Map<UUID, Location> selectionPos1;
    private Map<UUID, Location> selectionPos2;
    private Map<String, ArenaTemplate> templates;
    private Map<UUID, BukkitRunnable> previewTasks;

    public ArenaEditor(ArtixDuels plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.editSessions = new HashMap<>();
        this.selectionPos1 = new HashMap<>();
        this.selectionPos2 = new HashMap<>();
        this.templates = new HashMap<>();
        this.previewTasks = new HashMap<>();
        
        loadTemplates();
    }

    /**
     * Inicia uma sessão de edição de arena.
     */
    public ArenaEditSession startEditSession(Player player, String arenaName) {
        if (editSessions.containsKey(player.getUniqueId())) {
            player.sendMessage("§cVocê já está editando uma arena! Use /arenaeditor cancel para cancelar.");
            return null;
        }

        ArenaEditSession session = new ArenaEditSession(player, arenaName);
        editSessions.put(player.getUniqueId(), session);
        
        giveEditorItems(player);
        player.sendMessage("§aSessão de edição iniciada para a arena: §e" + arenaName);
        player.sendMessage("§7Use os itens na sua hotbar para selecionar áreas.");
        
        return session;
    }

    /**
     * Cancela uma sessão de edição.
     */
    public void cancelEditSession(UUID playerId) {
        ArenaEditSession session = editSessions.remove(playerId);
        if (session != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                stopPreview(player);
                player.getInventory().clear();
                player.sendMessage("§cSessão de edição cancelada.");
            }
        }
        selectionPos1.remove(playerId);
        selectionPos2.remove(playerId);
    }

    /**
     * Salva a arena editada.
     */
    public boolean saveArena(UUID playerId) {
        ArenaEditSession session = editSessions.get(playerId);
        if (session == null) {
            return false;
        }

        if (!session.isComplete()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§cArena incompleta! Defina todas as posições necessárias.");
            }
            return false;
        }

        Arena arena = arenaManager.getArena(session.getArenaName());
        if (arena == null) {
            arena = new Arena(session.getArenaName());
        }

        if (session.getPos1() != null && session.getPos2() != null) {
            // Calcular spawns baseados nas posições se não foram definidos
            if (session.getPlayer1Spawn() == null) {
                Location center = getCenter(session.getPos1(), session.getPos2());
                center.setY(session.getPos1().getY());
                session.setPlayer1Spawn(center.clone().add(-5, 0, 0));
            }
            if (session.getPlayer2Spawn() == null) {
                Location center = getCenter(session.getPos1(), session.getPos2());
                center.setY(session.getPos1().getY());
                session.setPlayer2Spawn(center.clone().add(5, 0, 0));
            }
        }

        arena.setPlayer1Spawn(session.getPlayer1Spawn());
        arena.setPlayer2Spawn(session.getPlayer2Spawn());
        arena.setSpectatorSpawn(session.getSpectatorSpawn());

        arenaManager.addArena(session.getArenaName(), arena);
        arenaManager.saveArena(session.getArenaName(), arena);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage("§aArena §e" + session.getArenaName() + " §asalva com sucesso!");
            cancelEditSession(playerId);
        }

        return true;
    }

    /**
     * Inicia o modo de teste da arena.
     */
    public void startTestMode(Player player) {
        ArenaEditSession session = editSessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage("§cVocê não está editando uma arena!");
            return;
        }

        if (!session.isComplete()) {
            player.sendMessage("§cComplete a arena antes de testar!");
            return;
        }

        session.setTesting(true);
        Location testSpawn = session.getPlayer1Spawn() != null ? session.getPlayer1Spawn() : player.getLocation();
        player.teleport(testSpawn);
        player.sendMessage("§aModo de teste ativado! Use /arenaeditor test stop para sair.");
    }

    /**
     * Para o modo de teste.
     */
    public void stopTestMode(Player player) {
        ArenaEditSession session = editSessions.get(player.getUniqueId());
        if (session != null) {
            session.setTesting(false);
            player.sendMessage("§cModo de teste desativado.");
        }
    }

    /**
     * Define a posição 1 da seleção.
     */
    public void setPos1(Player player, Location location) {
        selectionPos1.put(player.getUniqueId(), location);
        player.sendMessage("§aPosição 1 definida: §e" + formatLocation(location));
        
        ArenaEditSession session = editSessions.get(player.getUniqueId());
        if (session != null) {
            session.setPos1(location);
        }
        
        updatePreview(player);
    }

    /**
     * Define a posição 2 da seleção.
     */
    public void setPos2(Player player, Location location) {
        selectionPos2.put(player.getUniqueId(), location);
        player.sendMessage("§aPosição 2 definida: §e" + formatLocation(location));
        
        ArenaEditSession session = editSessions.get(player.getUniqueId());
        if (session != null) {
            session.setPos2(location);
        }
        
        updatePreview(player);
    }

    /**
     * Atualiza o preview visual da arena.
     */
    private void updatePreview(Player player) {
        stopPreview(player);
        
        Location pos1 = selectionPos1.get(player.getUniqueId());
        Location pos2 = selectionPos2.get(player.getUniqueId());
        
        if (pos1 == null || pos2 == null) {
            return;
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                showPreview(player, pos1, pos2);
            }
        };
        
        task.runTaskTimer(plugin, 0L, 20L); // A cada segundo
        previewTasks.put(player.getUniqueId(), task);
    }

    /**
     * Mostra preview visual da arena.
     */
    private void showPreview(Player player, Location pos1, Location pos2) {
        try {
            int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
            int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
            int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

            // Mostrar partículas nas bordas
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                            Location loc = new Location(pos1.getWorld(), x + 0.5, y + 0.5, z + 0.5);
                            try {
                                Class<?> particleClass = Class.forName("org.bukkit.Particle");
                                Object redstoneParticle = particleClass.getField("REDSTONE").get(null);
                                java.lang.reflect.Method spawnParticleMethod = player.getClass().getMethod("spawnParticle",
                                    particleClass, Location.class, int.class, double.class, double.class, double.class, double.class);
                                spawnParticleMethod.invoke(player, redstoneParticle, loc, 1, 0, 0, 0, 0);
                            } catch (Exception e) {
                                // Fallback para versões antigas
                                try {
                                    player.getWorld().playEffect(loc, org.bukkit.Effect.MOBSPAWNER_FLAMES, 0);
                                } catch (Exception ex) {
                                    // Ignorar se não suportar
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar erros de preview
        }
    }

    /**
     * Para o preview.
     */
    private void stopPreview(Player player) {
        BukkitRunnable task = previewTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Dá itens de edição para o jogador.
     */
    private void giveEditorItems(Player player) {
        player.getInventory().clear();
        
        // Item: Selecionar Pos1
        ItemStack pos1Item = createSelectionItem(Material.WOOD_AXE, "&a&lSelecionar Posição 1",
            "&7Clique em um bloco para definir",
            "&7a posição 1 da arena");
        player.getInventory().setItem(0, pos1Item);

        // Item: Selecionar Pos2
        ItemStack pos2Item = createSelectionItem(Material.STONE_AXE, "&e&lSelecionar Posição 2",
            "&7Clique em um bloco para definir",
            "&7a posição 2 da arena");
        player.getInventory().setItem(1, pos2Item);

        // Item: Definir Spawn Player 1
        ItemStack spawn1Item = createSelectionItem(Material.BANNER, "&b&lSpawn Jogador 1",
            "&7Clique em um bloco para definir",
            "&7o spawn do jogador 1");
        player.getInventory().setItem(2, spawn1Item);

        // Item: Definir Spawn Player 2
        ItemStack spawn2Item = createSelectionItem(Material.BANNER, "&c&lSpawn Jogador 2",
            "&7Clique em um bloco para definir",
            "&7o spawn do jogador 2");
        player.getInventory().setItem(3, spawn2Item);

        // Item: Definir Spawn Espectador
        ItemStack spectatorItem = createSelectionItem(Material.EYE_OF_ENDER, "&d&lSpawn Espectador",
            "&7Clique em um bloco para definir",
            "&7o spawn do espectador");
        player.getInventory().setItem(4, spectatorItem);

        // Item: Preview
        ItemStack previewItem = createActionItem(Material.GLASS, "&6&lPreview",
            "&7Ativa/desativa o preview",
            "&7visual da arena");
        player.getInventory().setItem(5, previewItem);

        // Item: Testar
        ItemStack testItem = createActionItem(Material.DIAMOND_SWORD, "&a&lTestar Arena",
            "&7Teleporta para a arena",
            "&7para testá-la");
        player.getInventory().setItem(6, testItem);

        // Item: Salvar
        ItemStack saveItem = createActionItem(Material.EMERALD, "&a&lSalvar Arena",
            "&7Salva a arena editada");
        player.getInventory().setItem(7, saveItem);

        // Item: Cancelar
        ItemStack cancelItem = createActionItem(Material.BARRIER, "&c&lCancelar",
            "&7Cancela a edição");
        player.getInventory().setItem(8, cancelItem);

        player.updateInventory();
    }

    /**
     * Cria item de seleção.
     */
    private ItemStack createSelectionItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }

    /**
     * Cria item de ação.
     */
    private ItemStack createActionItem(Material material, String name, String... lore) {
        return createSelectionItem(material, name, lore);
    }

    /**
     * Obtém a sessão de edição de um jogador.
     */
    public ArenaEditSession getEditSession(UUID playerId) {
        return editSessions.get(playerId);
    }

    /**
     * Obtém posição 1 da seleção.
     */
    public Location getPos1(UUID playerId) {
        return selectionPos1.get(playerId);
    }

    /**
     * Obtém posição 2 da seleção.
     */
    public Location getPos2(UUID playerId) {
        return selectionPos2.get(playerId);
    }

    /**
     * Carrega templates de arena.
     */
    private void loadTemplates() {
        // Template: Pequena
        ArenaTemplate smallTemplate = new ArenaTemplate("small", "Pequena", "Arena pequena para duelos rápidos", 10.0);
        templates.put("small", smallTemplate);

        // Template: Média
        ArenaTemplate mediumTemplate = new ArenaTemplate("medium", "Média", "Arena de tamanho médio", 20.0);
        templates.put("medium", mediumTemplate);

        // Template: Grande
        ArenaTemplate largeTemplate = new ArenaTemplate("large", "Grande", "Arena grande para duelos estratégicos", 30.0);
        templates.put("large", largeTemplate);
    }

    /**
     * Cria arena a partir de um template.
     */
    public ArenaEditSession createFromTemplate(Player player, String templateName, String arenaName) {
        ArenaTemplate template = templates.get(templateName);
        if (template == null) {
            player.sendMessage("§cTemplate não encontrado!");
            return null;
        }

        ArenaEditSession session = startEditSession(player, arenaName);
        if (session == null) {
            return null;
        }

        Location center = player.getLocation();
        double size = template.getDefaultSize();

        Location pos1 = center.clone().add(-size, -2, -size);
        Location pos2 = center.clone().add(size, 5, size);
        
        session.setPos1(pos1);
        session.setPos2(pos2);
        selectionPos1.put(player.getUniqueId(), pos1);
        selectionPos2.put(player.getUniqueId(), pos2);

        Location spawn1 = center.clone().add(-size/2, 0, 0);
        Location spawn2 = center.clone().add(size/2, 0, 0);
        Location spectatorSpawn = center.clone().add(0, size, 0);

        session.setPlayer1Spawn(spawn1);
        session.setPlayer2Spawn(spawn2);
        session.setSpectatorSpawn(spectatorSpawn);

        player.sendMessage("§aArena criada a partir do template: §e" + template.getDisplayName());
        updatePreview(player);

        return session;
    }

    /**
     * Obtém o plugin.
     */
    public ArtixDuels getPlugin() {
        return plugin;
    }

    /**
     * Exporta uma arena para arquivo.
     */
    public boolean exportArena(String arenaName, File exportFile) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) {
            return false;
        }

        try {
            FileWriter writer = new FileWriter(exportFile);
            writer.write("# Arena Export: " + arenaName + "\n");
            writer.write("name: " + arenaName + "\n");
            
            if (arena.getPlayer1Spawn() != null) {
                writer.write("player1-spawn: " + locationToString(arena.getPlayer1Spawn()) + "\n");
            }
            if (arena.getPlayer2Spawn() != null) {
                writer.write("player2-spawn: " + locationToString(arena.getPlayer2Spawn()) + "\n");
            }
            if (arena.getSpectatorSpawn() != null) {
                writer.write("spectator-spawn: " + locationToString(arena.getSpectatorSpawn()) + "\n");
            }
            
            writer.write("enabled: " + arena.isEnabled() + "\n");
            writer.write("kits-enabled: " + arena.isKitsEnabled() + "\n");
            writer.write("rules-enabled: " + arena.isRulesEnabled() + "\n");
            
            if (arena.getDefaultKit() != null) {
                writer.write("default-kit: " + arena.getDefaultKit() + "\n");
            }
            
            writer.close();
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao exportar arena: " + e.getMessage());
            return false;
        }
    }

    /**
     * Importa uma arena de arquivo.
     */
    public boolean importArena(File importFile, String arenaName) {
        try {
            org.bukkit.configuration.file.YamlConfiguration config = 
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(importFile);
            
            Arena arena = new Arena(arenaName);
            
            if (config.contains("player1-spawn")) {
                arena.setPlayer1Spawn(parseLocation(config.getString("player1-spawn")));
            }
            if (config.contains("player2-spawn")) {
                arena.setPlayer2Spawn(parseLocation(config.getString("player2-spawn")));
            }
            if (config.contains("spectator-spawn")) {
                arena.setSpectatorSpawn(parseLocation(config.getString("spectator-spawn")));
            }
            
            if (config.contains("enabled")) {
                arena.setEnabled(config.getBoolean("enabled"));
            }
            if (config.contains("kits-enabled")) {
                arena.setKitsEnabled(config.getBoolean("kits-enabled"));
            }
            if (config.contains("rules-enabled")) {
                arena.setRulesEnabled(config.getBoolean("rules-enabled"));
            }
            if (config.contains("default-kit")) {
                arena.setDefaultKit(config.getString("default-kit"));
            }
            
            arenaManager.addArena(arenaName, arena);
            arenaManager.saveArena(arenaName, arena);
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao importar arena: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtém todos os templates disponíveis.
     */
    public Map<String, ArenaTemplate> getTemplates() {
        return templates;
    }

    /**
     * Calcula o centro entre duas posições.
     */
    private Location getCenter(Location loc1, Location loc2) {
        double x = (loc1.getX() + loc2.getX()) / 2;
        double y = (loc1.getY() + loc2.getY()) / 2;
        double z = (loc1.getZ() + loc2.getZ()) / 2;
        return new Location(loc1.getWorld(), x, y, z);
    }

    /**
     * Formata localização para string.
     */
    private String formatLocation(Location loc) {
        return String.format("X: %.1f, Y: %.1f, Z: %.1f", loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Converte localização para string.
     */
    private String locationToString(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + 
               "," + loc.getYaw() + "," + loc.getPitch();
    }

    /**
     * Converte string para localização.
     */
    private Location parseLocation(String locString) {
        if (locString == null) return null;
        String[] parts = locString.split(",");
        if (parts.length < 4) return null;

        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
        float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}

