package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.Theme;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Month;
import java.util.*;

/**
 * Gerenciador de temas visuais.
 */
public class ThemeManager {
    private final ArtixDuels plugin;
    private Map<String, Theme> themes;
    private Map<UUID, String> playerThemes;
    private String defaultTheme;

    public ThemeManager(ArtixDuels plugin) {
        this.plugin = plugin;
        this.themes = new HashMap<>();
        this.playerThemes = new HashMap<>();
        this.defaultTheme = "dark";
        
        loadDefaultThemes();
        loadPlayerThemes();
    }

    /**
     * Carrega temas padrão.
     */
    private void loadDefaultThemes() {
        // Tema Dark
        Theme dark = new Theme("dark", "Escuro", "Tema escuro com cores suaves");
        dark.setColor("primary", "&b");
        dark.setColor("secondary", "&7");
        dark.setColor("success", "&a");
        dark.setColor("danger", "&c");
        dark.setColor("warning", "&e");
        dark.setColor("info", "&9");
        dark.setColor("title", "&6&l");
        dark.setColor("text", "&f");
        dark.setColor("border", "&8");
        themes.put("dark", dark);

        // Tema Light
        Theme light = new Theme("light", "Claro", "Tema claro com cores vibrantes");
        light.setColor("primary", "&9");
        light.setColor("secondary", "&8");
        light.setColor("success", "&2");
        light.setColor("danger", "&4");
        light.setColor("warning", "&6");
        light.setColor("info", "&3");
        light.setColor("title", "&9&l");
        light.setColor("text", "&0");
        light.setColor("border", "&7");
        themes.put("light", light);

        // Tema Colorful
        Theme colorful = new Theme("colorful", "Colorido", "Tema colorido e vibrante");
        colorful.setColor("primary", "&d");
        colorful.setColor("secondary", "&5");
        colorful.setColor("success", "&a");
        colorful.setColor("danger", "&c");
        colorful.setColor("warning", "&e");
        colorful.setColor("info", "&b");
        colorful.setColor("title", "&d&l");
        colorful.setColor("text", "&f");
        colorful.setColor("border", "&5");
        themes.put("colorful", colorful);

        // Tema Sazonal: Natal
        Theme christmas = new Theme("christmas", "Natal", "Tema especial de Natal");
        christmas.setColor("primary", "&c");
        christmas.setColor("secondary", "&a");
        christmas.setColor("success", "&a");
        christmas.setColor("danger", "&c");
        christmas.setColor("warning", "&e");
        christmas.setColor("info", "&b");
        christmas.setColor("title", "&c&l");
        christmas.setColor("text", "&f");
        christmas.setColor("border", "&a");
        christmas.setSeasonal(true);
        christmas.setSeason("winter");
        themes.put("christmas", christmas);

        // Tema Sazonal: Halloween
        Theme halloween = new Theme("halloween", "Halloween", "Tema especial de Halloween");
        halloween.setColor("primary", "&6");
        halloween.setColor("secondary", "&8");
        halloween.setColor("success", "&a");
        halloween.setColor("danger", "&4");
        halloween.setColor("warning", "&e");
        halloween.setColor("info", "&5");
        halloween.setColor("title", "&6&l");
        halloween.setColor("text", "&f");
        halloween.setColor("border", "&8");
        halloween.setSeasonal(true);
        halloween.setSeason("autumn");
        themes.put("halloween", halloween);
    }

    /**
     * Define o tema de um jogador.
     */
    public void setPlayerTheme(UUID playerId, String themeName) {
        if (themes.containsKey(themeName)) {
            playerThemes.put(playerId, themeName);
            savePlayerThemes();
        }
    }

    /**
     * Obtém o tema de um jogador.
     */
    public String getPlayerTheme(UUID playerId) {
        return playerThemes.getOrDefault(playerId, defaultTheme);
    }

    /**
     * Obtém o objeto Theme de um jogador.
     */
    public Theme getTheme(UUID playerId) {
        String themeName = getPlayerTheme(playerId);
        return themes.getOrDefault(themeName, themes.get(defaultTheme));
    }

    /**
     * Obtém um tema por nome.
     */
    public Theme getTheme(String themeName) {
        return themes.get(themeName);
    }

    /**
     * Aplica cores de tema a uma string.
     */
    public String applyTheme(String text, UUID playerId) {
        Theme theme = getTheme(playerId);
        return applyTheme(text, theme);
    }

