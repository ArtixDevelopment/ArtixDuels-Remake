package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.Kit;
import dev.artix.artixduels.models.KitTemplate;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Editor avançado de kits.
 */
public class KitEditor {
    private final ArtixDuels plugin;
    private final KitManager kitManager;
    private Map<UUID, String> editingSessions;
    private Map<String, KitTemplate> templates;
    private Map<UUID, Set<String>> favorites;
    private Map<UUID, String> previewSessions;

    public KitEditor(ArtixDuels plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.editingSessions = new HashMap<>();
        this.templates = new HashMap<>();
        this.favorites = new HashMap<>();
        this.previewSessions = new HashMap<>();
        
        loadTemplates();
        loadFavorites();
    }

    /**
     * Inicia uma sessão de edição de kit.
     */
    public boolean startEditSession(Player player, String kitName) {
        if (editingSessions.containsKey(player.getUniqueId())) {
            player.sendMessage("§cVocê já está editando um kit! Use /kiteditor cancel para cancelar.");
            return false;
        }

        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            player.sendMessage("§cKit não encontrado!");
            return false;
        }

        editingSessions.put(player.getUniqueId(), kitName);
        player.sendMessage("§aSessão de edição iniciada para o kit: §e" + kitName);
        player.sendMessage("§7Edite seu inventário e use §e/kiteditor save §7para salvar.");
        
