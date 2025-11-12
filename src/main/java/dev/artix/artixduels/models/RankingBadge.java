package dev.artix.artixduels.models;

import org.bukkit.ChatColor;

/**
 * Representa um badge de ranking.
 */
public enum RankingBadge {
    TOP_1(1, "&6&l#1", "&6&lCAMPEÃO"),
    TOP_10(10, "&e&lTOP 10", "&e&lELITE"),
    TOP_100(100, "&b&lTOP 100", "&b&lPROFISSIONAL"),
    NONE(0, "", "");

    private final int maxPosition;
    private final String prefix;
    private final String displayName;

    RankingBadge(int maxPosition, String prefix, String displayName) {
        this.maxPosition = maxPosition;
        this.prefix = prefix;
        this.displayName = displayName;
    }

    public int getMaxPosition() {
        return maxPosition;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RankingBadge getBadgeForPosition(int position) {
        if (position == 1) {
            return TOP_1;
        } else if (position <= 10) {
            return TOP_10;
        } else if (position <= 100) {
            return TOP_100;
        }
        return NONE;
    }

    public String getFormattedName(String playerName) {
        if (this == NONE) {
            return playerName;
        }
        return ChatColor.translateAlternateColorCodes('&', prefix + " &7" + playerName);
    }
}

