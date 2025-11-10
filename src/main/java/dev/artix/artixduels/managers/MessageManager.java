package dev.artix.artixduels.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private FileConfiguration messagesConfig;
    private String prefix;
    private Map<String, String> messages;

    public MessageManager(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
        this.messages = new HashMap<>();
        loadMessages();
    }

    private void loadMessages() {
        prefix = ChatColor.translateAlternateColorCodes('&', 
            messagesConfig.getString("messages.prefix", "&6[ArtixDuels] &r"));
        
        loadSection("messages.duel", messages);
        loadSection("messages.error", messages);
        loadSection("messages.admin", messages);
        loadSection("messages.betting", messages);
        loadSection("messages.spectator", messages);
        loadSection("messages.stats", messages);
        loadSection("messages.history", messages);
        loadSection("messages.gui", messages);
    }

    private void loadSection(String path, Map<String, String> map) {
        if (messagesConfig.getConfigurationSection(path) != null) {
            for (String key : messagesConfig.getConfigurationSection(path).getKeys(false)) {
                String fullKey = path.replace("messages.", "") + "." + key;
                String value = messagesConfig.getString(path + "." + key, "");
                map.put(fullKey, ChatColor.translateAlternateColorCodes('&', value));
            }
        }
    }

    public String getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, "&cMensagem não encontrada: " + key);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return prefix + message;
    }

    public String getMessageNoPrefix(String key) {
        return getMessageNoPrefix(key, new HashMap<>());
    }

    public String getMessageNoPrefix(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, "&cMensagem não encontrada: " + key);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return message;
    }

    public void reload() {
        messages.clear();
        loadMessages();
    }
}

