package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de replays de duelos.
 */
public class ReplayManager {
    private final ArtixDuels plugin;
    private Map<UUID, Replay> activeRecordings;
    private Map<UUID, ReplaySession> activeSessions;
    private Map<UUID, Replay> savedReplays;
    private File replaysFolder;
    private static final int RECORDING_INTERVAL = 2; // Gravar a cada 2 ticks (0.1 segundos)

    public ReplayManager(ArtixDuels plugin) {
        this.plugin = plugin;
        this.activeRecordings = new HashMap<>();
        this.activeSessions = new HashMap<>();
        this.savedReplays = new HashMap<>();
        
        replaysFolder = new File(plugin.getDataFolder(), "replays");
        if (!replaysFolder.exists()) {
            replaysFolder.mkdirs();
        }
        
        loadSavedReplays();
        startRecordingTask();
        startPlaybackTask();
    }

    /**
     * Inicia a gravação de um duelo.
     */
    public void startRecording(Duel duel) {
        UUID duelId = duel.getPlayer1Id();
        
        org.bukkit.entity.Player player1 = Bukkit.getPlayer(duel.getPlayer1Id());
        org.bukkit.entity.Player player2 = Bukkit.getPlayer(duel.getPlayer2Id());
        
        if (player1 == null || player2 == null) {
            return;
        }

        Replay replay = new Replay(
            duel.getPlayer1Id(),
            player1.getName(),
            duel.getPlayer2Id(),
            player2.getName(),
            duel.getKitName(),
            duel.getArenaName(),
            duel.getMode()
        );

        activeRecordings.put(duelId, replay);
        plugin.getLogger().info("Iniciando gravação de replay: " + replay.getReplayId());
    }

    /**
     * Para a gravação de um duelo.
     */
    public void stopRecording(Duel duel, UUID winnerId) {
        UUID duelId = duel.getPlayer1Id();
        Replay replay = activeRecordings.remove(duelId);
        
        if (replay == null) {
            return;
        }

        replay.setEndTime(System.currentTimeMillis());
        
        org.bukkit.entity.Player winner = Bukkit.getPlayer(winnerId);
        if (winner != null) {
            replay.setWinner(winnerId, winner.getName());
        }

        saveReplay(replay);
        plugin.getLogger().info("Gravação finalizada: " + replay.getReplayId());
    }

    /**
     * Grava um frame do replay.
     */
    private void recordFrame(Replay replay) {
        if (replay == null) {
            return;
        }

        long timestamp = System.currentTimeMillis() - replay.getStartTime();
        ReplayFrame frame = new ReplayFrame(timestamp);

        org.bukkit.entity.Player player1 = Bukkit.getPlayer(replay.getPlayer1Id());
        org.bukkit.entity.Player player2 = Bukkit.getPlayer(replay.getPlayer2Id());

        if (player1 != null && player1.isOnline()) {
            recordPlayerFrame(frame, player1);
        }

        if (player2 != null && player2.isOnline()) {
            recordPlayerFrame(frame, player2);
        }

        replay.addFrame(frame);
    }

    /**
     * Grava um frame de um jogador.
     */
    private void recordPlayerFrame(ReplayFrame frame, Player player) {
        ReplayFrame.EntitySnapshot entitySnapshot = new ReplayFrame.EntitySnapshot(
            player.getHealth(),
            player.getMaxHealth(),
            player.getFoodLevel(),
            player.getSaturation()
        );
        frame.addEntitySnapshot(player.getUniqueId(), entitySnapshot);

        ReplayFrame.LocationSnapshot locationSnapshot = new ReplayFrame.LocationSnapshot(player.getLocation());
        frame.addLocationSnapshot(player.getUniqueId(), locationSnapshot);

        ReplayFrame.InventorySnapshot inventorySnapshot = new ReplayFrame.InventorySnapshot(
            player.getInventory().getContents(),
            player.getInventory().getArmorContents()
        );
        frame.addInventorySnapshot(player.getUniqueId(), inventorySnapshot);
    }

