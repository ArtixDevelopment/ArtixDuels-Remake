package dev.artix.artixduels.npcs;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.managers.PlaceholderManager;
import dev.artix.artixduels.managers.StatsManager;
import dev.artix.artixduels.models.DuelMode;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DuelNPC {
    private ArtixDuels plugin;
    private Map<String, NPC> npcs;
    private Map<String, DuelMode> npcModes;
    private DuelManager duelManager;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private StatsManager statsManager;
    private PlaceholderManager placeholderManager;
    private HologramManager hologramManager;

    public DuelNPC(ArtixDuels plugin, DuelManager duelManager, KitManager kitManager, ArenaManager arenaManager, StatsManager statsManager, PlaceholderManager placeholderManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
        this.statsManager = statsManager;
        this.placeholderManager = placeholderManager;
        this.npcs = new HashMap<>();
        this.npcModes = new HashMap<>();
        this.hologramManager = new HologramManager(plugin, duelManager, statsManager, placeholderManager);
    }

    public void loadNPCs(FileConfiguration config) {
        if (!CitizensAPI.hasImplementation()) {
            plugin.getLogger().warning("Citizens não está instalado! NPCs não serão criados.");
            return;
        }

        ConfigurationSection npcsMainSection = config.getConfigurationSection("npcs");
        if (npcsMainSection == null) return;

        boolean enabled = npcsMainSection.getBoolean("enabled", true);
        if (!enabled) {
            plugin.getLogger().info("NPCs estão desabilitados no npcs.yml");
            return;
        }

        for (String npcName : npcsMainSection.getKeys(false)) {
            if (npcName.equals("enabled")) continue;
            
            ConfigurationSection npcSection = npcsMainSection.getConfigurationSection(npcName);
            if (npcSection == null) continue;

            String modeString = npcSection.getString("mode", "");
            DuelMode mode = DuelMode.fromString(modeString);
            if (mode == null) {
                plugin.getLogger().warning("Modo inválido para NPC: " + npcName + " (modo: " + modeString + ")");
                continue;
            }

            String displayName = ChatColor.translateAlternateColorCodes('&', npcSection.getString("display-name", npcName));
            String skinName = npcSection.getString("skin", "");
            Location location = parseLocation(npcSection.getString("location", ""));

            if (location == null) {
                plugin.getLogger().warning("Localização inválida para NPC: " + npcName);
                continue;
            }

            boolean lookCloseEnabled = true;
            int lookCloseRange = 5;
            ConfigurationSection lookCloseSection = npcSection.getConfigurationSection("look-close");
            if (lookCloseSection != null) {
                lookCloseEnabled = lookCloseSection.getBoolean("enabled", true);
                lookCloseRange = lookCloseSection.getInt("range", 5);
            }

            createNPC(npcName, displayName, skinName, location, lookCloseEnabled, lookCloseRange, mode);
            hologramManager.loadHolograms(config, npcName, location, mode);
            npcModes.put(npcName, mode);
        }
        
        hologramManager.startUpdateTask();
    }

    private void createNPC(String name, String displayName, String skinName, Location location, boolean lookCloseEnabled, int lookCloseRange, DuelMode mode) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, displayName);
        npc.spawn(location);

        if (!skinName.isEmpty()) {
            SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
            skinTrait.setSkinName(skinName);
        }

        LookClose lookClose = npc.getTrait(LookClose.class);
        lookClose.setRange(lookCloseRange);
        lookClose.lookClose(lookCloseEnabled);

        Equipment equipment = npc.getTrait(Equipment.class);
        equipment.set(Equipment.EquipmentSlot.HAND, null);
        equipment.set(Equipment.EquipmentSlot.OFF_HAND, null);

        npc.data().set("duel-npc", true);
        npc.data().set("duel-npc-name", name);
        npc.data().set("duel-mode", mode.getName());

        npcs.put(name, npc);
        plugin.getLogger().info("NPC criado: " + name + " (Modo: " + mode.getDisplayName() + ") em " + location.toString());
    }

    public void onNPCClick(Player player, NPC npc) {
        if (!npc.data().has("duel-npc") || !npc.data().get("duel-npc").equals(true)) {
            return;
        }

        String npcName = npc.data().get("duel-npc-name").toString();
        openDuelGUI(player, npcName);
    }

    private void openDuelGUI(Player player, String npcName) {
        DuelMode mode = npcModes.get(npcName);
        if (mode == null) {
            player.sendMessage("§cErro ao identificar o modo de duelo!");
            return;
        }

        player.sendMessage("§6=== " + mode.getDisplayName() + " ===");
        player.sendMessage("§7Use §a/duelo queue " + mode.getName().toLowerCase() + " §7para entrar na fila!");
        player.sendMessage("§7Use §a/duelo <jogador> " + mode.getName().toLowerCase() + " §7para desafiar alguém!");
        player.sendMessage("§7Use §a/stats §7para ver suas estatísticas!");
        
        duelManager.addToMatchmaking(player, mode);
    }

    public DuelMode getNPCMode(String npcName) {
        return npcModes.get(npcName);
    }

    private Location parseLocation(String locString) {
        if (locString.isEmpty()) return null;

        String[] parts = locString.split(",");
        if (parts.length < 4) return null;

        try {
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

            return new Location(org.bukkit.Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao parsear localização: " + locString);
            return null;
        }
    }

    public void removeAllNPCs() {
        hologramManager.stopUpdateTask();
        hologramManager.removeAllHolograms();
        
        for (NPC npc : npcs.values()) {
            npc.destroy();
        }
        npcs.clear();
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public NPC getNPC(String name) {
        return npcs.get(name);
    }
}

