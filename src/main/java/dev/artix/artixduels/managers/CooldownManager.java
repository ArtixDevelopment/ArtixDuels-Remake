package dev.artix.artixduels.managers;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private Map<UUID, Long> duelCooldowns;
    private Map<UUID, Long> requestCooldowns;
    private long duelCooldownTime;
    private long requestCooldownTime;

    public CooldownManager(FileConfiguration config) {
        this.duelCooldowns = new HashMap<>();
        this.requestCooldowns = new HashMap<>();
        this.duelCooldownTime = config.getLong("cooldowns.duel", 60) * 1000;
        this.requestCooldownTime = config.getLong("cooldowns.request", 10) * 1000;
    }

    public boolean isOnDuelCooldown(UUID playerId) {
        if (!duelCooldowns.containsKey(playerId)) {
            return false;
        }
        long cooldownEnd = duelCooldowns.get(playerId);
        if (System.currentTimeMillis() >= cooldownEnd) {
            duelCooldowns.remove(playerId);
            return false;
        }
        return true;
    }

    public boolean isOnRequestCooldown(UUID playerId) {
        if (!requestCooldowns.containsKey(playerId)) {
            return false;
        }
        long cooldownEnd = requestCooldowns.get(playerId);
        if (System.currentTimeMillis() >= cooldownEnd) {
            requestCooldowns.remove(playerId);
            return false;
        }
        return true;
    }

    public void setDuelCooldown(UUID playerId) {
        duelCooldowns.put(playerId, System.currentTimeMillis() + duelCooldownTime);
    }

    public void setRequestCooldown(UUID playerId) {
        requestCooldowns.put(playerId, System.currentTimeMillis() + requestCooldownTime);
    }

    public long getRemainingDuelCooldown(UUID playerId) {
        if (!duelCooldowns.containsKey(playerId)) {
            return 0;
        }
        long remaining = duelCooldowns.get(playerId) - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    public long getRemainingRequestCooldown(UUID playerId) {
        if (!requestCooldowns.containsKey(playerId)) {
            return 0;
        }
        long remaining = requestCooldowns.get(playerId) - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    public void removeCooldown(UUID playerId) {
        duelCooldowns.remove(playerId);
        requestCooldowns.remove(playerId);
    }

    public void reload(FileConfiguration config) {
        this.duelCooldownTime = config.getLong("cooldowns.duel", 60) * 1000;
        this.requestCooldownTime = config.getLong("cooldowns.request", 10) * 1000;
    }
}

