package dev.artix.artixduels.managers;

import dev.artix.artixduels.models.Duel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Analisador de combate que rastreia estatísticas detalhadas durante duelos.
 * Rastreia dano dado/recebido, hits, combos, etc.
 */
public class CombatAnalyzer {
    private Map<UUID, CombatStats> combatStats;

    public CombatAnalyzer() {
        this.combatStats = new HashMap<>();
    }

    /**
     * Inicia o rastreamento de combate para um duelo.
     */
    public void startTracking(Duel duel) {
        combatStats.put(duel.getPlayer1Id(), new CombatStats());
        combatStats.put(duel.getPlayer2Id(), new CombatStats());
    }

    /**
     * Para o rastreamento de combate para um duelo.
     */
    public void stopTracking(Duel duel) {
        combatStats.remove(duel.getPlayer1Id());
        combatStats.remove(duel.getPlayer2Id());
    }

    /**
     * Registra dano dado por um jogador.
     */
    public void recordDamageDealt(UUID playerId, double damage) {
        CombatStats stats = combatStats.get(playerId);
        if (stats != null) {
            stats.addDamageDealt(damage);
        }
    }

    /**
     * Registra dano recebido por um jogador.
     */
    public void recordDamageTaken(UUID playerId, double damage) {
        CombatStats stats = combatStats.get(playerId);
        if (stats != null) {
            stats.addDamageTaken(damage);
        }
    }

    /**
     * Registra um hit dado por um jogador.
     */
    public void recordHit(UUID playerId) {
        CombatStats stats = combatStats.get(playerId);
        if (stats != null) {
            stats.addHit();
        }
    }

    /**
     * Registra um hit recebido por um jogador.
     */
    public void recordHitTaken(UUID playerId) {
        CombatStats stats = combatStats.get(playerId);
        if (stats != null) {
            stats.addHitTaken();
        }
    }

    /**
     * Registra um combo de um jogador.
     */
    public void recordCombo(UUID playerId, int combo) {
        CombatStats stats = combatStats.get(playerId);
        if (stats != null) {
            stats.setMaxCombo(Math.max(stats.getMaxCombo(), combo));
        }
    }

    /**
     * Obtém as estatísticas de combate de um jogador.
     */
    public CombatStats getCombatStats(UUID playerId) {
        return combatStats.get(playerId);
    }

    /**
     * Classe para armazenar estatísticas de combate.
     */
    public static class CombatStats {
        private double damageDealt;
        private double damageTaken;
        private int hits;
        private int hitsTaken;
        private int maxCombo;
        private long firstHitTime;
        private long lastHitTime;

        public CombatStats() {
            this.damageDealt = 0;
            this.damageTaken = 0;
            this.hits = 0;
            this.hitsTaken = 0;
            this.maxCombo = 0;
            this.firstHitTime = System.currentTimeMillis();
            this.lastHitTime = System.currentTimeMillis();
        }

        public void addDamageDealt(double damage) {
            this.damageDealt += damage;
        }

        public void addDamageTaken(double damage) {
            this.damageTaken += damage;
        }

        public void addHit() {
            this.hits++;
            this.lastHitTime = System.currentTimeMillis();
        }

        public void addHitTaken() {
            this.hitsTaken++;
        }

        public void setMaxCombo(int combo) {
            this.maxCombo = Math.max(this.maxCombo, combo);
        }

        public double getDamageDealt() {
            return damageDealt;
        }

        public double getDamageTaken() {
            return damageTaken;
        }

        public int getHits() {
            return hits;
        }

        public int getHitsTaken() {
            return hitsTaken;
        }

        public int getMaxCombo() {
            return maxCombo;
        }

        public long getFirstHitTime() {
            return firstHitTime;
        }

        public long getLastHitTime() {
            return lastHitTime;
        }

        public double getHitAccuracy() {
            if (hits + hitsTaken == 0) return 0.0;
            return (double) hits / (hits + hitsTaken) * 100.0;
        }

        public double getDamagePerHit() {
            if (hits == 0) return 0.0;
            return damageDealt / hits;
        }
    }
}