    /**
     * Salva um replay.
     */
    private void saveReplay(Replay replay) {
        File replayFile = new File(replaysFolder, replay.getReplayId().toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(replayFile);

        config.set("replay_id", replay.getReplayId().toString());
        config.set("player1_id", replay.getPlayer1Id().toString());
        config.set("player1_name", replay.getPlayer1Name());
        config.set("player2_id", replay.getPlayer2Id().toString());
        config.set("player2_name", replay.getPlayer2Name());
        config.set("kit_name", replay.getKitName());
        config.set("arena_name", replay.getArenaName());
        config.set("mode", replay.getMode().getName());
        config.set("start_time", replay.getStartTime());
        config.set("end_time", replay.getEndTime());
        config.set("duration", replay.getDuration());
        if (replay.getWinnerId() != null) {
            config.set("winner_id", replay.getWinnerId().toString());
            config.set("winner_name", replay.getWinnerName());
        }

        List<Map<String, Object>> framesData = new ArrayList<>();
        for (ReplayFrame frame : replay.getFrames()) {
            Map<String, Object> frameData = new HashMap<>();
            frameData.put("timestamp", frame.getTimestamp());
            
            Map<String, Object> locations = new HashMap<>();
            for (Map.Entry<UUID, ReplayFrame.LocationSnapshot> entry : frame.getLocationSnapshots().entrySet()) {
                Map<String, Object> locData = new HashMap<>();
                locData.put("x", entry.getValue().getX());
                locData.put("y", entry.getValue().getY());
                locData.put("z", entry.getValue().getZ());
                locData.put("yaw", entry.getValue().getYaw());
                locData.put("pitch", entry.getValue().getPitch());
                locations.put(entry.getKey().toString(), locData);
            }
            frameData.put("locations", locations);
            
            framesData.add(frameData);
        }
        config.set("frames", framesData);

        try {
            config.save(replayFile);
            savedReplays.put(replay.getReplayId(), replay);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar replay: " + e.getMessage());
        }
    }

    /**
     * Carrega replays salvos.
     */
    private void loadSavedReplays() {
        File[] files = replaysFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                UUID replayId = UUID.fromString(config.getString("replay_id"));
                
                Replay replay = new Replay(
                    UUID.fromString(config.getString("player1_id")),
                    config.getString("player1_name"),
                    UUID.fromString(config.getString("player2_id")),
                    config.getString("player2_name"),
                    config.getString("kit_name"),
                    config.getString("arena_name"),
                    DuelMode.fromString(config.getString("mode"))
                );
                
                replay.setEndTime(config.getLong("end_time"));
                if (config.contains("winner_id")) {
                    replay.setWinner(UUID.fromString(config.getString("winner_id")), 
                                   config.getString("winner_name"));
                }
                
                savedReplays.put(replayId, replay);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar replay " + file.getName() + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Carregados " + savedReplays.size() + " replays.");
    }

    /**
     * Inicia uma sessão de reprodução.
     */
    public ReplaySession startPlayback(Player viewer, UUID replayId) {
        Replay replay = savedReplays.get(replayId);
        if (replay == null) {
            return null;
        }

        ReplaySession session = new ReplaySession(viewer.getUniqueId(), replay);
        activeSessions.put(viewer.getUniqueId(), session);
        
        return session;
    }

    /**
     * Para uma sessão de reprodução.
     */
    public void stopPlayback(UUID viewerId) {
        ReplaySession session = activeSessions.remove(viewerId);
        if (session != null) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null) {
                viewer.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
        }
    }

    /**
     * Obtém uma sessão de reprodução.
     */
    public ReplaySession getSession(UUID viewerId) {
        return activeSessions.get(viewerId);
    }

    /**
     * Obtém todos os replays salvos.
     */
    public List<Replay> getSavedReplays() {
        return new ArrayList<>(savedReplays.values());
    }

    /**
     * Obtém um replay por ID.
     */
    public Replay getReplay(UUID replayId) {
        return savedReplays.get(replayId);
    }

    /**
     * Deleta um replay.
     */
    public boolean deleteReplay(UUID replayId) {
        Replay replay = savedReplays.remove(replayId);
        if (replay == null) {
            return false;
        }

        File replayFile = new File(replaysFolder, replayId.toString() + ".yml");
        if (replayFile.exists()) {
            replayFile.delete();
        }

        return true;
    }

    /**
     * Tarefa de gravação.
     */
    private void startRecordingTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Replay replay : new ArrayList<>(activeRecordings.values())) {
                recordFrame(replay);
            }
        }, 0L, RECORDING_INTERVAL);
    }

    /**
     * Tarefa de reprodução.
     */
    private void startPlaybackTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (ReplaySession session : new ArrayList<>(activeSessions.values())) {
                if (session.getState() == ReplaySession.ReplayState.PLAYING) {
                    playFrame(session);
                }
            }
        }, 0L, 1L);
    }

    /**
     * Reproduz um frame.
     */
    private void playFrame(ReplaySession session) {
        Replay replay = session.getReplay();
        if (session.getCurrentFrame() >= replay.getTotalFrames()) {
            session.setState(ReplaySession.ReplayState.STOPPED);
            return;
        }

        ReplayFrame frame = replay.getFrames().get(session.getCurrentFrame());
        Player viewer = Bukkit.getPlayer(session.getViewerId());
        
        if (viewer == null || !viewer.isOnline()) {
            activeSessions.remove(session.getViewerId());
            return;
        }

        if (!session.isFreeCamera()) {
            ReplayFrame.LocationSnapshot player1Loc = frame.getLocationSnapshots().get(replay.getPlayer1Id());
            if (player1Loc != null) {
                Location loc = player1Loc.toLocation(viewer.getWorld());
                viewer.teleport(loc);
            }
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - session.getLastUpdate();
        long frameTime = (long) (50 / session.getPlaybackSpeed());
        
        if (elapsed >= frameTime) {
            session.setCurrentFrame(session.getCurrentFrame() + 1);
            session.updateLastUpdate();
        }
    }
}

