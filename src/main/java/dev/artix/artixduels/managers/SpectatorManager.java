package dev.artix.artixduels.managers;

import dev.artix.artixduels.models.Arena;
import dev.artix.artixduels.models.Duel;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SpectatorManager {
    private Map<UUID, Location> savedLocations;
    private Map<UUID, ItemStack[]> savedInventories;
    private Map<UUID, ItemStack[]> savedArmor;
    private Map<UUID, GameMode> savedGameModes;
    private Map<UUID, Duel> spectatorDuels;
    private ArenaManager arenaManager;

    public SpectatorManager(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
        this.savedLocations = new HashMap<>();
        this.savedInventories = new HashMap<>();
        this.savedArmor = new HashMap<>();
        this.savedGameModes = new HashMap<>();
        this.spectatorDuels = new HashMap<>();
    }

    public void addSpectator(Player player, Duel duel) {
        if (duel.getSpectators().contains(player.getUniqueId())) {
            return;
        }

        savePlayerState(player);
        duel.getSpectators().add(player.getUniqueId());
        spectatorDuels.put(player.getUniqueId(), duel);

        Arena arena = arenaManager.getArena(duel.getArenaName());
        if (arena != null && arena.getSpectatorSpawn() != null) {
            player.teleport(arena.getSpectatorSpawn());
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        player.sendMessage("§aVocê entrou como espectador do duelo!");
    }

    public void removeSpectator(Player player) {
        if (!spectatorDuels.containsKey(player.getUniqueId())) {
            return;
        }

        Duel duel = spectatorDuels.get(player.getUniqueId());
        duel.getSpectators().remove(player.getUniqueId());
        spectatorDuels.remove(player.getUniqueId());

        restorePlayerState(player);
        player.sendMessage("§cVocê saiu do modo espectador.");
    }

    private void savePlayerState(Player player) {
        savedLocations.put(player.getUniqueId(), player.getLocation().clone());
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents().clone());
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());
        savedGameModes.put(player.getUniqueId(), player.getGameMode());
    }

    private void restorePlayerState(Player player) {
        UUID playerId = player.getUniqueId();

        if (savedLocations.containsKey(playerId)) {
            player.teleport(savedLocations.get(playerId));
            savedLocations.remove(playerId);
        }

        if (savedInventories.containsKey(playerId)) {
            player.getInventory().setContents(savedInventories.get(playerId));
            savedInventories.remove(playerId);
        }

        if (savedArmor.containsKey(playerId)) {
            player.getInventory().setArmorContents(savedArmor.get(playerId));
            savedArmor.remove(playerId);
        }

        if (savedGameModes.containsKey(playerId)) {
            player.setGameMode(savedGameModes.get(playerId));
            savedGameModes.remove(playerId);
        }

        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.setGameMode(GameMode.SURVIVAL);
    }

    public void removeAllSpectators(Duel duel) {
        List<UUID> toRemove = new ArrayList<>();
        for (UUID spectatorId : duel.getSpectators()) {
            Player spectator = Bukkit.getPlayer(spectatorId);
            if (spectator != null && spectator.isOnline()) {
                removeSpectator(spectator);
            }
            toRemove.add(spectatorId);
        }

        for (UUID id : toRemove) {
            duel.getSpectators().remove(id);
            spectatorDuels.remove(id);
        }
    }

    public boolean isSpectator(Player player) {
        return spectatorDuels.containsKey(player.getUniqueId());
    }

    public Duel getSpectatorDuel(Player player) {
        return spectatorDuels.get(player.getUniqueId());
    }
}

