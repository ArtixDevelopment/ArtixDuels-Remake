package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Duel;
import dev.artix.artixduels.models.DuelRequest;
import dev.artix.artixduels.models.NotificationPreferences;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gerenciador de notificações para duelos.
 * Envia notificações sobre convites, duelos, etc.
 */
public class NotificationManager {
    private final ArtixDuels plugin;
    private final DuelManager duelManager;
    private Map<UUID, BukkitTask> requestNotifications;
    private Map<UUID, BukkitTask> duelNotifications;
    private Map<UUID, NotificationPreferences> preferences;

    public NotificationManager(ArtixDuels plugin, DuelManager duelManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.requestNotifications = new HashMap<>();
        this.duelNotifications = new HashMap<>();
        this.preferences = new HashMap<>();
    }

    /**
     * Obtém as preferências de notificação de um jogador.
     */
    public NotificationPreferences getPreferences(UUID playerId) {
        return preferences.computeIfAbsent(playerId, NotificationPreferences::new);
    }

    /**
     * Define as preferências de notificação de um jogador.
     */
    public void setPreferences(UUID playerId, NotificationPreferences prefs) {
        preferences.put(playerId, prefs);
    }

    /**
     * Notifica um jogador sobre um convite de duelo pendente.
     */
    public void notifyDuelRequest(Player player, DuelRequest request) {
        Player challenger = Bukkit.getPlayer(request.getChallengerId());
        if (challenger == null || !challenger.isOnline()) {
            return;
        }

        NotificationPreferences prefs = getPreferences(player.getUniqueId());

        // Notificação inicial
        sendRequestNotification(player, challenger.getName(), request.getMode().getDisplayName(), prefs);

        // Notificações periódicas (a cada 30 segundos)
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!duelManager.hasPendingRequest(player.getUniqueId())) {
                cancelRequestNotification(player.getUniqueId());
                return;
            }
            NotificationPreferences currentPrefs = getPreferences(player.getUniqueId());
            sendRequestNotification(player, challenger.getName(), request.getMode().getDisplayName(), currentPrefs);
        }, 600L, 600L); // 30 segundos = 600 ticks

        requestNotifications.put(player.getUniqueId(), task);
    }

    /**
     * Cancela notificações de convite.
     */
    public void cancelRequestNotification(UUID playerId) {
        BukkitTask task = requestNotifications.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Notifica jogadores sobre um duelo iniciando.
     */
    public void notifyDuelStart(Duel duel) {
        Player player1 = Bukkit.getPlayer(duel.getPlayer1Id());
        Player player2 = Bukkit.getPlayer(duel.getPlayer2Id());

        NotificationPreferences prefs1 = getPreferences(duel.getPlayer1Id());
        NotificationPreferences prefs2 = getPreferences(duel.getPlayer2Id());

        if (player1 != null && player1.isOnline()) {
            sendNotification(player1, "§6§l[DUELO] §eDuelo iniciado contra §c" + player2.getName() + "§e!",
                "§7Modo: §b" + duel.getMode().getDisplayName(), NotificationType.DUEL_START, prefs1);
        }

        if (player2 != null && player2.isOnline()) {
            sendNotification(player2, "§6§l[DUELO] §eDuelo iniciado contra §c" + player1.getName() + "§e!",
                "§7Modo: §b" + duel.getMode().getDisplayName(), NotificationType.DUEL_START, prefs2);
        }
    }

    /**
     * Notifica jogadores sobre um duelo terminando.
     */
    public void notifyDuelEnd(Duel duel, UUID winnerId) {
        Player winner = Bukkit.getPlayer(winnerId);
        UUID loserId = duel.getPlayer1Id().equals(winnerId) ? duel.getPlayer2Id() : duel.getPlayer1Id();
        Player loser = Bukkit.getPlayer(loserId);

        NotificationPreferences winnerPrefs = getPreferences(winnerId);
        NotificationPreferences loserPrefs = getPreferences(loserId);

        if (winner != null && winner.isOnline()) {
            sendNotification(winner, "§a§l[DUELO] §aVocê venceu o duelo!",
                "§7Parabéns pela vitória!", NotificationType.DUEL_WIN, winnerPrefs);
        }

        if (loser != null && loser.isOnline()) {
            sendNotification(loser, "§c§l[DUELO] §cVocê perdeu o duelo!",
                "§7Continue tentando!", NotificationType.DUEL_LOSS, loserPrefs);
        }
    }

    /**
     * Envia uma notificação de convite.
     */
    private void sendRequestNotification(Player player, String challengerName, String mode, NotificationPreferences prefs) {
        sendNotification(player, "§6§l[CONVITE] §e" + challengerName + " §7desafiou você para um duelo!",
            "§7Modo: §b" + mode + " §7| Use §a/accept §7ou §c/deny", NotificationType.DUEL_REQUEST, prefs);
    }

    /**
     * Envia uma notificação completa com todos os tipos suportados.
     */
    public void sendNotification(Player player, String message, String subtitle, NotificationType type, NotificationPreferences prefs) {
        if (prefs == null) {
            prefs = getPreferences(player.getUniqueId());
        }

        // Chat (sempre habilitado como fallback)
        if (prefs.isChatEnabled()) {
            player.sendMessage(message);
            if (subtitle != null && !subtitle.isEmpty()) {
                player.sendMessage(subtitle);
            }
        }

        // Título
        if (prefs.isTitleEnabled()) {
            sendTitleNotification(player, message, subtitle, type);
        }

        // Actionbar
        if (prefs.isActionbarEnabled()) {
            sendActionbarNotification(player, message, type);
        }

        // Som
        if (prefs.isSoundEnabled()) {
            sendSoundNotification(player, type);
        }

        // Partículas
        if (prefs.isParticlesEnabled()) {
            sendParticleNotification(player, type);
        }
    }

    /**
     * Envia notificação de título.
     */
    private void sendTitleNotification(Player player, String title, String subtitle, NotificationType type) {
        try {
            // Tentar usar reflexão para versões mais novas (1.8+)
            java.lang.reflect.Method sendTitleMethod = player.getClass().getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            int fadeIn = 10;
            int stay = 60;
            int fadeOut = 20;

            if (type == NotificationType.DUEL_REQUEST) {
                fadeIn = 5;
                stay = 40;
                fadeOut = 10;
            }

            sendTitleMethod.invoke(player, cleanColorCodes(title), cleanColorCodes(subtitle != null ? subtitle : ""), fadeIn, stay, fadeOut);
        } catch (Exception e) {
            // Fallback para versões antigas do Minecraft (1.7.10)
            try {
                java.lang.reflect.Method sendTitleMethod = player.getClass().getMethod("sendTitle", String.class, String.class);
                sendTitleMethod.invoke(player, cleanColorCodes(title), cleanColorCodes(subtitle != null ? subtitle : ""));
            } catch (Exception ex) {
                // Se não suportar título, apenas ignora
            }
        }
    }

    /**
     * Envia notificação de actionbar.
     */
    private void sendActionbarNotification(Player player, String message, NotificationType type) {
        try {
            // Tentar usar reflexão para actionbar (1.8+)
            Object spigot = player.getClass().getMethod("spigot").invoke(player);
            java.lang.reflect.Method sendMessageMethod = spigot.getClass().getMethod("sendMessage", 
                net.md_5.bungee.api.ChatMessageType.class, net.md_5.bungee.api.chat.BaseComponent[].class);
            
            net.md_5.bungee.api.chat.BaseComponent[] components = 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message);
            sendMessageMethod.invoke(spigot, net.md_5.bungee.api.ChatMessageType.ACTION_BAR, components);
        } catch (Exception e) {
            // Fallback: usar chat se actionbar não estiver disponível
            if (getPreferences(player.getUniqueId()).isChatEnabled()) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * Envia notificação de som.
     */
    private void sendSoundNotification(Player player, NotificationType type) {
        Sound sound = getSoundForType(type);
        float volume = 1.0f;
        float pitch = 1.0f;

        switch (type) {
            case DUEL_REQUEST:
                pitch = 1.2f;
                break;
            case DUEL_WIN:
                pitch = 1.5f;
                volume = 1.2f;
                break;
            case DUEL_LOSS:
                pitch = 0.8f;
                break;
            case DUEL_START:
                pitch = 1.0f;
                break;
            case CHALLENGE_COMPLETE:
                pitch = 1.3f;
                volume = 1.1f;
                break;
            case RANK_UP:
                pitch = 1.4f;
                volume = 1.2f;
                break;
            case ACHIEVEMENT_UNLOCKED:
                pitch = 1.5f;
                volume = 1.2f;
                break;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Envia notificação de partículas.
     */
    private void sendParticleNotification(Player player, NotificationType type) {
        try {
            // Tentar usar partículas (1.9+)
            Class<?> particleClass = Class.forName("org.bukkit.Particle");
            Object particle = getParticleForType(type, particleClass);
            org.bukkit.Location loc = player.getLocation().add(0, 1, 0);
            int count = getParticleCountForType(type);

            java.lang.reflect.Method spawnParticleMethod = player.getClass().getMethod("spawnParticle", 
                particleClass, org.bukkit.Location.class, int.class, double.class, double.class, double.class, double.class);
            spawnParticleMethod.invoke(player, particle, loc, count, 0.5, 0.5, 0.5, 0.1);
        } catch (Exception e) {
            // Fallback para versões antigas (1.7.10)
            try {
                org.bukkit.Effect effect = getEffectForType(type);
                org.bukkit.Location loc = player.getLocation().add(0, 1, 0);
                player.getWorld().playEffect(loc, effect, 0);
            } catch (Exception ex) {
                // Se não suportar partículas, apenas ignora
            }
        }
    }

    /**
     * Obtém o som apropriado para o tipo de notificação.
     */
    private Sound getSoundForType(NotificationType type) {
        switch (type) {
            case DUEL_REQUEST:
                return Sound.NOTE_PLING;
            case DUEL_WIN:
                return Sound.LEVEL_UP;
            case DUEL_LOSS:
                return Sound.ANVIL_BREAK;
            case DUEL_START:
                return Sound.ENDERDRAGON_GROWL;
            case CHALLENGE_COMPLETE:
                return Sound.LEVEL_UP;
            case RANK_UP:
                return Sound.LEVEL_UP;
            case ACHIEVEMENT_UNLOCKED:
                return Sound.LEVEL_UP;
            default:
                return Sound.NOTE_PLING;
        }
    }

    /**
     * Obtém a partícula apropriada para o tipo de notificação.
     */
    private Object getParticleForType(NotificationType type, Class<?> particleClass) {
        try {
            switch (type) {
                case DUEL_REQUEST:
                    return particleClass.getField("VILLAGER_HAPPY").get(null);
                case DUEL_WIN:
                    return particleClass.getField("FIREWORKS_SPARK").get(null);
                case DUEL_LOSS:
                    return particleClass.getField("SMOKE_LARGE").get(null);
                case DUEL_START:
                    return particleClass.getField("FLAME").get(null);
                case CHALLENGE_COMPLETE:
                    return particleClass.getField("FIREWORKS_SPARK").get(null);
                case RANK_UP:
                    return particleClass.getField("VILLAGER_HAPPY").get(null);
                case ACHIEVEMENT_UNLOCKED:
                    return particleClass.getField("FIREWORKS_SPARK").get(null);
                default:
                    return particleClass.getField("VILLAGER_HAPPY").get(null);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtém o efeito apropriado para versões antigas (1.7.10).
     */
    private org.bukkit.Effect getEffectForType(NotificationType type) {
        switch (type) {
            case DUEL_REQUEST:
                return org.bukkit.Effect.HAPPY_VILLAGER;
            case DUEL_WIN:
                return org.bukkit.Effect.FIREWORKS_SPARK;
            case DUEL_LOSS:
                return org.bukkit.Effect.SMOKE;
            case DUEL_START:
                return org.bukkit.Effect.MOBSPAWNER_FLAMES;
            case CHALLENGE_COMPLETE:
                return org.bukkit.Effect.FIREWORKS_SPARK;
            case RANK_UP:
                return org.bukkit.Effect.HAPPY_VILLAGER;
            case ACHIEVEMENT_UNLOCKED:
                return org.bukkit.Effect.FIREWORKS_SPARK;
            default:
                return org.bukkit.Effect.HAPPY_VILLAGER;
        }
    }

    /**
     * Obtém a quantidade de partículas para o tipo.
     */
    private int getParticleCountForType(NotificationType type) {
        switch (type) {
            case DUEL_REQUEST:
                return 10;
            case DUEL_WIN:
                return 30;
            case DUEL_LOSS:
                return 15;
            case DUEL_START:
                return 20;
            case CHALLENGE_COMPLETE:
                return 25;
            case RANK_UP:
                return 25;
            case ACHIEVEMENT_UNLOCKED:
                return 30;
            default:
                return 10;
        }
    }

    /**
     * Remove códigos de cor de uma string para títulos.
     */
    private String cleanColorCodes(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
    }

    /**
     * Limpa todas as notificações.
     */
    public void clearAll() {
        for (BukkitTask task : requestNotifications.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        requestNotifications.clear();

        for (BukkitTask task : duelNotifications.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        duelNotifications.clear();
    }

    /**
     * Tipos de notificação.
     */
    public enum NotificationType {
        DUEL_REQUEST,
        DUEL_START,
        DUEL_WIN,
        DUEL_LOSS,
        CHALLENGE_COMPLETE,
        RANK_UP,
        ACHIEVEMENT_UNLOCKED
    }

    /**
     * Envia notificação de conquista desbloqueada.
     */
    public void sendAchievementNotification(Player player, dev.artix.artixduels.models.Achievement achievement) {
        NotificationPreferences prefs = getPreferences(player.getUniqueId());
        
        String title = "§6§lCONQUISTA DESBLOQUEADA!";
        String subtitle = achievement.getRarity().getDisplayName() + " " + achievement.getName();
        
        sendNotification(player, title, subtitle, NotificationType.ACHIEVEMENT_UNLOCKED, prefs);
        
        if (prefs.isChatEnabled()) {
            player.sendMessage("§6§l=== CONQUISTA DESBLOQUEADA ===");
            player.sendMessage(achievement.getRarity().getDisplayName() + " §e" + achievement.getName());
            player.sendMessage("§7" + achievement.getDescription());
        }
    }
}
