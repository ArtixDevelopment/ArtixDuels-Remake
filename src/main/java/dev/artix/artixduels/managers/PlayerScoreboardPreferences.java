package dev.artix.artixduels.managers;

import dev.artix.artixduels.models.DuelMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerScoreboardPreferences {
    private File preferencesFile;
    private FileConfiguration preferencesConfig;
    private Map<UUID, DuelMode> playerActiveMode;

    public PlayerScoreboardPreferences(File dataFolder) {
        this.preferencesFile = new File(dataFolder, "scoreboard_preferences.yml");
        this.playerActiveMode = new HashMap<>();
        loadPreferences();
    }

    private void loadPreferences() {
        if (!preferencesFile.exists()) {
            try {
                preferencesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        preferencesConfig = YamlConfiguration.loadConfiguration(preferencesFile);
        
        if (preferencesConfig.contains("players")) {
            for (String uuidString : preferencesConfig.getConfigurationSection("players").getKeys(false)) {
                UUID playerId = UUID.fromString(uuidString);
                String modeName = preferencesConfig.getString("players." + uuidString + ".active-mode");
                
                if (modeName != null) {
                    DuelMode mode = DuelMode.fromString(modeName.toUpperCase());
                    if (mode != null) {
                        playerActiveMode.put(playerId, mode);
                    }
                }
            }
        }
    }

    public DuelMode getPlayerActiveMode(UUID playerId) {
        DuelMode mode = playerActiveMode.get(playerId);
        if (mode == null) {
            // Por padrão, usar o primeiro modo
            mode = DuelMode.values().length > 0 ? DuelMode.values()[0] : null;
            if (mode != null) {
                playerActiveMode.put(playerId, mode);
                savePreferences();
            }
        }
        return mode;
    }

    public void setPlayerActiveMode(UUID playerId, DuelMode mode) {
        if (mode == null) {
            // Se null, usar o primeiro modo
            mode = DuelMode.values().length > 0 ? DuelMode.values()[0] : null;
        }
        if (mode != null) {
            playerActiveMode.put(playerId, mode);
            savePreferences();
        }
    }

    private void savePreferences() {
        for (Map.Entry<UUID, DuelMode> entry : playerActiveMode.entrySet()) {
            preferencesConfig.set("players." + entry.getKey().toString() + ".active-mode", entry.getValue().getName());
        }
        
        try {
            preferencesConfig.save(preferencesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        playerActiveMode.clear();
        loadPreferences();
    }
}

