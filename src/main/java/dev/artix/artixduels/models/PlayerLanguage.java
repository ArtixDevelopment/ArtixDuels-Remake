package dev.artix.artixduels.models;

import java.util.UUID;

/**
 * Idioma preferido de um jogador.
 */
public class PlayerLanguage {
    private UUID playerId;
    private String languageCode;

    public PlayerLanguage(UUID playerId, String languageCode) {
        this.playerId = playerId;
        this.languageCode = languageCode;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}

