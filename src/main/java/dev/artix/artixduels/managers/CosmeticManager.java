package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Cosmetic;
import dev.artix.artixduels.models.PlayerCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Gerenciador de cosméticos.
 */
public class CosmeticManager {
    private final ArtixDuels plugin;
    private final StatsManager statsManager;
    private FileConfiguration cosmeticsConfig;
    private File cosmeticsFile;
    private Map<String, Cosmetic> cosmetics;
    private Map<UUID, PlayerCosmetics> playerCosmetics;
    private Map<UUID, Integer> trailTasks;

    public CosmeticManager(ArtixDuels plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.cosmetics = new HashMap<>();
        this.playerCosmetics = new HashMap<>();
        this.trailTasks = new HashMap<>();
        
        loadCosmeticsConfig();
        loadCosmetics();
    }

    private void loadCosmeticsConfig() {
        cosmeticsFile = new File(plugin.getDataFolder(), "cosmetics.yml");
        if (!cosmeticsFile.exists()) {
            plugin.saveResource("cosmetics.yml", false);
        }
        cosmeticsConfig = YamlConfiguration.loadConfiguration(cosmeticsFile);
    }

    private void loadCosmetics() {
        cosmetics.clear();
        
        if (cosmeticsConfig.contains("cosmetics")) {
            ConfigurationSection cosmeticsSection = cosmeticsConfig.getConfigurationSection("cosmetics");
            if (cosmeticsSection != null) {
                for (String key : cosmeticsSection.getKeys(false)) {
                    ConfigurationSection cosmeticSection = cosmeticsSection.getConfigurationSection(key);
                    if (cosmeticSection != null) {
                        Cosmetic cosmetic = Cosmetic.fromConfig(cosmeticSection);
                        cosmetics.put(cosmetic.getId(), cosmetic);
                    }
                }
            }
        }
        
        plugin.getLogger().info("Carregados " + cosmetics.size() + " cosméticos.");
    }

    public void reload() {
        loadCosmeticsConfig();
        loadCosmetics();
    }

    /**
     * Obtém os cosméticos de um jogador.
     */
    public PlayerCosmetics getPlayerCosmetics(UUID playerId) {
        return playerCosmetics.computeIfAbsent(playerId, PlayerCosmetics::new);
    }

    /**
     * Desbloqueia um cosmético para um jogador.
     */
    public void unlockCosmetic(UUID playerId, String cosmeticId) {
        PlayerCosmetics playerCosmetics = getPlayerCosmetics(playerId);
        playerCosmetics.unlockCosmetic(cosmeticId);
    }

    /**
     * Ativa um cosmético para um jogador.
     */
    public void setActiveCosmetic(UUID playerId, Cosmetic.CosmeticType type, String cosmeticId) {
        PlayerCosmetics playerCosmetics = getPlayerCosmetics(playerId);
        if (!playerCosmetics.hasCosmeticUnlocked(cosmeticId)) {
            return;
        }
        playerCosmetics.setActiveCosmetic(type, cosmeticId);
    }

    /**
     * Obtém o cosmético ativo de um tipo para um jogador.
     */
    public Cosmetic getActiveCosmetic(UUID playerId, Cosmetic.CosmeticType type) {
        PlayerCosmetics playerCosmetics = getPlayerCosmetics(playerId);
        String cosmeticId = playerCosmetics.getActiveCosmetic(type);
        if (cosmeticId == null || cosmeticId.isEmpty()) {
            return null;
        }
        return cosmetics.get(cosmeticId);
    }

    /**
     * Executa efeito de vitória.
     */
    public void playVictoryEffect(Player player) {
        Cosmetic cosmetic = getActiveCosmetic(player.getUniqueId(), Cosmetic.CosmeticType.VICTORY_EFFECT);
        if (cosmetic == null) {
            playDefaultVictoryEffect(player);
            return;
        }

        Map<String, Object> data = cosmetic.getData();
        String effectType = (String) data.getOrDefault("effect", "FIREWORKS_SPARK");
        int count = ((Number) data.getOrDefault("count", 50)).intValue();
        double offsetX = ((Number) data.getOrDefault("offset-x", 1.0)).doubleValue();
        double offsetY = ((Number) data.getOrDefault("offset-y", 1.0)).doubleValue();
        double offsetZ = ((Number) data.getOrDefault("offset-z", 1.0)).doubleValue();

        try {
            Effect effect = Effect.valueOf(effectType);
            Location loc = player.getLocation();
            for (int i = 0; i < count; i++) {
                double x = loc.getX() + (Math.random() - 0.5) * offsetX * 2;
                double y = loc.getY() + Math.random() * offsetY;
                double z = loc.getZ() + (Math.random() - 0.5) * offsetZ * 2;
                player.getWorld().playEffect(new Location(player.getWorld(), x, y, z), effect, 0);
            }
        } catch (IllegalArgumentException e) {
            playDefaultVictoryEffect(player);
        }
    }

