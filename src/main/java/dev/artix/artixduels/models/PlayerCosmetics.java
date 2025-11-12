package dev.artix.artixduels.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa os cosméticos de um jogador.
 */
public class PlayerCosmetics {
    private UUID playerId;
    private Map<String, Boolean> unlockedCosmetics;
    private Map<Cosmetic.CosmeticType, String> activeCosmetics;

    public PlayerCosmetics(UUID playerId) {
        this.playerId = playerId;
        this.unlockedCosmetics = new HashMap<>();
        this.activeCosmetics = new HashMap<>();
    }

    public PlayerCosmetics(UUID playerId, Map<String, Boolean> unlockedCosmetics,
                          Map<Cosmetic.CosmeticType, String> activeCosmetics) {
        this.playerId = playerId;
        this.unlockedCosmetics = unlockedCosmetics != null ? unlockedCosmetics : new HashMap<>();
        this.activeCosmetics = activeCosmetics != null ? activeCosmetics : new HashMap<>();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean hasCosmeticUnlocked(String cosmeticId) {
        return unlockedCosmetics.getOrDefault(cosmeticId, false);
    }

    public void unlockCosmetic(String cosmeticId) {
        unlockedCosmetics.put(cosmeticId, true);
    }

    public void lockCosmetic(String cosmeticId) {
        unlockedCosmetics.put(cosmeticId, false);
    }

    public String getActiveCosmetic(Cosmetic.CosmeticType type) {
        return activeCosmetics.get(type);
    }

    public void setActiveCosmetic(Cosmetic.CosmeticType type, String cosmeticId) {
        if (cosmeticId == null || cosmeticId.isEmpty()) {
            activeCosmetics.remove(type);
        } else {
            activeCosmetics.put(type, cosmeticId);
        }
    }

    public Map<String, Boolean> getUnlockedCosmetics() {
        return unlockedCosmetics;
    }

    public Map<Cosmetic.CosmeticType, String> getActiveCosmetics() {
        return activeCosmetics;
    }
}

