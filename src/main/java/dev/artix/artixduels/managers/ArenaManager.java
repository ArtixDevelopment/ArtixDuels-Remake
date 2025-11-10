package dev.artix.artixduels.managers;

import dev.artix.artixduels.models.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaManager {
    private Map<String, Arena> arenas;
    private List<Arena> availableArenas;
    private File configFile;
    private FileConfiguration config;

    public ArenaManager(FileConfiguration config, File configFile) {
        this.arenas = new HashMap<>();
        this.availableArenas = new ArrayList<>();
        this.config = config;
        this.configFile = configFile;
        loadArenas(config);
    }

    private void loadArenas(FileConfiguration config) {
        ConfigurationSection arenasSection = config.getConfigurationSection("arenas");
        if (arenasSection == null) return;

        for (String arenaName : arenasSection.getKeys(false)) {
            ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaName);
            if (arenaSection == null) continue;

            Arena arena = new Arena(arenaName);

            if (arenaSection.contains("player1-spawn")) {
                Location loc1 = parseLocation(arenaSection.getString("player1-spawn"));
                if (loc1 != null) arena.setPlayer1Spawn(loc1);
            }

            if (arenaSection.contains("player2-spawn")) {
                Location loc2 = parseLocation(arenaSection.getString("player2-spawn"));
                if (loc2 != null) arena.setPlayer2Spawn(loc2);
            }

            if (arenaSection.contains("spectator-spawn")) {
                Location specLoc = parseLocation(arenaSection.getString("spectator-spawn"));
                if (specLoc != null) arena.setSpectatorSpawn(specLoc);
            }

            arenas.put(arenaName, arena);
            availableArenas.add(arena);
        }
    }

    private Location parseLocation(String locString) {
        String[] parts = locString.split(",");
        if (parts.length < 4) return null;

        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
        float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Arena getAvailableArena() {
        for (Arena arena : availableArenas) {
            if (!arena.isInUse()) {
                return arena;
            }
        }
        return null;
    }

    public void setArenaInUse(String name, boolean inUse) {
        Arena arena = arenas.get(name);
        if (arena != null) {
            arena.setInUse(inUse);
        }
    }

    public Map<String, Arena> getArenas() {
        return arenas;
    }

    public void addArena(String name, Arena arena) {
        arenas.put(name, arena);
        availableArenas.add(arena);
    }

    public void removeArena(String name) {
        Arena arena = arenas.remove(name);
        if (arena != null) {
            availableArenas.remove(arena);
        }
    }

    public boolean arenaExists(String name) {
        return arenas.containsKey(name);
    }

    public void saveArena(String name, Arena arena) {
        String path = "arenas." + name;
        config.set(path + ".player1-spawn", locationToString(arena.getPlayer1Spawn()));
        config.set(path + ".player2-spawn", locationToString(arena.getPlayer2Spawn()));
        config.set(path + ".spectator-spawn", locationToString(arena.getSpectatorSpawn()));
        
        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Erro ao salvar arena: " + e.getMessage());
        }
    }

    private String locationToString(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }
}

