package dev.artix.artixduels.listeners;

import dev.artix.artixduels.managers.CombatAnalyzer;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.models.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Listener para rastrear estatísticas de combate durante duelos.
 */
public class CombatListener implements Listener {
    private final DuelManager duelManager;
    private final CombatAnalyzer combatAnalyzer;

    public CombatListener(DuelManager duelManager, CombatAnalyzer combatAnalyzer) {
        this.duelManager = duelManager;
        this.combatAnalyzer = combatAnalyzer;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Duel duel = duelManager.getPlayerDuel(player);

        if (duel == null || duel.getState() != Duel.DuelState.FIGHTING) {
            return;
        }

        // Registrar dano recebido
        double damage = event.getFinalDamage();
        combatAnalyzer.recordDamageTaken(player.getUniqueId(), damage);

        // Se o dano foi causado por outro jogador
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            if (damageEvent.getDamager() instanceof Player) {
                Player damager = (Player) damageEvent.getDamager();
                Duel damagerDuel = duelManager.getPlayerDuel(damager);

                // Verificar se estão no mesmo duelo
                if (damagerDuel != null && damagerDuel.equals(duel)) {
                    // Registrar dano dado
                    combatAnalyzer.recordDamageDealt(damager.getUniqueId(), damage);
                    // Registrar hit
                    combatAnalyzer.recordHit(damager.getUniqueId());
                    combatAnalyzer.recordHitTaken(player.getUniqueId());
                }
            }
        }
    }
}