    /**
     * Executa efeito padrão de vitória.
     */
    private void playDefaultVictoryEffect(Player player) {
        Location loc = player.getLocation();
        for (int i = 0; i < 30; i++) {
            double angle = 2 * Math.PI * i / 30;
            double x = loc.getX() + Math.cos(angle) * 1.5;
            double y = loc.getY() + 1;
            double z = loc.getZ() + Math.sin(angle) * 1.5;
            player.getWorld().playEffect(new Location(player.getWorld(), x, y, z), Effect.FIREWORKS_SPARK, 0);
        }
    }

    /**
     * Executa efeito de kill.
     */
    public void playKillEffect(Player killer, Player victim) {
        Cosmetic cosmetic = getActiveCosmetic(killer.getUniqueId(), Cosmetic.CosmeticType.KILL_EFFECT);
        if (cosmetic == null) {
            playDefaultKillEffect(killer, victim);
            return;
        }

        Map<String, Object> data = cosmetic.getData();
        String effectType = (String) data.getOrDefault("effect", "EXPLOSION");
        int count = ((Number) data.getOrDefault("count", 20)).intValue();

        try {
            Effect effect = Effect.valueOf(effectType);
            Location loc = victim.getLocation();
            for (int i = 0; i < count; i++) {
                double x = loc.getX() + (Math.random() - 0.5) * 2;
                double y = loc.getY() + Math.random() * 2;
                double z = loc.getZ() + (Math.random() - 0.5) * 2;
                killer.getWorld().playEffect(new Location(killer.getWorld(), x, y, z), effect, 0);
            }
        } catch (IllegalArgumentException e) {
            playDefaultKillEffect(killer, victim);
        }
    }

    /**
     * Executa efeito padrão de kill.
     */
    private void playDefaultKillEffect(Player killer, Player victim) {
        Location loc = victim.getLocation();
        for (int i = 0; i < 15; i++) {
            double x = loc.getX() + (Math.random() - 0.5) * 2;
            double y = loc.getY() + Math.random() * 2;
            double z = loc.getZ() + (Math.random() - 0.5) * 2;
            killer.getWorld().playEffect(new Location(killer.getWorld(), x, y, z), Effect.EXPLOSION, 0);
        }
    }

    /**
     * Inicia trail para um jogador.
     */
    public void startTrail(Player player) {
        stopTrail(player);
        
        Cosmetic cosmetic = getActiveCosmetic(player.getUniqueId(), Cosmetic.CosmeticType.TRAIL);
        if (cosmetic == null) {
            return;
        }

        Map<String, Object> data = cosmetic.getData();
        String effectType = (String) data.getOrDefault("effect", "REDSTONE");
        int interval = ((Number) data.getOrDefault("interval", 5)).intValue();

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                stopTrail(player);
                return;
            }

            try {
                Effect effect = Effect.valueOf(effectType);
                Location loc = player.getLocation();
                loc.add(0, 0.5, 0);
                player.getWorld().playEffect(loc, effect, 0);
            } catch (IllegalArgumentException e) {
                stopTrail(player);
            }
        }, 0L, interval).getTaskId();

        trailTasks.put(player.getUniqueId(), taskId);
    }

    /**
     * Para trail de um jogador.
     */
    public void stopTrail(Player player) {
        Integer taskId = trailTasks.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Para todos os trails.
     */
    public void stopAllTrails() {
        for (Integer taskId : trailTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        trailTasks.clear();
    }

    /**
     * Obtém todos os cosméticos de um tipo.
     */
    public List<Cosmetic> getCosmeticsByType(Cosmetic.CosmeticType type) {
        List<Cosmetic> result = new ArrayList<>();
        for (Cosmetic cosmetic : cosmetics.values()) {
            if (cosmetic.getType() == type) {
                result.add(cosmetic);
            }
        }
        return result;
    }

    /**
     * Obtém todos os cosméticos desbloqueados de um jogador.
     */
    public List<Cosmetic> getUnlockedCosmetics(UUID playerId, Cosmetic.CosmeticType type) {
        PlayerCosmetics playerCosmetics = getPlayerCosmetics(playerId);
        List<Cosmetic> result = new ArrayList<>();
        for (Cosmetic cosmetic : getCosmeticsByType(type)) {
            if (playerCosmetics.hasCosmeticUnlocked(cosmetic.getId())) {
                result.add(cosmetic);
            }
        }
        return result;
    }
}

