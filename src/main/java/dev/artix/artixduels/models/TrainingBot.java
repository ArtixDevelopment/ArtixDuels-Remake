package dev.artix.artixduels.models;

import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Representa um bot de treinamento.
 */
public class TrainingBot {
    private UUID botId;
    private String botName;
    private BotDifficulty difficulty;
    private Zombie entity;
    private Player target;
    private long lastAction;
    private int comboCount;
    private boolean isBlocking;
    private double health;
    private double maxHealth;

    public TrainingBot(String botName, BotDifficulty difficulty, Plugin plugin) {
        this.botId = UUID.randomUUID();
        this.botName = botName;
        this.difficulty = difficulty;
        this.lastAction = System.currentTimeMillis();
        this.comboCount = 0;
        this.isBlocking = false;
        this.health = 20.0;
        this.maxHealth = 20.0;
    }

    public UUID getBotId() {
        return botId;
    }

    public String getBotName() {
        return botName;
    }

    public BotDifficulty getDifficulty() {
        return difficulty;
    }

    public Zombie getEntity() {
        return entity;
    }

    public void setEntity(Zombie entity, Plugin plugin) {
        this.entity = entity;
        if (entity != null) {
            entity.setCustomName(botName);
            entity.setCustomNameVisible(true);
            entity.setMetadata("TrainingBot", new FixedMetadataValue(plugin, true));
            entity.setMetadata("BotId", new FixedMetadataValue(plugin, botId.toString()));
        }
    }

    public Player getTarget() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    public long getLastAction() {
        return lastAction;
    }

    public void updateLastAction() {
        this.lastAction = System.currentTimeMillis();
    }

    public int getComboCount() {
        return comboCount;
    }

    public void incrementCombo() {
        this.comboCount++;
    }

    public void resetCombo() {
        this.comboCount = 0;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public void setBlocking(boolean blocking) {
        this.isBlocking = blocking;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
        if (entity != null) {
            entity.setHealth(health);
        }
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
        if (entity != null) {
            entity.setMaxHealth(maxHealth);
        }
    }

    public void remove() {
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }
}

