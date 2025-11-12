package dev.artix.artixduels.models;

import java.util.List;

/**
 * Representa uma conquista.
 */
public class Achievement {
    private String id;
    private String name;
    private String description;
    private String category;
    private AchievementRarity rarity;
    private int targetValue;
    private String targetType;
    private List<String> rewards;
    private String icon;

    public enum AchievementRarity {
        COMMON("&7Comum"),
        RARE("&bRaro"),
        EPIC("&5Épico"),
        LEGENDARY("&6Lendário");

        private String displayName;

        AchievementRarity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Achievement(String id, String name, String description, String category, 
                      AchievementRarity rarity, int targetValue, String targetType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.rarity = rarity;
        this.targetValue = targetValue;
        this.targetType = targetType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public AchievementRarity getRarity() {
        return rarity;
    }

    public void setRarity(AchievementRarity rarity) {
        this.rarity = rarity;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public void setRewards(List<String> rewards) {
        this.rewards = rewards;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}

