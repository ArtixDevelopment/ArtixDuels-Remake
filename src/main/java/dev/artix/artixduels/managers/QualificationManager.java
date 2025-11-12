package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Qualification;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de qualificações.
 */
public class QualificationManager {
    private final ArtixDuels plugin;
    private final StatsManager statsManager;
    private Map<UUID, Qualification> qualifications;
    private List<Qualification> sortedQualifications;

    public QualificationManager(ArtixDuels plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.qualifications = new HashMap<>();
        this.sortedQualifications = new ArrayList<>();
        
        loadQualifications();
        updateRankings();
    }

    public void updateQualificationPoints(UUID playerId, int points) {
        Qualification qual = qualifications.computeIfAbsent(playerId, Qualification::new);
        qual.addPoints(points);
        qual.setLastUpdate(System.currentTimeMillis());
        updateRankings();
        saveQualifications();
    }

    public Qualification getQualification(UUID playerId) {
        return qualifications.computeIfAbsent(playerId, Qualification::new);
    }

    public int getQualificationRank(UUID playerId) {
        Qualification qual = qualifications.get(playerId);
        return qual != null ? qual.getRank() : 0;
    }

    public String getQualificationTier(UUID playerId) {
        Qualification qual = qualifications.get(playerId);
        return qual != null ? qual.getTier() : "BRONZE";
    }

    public boolean canParticipateInTournament(UUID playerId, int requiredPoints) {
        Qualification qual = qualifications.get(playerId);
        return qual != null && qual.canParticipateInTournament(requiredPoints);
    }

    private void updateRankings() {
        sortedQualifications = new ArrayList<>(qualifications.values());
        sortedQualifications.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints()));
        
        for (int i = 0; i < sortedQualifications.size(); i++) {
            sortedQualifications.get(i).setRank(i + 1);
        }
    }

    public List<Qualification> getTopQualifications(int limit) {
        return sortedQualifications.subList(0, Math.min(limit, sortedQualifications.size()));
    }

    private void loadQualifications() {
        File qualFile = new File(plugin.getDataFolder(), "qualifications.yml");
        if (!qualFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(qualFile);
        if (config.contains("qualifications")) {
            for (String playerIdStr : config.getConfigurationSection("qualifications").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    String path = "qualifications." + playerIdStr;
                    int points = config.getInt(path + ".points", 0);
                    String tier = config.getString(path + ".tier", "BRONZE");
                    
                    Qualification qual = new Qualification(playerId);
                    qual.setPoints(points);
                    qual.setTier(tier);
                    qualifications.put(playerId, qual);
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }
    }

    private void saveQualifications() {
        File qualFile = new File(plugin.getDataFolder(), "qualifications.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(qualFile);

        for (Map.Entry<UUID, Qualification> entry : qualifications.entrySet()) {
            String path = "qualifications." + entry.getKey().toString();
            Qualification qual = entry.getValue();
            config.set(path + ".points", qual.getPoints());
            config.set(path + ".tier", qual.getTier());
            config.set(path + ".rank", qual.getRank());
        }

        try {
            config.save(qualFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar qualificações: " + e.getMessage());
        }
    }
}

