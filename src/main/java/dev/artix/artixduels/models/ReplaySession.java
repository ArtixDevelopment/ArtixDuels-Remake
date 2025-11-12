package dev.artix.artixduels.models;

import java.util.UUID;

/**
 * Representa uma sessão de reprodução de replay.
 */
public class ReplaySession {
    private UUID sessionId;
    private UUID viewerId;
    private Replay replay;
    private int currentFrame;
    private ReplayState state;
    private double playbackSpeed;
    private boolean freeCamera;
    private long lastUpdate;

    public ReplaySession(UUID viewerId, Replay replay) {
        this.sessionId = UUID.randomUUID();
        this.viewerId = viewerId;
        this.replay = replay;
        this.currentFrame = 0;
        this.state = ReplayState.PAUSED;
        this.playbackSpeed = 1.0;
        this.freeCamera = false;
        this.lastUpdate = System.currentTimeMillis();
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getViewerId() {
        return viewerId;
    }

    public Replay getReplay() {
        return replay;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = Math.max(0, Math.min(currentFrame, replay.getTotalFrames() - 1));
    }

    public ReplayState getState() {
        return state;
    }

    public void setState(ReplayState state) {
        this.state = state;
        this.lastUpdate = System.currentTimeMillis();
    }

    public double getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(double playbackSpeed) {
        this.playbackSpeed = Math.max(0.25, Math.min(playbackSpeed, 4.0));
    }

    public boolean isFreeCamera() {
        return freeCamera;
    }

    public void setFreeCamera(boolean freeCamera) {
        this.freeCamera = freeCamera;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void updateLastUpdate() {
        this.lastUpdate = System.currentTimeMillis();
    }

    public boolean isFinished() {
        return currentFrame >= replay.getTotalFrames() - 1;
    }

    public enum ReplayState {
        PLAYING,
        PAUSED,
        STOPPED
    }
}

