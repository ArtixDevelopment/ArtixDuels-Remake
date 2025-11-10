package dev.artix.artixduels.models;

import org.bukkit.Location;

public class Arena {
    private String name;
    private Location player1Spawn;
    private Location player2Spawn;
    private Location spectatorSpawn;
    private boolean inUse;
    private boolean enabled;
    private boolean kitsEnabled;
    private boolean rulesEnabled;
    private String defaultKit;

    public Arena(String name) {
        this.name = name;
        this.inUse = false;
        this.enabled = true;
        this.kitsEnabled = true;
        this.rulesEnabled = true;
        this.defaultKit = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isKitsEnabled() {
        return kitsEnabled;
    }

    public void setKitsEnabled(boolean kitsEnabled) {
        this.kitsEnabled = kitsEnabled;
    }

    public boolean isRulesEnabled() {
        return rulesEnabled;
    }

    public void setRulesEnabled(boolean rulesEnabled) {
        this.rulesEnabled = rulesEnabled;
    }

    public String getDefaultKit() {
        return defaultKit;
    }

    public void setDefaultKit(String defaultKit) {
        this.defaultKit = defaultKit;
    }
}

