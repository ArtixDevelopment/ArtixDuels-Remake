package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.database.IStatsDAO;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class HologramSystemManager {
    private ArtixDuels plugin;
    private StatsManager statsManager;
    private IStatsDAO statsDAO;
    private Map<String, HologramData> holograms;
    private Map<UUID, Map<String, Integer>> playerPages;
    private Map<org.bukkit.entity.ArmorStand, String> standToHologram;
    private int updateTaskId;

    public HologramSystemManager(ArtixDuels plugin, StatsManager statsManager, IStatsDAO statsDAO) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.statsDAO = statsDAO;
        this.holograms = new HashMap<>();
        this.playerPages = new HashMap<>();
        this.standToHologram = new HashMap<>();
    }

    public void createHologram(String name, Location location, HologramType type, DuelMode mode) {
        if (holograms.containsKey(name)) {
            removeHologram(name);
        }

        HologramData data = new HologramData(name, location, type, mode);
        holograms.put(name, data);
        updateHologram(name);
        saveHolograms();
    }

    public void removeHologram(String name) {
        HologramData data = holograms.remove(name);
        if (data != null) {
            for (ArmorStand stand : data.getStands()) {
                standToHologram.remove(stand);
                stand.remove();
            }
        }
        saveHolograms();
    }

    public void updateHologram(String name) {
        HologramData data = holograms.get(name);
        if (data == null) return;

        // Remover stands antigos
        for (ArmorStand stand : data.getStands()) {
            standToHologram.remove(stand);
            stand.remove();
        }
        data.getStands().clear();

        List<String> lines = generateLines(data);
        Location baseLocation = data.getLocation().clone().add(0, lines.size() * 0.25, 0);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Location lineLocation = baseLocation.clone().subtract(0, i * 0.25, 0);

            ArmorStand stand = (ArmorStand) data.getLocation().getWorld().spawnEntity(lineLocation, EntityType.ARMOR_STAND);
            stand.setCustomNameVisible(true);
            stand.setCustomName(ChatColor.translateAlternateColorCodes('&', line));
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setMarker(true);
            stand.setCanPickupItems(false);
            stand.setRemoveWhenFarAway(false);

            data.getStands().add(stand);
            standToHologram.put(stand, name);
        }
    }

    private List<String> generateLines(HologramData data) {
        return generateLines(data, null);
    }

    private List<String> generateLines(HologramData data, Player player) {
        List<String> lines = new ArrayList<>();

        switch (data.getType()) {
            case MODE_SELECTION:
                lines.add("&b&lSELECIONAR MODO");
                lines.add(" ");
                for (DuelMode mode : DuelMode.values()) {
                    String color = mode == data.getMode() ? "&a" : "&f";
                    lines.add(color + mode.getDisplayName());
                }
                lines.add(" ");
                lines.add("&eClique para alterar!");
                break;

            case TOP_WINS:
                lines.add("&b&lTOP 30 " + data.getMode().getDisplayName().toUpperCase() + " WINS");
                List<PlayerStats> topWins = getTopWins(data.getMode(), 30);
                int maxPages = (int) Math.ceil(topWins.size() / 10.0);
                if (maxPages == 0) maxPages = 1;
                
                int currentPage = 0;
                if (player != null) {
                    Map<String, Integer> playerHologramPages = playerPages.getOrDefault(player.getUniqueId(), new HashMap<>());
                    currentPage = playerHologramPages.getOrDefault(data.getName(), 0) % maxPages;
                } else {
                    currentPage = data.getPage() % maxPages;
                }
                
                lines.add("&7(" + (currentPage + 1) + "/" + maxPages + ")");
                lines.add(" ");
                int start = currentPage * 10;
                int end = Math.min(start + 10, topWins.size());
                for (int i = start; i < end; i++) {
                    PlayerStats stats = topWins.get(i);
                    int position = i + 1;
                    int wins = stats.getModeStats(data.getMode()).getWins();
                    lines.add("&7" + position + ". &e" + stats.getPlayerName() + " &f" + wins);
                }
                if (topWins.isEmpty()) {
                    lines.add("&7Nenhum jogador ainda");
                }
                lines.add(" ");
                lines.add("&eClique para ver mais!");
                break;

            case TOP_STREAK:
                lines.add("&b&lTOP 30 " + data.getMode().getDisplayName().toUpperCase() + " STREAK");
                List<PlayerStats> topStreak = getTopStreak(data.getMode(), 30);
                maxPages = (int) Math.ceil(topStreak.size() / 10.0);
                if (maxPages == 0) maxPages = 1;
                
                currentPage = 0;
                if (player != null) {
                    Map<String, Integer> playerHologramPages = playerPages.getOrDefault(player.getUniqueId(), new HashMap<>());
                    currentPage = playerHologramPages.getOrDefault(data.getName(), 0) % maxPages;
                } else {
                    currentPage = data.getPage() % maxPages;
                }
                
                lines.add("&7(" + (currentPage + 1) + "/" + maxPages + ")");
                lines.add(" ");
                start = currentPage * 10;
                end = Math.min(start + 10, topStreak.size());
                for (int i = start; i < end; i++) {
                    PlayerStats stats = topStreak.get(i);
                    int position = i + 1;
                    int streak = stats.getBestWinStreak();
                    lines.add("&7" + position + ". &e" + stats.getPlayerName() + " &f" + streak);
                }
                if (topStreak.isEmpty()) {
                    lines.add("&7Nenhum jogador ainda");
                }
                lines.add(" ");
                lines.add("&eClique para ver mais!");
                break;
        }

        return lines;
    }

    public void nextPage(Player player, String hologramName) {
        HologramData data = holograms.get(hologramName);
        if (data == null) return;

        if (data.getType() == HologramType.MODE_SELECTION) {
            // Ciclar entre modos
            DuelMode[] modes = DuelMode.values();
            int currentIndex = Arrays.asList(modes).indexOf(data.getMode());
            int nextIndex = (currentIndex + 1) % modes.length;
            data.setMode(modes[nextIndex]);
            updateHologram(hologramName);
        } else {
            // Próxima página (por holograma e player)
            Map<String, Integer> playerHologramPages = playerPages.getOrDefault(player.getUniqueId(), new HashMap<>());
            int currentPage = playerHologramPages.getOrDefault(hologramName, 0);
            
            int maxPages = 3;
            if (data.getType() == HologramType.TOP_WINS) {
                List<PlayerStats> topWins = getTopWins(data.getMode(), 30);
                maxPages = (int) Math.ceil(topWins.size() / 10.0);
                if (maxPages == 0) maxPages = 1;
            } else if (data.getType() == HologramType.TOP_STREAK) {
                List<PlayerStats> topStreak = getTopStreak(data.getMode(), 30);
                maxPages = (int) Math.ceil(topStreak.size() / 10.0);
                if (maxPages == 0) maxPages = 1;
            }
            
            int nextPage = (currentPage + 1) % maxPages;
            playerHologramPages.put(hologramName, nextPage);
            playerPages.put(player.getUniqueId(), playerHologramPages);
            
            // Atualizar holograma com a página do jogador
            updateHologramForPlayer(hologramName, player);
        }
    }

    public void updateHologramForPlayer(String name, Player player) {
        HologramData data = holograms.get(name);
        if (data == null) return;

        // Remover stands antigos
        for (ArmorStand stand : data.getStands()) {
            standToHologram.remove(stand);
            stand.remove();
        }
        data.getStands().clear();

        List<String> lines = generateLines(data, player);
        Location baseLocation = data.getLocation().clone().add(0, lines.size() * 0.25, 0);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Location lineLocation = baseLocation.clone().subtract(0, i * 0.25, 0);

            ArmorStand stand = (ArmorStand) data.getLocation().getWorld().spawnEntity(lineLocation, EntityType.ARMOR_STAND);
            stand.setCustomNameVisible(true);
            stand.setCustomName(ChatColor.translateAlternateColorCodes('&', line));
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setMarker(true);
            stand.setCanPickupItems(false);
            stand.setRemoveWhenFarAway(false);

            data.getStands().add(stand);
            standToHologram.put(stand, name);
        }
    }

    public String getHologramAt(org.bukkit.entity.ArmorStand stand) {
        return standToHologram.get(stand);
    }

    public String getHologramAt(Location location) {
        for (Map.Entry<String, HologramData> entry : holograms.entrySet()) {
            HologramData data = entry.getValue();
            Location hologramLoc = data.getLocation();
            
            if (hologramLoc.getWorld() != location.getWorld()) continue;
            
            double distance = location.distance(hologramLoc);
            if (distance <= 3.0) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Set<String> getHologramNames() {
        return new HashSet<>(holograms.keySet());
    }

    public HologramData getHologramData(String name) {
        return holograms.get(name);
    }

    public String getHologramType(String name) {
        HologramData data = holograms.get(name);
        if (data == null) return null;
        return data.getType().name().toLowerCase().replace("_", "-");
    }

    public String getHologramMode(String name) {
        HologramData data = holograms.get(name);
        if (data == null) return null;
        return data.getMode().getDisplayName();
    }

    private List<PlayerStats> getTopWins(DuelMode mode, int limit) {
        List<PlayerStats> allStats = getAllPlayerStats();
        List<PlayerStats> modeStats = new ArrayList<>();

        for (PlayerStats stats : allStats) {
            if (stats.getModeStats(mode).getWins() > 0) {
                modeStats.add(stats);
            }
        }

        modeStats.sort((a, b) -> Integer.compare(
            b.getModeStats(mode).getWins(),
            a.getModeStats(mode).getWins()
        ));

        return modeStats.subList(0, Math.min(limit, modeStats.size()));
    }

    private List<PlayerStats> getTopStreak(DuelMode mode, int limit) {
        List<PlayerStats> allStats = getAllPlayerStats();
        List<PlayerStats> modeStats = new ArrayList<>();

        for (PlayerStats stats : allStats) {
            if (stats.getBestWinStreak() > 0) {
                modeStats.add(stats);
            }
        }

        modeStats.sort((a, b) -> Integer.compare(b.getBestWinStreak(), a.getBestWinStreak()));

        return modeStats.subList(0, Math.min(limit, modeStats.size()));
    }

    private List<PlayerStats> getAllPlayerStats() {
        List<PlayerStats> allStats = new ArrayList<>();
        File statsFolder = new File(plugin.getDataFolder(), "stats");
        
        if (!statsFolder.exists()) {
            return allStats;
        }

        File[] files = statsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return allStats;
        }

        for (File file : files) {
            try {
                String fileName = file.getName().replace(".yml", "");
                UUID playerId = UUID.fromString(fileName);
                PlayerStats stats = statsDAO.getPlayerStats(playerId);
                if (stats != null) {
                    allStats.add(stats);
                }
            } catch (Exception e) {
                // Ignorar arquivos inválidos
            }
        }

        return allStats;
    }

    public void loadHolograms() {
        File hologramsFile = new File(plugin.getDataFolder(), "holograms.yml");
        if (!hologramsFile.exists()) {
            return;
        }

        FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(hologramsFile);
        org.bukkit.configuration.ConfigurationSection hologramsSection = config.getConfigurationSection("holograms");
        if (hologramsSection == null) return;

        for (String name : hologramsSection.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection hologramSection = hologramsSection.getConfigurationSection(name);
            if (hologramSection == null) continue;

            String worldName = hologramSection.getString("world");
            double x = hologramSection.getDouble("x");
            double y = hologramSection.getDouble("y");
            double z = hologramSection.getDouble("z");
            String typeString = hologramSection.getString("type");
            String modeString = hologramSection.getString("mode");

            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null) continue;

            Location location = new Location(world, x, y, z);
            HologramType type = HologramType.valueOf(typeString);
            DuelMode mode = DuelMode.fromString(modeString);

            if (type != null && mode != null) {
                createHologram(name, location, type, mode);
            }
        }
    }

    public void saveHolograms() {
        File hologramsFile = new File(plugin.getDataFolder(), "holograms.yml");
        FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(hologramsFile);

        config.set("holograms", null);

        for (Map.Entry<String, HologramData> entry : holograms.entrySet()) {
            String name = entry.getKey();
            HologramData data = entry.getValue();
            Location loc = data.getLocation();

            String path = "holograms." + name;
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".type", data.getType().name());
            config.set(path + ".mode", data.getMode().getName());
        }

        try {
            config.save(hologramsFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao salvar hologramas: " + e.getMessage());
        }
    }

    public void startUpdateTask() {
        if (updateTaskId != 0) {
            plugin.getServer().getScheduler().cancelTask(updateTaskId);
        }
        updateTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (String name : holograms.keySet()) {
                updateHologram(name);
            }
        }, 0L, 100L); // Atualizar a cada 5 segundos
    }

    public void stopUpdateTask() {
        if (updateTaskId != 0) {
            plugin.getServer().getScheduler().cancelTask(updateTaskId);
            updateTaskId = 0;
        }
    }

    public enum HologramType {
        MODE_SELECTION,
        TOP_WINS,
        TOP_STREAK
    }

    private static class HologramData {
        private String name;
        private Location location;
        private HologramType type;
        private DuelMode mode;
        private int page;
        private List<ArmorStand> stands;

        public HologramData(String name, Location location, HologramType type, DuelMode mode) {
            this.name = name;
            this.location = location;
            this.type = type;
            this.mode = mode;
            this.page = 0;
            this.stands = new ArrayList<>();
        }

        public String getName() { return name; }
        public Location getLocation() { return location; }
        public HologramType getType() { return type; }
        public DuelMode getMode() { return mode; }
        public void setMode(DuelMode mode) { this.mode = mode; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public List<ArmorStand> getStands() { return stands; }
    }
}

