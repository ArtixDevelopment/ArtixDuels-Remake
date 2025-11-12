package dev.artix.artixduels.models;

import java.util.List;
import java.util.Map;

/**
 * Representa uma loot box.
 */
public class LootBox {
    private String id;
    private String name;
    private LootBoxRarity rarity;
    private List<LootBoxReward> rewards;
    private double dropChance;
    private String icon;

    public enum LootBoxRarity {
        COMMON("&7Comum"),
        RARE("&bRaro"),
        EPIC("&5Épico"),
        LEGENDARY("&6Lendário");

        private String displayName;

        LootBoxRarity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public LootBox(String id, String name, LootBoxRarity rarity) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
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

    public LootBoxRarity getRarity() {
        return rarity;
    }

    public void setRarity(LootBoxRarity rarity) {
        this.rarity = rarity;
    }

    public List<LootBoxReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<LootBoxReward> rewards) {
        this.rewards = rewards;
    }

    public double getDropChance() {
        return dropChance;
    }

    public void setDropChance(double dropChance) {
        this.dropChance = dropChance;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public static class LootBoxReward {
        private String type;
        private Map<String, Object> data;
        private double chance;

        public LootBoxReward(String type, Map<String, Object> data, double chance) {
            this.type = type;
            this.data = data;
            this.chance = chance;
        }

        public String getType() {
            return type;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public double getChance() {
            return chance;
        }
    }
}

