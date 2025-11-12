package dev.artix.artixduels.models;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa um cosmético disponível no plugin.
 */
public class Cosmetic {
    private String id;
    private String name;
    private String description;
    private CosmeticType type;
    private CosmeticRarity rarity;
    private boolean unlocked;
    private Map<String, Object> data;
    private Material displayMaterial;
    private int displayData;
    private int price;
    private String unlockRequirement;

    public Cosmetic(String id, String name, String description, CosmeticType type,
                   CosmeticRarity rarity, boolean unlocked, Map<String, Object> data,
                   Material displayMaterial, int displayData, int price, String unlockRequirement) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.rarity = rarity;
        this.unlocked = unlocked;
        this.data = data != null ? data : new HashMap<>();
        this.displayMaterial = displayMaterial;
        this.displayData = displayData;
        this.price = price;
        this.unlockRequirement = unlockRequirement;
    }

    public static Cosmetic fromConfig(ConfigurationSection section) {
        String id = section.getName();
        String name = section.getString("name", "Cosmético");
        String description = section.getString("description", "");
        CosmeticType type = CosmeticType.fromString(section.getString("type", "VICTORY_EFFECT"));
        CosmeticRarity rarity = CosmeticRarity.fromString(section.getString("rarity", "COMMON"));
        boolean unlocked = section.getBoolean("unlocked", false);
        int price = section.getInt("price", 0);
        String unlockRequirement = section.getString("unlock-requirement", "");

        Map<String, Object> data = new HashMap<>();
        if (section.contains("data")) {
            ConfigurationSection dataSection = section.getConfigurationSection("data");
            if (dataSection != null) {
                for (String key : dataSection.getKeys(false)) {
                    data.put(key, dataSection.get(key));
                }
            }
        }

        Material displayMaterial = Material.valueOf(section.getString("display-material", "PAPER"));
        int displayData = section.getInt("display-data", 0);

        return new Cosmetic(id, name, description, type, rarity, unlocked, data,
                           displayMaterial, displayData, price, unlockRequirement);
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

    public CosmeticType getType() {
        return type;
    }

    public CosmeticRarity getRarity() {
        return rarity;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public int getDisplayData() {
        return displayData;
    }

    public int getPrice() {
        return price;
    }

    public String getUnlockRequirement() {
        return unlockRequirement;
    }

    public enum CosmeticType {
        VICTORY_EFFECT("victory_effect"),
        TRAIL("trail"),
        KILL_EFFECT("kill_effect"),
        TITLE("title"),
        BADGE("badge"),
        ARENA_SKIN("arena_skin");

        private String name;

        CosmeticType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static CosmeticType fromString(String name) {
            for (CosmeticType type : values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return VICTORY_EFFECT;
        }
    }

    public enum CosmeticRarity {
        COMMON("common", "&7"),
        UNCOMMON("uncommon", "&a"),
        RARE("rare", "&b"),
        EPIC("epic", "&5"),
        LEGENDARY("legendary", "&6"),
        MYTHIC("mythic", "&d");

        private String name;
        private String color;

        CosmeticRarity(String name, String color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        public static CosmeticRarity fromString(String name) {
            for (CosmeticRarity rarity : values()) {
                if (rarity.name.equalsIgnoreCase(name)) {
                    return rarity;
                }
            }
            return COMMON;
        }
    }
}

