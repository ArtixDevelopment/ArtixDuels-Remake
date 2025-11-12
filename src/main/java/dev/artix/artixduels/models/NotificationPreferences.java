package dev.artix.artixduels.models;

import java.util.UUID;

/**
 * Preferências de notificação de um jogador.
 */
public class NotificationPreferences {
    private UUID playerId;
    private boolean soundEnabled;
    private boolean particlesEnabled;
    private boolean titleEnabled;
    private boolean actionbarEnabled;
    private boolean chatEnabled;

    public NotificationPreferences(UUID playerId) {
        this.playerId = playerId;
        this.soundEnabled = true;
        this.particlesEnabled = true;
        this.titleEnabled = true;
        this.actionbarEnabled = true;
        this.chatEnabled = true;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isParticlesEnabled() {
        return particlesEnabled;
    }

    public void setParticlesEnabled(boolean particlesEnabled) {
        this.particlesEnabled = particlesEnabled;
    }

    public boolean isTitleEnabled() {
        return titleEnabled;
    }

    public void setTitleEnabled(boolean titleEnabled) {
        this.titleEnabled = titleEnabled;
    }

    public boolean isActionbarEnabled() {
        return actionbarEnabled;
    }

    public void setActionbarEnabled(boolean actionbarEnabled) {
        this.actionbarEnabled = actionbarEnabled;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }
}

