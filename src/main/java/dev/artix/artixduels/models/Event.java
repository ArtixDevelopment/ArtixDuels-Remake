package dev.artix.artixduels.models;

import java.util.List;
import java.util.Map;

/**
 * Representa um evento especial.
 */
public class Event {
    private String id;
    private String name;
    private String description;
    private EventType type;
    private long startTime;
    private long endTime;
    private boolean active;
    private List<String> specialModes;
    private Map<String, Object> rewards;
    private List<String> challenges;
    private String theme;

    public enum EventType {
        SEASONAL("Sazonal"),
        SPECIAL("Especial"),
        LIMITED("Limitado");

        private String displayName;

        EventType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Event(String id, String name, EventType type, long startTime, long endTime) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getType() {
        return type;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active && System.currentTimeMillis() >= startTime && System.currentTimeMillis() <= endTime;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getSpecialModes() {
        return specialModes;
    }

    public void setSpecialModes(List<String> specialModes) {
        this.specialModes = specialModes;
    }

    public Map<String, Object> getRewards() {
        return rewards;
    }

    public void setRewards(Map<String, Object> rewards) {
        this.rewards = rewards;
    }

    public List<String> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<String> challenges) {
        this.challenges = challenges;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}

