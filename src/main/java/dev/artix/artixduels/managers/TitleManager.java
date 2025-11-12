package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.PlayerStats;
import dev.artix.artixduels.models.Title;
import dev.artix.artixduels.models.TitleProgress;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de títulos e badges.
 */
public class TitleManager {
    private final ArtixDuels plugin;
    private final StatsManager statsManager;
    private final AchievementManager achievementManager;
    private Map<String, Title> titles;
    private Map<UUID, String> activeTitles;
    private Map<UUID, Set<String>> unlockedTitles;
    private Map<UUID, Map<String, TitleProgress>> titleProgress;

    public TitleManager(ArtixDuels plugin, StatsManager statsManager, AchievementManager achievementManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.achievementManager = achievementManager;
        this.titles = new HashMap<>();
        this.activeTitles = new HashMap<>();
        this.unlockedTitles = new HashMap<>();
        this.titleProgress = new HashMap<>();
        
        loadTitles();
        loadPlayerTitles();
    }

    /**
     * Carrega títulos do arquivo de configuração.
     */
    private void loadTitles() {
        File titlesFile = new File(plugin.getDataFolder(), "titles.yml");
        if (!titlesFile.exists()) {
            createDefaultTitles(titlesFile);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(titlesFile);
        if (config.contains("titles")) {
            for (String titleId : config.getConfigurationSection("titles").getKeys(false)) {
                String path = "titles." + titleId;
                String name = config.getString(path + ".name");
                String displayName = config.getString(path + ".display-name");
                String description = config.getString(path + ".description");
                String rarityStr = config.getString(path + ".rarity", "COMMON");
                Title.TitleRarity rarity = Title.TitleRarity.valueOf(rarityStr.toUpperCase());
                List<String> requirements = config.getStringList(path + ".requirements");
                String badgeIcon = config.getString(path + ".badge-icon", "STAR");

                Title title = new Title(titleId, name, displayName, description, rarity);
                title.setRequirements(requirements);
                title.setBadgeIcon(badgeIcon);
                titles.put(titleId, title);
            }
        }
    }

    /**
     * Cria títulos padrão.
     */
    private void createDefaultTitles(File file) {
        try {
            file.createNewFile();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Título: Novato
            config.set("titles.novice.name", "Novato");
            config.set("titles.novice.display-name", "&7[Novato]");
            config.set("titles.novice.description", "Primeiro título desbloqueado");
            config.set("titles.novice.rarity", "COMMON");
            config.set("titles.novice.requirements", Arrays.asList("wins:1"));

            // Título: Vencedor
            config.set("titles.winner.name", "Vencedor");
            config.set("titles.winner.display-name", "&a[Vencedor]");
            config.set("titles.winner.description", "Ganhe 10 duelos");
            config.set("titles.winner.rarity", "COMMON");
            config.set("titles.winner.requirements", Arrays.asList("wins:10"));

            // Título: Mestre
            config.set("titles.master.name", "Mestre");
            config.set("titles.master.display-name", "&6[Mestre]");
            config.set("titles.master.description", "Ganhe 100 duelos");
            config.set("titles.master.rarity", "RARE");
            config.set("titles.master.requirements", Arrays.asList("wins:100"));

            // Título: Elite
            config.set("titles.elite.name", "Elite");
            config.set("titles.elite.display-name", "&5[Elite]");
            config.set("titles.elite.description", "Alcance 2000 de ELO");
            config.set("titles.elite.rarity", "EPIC");
            config.set("titles.elite.requirements", Arrays.asList("elo:2000"));

            // Título: Lendário
            config.set("titles.legendary.name", "Lendário");
            config.set("titles.legendary.display-name", "&6&l[Lendário]");
            config.set("titles.legendary.description", "Alcance 2500 de ELO e ganhe 500 duelos");
            config.set("titles.legendary.rarity", "LEGENDARY");
            config.set("titles.legendary.requirements", Arrays.asList("elo:2500", "wins:500"));

            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao criar arquivo de títulos: " + e.getMessage());
        }
    }

    /**
     * Verifica e atualiza progresso de títulos de um jogador.
     */
    public void updateTitleProgress(UUID playerId) {
        PlayerStats stats = statsManager.getPlayerStats(playerId, null);
        if (stats == null) return;

        Map<String, TitleProgress> progress = titleProgress.computeIfAbsent(playerId, k -> new HashMap<>());
        Set<String> unlocked = unlockedTitles.computeIfAbsent(playerId, k -> new HashSet<>());

        for (Title title : titles.values()) {
            if (unlocked.contains(title.getId())) continue;

            TitleProgress titleProgress = progress.computeIfAbsent(title.getId(), 
                k -> new TitleProgress(playerId, title.getId()));

            boolean allRequirementsMet = true;
            for (String requirement : title.getRequirements()) {
                String[] parts = requirement.split(":");
                if (parts.length != 2) continue;

                String reqType = parts[0];
                int reqValue = Integer.parseInt(parts[1]);

                int currentValue = getRequirementValue(stats, reqType);
                titleProgress.addRequirementProgress(reqType, currentValue);

                if (currentValue < reqValue) {
                    allRequirementsMet = false;
                }
            }

            if (allRequirementsMet && !unlocked.contains(title.getId())) {
                unlockTitle(playerId, title);
            }
        }

        savePlayerTitles(playerId);
    }

    /**
     * Obtém valor de um requisito.
     */
    private int getRequirementValue(PlayerStats stats, String reqType) {
        switch (reqType.toLowerCase()) {
            case "wins":
                return stats.getWins();
            case "losses":
                return stats.getLosses();
            case "elo":
                return stats.getElo();
            case "streak":
                return stats.getBestWinStreak();
            case "xp":
                return stats.getXp();
            case "level":
                return stats.getLevel();
            default:
                return 0;
        }
    }

    /**
     * Desbloqueia um título.
     */
    private void unlockTitle(UUID playerId, Title title) {
        unlockedTitles.computeIfAbsent(playerId, k -> new HashSet<>()).add(title.getId());
        
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendMessage("§6§l=== TÍTULO DESBLOQUEADO ===");
            player.sendMessage(title.getRarity().getDisplayName() + " §e" + title.getDisplayName());
            player.sendMessage("§7" + title.getDescription());
        }

        savePlayerTitles(playerId);
    }

