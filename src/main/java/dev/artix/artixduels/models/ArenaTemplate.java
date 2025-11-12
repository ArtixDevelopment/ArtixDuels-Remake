package dev.artix.artixduels.models;

import org.bukkit.Location;

/**
 * Template de arena para criação rápida.
 */
public class ArenaTemplate {
    private String name;
    private String displayName;
    private String description;
    private double defaultSize;
    private Location defaultPlayer1Spawn;
    private Location defaultPlayer2Spawn;
    private Location defaultSpectatorSpawn;

    public ArenaTemplate(String name, String displayName, String description, double defaultSize) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.defaultSize = defaultSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(double defaultSize) {
        this.defaultSize = defaultSize;
    }

    public Location getDefaultPlayer1Spawn() {
        return defaultPlayer1Spawn;
    }

    public void setDefaultPlayer1Spawn(Location defaultPlayer1Spawn) {
        this.defaultPlayer1Spawn = defaultPlayer1Spawn;
    }

    public Location getDefaultPlayer2Spawn() {
        return defaultPlayer2Spawn;
    }

    public void setDefaultPlayer2Spawn(Location defaultPlayer2Spawn) {
        this.defaultPlayer2Spawn = defaultPlayer2Spawn;
    }

    public Location getDefaultSpectatorSpawn() {
        return defaultSpectatorSpawn;
    }

    public void setDefaultSpectatorSpawn(Location defaultSpectatorSpawn) {
        this.defaultSpectatorSpawn = defaultSpectatorSpawn;
    }
}

