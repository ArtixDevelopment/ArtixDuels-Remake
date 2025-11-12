package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Event;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de eventos especiais.
 */
public class EventManager {
    private final ArtixDuels plugin;
    private final NotificationManager notificationManager;
    private Map<String, Event> events;
    private Event currentEvent;

    public EventManager(ArtixDuels plugin, NotificationManager notificationManager) {
        this.plugin = plugin;
        this.notificationManager = notificationManager;
        this.events = new HashMap<>();
        
        loadEvents();
        checkCurrentEvent();
    }

    public void createEvent(String id, String name, Event.EventType type, long startTime, long endTime) {
        Event event = new Event(id, name, type, startTime, endTime);
        events.put(id, event);
        saveEvents();
    }

    public void startEvent(String eventId) {
        Event event = events.get(eventId);
        if (event == null) return;
        
        if (currentEvent != null) {
            currentEvent.setActive(false);
        }
        
        event.setActive(true);
        currentEvent = event;
        
        // Notificar jogadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("§6§l[EVENTO] §e" + event.getName() + " começou!");
        }
        
        saveEvents();
    }

    public void endEvent(String eventId) {
        Event event = events.get(eventId);
        if (event == null) return;
        
        event.setActive(false);
        if (currentEvent != null && currentEvent.getId().equals(eventId)) {
            currentEvent = null;
        }
        
        saveEvents();
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    private void checkCurrentEvent() {
        for (Event event : events.values()) {
            if (event.isActive()) {
                currentEvent = event;
                return;
            }
        }
    }

    private void loadEvents() {
        File eventsFile = new File(plugin.getDataFolder(), "events.yml");
        if (!eventsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventsFile);
        if (config.contains("events")) {
            for (String eventId : config.getConfigurationSection("events").getKeys(false)) {
                String path = "events." + eventId;
                String name = config.getString(path + ".name");
                String typeStr = config.getString(path + ".type", "SPECIAL");
                Event.EventType type = Event.EventType.valueOf(typeStr);
                long startTime = config.getLong(path + ".start-time");
                long endTime = config.getLong(path + ".end-time");
                boolean active = config.getBoolean(path + ".active", false);

                Event event = new Event(eventId, name, type, startTime, endTime);
                event.setActive(active);
                events.put(eventId, event);
            }
        }
    }

    private void saveEvents() {
        File eventsFile = new File(plugin.getDataFolder(), "events.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventsFile);

        for (Map.Entry<String, Event> entry : events.entrySet()) {
            String path = "events." + entry.getKey();
            Event event = entry.getValue();
            config.set(path + ".name", event.getName());
            config.set(path + ".type", event.getType().toString());
            config.set(path + ".start-time", event.getStartTime());
            config.set(path + ".end-time", event.getEndTime());
            config.set(path + ".active", event.isActive());
        }

        try {
            config.save(eventsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar eventos: " + e.getMessage());
        }
    }
}

