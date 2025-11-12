package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Festival;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de festivais.
 */
public class FestivalManager {
    private final ArtixDuels plugin;
    private final RewardManager rewardManager;
    private Map<String, Festival> festivals;
    private Festival currentFestival;

    public FestivalManager(ArtixDuels plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.festivals = new HashMap<>();
        
        loadFestivals();
        checkCurrentFestival();
    }

    public void createFestival(String id, String name, long startTime, long endTime) {
        Festival festival = new Festival(id, name, startTime, endTime);
        festivals.put(id, festival);
        saveFestivals();
    }

    public void startFestival(String festivalId) {
        Festival festival = festivals.get(festivalId);
        if (festival == null) return;
        
        if (currentFestival != null) {
            currentFestival.setActive(false);
        }
        
        festival.setActive(true);
        currentFestival = festival;
        
        // Notificar jogadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("§6§l[FESTIVAL] §e" + festival.getName() + " começou!");
        }
        
        saveFestivals();
    }

    public void updateFestivalProgress(UUID playerId, String challengeId, int progress) {
        if (currentFestival == null) return;
        
        Festival.FestivalProgress playerProgress = currentFestival.getPlayerProgress()
            .computeIfAbsent(playerId, k -> new Festival.FestivalProgress(playerId));
        
        playerProgress.getChallengeProgress().put(challengeId, 
            playerProgress.getChallengeProgress().getOrDefault(challengeId, 0) + progress);
        
        saveFestivals();
    }

    public Festival getCurrentFestival() {
        return currentFestival;
    }

    private void checkCurrentFestival() {
        for (Festival festival : festivals.values()) {
            if (festival.isActive()) {
                currentFestival = festival;
                return;
            }
        }
    }

    private void loadFestivals() {
        File festivalsFile = new File(plugin.getDataFolder(), "festivals.yml");
        if (!festivalsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(festivalsFile);
        if (config.contains("festivals")) {
            for (String festivalId : config.getConfigurationSection("festivals").getKeys(false)) {
                String path = "festivals." + festivalId;
                String name = config.getString(path + ".name");
                long startTime = config.getLong(path + ".start-time");
                long endTime = config.getLong(path + ".end-time");
                boolean active = config.getBoolean(path + ".active", false);

                Festival festival = new Festival(festivalId, name, startTime, endTime);
                festival.setActive(active);
                festivals.put(festivalId, festival);
            }
        }
    }

    private void saveFestivals() {
        File festivalsFile = new File(plugin.getDataFolder(), "festivals.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(festivalsFile);

        for (Map.Entry<String, Festival> entry : festivals.entrySet()) {
            String path = "festivals." + entry.getKey();
            Festival festival = entry.getValue();
            config.set(path + ".name", festival.getName());
            config.set(path + ".start-time", festival.getStartTime());
            config.set(path + ".end-time", festival.getEndTime());
            config.set(path + ".active", festival.isActive());
        }

        try {
            config.save(festivalsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar festivais: " + e.getMessage());
        }
    }
}

