package dev.artix.artixduels.listeners;

import dev.artix.artixduels.managers.TrainingManager;
import dev.artix.artixduels.models.TrainingBot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener para eventos de treinamento.
 */
public class TrainingListener implements Listener {
    private final TrainingManager trainingManager;

    public TrainingListener(TrainingManager trainingManager) {
        this.trainingManager = trainingManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Zombie && event.getDamager() instanceof Player) {
            Zombie zombie = (Zombie) event.getEntity();
            Player player = (Player) event.getDamager();
            
            if (zombie.hasMetadata("TrainingBot")) {
                TrainingBot bot = trainingManager.getBotByEntity(zombie);
                if (bot != null) {
                    dev.artix.artixduels.models.TrainingSession session = trainingManager.getSession(player.getUniqueId());
                    if (session != null && session.getBot().getBotId().equals(bot.getBotId())) {
                        double damage = event.getFinalDamage();
                        session.getStats().addPlayerDamageDealt(damage);
                        session.getStats().addBotDamageTaken(damage);
                        session.getStats().addPlayerHit();
                    }
                }
            }
        } else if (event.getEntity() instanceof Player && event.getDamager() instanceof Zombie) {
            Player player = (Player) event.getEntity();
            Zombie zombie = (Zombie) event.getDamager();
            
            if (zombie.hasMetadata("TrainingBot")) {
                TrainingBot bot = trainingManager.getBotByEntity(zombie);
                if (bot != null) {
                    dev.artix.artixduels.models.TrainingSession session = trainingManager.getSession(player.getUniqueId());
                    if (session != null && session.getBot().getBotId().equals(bot.getBotId())) {
                        double damage = event.getFinalDamage();
                        session.getStats().addBotDamageDealt(damage);
                        session.getStats().addPlayerDamageTaken(damage);
                        session.getStats().addBotHit();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Zombie) {
            Zombie zombie = (Zombie) event.getEntity();
            if (zombie.hasMetadata("TrainingBot")) {
                event.getDrops().clear();
                event.setDroppedExp(0);
                
                TrainingBot bot = trainingManager.getBotByEntity(zombie);
                if (bot != null) {
                    trainingManager.handleBotDeath(bot);
                }
            }
        } else if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            dev.artix.artixduels.models.TrainingSession session = trainingManager.getSession(player.getUniqueId());
            if (session != null) {
                event.getDrops().clear();
                event.setDroppedExp(0);
                trainingManager.handlePlayerDeath(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        dev.artix.artixduels.models.TrainingSession session = trainingManager.getSession(player.getUniqueId());
        if (session != null) {
            trainingManager.stopTraining(player.getUniqueId());
        }
    }
}

