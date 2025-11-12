package dev.artix.artixduels.models;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa um desafio diário ou semanal.
 */
public class Challenge {
    private String id;
    private String name;
    private String description;
    private ChallengeType type;
    private ChallengeResetType resetType;
    private ChallengeObjective objective;
    private int target;
    private Map<String, Object> rewards;
    private Material displayMaterial;
    private int displayData;
    private int priority;
    private Map<String, Object> metadata;

    public Challenge(String id, String name, String description, ChallengeType type, 
                     ChallengeResetType resetType, ChallengeObjective objective, int target,
                     Map<String, Object> rewards, Material displayMaterial, int displayData, int priority) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.resetType = resetType;
        this.objective = objective;
        this.target = target;
        this.rewards = rewards != null ? rewards : new HashMap<>();
        this.displayMaterial = displayMaterial;
        this.displayData = displayData;
        this.priority = priority;
        this.metadata = new HashMap<>();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public static Challenge fromConfig(ConfigurationSection section) {
        String id = section.getName();
        String name = section.getString("name", "Desafio");
        String description = section.getString("description", "");
        ChallengeType type = ChallengeType.fromString(section.getString("type", "DAILY"));
        ChallengeResetType resetType = ChallengeResetType.fromString(section.getString("reset-type", "DAILY"));
        ChallengeObjective objective = ChallengeObjective.fromString(section.getString("objective", "WIN_DUELS"));
        int target = section.getInt("target", 1);
        
        Map<String, Object> rewards = new HashMap<>();
        if (section.contains("rewards")) {
            ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                for (String key : rewardsSection.getKeys(false)) {
                    rewards.put(key, rewardsSection.get(key));
                }
            }
        }
        
        Material displayMaterial = Material.valueOf(section.getString("display-material", "PAPER"));
        int displayData = section.getInt("display-data", 0);
        int priority = section.getInt("priority", 0);
        
        Challenge challenge = new Challenge(id, name, description, type, resetType, objective, target,
                           rewards, displayMaterial, displayData, priority);
        
        if (section.contains("mode")) {
            challenge.setMetadata("mode", section.getString("mode"));
        }
        
        return challenge;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ChallengeType getType() {
        return type;
    }

    public ChallengeResetType getResetType() {
        return resetType;
    }

    public ChallengeObjective getObjective() {
        return objective;
    }

    public int getTarget() {
        return target;
    }

    public Map<String, Object> getRewards() {
        return rewards;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public int getDisplayData() {
        return displayData;
    }

    public int getPriority() {
        return priority;
    }

    public enum ChallengeType {
        DAILY("daily"),
        WEEKLY("weekly");

        private String name;

        ChallengeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static ChallengeType fromString(String name) {
            for (ChallengeType type : values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return DAILY;
        }
    }

    public enum ChallengeResetType {
        DAILY("daily"),
        WEEKLY("weekly");

        private String name;

        ChallengeResetType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static ChallengeResetType fromString(String name) {
            for (ChallengeResetType type : values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return DAILY;
        }
    }

    public enum ChallengeObjective {
        WIN_DUELS("win_duels"),
        WIN_DUELS_MODE("win_duels_mode"),
        GET_KILLS("get_kills"),
        GET_KILLS_MODE("get_kills_mode"),
        REACH_STREAK("reach_streak"),
        GAIN_ELO("gain_elo"),
        GAIN_XP("gain_xp"),
        PLAY_DUELS("play_duels"),
        ACCEPT_DUELS("accept_duels");

        private String name;

        ChallengeObjective(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static ChallengeObjective fromString(String name) {
            for (ChallengeObjective objective : values()) {
                if (objective.name.equalsIgnoreCase(name)) {
                    return objective;
                }
            }
            return WIN_DUELS;
        }
    }
}

