package dev.artix.artixduels.models;

/**
 * Representa um período de ranking.
 */
public enum RankingPeriod {
    DAILY("daily", "Diário"),
    WEEKLY("weekly", "Semanal"),
    MONTHLY("monthly", "Mensal"),
    SEASONAL("seasonal", "Sazonal"),
    ALL_TIME("all_time", "Todos os Tempos");

    private final String name;
    private final String displayName;

    RankingPeriod(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RankingPeriod fromString(String name) {
        for (RankingPeriod period : values()) {
            if (period.name.equalsIgnoreCase(name)) {
                return period;
            }
        }
        return ALL_TIME;
    }
}

