package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Gerenciador de treinamento contra bots.
 */
public class TrainingManager {
    private final ArtixDuels plugin;
    private final KitManager kitManager;
    private final ArenaManager arenaManager;
    private Map<UUID, TrainingSession> activeSessions;
    private Map<UUID, TrainingBot> activeBots;
    private static final long BOT_ACTION_INTERVAL = 20L; // 1 segundo

    public TrainingManager(ArtixDuels plugin, KitManager kitManager, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
        this.activeSessions = new HashMap<>();
        this.activeBots = new HashMap<>();
        
        startBotAITask();
    }

    /**
     * Inicia uma sessão de treinamento.
     */
    public TrainingSession startTraining(Player player, BotDifficulty difficulty, String kitName, String arenaName, DuelMode mode) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage("§cVocê já está em uma sessão de treinamento!");
            return null;
        }

        Kit kit = kitManager.getKit(kitName);
        Arena arena = arenaManager.getArena(arenaName);

        if (kit == null) {
            player.sendMessage("§cKit não encontrado!");
            return null;
        }

        if (arena == null) {
            arena = arenaManager.getAvailableArena();
            if (arena == null) {
                player.sendMessage("§cNenhuma arena disponível!");
                return null;
            }
        }

        if (arena.isInUse()) {
            player.sendMessage("§cA arena está em uso!");
            return null;
        }

        arena.setInUse(true);

        String botName = "§7[Bot] §e" + difficulty.getDisplayName();
        TrainingBot bot = new TrainingBot(botName, difficulty, plugin);
        
        Location spawnLocation = arena.getPlayer1Spawn();
        if (spawnLocation == null) {
            spawnLocation = arena.getPlayer2Spawn();
            if (spawnLocation == null) {
                player.sendMessage("§cArena não tem spawn configurado!");
                return null;
            }
        }

        Zombie botEntity = spawnLocation.getWorld().spawn(spawnLocation, Zombie.class);
        botEntity.setCustomName(botName);
        botEntity.setCustomNameVisible(true);
        bot.setEntity(botEntity, plugin);
        bot.setTarget(player);
        bot.setMaxHealth(20.0);
        bot.setHealth(20.0);

        activeBots.put(bot.getBotId(), bot);

        TrainingSession session = new TrainingSession(player.getUniqueId(), bot, kitName, arenaName, mode);
        activeSessions.put(player.getUniqueId(), session);

        Location playerSpawn = arena.getPlayer2Spawn();
        if (playerSpawn == null) {
            playerSpawn = arena.getPlayer1Spawn();
        }
        if (playerSpawn != null) {
            player.teleport(playerSpawn);
        }
        giveKit(player, kit);
        giveBotKit(botEntity, kit);

        player.sendMessage("§aTreinamento iniciado contra bot " + difficulty.getDisplayName() + "!");
        player.sendMessage("§7Use §e/training stop §7para parar o treinamento.");

        return session;
    }

    /**
     * Para uma sessão de treinamento.
     */
    public void stopTraining(UUID playerId) {
        TrainingSession session = activeSessions.remove(playerId);
        if (session == null) {
            return;
        }

        session.setActive(false);
        TrainingBot bot = session.getBot();
        if (bot != null) {
            bot.remove();
            activeBots.remove(bot.getBotId());
        }

        Arena arena = arenaManager.getArena(session.getArenaName());
        if (arena != null) {
            arena.setInUse(false);
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendMessage("§cTreinamento finalizado!");
            showTrainingResults(player, session);
        }
    }

    /**
     * Obtém a sessão de treinamento de um jogador.
     */
    public TrainingSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    /**
     * Obtém um bot por ID.
     */
    public TrainingBot getBot(UUID botId) {
        return activeBots.get(botId);
    }

    /**
     * Obtém um bot por entidade.
     */
    public TrainingBot getBotByEntity(Zombie entity) {
        if (entity.hasMetadata("BotId")) {
            String botIdStr = entity.getMetadata("BotId").get(0).asString();
            try {
                UUID botId = UUID.fromString(botIdStr);
                return activeBots.get(botId);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Dá kit para o jogador.
     */
    private void giveKit(Player player, Kit kit) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        
        if (kit.getContents() != null) {
            for (int i = 0; i < kit.getContents().length && i < 36; i++) {
                if (kit.getContents()[i] != null) {
                    player.getInventory().setItem(i, kit.getContents()[i]);
                }
            }
        }
        
        if (kit.getArmor() != null) {
            ItemStack[] armor = kit.getArmor();
            player.getInventory().setHelmet(armor[0]);
            player.getInventory().setChestplate(armor[1]);
            player.getInventory().setLeggings(armor[2]);
            player.getInventory().setBoots(armor[3]);
        }
        
        player.updateInventory();
    }

    /**
     * Dá kit para o bot.
     */
    private void giveBotKit(Zombie bot, Kit kit) {
        bot.getEquipment().clear();
        
        if (kit.getContents() != null && kit.getContents().length > 0) {
            for (ItemStack item : kit.getContents()) {
                if (item != null) {
                    bot.getEquipment().setItemInHand(item);
                    break;
                }
            }
        }
        
        if (kit.getArmor() != null) {
            ItemStack[] armor = kit.getArmor();
            bot.getEquipment().setHelmet(armor[0]);
            bot.getEquipment().setChestplate(armor[1]);
            bot.getEquipment().setLeggings(armor[2]);
            bot.getEquipment().setBoots(armor[3]);
        }
    }

    /**
     * Mostra resultados do treinamento.
     */
    private void showTrainingResults(Player player, TrainingSession session) {
        TrainingSession.TrainingStats stats = session.getStats();
        
        player.sendMessage("§6§l=== RESULTADOS DO TREINAMENTO ===");
        player.sendMessage("§7Duração: §b" + formatDuration(session.getDuration()));
        player.sendMessage("§7Acertos: §a" + stats.getPlayerHits() + " §7/ §c" + stats.getBotHits());
        player.sendMessage("§7Precisão: §b" + String.format("%.1f", stats.getPlayerAccuracy()) + "%");
        player.sendMessage("§7Eliminações: §a" + stats.getPlayerKills() + " §7/ §c" + stats.getBotKills());
        player.sendMessage("§7Taxa de Vitória: §b" + String.format("%.1f", stats.getPlayerWinRate()) + "%");
        player.sendMessage("§7Combos: §e" + stats.getPlayerCombos());
        player.sendMessage("§7Dano Causado: §a" + String.format("%.1f", stats.getPlayerDamageDealt()));
        player.sendMessage("§7Dano Recebido: §c" + String.format("%.1f", stats.getPlayerDamageTaken()));
    }

    /**
     * Formata duração.
     */
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Inicia tarefa de IA do bot.
     */
    private void startBotAITask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (TrainingBot bot : new ArrayList<>(activeBots.values())) {
                    if (bot.getEntity() == null || bot.getEntity().isDead()) {
                        continue;
                    }

                    Player target = bot.getTarget();
                    if (target == null || !target.isOnline()) {
                        continue;
                    }

                    TrainingSession session = getSessionForBot(bot);
                    if (session == null || !session.isActive()) {
                        continue;
                    }

                    updateBotAI(bot, target, session);
                }
            }
        }.runTaskTimer(plugin, 0L, BOT_ACTION_INTERVAL);
    }

    /**
     * Atualiza IA do bot.
     */
    private void updateBotAI(TrainingBot bot, Player target, TrainingSession session) {
        Zombie entity = bot.getEntity();
        if (entity == null || entity.isDead()) {
            return;
        }

        Location botLoc = entity.getLocation();
        Location targetLoc = target.getLocation();
        double distance = botLoc.distance(targetLoc);

        BotDifficulty difficulty = bot.getDifficulty();
        Random random = new Random();

        if (distance > 20) {
            entity.teleport(targetLoc);
        } else if (distance > 3) {
            if (random.nextDouble() < difficulty.getMovementChance()) {
                entity.setTarget(target);
            }
        } else {
            if (random.nextDouble() < difficulty.getHitChance()) {
                target.damage(1.0, entity);
                session.getStats().addBotHit();
                bot.incrementCombo();
                
                if (bot.getComboCount() >= 3 && random.nextDouble() < difficulty.getComboChance()) {
                    session.getStats().addBotCombo();
                }
            }
        }

        if (random.nextDouble() < difficulty.getBlockChance() && distance < 2) {
            bot.setBlocking(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> bot.setBlocking(false), 20L);
        }

        bot.updateLastAction();
    }

    /**
     * Obtém sessão para um bot.
     */
    private TrainingSession getSessionForBot(TrainingBot bot) {
        for (TrainingSession session : activeSessions.values()) {
            if (session.getBot().getBotId().equals(bot.getBotId())) {
                return session;
            }
        }
        return null;
    }

    /**
     * Processa morte do bot.
     */
    public void handleBotDeath(TrainingBot bot) {
        TrainingSession session = getSessionForBot(bot);
        if (session != null) {
            session.getStats().addPlayerKill();
            
            Player player = Bukkit.getPlayer(session.getPlayerId());
            if (player != null && player.isOnline()) {
                player.sendMessage("§aVocê eliminou o bot!");
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    bot.setHealth(bot.getMaxHealth());
                    bot.getEntity().setHealth(bot.getMaxHealth());
                    Location spawnLoc = arenaManager.getArena(session.getArenaName()).getPlayer1Spawn();
                    if (spawnLoc != null) {
                        bot.getEntity().teleport(spawnLoc);
                    }
                }, 60L);
            }
        }
    }

    /**
     * Processa morte do jogador.
     */
    public void handlePlayerDeath(Player player) {
        TrainingSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.getStats().addBotKill();
            
            player.sendMessage("§cVocê foi eliminado pelo bot!");
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                Location spawnLoc = arenaManager.getArena(session.getArenaName()).getPlayer2Spawn();
                if (spawnLoc != null) {
                    player.teleport(spawnLoc);
                }
                giveKit(player, kitManager.getKit(session.getKitName()));
            }, 60L);
        }
    }
}

