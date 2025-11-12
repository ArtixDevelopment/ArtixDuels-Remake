package dev.artix.artixduels.models;

import org.bukkit.inventory.ItemStack;

/**
 * Template de kit para criação rápida.
 */
public class KitTemplate {
    private String name;
    private String displayName;
    private String description;
    private DuelMode mode;
    private ItemStack[] contents;
    private ItemStack[] armor;

    public KitTemplate(String name, String displayName, String description, DuelMode mode) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.mode = mode;
        this.contents = new ItemStack[36];
        this.armor = new ItemStack[4];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DuelMode getMode() {
        return mode;
    }

    public void setMode(DuelMode mode) {
        this.mode = mode;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }
}

