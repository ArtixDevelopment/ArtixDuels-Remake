package dev.artix.artixduels;

import dev.artix.artixduels.commands.*;
import dev.artix.artixduels.database.DatabaseManager;
import dev.artix.artixduels.database.IDuelHistoryDAO;
import dev.artix.artixduels.database.IStatsDAO;
import dev.artix.artixduels.gui.ConfigGUI;
import dev.artix.artixduels.gui.DuelModeSelectionGUI;
import dev.artix.artixduels.gui.ScoreboardModeSelectionGUI;
import dev.artix.artixduels.listeners.DuelListener;
import dev.artix.artixduels.listeners.NPCListener;
import dev.artix.artixduels.listeners.ProfileItemListener;
import dev.artix.artixduels.listeners.TablistListener;
import dev.artix.artixduels.managers.*;
import dev.artix.artixduels.npcs.DuelNPC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ArtixDuels extends JavaPlugin {

    private DatabaseManager databaseManager;
    private IStatsDAO statsDAO;
    private IDuelHistoryDAO historyDAO;
    private StatsManager statsManager;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private ScoreboardManager scoreboardManager;
    private RewardManager rewardManager;
    private BetManager betManager;
    private CooldownManager cooldownManager;
    private SpectatorManager spectatorManager;
    private DuelManager duelManager;
    private DuelNPC duelNPC;
    private TablistManager tablistManager;
    private MessageManager messageManager;
    private ConfigGUI configGUI;
    private DuelModeSelectionGUI duelModeSelectionGUI;
    private PlayerScoreboardPreferences scoreboardPreferences;
    private ScoreboardModeSelectionGUI scoreboardModeSelectionGUI;
    private FileConfiguration scoreboardConfig;
    private File scoreboardFile;
    private FileConfiguration tablistConfig;
    private File tablistFile;
    private FileConfiguration npcsConfig;
    private File npcsFile;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private FileConfiguration kitsConfig;
    private File kitsFile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadScoreboardConfig();
        loadTablistConfig();
        loadNPCsConfig();
        loadMessagesConfig();
        loadKitsConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        statsDAO = databaseManager.getStatsDAO();
        historyDAO = databaseManager.getHistoryDAO();
        statsManager = new StatsManager(statsDAO);

        messageManager = new MessageManager(messagesConfig);

        kitManager = new KitManager(kitsConfig, kitsFile);
        arenaManager = new ArenaManager(getConfig(), new File(getDataFolder(), "config.yml"));
        
        PlaceholderManager placeholderManager = new PlaceholderManager(null, statsManager);
        scoreboardPreferences = new PlayerScoreboardPreferences(getDataFolder());
        scoreboardManager = new ScoreboardManager(statsManager, scoreboardConfig, placeholderManager, scoreboardPreferences);
        rewardManager = new RewardManager(this, getConfig());
        betManager = new BetManager(this, getConfig());
        cooldownManager = new CooldownManager(getConfig());
        spectatorManager = new SpectatorManager(arenaManager);
        
        duelManager = new DuelManager(this, kitManager, arenaManager, statsManager,
                scoreboardManager, rewardManager, betManager, cooldownManager, spectatorManager, historyDAO);

        tablistManager = new TablistManager(tablistConfig, statsManager, duelManager);

        configGUI = new ConfigGUI(this, kitManager, arenaManager, messageManager);
        getServer().getPluginManager().registerEvents(configGUI, this);

        duelModeSelectionGUI = new DuelModeSelectionGUI(this, duelManager, kitManager, arenaManager, messageManager);
        getServer().getPluginManager().registerEvents(duelModeSelectionGUI, this);

        scoreboardModeSelectionGUI = new ScoreboardModeSelectionGUI(scoreboardPreferences, messageManager, scoreboardManager);
        getServer().getPluginManager().registerEvents(scoreboardModeSelectionGUI, this);

        duelNPC = new DuelNPC(this, duelManager, kitManager, arenaManager, statsManager, placeholderManager);
        duelNPC.loadNPCs(npcsConfig);

        startTablistUpdateTask();

        getCommand("duelo").setExecutor(new DuelCommand(this, duelManager, kitManager, arenaManager, duelModeSelectionGUI));
        getCommand("accept").setExecutor(new AcceptCommand(duelManager));
        getCommand("deny").setExecutor(new DenyCommand(duelManager));
        getCommand("stats").setExecutor(new StatsCommand(statsManager));
        getCommand("spectate").setExecutor(new SpectateCommand(duelManager, spectatorManager));
        getCommand("history").setExecutor(new HistoryCommand(historyDAO));
        getCommand("scoreboard").setExecutor(new ScoreboardCommand(scoreboardModeSelectionGUI));
        getCommand("dueladmin").setExecutor(new DuelAdminCommand(this, duelManager, kitManager, arenaManager, statsManager, messageManager, configGUI));

        getServer().getPluginManager().registerEvents(new DuelListener(duelManager), this);
        getServer().getPluginManager().registerEvents(new TablistListener(tablistManager), this);
        getServer().getPluginManager().registerEvents(new ProfileItemListener(this, scoreboardModeSelectionGUI), this);
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getServer().getPluginManager().registerEvents(new NPCListener(duelNPC), this);
        }

        getLogger().info("ArtixDuels habilitado com sucesso!");
    }

    private void startTablistUpdateTask() {
        if (tablistManager == null || !tablistManager.isEnabled()) return;

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            tablistManager.updateAllTablists();
        }, 0L, tablistManager.getUpdateInterval());
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) {
            scoreboardManager.clearAllScoreboards();
        }
        if (duelNPC != null) {
            duelNPC.removeAllNPCs();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("ArtixDuels desabilitado.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public DuelNPC getDuelNPC() {
        return duelNPC;
    }

    private void loadScoreboardConfig() {
        scoreboardFile = new File(getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }

    public void reloadScoreboardConfig() {
        if (scoreboardFile == null) {
            scoreboardFile = new File(getDataFolder(), "scoreboard.yml");
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        if (scoreboardManager != null && duelManager != null && scoreboardPreferences != null) {
            PlaceholderManager placeholderManager = new PlaceholderManager(duelManager, statsManager);
            scoreboardManager = new ScoreboardManager(statsManager, scoreboardConfig, placeholderManager, scoreboardPreferences);
        }
    }

    public FileConfiguration getScoreboardConfig() {
        if (scoreboardConfig == null) {
            loadScoreboardConfig();
        }
        return scoreboardConfig;
    }

    private void loadTablistConfig() {
        tablistFile = new File(getDataFolder(), "tablist.yml");
        if (!tablistFile.exists()) {
            saveResource("tablist.yml", false);
        }
        tablistConfig = YamlConfiguration.loadConfiguration(tablistFile);
    }

    public void reloadTablistConfig() {
        if (tablistFile == null) {
            tablistFile = new File(getDataFolder(), "tablist.yml");
        }
        tablistConfig = YamlConfiguration.loadConfiguration(tablistFile);
        if (tablistManager != null) {
            tablistManager = new TablistManager(tablistConfig, statsManager, duelManager);
            startTablistUpdateTask();
        }
    }

    public FileConfiguration getTablistConfig() {
        if (tablistConfig == null) {
            loadTablistConfig();
        }
        return tablistConfig;
    }

    public TablistManager getTablistManager() {
        return tablistManager;
    }

    private void loadNPCsConfig() {
        npcsFile = new File(getDataFolder(), "npcs.yml");
        if (!npcsFile.exists()) {
            saveResource("npcs.yml", false);
        }
        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
    }

    public void reloadNPCsConfig() {
        if (npcsFile == null) {
            npcsFile = new File(getDataFolder(), "npcs.yml");
        }
        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
        if (duelNPC != null) {
            duelNPC.removeAllNPCs();
            duelNPC.loadNPCs(npcsConfig);
        }
    }

    public FileConfiguration getNPCsConfig() {
        if (npcsConfig == null) {
            loadNPCsConfig();
        }
        return npcsConfig;
    }

    private void loadMessagesConfig() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessagesConfig() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        if (messageManager != null) {
            messageManager.reload();
        }
    }

    public FileConfiguration getMessagesConfig() {
        if (messagesConfig == null) {
            loadMessagesConfig();
        }
        return messagesConfig;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    private void loadKitsConfig() {
        kitsFile = new File(getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            saveResource("kits.yml", false);
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    public void reloadKitsConfig() {
        if (kitsFile == null) {
            kitsFile = new File(getDataFolder(), "kits.yml");
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        if (kitManager != null) {
            kitManager = new KitManager(kitsConfig, kitsFile);
        }
    }

    public FileConfiguration getKitsConfig() {
        if (kitsConfig == null) {
            loadKitsConfig();
        }
        return kitsConfig;
    }
}