    /**
     * Define título ativo de um jogador.
     */
    public void setActiveTitle(UUID playerId, String titleId) {
        Set<String> unlocked = unlockedTitles.get(playerId);
        if (unlocked == null || !unlocked.contains(titleId)) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§cVocê não desbloqueou este título!");
            }
            return;
        }

        activeTitles.put(playerId, titleId);
        savePlayerTitles(playerId);
    }

    /**
     * Remove título ativo de um jogador.
     */
    public void removeActiveTitle(UUID playerId) {
        activeTitles.remove(playerId);
        savePlayerTitles(playerId);
    }

    /**
     * Obtém título ativo de um jogador.
     */
    public String getActiveTitle(UUID playerId) {
        return activeTitles.get(playerId);
    }

    /**
     * Obtém display name do título ativo.
     */
    public String getActiveTitleDisplay(UUID playerId) {
        String titleId = activeTitles.get(playerId);
        if (titleId == null) return "";
        
        Title title = titles.get(titleId);
        return title != null ? title.getDisplayName() : "";
    }

    /**
     * Obtém todos os títulos.
     */
    public Map<String, Title> getTitles() {
        return titles;
    }

    /**
     * Obtém títulos desbloqueados de um jogador.
     */
    public Set<String> getUnlockedTitles(UUID playerId) {
        return unlockedTitles.getOrDefault(playerId, new HashSet<>());
    }

    /**
     * Obtém progresso de títulos de um jogador.
     */
    public Map<String, TitleProgress> getTitleProgress(UUID playerId) {
        return titleProgress.computeIfAbsent(playerId, k -> new HashMap<>());
    }

    /**
     * Carrega títulos dos jogadores.
     */
    private void loadPlayerTitles() {
        File prefsFile = new File(plugin.getDataFolder(), "player_titles.yml");
        if (!prefsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(prefsFile);
        
        if (config.contains("active")) {
            for (String playerIdStr : config.getConfigurationSection("active").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    String titleId = config.getString("active." + playerIdStr);
                    activeTitles.put(playerId, titleId);
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }

        if (config.contains("unlocked")) {
            for (String playerIdStr : config.getConfigurationSection("unlocked").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    List<String> titleIds = config.getStringList("unlocked." + playerIdStr);
                    unlockedTitles.put(playerId, new HashSet<>(titleIds));
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }
    }

    /**
     * Salva títulos de um jogador.
     */
    private void savePlayerTitles(UUID playerId) {
        File prefsFile = new File(plugin.getDataFolder(), "player_titles.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(prefsFile);

        String titleId = activeTitles.get(playerId);
        if (titleId != null) {
            config.set("active." + playerId.toString(), titleId);
        }

        Set<String> unlocked = unlockedTitles.get(playerId);
        if (unlocked != null) {
            config.set("unlocked." + playerId.toString(), new ArrayList<>(unlocked));
        }

        try {
            config.save(prefsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar títulos: " + e.getMessage());
        }
    }
}