    /**
     * Aplica cores de tema a uma string.
     */
    public String applyTheme(String text, Theme theme) {
        if (theme == null) {
            return ChatColor.translateAlternateColorCodes('&', text);
        }

        String result = text;
        for (Map.Entry<String, String> entry : theme.getColors().entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    /**
     * Obtém cor do tema.
     */
    public String getColor(UUID playerId, String colorKey) {
        Theme theme = getTheme(playerId);
        return theme != null ? theme.getColor(colorKey) : "&f";
    }

    /**
     * Obtém todos os temas disponíveis.
     */
    public Map<String, Theme> getThemes() {
        return themes;
    }

    /**
     * Obtém temas sazonais ativos.
     */
    public List<Theme> getSeasonalThemes() {
        List<Theme> seasonal = new ArrayList<>();
        Month currentMonth = java.time.LocalDate.now().getMonth();
        
        for (Theme theme : themes.values()) {
            if (theme.isSeasonal()) {
                String season = theme.getSeason();
                if (season != null) {
                    boolean isActive = false;
                    if (season.equals("winter") && (currentMonth == Month.DECEMBER || currentMonth == Month.JANUARY)) {
                        isActive = true;
                    } else if (season.equals("autumn") && (currentMonth == Month.OCTOBER || currentMonth == Month.NOVEMBER)) {
                        isActive = true;
                    } else if (season.equals("spring") && (currentMonth == Month.MARCH || currentMonth == Month.APRIL || currentMonth == Month.MAY)) {
                        isActive = true;
                    } else if (season.equals("summer") && (currentMonth == Month.JUNE || currentMonth == Month.JULY || currentMonth == Month.AUGUST)) {
                        isActive = true;
                    }
                    
                    if (isActive) {
                        seasonal.add(theme);
                    }
                }
            }
        }
        
        return seasonal;
    }

    /**
     * Cria tema customizado para um jogador.
     */
    public boolean createCustomTheme(UUID playerId, String themeName, Map<String, String> colors) {
        Theme customTheme = new Theme("custom_" + playerId.toString(), themeName, "Tema customizado");
        customTheme.setColors(colors);
        themes.put("custom_" + playerId.toString(), customTheme);
        setPlayerTheme(playerId, "custom_" + playerId.toString());
        saveThemes();
        return true;
    }

    /**
     * Carrega temas de jogadores.
     */
    private void loadPlayerThemes() {
        File prefsFile = new File(plugin.getDataFolder(), "player_themes.yml");
        if (!prefsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(prefsFile);
        if (config.contains("themes")) {
            for (String playerIdStr : config.getConfigurationSection("themes").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    String themeName = config.getString("themes." + playerIdStr, defaultTheme);
                    playerThemes.put(playerId, themeName);
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }
    }

    /**
     * Salva temas de jogadores.
     */
    private void savePlayerThemes() {
        File prefsFile = new File(plugin.getDataFolder(), "player_themes.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(prefsFile);

        for (Map.Entry<UUID, String> entry : playerThemes.entrySet()) {
            config.set("themes." + entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(prefsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar temas: " + e.getMessage());
        }
    }

    /**
     * Salva temas customizados.
     */
    private void saveThemes() {
        File themesFile = new File(plugin.getDataFolder(), "custom_themes.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(themesFile);

        for (Map.Entry<String, Theme> entry : themes.entrySet()) {
            if (entry.getKey().startsWith("custom_")) {
                Theme theme = entry.getValue();
                String path = "themes." + entry.getKey();
                config.set(path + ".name", theme.getName());
                config.set(path + ".display-name", theme.getDisplayName());
                config.set(path + ".description", theme.getDescription());
                config.set(path + ".colors", theme.getColors());
            }
        }

        try {
            config.save(themesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar temas customizados: " + e.getMessage());
        }
    }

    /**
     * Carrega temas customizados.
     */
    public void loadCustomThemes() {
        File themesFile = new File(plugin.getDataFolder(), "custom_themes.yml");
        if (!themesFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(themesFile);
        if (config.contains("themes")) {
            for (String themeKey : config.getConfigurationSection("themes").getKeys(false)) {
                String path = "themes." + themeKey;
                String name = config.getString(path + ".name");
                String displayName = config.getString(path + ".display-name");
                String description = config.getString(path + ".description");
                
                Theme theme = new Theme(name, displayName, description);
                if (config.contains(path + ".colors")) {
                    Map<String, String> colors = new HashMap<>();
                    for (String colorKey : config.getConfigurationSection(path + ".colors").getKeys(false)) {
                        colors.put(colorKey, config.getString(path + ".colors." + colorKey));
                    }
                    theme.setColors(colors);
                }
                
                themes.put(themeKey, theme);
            }
        }
    }
}