        return true;
    }

    /**
     * Cancela uma sessão de edição.
     */
    public void cancelEditSession(UUID playerId) {
        editingSessions.remove(playerId);
        previewSessions.remove(playerId);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendMessage("§cSessão de edição cancelada.");
        }
    }

    /**
     * Salva o kit editado.
     */
    public boolean saveKit(Player player) {
        String kitName = editingSessions.get(player.getUniqueId());
        if (kitName == null) {
            player.sendMessage("§cVocê não está editando um kit!");
            return false;
        }

        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            player.sendMessage("§cKit não encontrado!");
            return false;
        }

        kit.setContents(player.getInventory().getContents().clone());
        kit.setArmor(player.getInventory().getArmorContents().clone());

        kitManager.saveKit(kitName, kit);
        editingSessions.remove(player.getUniqueId());
        
        player.sendMessage("§aKit §e" + kitName + " §asalvo com sucesso!");
        return true;
    }

    /**
     * Cria um kit a partir de um template.
     */
    public boolean createFromTemplate(Player player, String templateName, String kitName) {
        KitTemplate template = templates.get(templateName);
        if (template == null) {
            player.sendMessage("§cTemplate não encontrado!");
            return false;
        }

        if (kitManager.kitExists(kitName)) {
            player.sendMessage("§cKit já existe!");
            return false;
        }

        Kit kit = new Kit(kitName, template.getDisplayName(), template.getMode());
        if (template.getContents() != null) {
            kit.setContents(template.getContents().clone());
        }
        if (template.getArmor() != null) {
            kit.setArmor(template.getArmor().clone());
        }

        kitManager.addKit(kitName, kit);
        kitManager.saveKit(kitName, kit);
        
        player.sendMessage("§aKit criado a partir do template: §e" + template.getDisplayName());
        return true;
    }

    /**
     * Inicia preview de um kit.
     */
    public boolean startPreview(Player player, String kitName) {
        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            player.sendMessage("§cKit não encontrado!");
            return false;
        }

        // Aplicar kit
        player.getInventory().setContents(kit.getContents() != null ? kit.getContents().clone() : new ItemStack[36]);
        player.getInventory().setArmorContents(kit.getArmor() != null ? kit.getArmor().clone() : new ItemStack[4]);
        
        previewSessions.put(player.getUniqueId(), kitName);
        player.sendMessage("§aPreview do kit §e" + kitName + " §aativado!");
        player.sendMessage("§7Use §e/kiteditor preview stop §7para sair do preview.");
        
        return true;
    }

    /**
     * Para o preview.
     */
    public void stopPreview(Player player) {
        String kitName = previewSessions.remove(player.getUniqueId());
        if (kitName != null) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.sendMessage("§cPreview desativado.");
        }
    }

    /**
     * Adiciona kit aos favoritos.
     */
    public void addFavorite(UUID playerId, String kitName) {
        favorites.computeIfAbsent(playerId, k -> new HashSet<>()).add(kitName);
        saveFavorites();
    }

    /**
     * Remove kit dos favoritos.
     */
    public void removeFavorite(UUID playerId, String kitName) {
        Set<String> playerFavorites = favorites.get(playerId);
        if (playerFavorites != null) {
            playerFavorites.remove(kitName);
            saveFavorites();
        }
    }

    /**
     * Verifica se kit é favorito.
     */
    public boolean isFavorite(UUID playerId, String kitName) {
        Set<String> playerFavorites = favorites.get(playerId);
        return playerFavorites != null && playerFavorites.contains(kitName);
    }

    /**
     * Obtém favoritos do jogador.
     */
    public Set<String> getFavorites(UUID playerId) {
        return favorites.getOrDefault(playerId, new HashSet<>());
    }

    /**
     * Importa kit de arquivo.
     */
    public boolean importKit(File importFile, String kitName) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(importFile);
            
            String displayName = config.getString("display-name", kitName);
            String modeString = config.getString("mode", "BEDFIGHT");
            DuelMode mode = DuelMode.fromString(modeString);
            if (mode == null) {
                mode = DuelMode.BEDFIGHT;
            }

            Kit kit = new Kit(kitName, displayName, mode);

            if (config.contains("contents")) {
                @SuppressWarnings("unchecked")
                List<ItemStack> contentsList = (List<ItemStack>) config.getList("contents");
                if (contentsList != null) {
                    ItemStack[] contents = contentsList.toArray(new ItemStack[36]);
                    kit.setContents(contents);
                }
            }

            if (config.contains("armor")) {
                @SuppressWarnings("unchecked")
                List<ItemStack> armorList = (List<ItemStack>) config.getList("armor");
                if (armorList != null) {
                    ItemStack[] armor = armorList.toArray(new ItemStack[4]);
                    kit.setArmor(armor);
                }
            }

            kitManager.addKit(kitName, kit);
            kitManager.saveKit(kitName, kit);
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao importar kit: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporta kit para arquivo.
     */
    public boolean exportKit(String kitName, File exportFile) {
        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            return false;
        }

        try {
            FileWriter writer = new FileWriter(exportFile);
            writer.write("# Kit Export: " + kitName + "\n");
            writer.write("name: " + kitName + "\n");
            writer.write("display-name: " + kit.getDisplayName() + "\n");
            writer.write("mode: " + kit.getMode().getName() + "\n");
            
            if (kit.getContents() != null) {
                writer.write("contents:\n");
                for (int i = 0; i < kit.getContents().length; i++) {
                    if (kit.getContents()[i] != null) {
                        writer.write("  - slot: " + i + "\n");
                        writer.write("    item: " + kit.getContents()[i].getType().toString() + "\n");
                    }
                }
            }
            
            if (kit.getArmor() != null) {
                writer.write("armor:\n");
                for (int i = 0; i < kit.getArmor().length; i++) {
                    if (kit.getArmor()[i] != null) {
                        writer.write("  - slot: " + i + "\n");
                        writer.write("    item: " + kit.getArmor()[i].getType().toString() + "\n");
                    }
                }
            }
            
            writer.close();
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao exportar kit: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carrega templates de kits.
     */
    private void loadTemplates() {
        // Template: PvP Básico
        KitTemplate pvpBasic = new KitTemplate("pvp_basic", "PvP Básico", "Kit básico para PvP", DuelMode.BEDFIGHT);
        templates.put("pvp_basic", pvpBasic);

        // Template: Soup
        KitTemplate soup = new KitTemplate("soup", "Soup", "Kit com sopas para regeneração", DuelMode.SOUP);
        templates.put("soup", soup);

        // Template: NoDebuff
        KitTemplate nodebuff = new KitTemplate("nodebuff", "NoDebuff", "Kit sem efeitos negativos", DuelMode.GLADIATOR);
        templates.put("nodebuff", nodebuff);
    }

    /**
     * Obtém todos os templates.
     */
    public Map<String, KitTemplate> getTemplates() {
        return templates;
    }

    /**
     * Carrega favoritos dos jogadores.
     */
    private void loadFavorites() {
        File prefsFile = new File(plugin.getDataFolder(), "kit_favorites.yml");
        if (!prefsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(prefsFile);
        if (config.contains("favorites")) {
            for (String playerIdStr : config.getConfigurationSection("favorites").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    List<String> kitNames = config.getStringList("favorites." + playerIdStr);
                    favorites.put(playerId, new HashSet<>(kitNames));
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }
    }

    /**
     * Salva favoritos dos jogadores.
     */
    private void saveFavorites() {
        File prefsFile = new File(plugin.getDataFolder(), "kit_favorites.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(prefsFile);

        for (Map.Entry<UUID, Set<String>> entry : favorites.entrySet()) {
            config.set("favorites." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        try {
            config.save(prefsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar favoritos: " + e.getMessage());
        }
    }

    /**
     * Obtém a sessão de edição de um jogador.
     */
    public String getEditSession(UUID playerId) {
        return editingSessions.get(playerId);
    }

    /**
     * Verifica se jogador está em preview.
     */
    public boolean isInPreview(UUID playerId) {
        return previewSessions.containsKey(playerId);
    }

    /**
     * Obtém o plugin.
     */
    public ArtixDuels getPlugin() {
        return plugin;
    }

    /**
     * Obtém o KitManager.
     */
    public KitManager getKitManager() {
        return kitManager;
    }
}

