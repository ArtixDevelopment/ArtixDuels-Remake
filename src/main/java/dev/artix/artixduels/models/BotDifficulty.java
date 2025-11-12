package dev.artix.artixduels.models;

/**
 * Representa o nível de dificuldade de um bot.
 */
public enum BotDifficulty {
    EASY("Fácil", 0.3, 0.5, 0.4, 0.3),
    MEDIUM("Médio", 0.6, 0.7, 0.6, 0.5),
    HARD("Difícil", 0.8, 0.9, 0.8, 0.7),
    EXPERT("Expert", 0.95, 1.0, 0.95, 0.9);

    private final String displayName;
    private final double hitChance;
    private final double blockChance;
    private final double comboChance;
    private final double movementChance;

    BotDifficulty(String displayName, double hitChance, double blockChance, 
                  double comboChance, double movementChance) {
        this.displayName = displayName;
        this.hitChance = hitChance;
        this.blockChance = blockChance;
        this.comboChance = comboChance;
        this.movementChance = movementChance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getHitChance() {
        return hitChance;
    }

    public double getBlockChance() {
        return blockChance;
    }

    public double getComboChance() {
        return comboChance;
    }

    public double getMovementChance() {
        return movementChance;
    }
}

