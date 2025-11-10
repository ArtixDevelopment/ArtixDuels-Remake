package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DuelGUI {
    private KitManager kitManager;
    private ArenaManager arenaManager;

    public DuelGUI(KitManager kitManager, ArenaManager arenaManager) {
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
    }

    public void openKitSelection(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6Selecione um Kit");

        int slot = 0;
        for (Kit kit : kitManager.getKits().values()) {
            ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a" + kit.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Clique para selecionar este kit");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot, item);
            slot++;
        }

        player.openInventory(gui);
    }

    public void openArenaSelection(Player player, String kitName) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6Selecione uma Arena");

        int slot = 0;
        for (String arenaName : arenaManager.getArenas().keySet()) {
            ItemStack item = new ItemStack(Material.GRASS);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a" + arenaName);
            List<String> lore = new ArrayList<>();
            lore.add("§7Clique para selecionar esta arena");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot, item);
            slot++;
        }

        player.openInventory(gui);
    }
}

