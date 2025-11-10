package dev.artix.artixduels.models;

public enum DuelMode {
    BEDFIGHT("BedFight", "BedFight"),
    STICKFIGHT("StickFight", "StickFight"),
    SOUP("Soup", "Soup"),
    SOUPRECRAFT("SoupRecraft", "SoupRecraft"),
    GLADIATOR("Gladiator", "Gladiator"),
    FASTOB("FastOB", "FastOB"),
    BOXING("Boxing", "Boxing"),
    FIREBALLFIGHT("FireballFight", "FireballFight"),
    SUMO("Sumo", "Sumo"),
    BATTLERUSH("BattleRush", "BattleRush"),
    TNTSUMO("TNTSumo", "TNTSumo");

    private String name;
    private String displayName;

    DuelMode(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DuelMode fromString(String name) {
        for (DuelMode mode : values()) {
            if (mode.name.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}

