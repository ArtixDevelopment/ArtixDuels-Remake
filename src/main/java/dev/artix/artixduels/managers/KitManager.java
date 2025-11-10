package dev.artix.artixduels.managers;

import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitManager {
    private Map<String, Kit> kits;
    private File configFile;
    private FileConfiguration config;

    public KitManager(FileConfiguration config, File configFile) {
        this.kits = new HashMap<>();
        this.config = config;
        this.configFile = configFile;
        loadKits(config);
    }

    private void loadKits(FileConfiguration config) {
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection == null) return;

        for (String kitName : kitsSection.getKeys(false)) {
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);
            if (kitSection == null) continue;

            String displayName = kitSection.getString("display-name", kitName);
            String modeString = kitSection.getString("mode", "BEDFIGHT");
            DuelMode mode = dev.artix.artixduels.models.DuelMode.fromString(modeString);
            if (mode == null) {
                mode = dev.artix.artixduels.models.DuelMode.BEDFIGHT;
            }
            Kit kit = new Kit(kitName, displayName, mode);

            if (kitSection.contains("contents")) {
                @SuppressWarnings("unchecked")
                java.util.List<ItemStack> contentsList = (java.util.List<ItemStack>) kitSection.getList("contents");
                if (contentsList != null) {
                    ItemStack[] contents = contentsList.toArray(new ItemStack[36]);
                    kit.setContents(contents);
                }
            }

            if (kitSection.contains("armor")) {
                @SuppressWarnings("unchecked")
                java.util.List<ItemStack> armorList = (java.util.List<ItemStack>) kitSection.getList("armor");
                if (armorList != null) {
                    ItemStack[] armor = armorList.toArray(new ItemStack[4]);
                    kit.setArmor(armor);
                }
            }

            kits.put(kitName, kit);
        }
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Map<String, Kit> getKits() {
        return kits;
    }

    public void addKit(String name, Kit kit) {
        kits.put(name, kit);
    }

    public void removeKit(String name) {
        kits.remove(name);
    }

    public boolean kitExists(String name) {
        return kits.containsKey(name);
    }

    public void saveKit(String name, Kit kit) {
        String path = "kits." + name;
        config.set(path + ".display-name", kit.getDisplayName());
        config.set(path + ".mode", kit.getMode().getName());
        
        List<ItemStack> contentsList = new ArrayList<>();
        if (kit.getContents() != null) {
            for (ItemStack item : kit.getContents()) {
                if (item != null) {
                    contentsList.add(item);
                } else {
                    contentsList.add(null);
                }
            }
        }
        config.set(path + ".contents", contentsList);
        
        List<ItemStack> armorList = new ArrayList<>();
        if (kit.getArmor() != null) {
            for (ItemStack item : kit.getArmor()) {
                if (item != null) {
                    armorList.add(item);
                } else {
                    armorList.add(null);
                }
            }
        }
        config.set(path + ".armor", armorList);
        
        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Erro ao salvar kit: " + e.getMessage());
        }
    }
}

