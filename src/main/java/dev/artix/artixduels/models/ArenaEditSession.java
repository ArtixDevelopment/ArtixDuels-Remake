package dev.artix.artixduels.models;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Sessão de edição de arena.
 */
public class ArenaEditSession {
    private UUID playerId;
    private String arenaName;
    private Location pos1;
    private Location pos2;
    private Location player1Spawn;
    private Location player2Spawn;
    private Location spectatorSpawn;
    private boolean testing;
    private long startTime;

    public ArenaEditSession(Player player, String arenaName) {
        this.playerId = player.getUniqueId();
        this.arenaName = arenaName;
        this.testing = false;
        this.startTime = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getArenaName() {
        return arenaName;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getPlayer1Spawn() {
        return player1Spawn;
    }

    public void setPlayer1Spawn(Location player1Spawn) {
        this.player1Spawn = player1Spawn;
    }

    public Location getPlayer2Spawn() {
        return player2Spawn;
    }

    public void setPlayer2Spawn(Location player2Spawn) {
        this.player2Spawn = player2Spawn;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public boolean isTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean hasBothPositions() {
        return pos1 != null && pos2 != null;
    }

    public boolean isComplete() {
        return hasBothPositions() && player1Spawn != null && player2Spawn != null;
    }
}

