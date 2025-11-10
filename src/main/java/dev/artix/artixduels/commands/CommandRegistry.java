package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {
    private ArtixDuels plugin;
    private CommandMap commandMap;

    public CommandRegistry(ArtixDuels plugin) {
        this.plugin = plugin;
        this.commandMap = getCommandMap();
    }

    private CommandMap getCommandMap() {
        try {
            org.bukkit.plugin.SimplePluginManager pluginManager = (org.bukkit.plugin.SimplePluginManager) plugin.getServer().getPluginManager();
            Field commandMapField = pluginManager.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(pluginManager);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao obter CommandMap: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void registerCommand(String name, String description, List<String> aliases, String permission, CommandExecutor executor, TabCompleter tabCompleter) {
        if (commandMap == null) {
            plugin.getLogger().warning("CommandMap não disponível, não foi possível registrar o comando: " + name);
            return;
        }

        Command command = new Command(name, description, "/" + name, new ArrayList<>(aliases)) {
            @Override
            public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                    sender.sendMessage("§cVocê não tem permissão para usar este comando!");
                    return true;
                }
                return executor.onCommand(sender, this, commandLabel, args);
            }

            @Override
            public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                if (tabCompleter != null) {
                    return tabCompleter.onTabComplete(sender, this, alias, args);
                }
                return super.tabComplete(sender, alias, args);
            }
        };

        if (permission != null && !permission.isEmpty()) {
            command.setPermission(permission);
        }

        commandMap.register("artixduels", command);
    }

    public void registerAllCommands() {
        // Duelo
        registerCommand("duelo", "Comando principal de duelos",
            java.util.Arrays.asList("duel", "duelos"), null,
            new DuelCommand(plugin, plugin.getDuelManager(), plugin.getKitManager(), plugin.getArenaManager(), plugin.getDuelModeSelectionGUI()),
            new DuelTabCompleter());

        // Accept
        registerCommand("accept", "Aceitar um convite de duelo",
            java.util.Arrays.asList("aceitar"), null,
            new AcceptCommand(plugin.getDuelManager()), null);

        // Deny
        registerCommand("deny", "Recusar um convite de duelo",
            java.util.Arrays.asList("recusar"), null,
            new DenyCommand(plugin.getDuelManager()), null);

        // Stats
        registerCommand("stats", "Ver estatísticas de duelos",
            java.util.Arrays.asList("estatisticas", "estatisticas"), null,
            new StatsCommand(plugin.getStatsManager()), null);

        // Spectate
        registerCommand("spectate", "Espectar um duelo",
            java.util.Arrays.asList("espectar", "spec"), null,
            new SpectateCommand(plugin.getDuelManager(), plugin.getSpectatorManager()), null);

        // History
        registerCommand("history", "Ver histórico de duelos",
            java.util.Arrays.asList("historico"), null,
            new HistoryCommand(plugin.getHistoryDAO()), null);

        // Scoreboard
        registerCommand("scoreboard", "Configurar modos do scoreboard",
            java.util.Arrays.asList("sb", "score"), null,
            new ScoreboardCommand(plugin.getScoreboardModeSelectionGUI()), null);

        // DuelAdmin
        registerCommand("dueladmin", "Comandos administrativos de duelos",
            java.util.Arrays.asList("dueladm", "dadm"), "artixduels.admin",
            new DuelAdminCommand(plugin, plugin.getDuelManager(), plugin.getKitManager(), plugin.getArenaManager(), plugin.getStatsManager(), plugin.getMessageManager(), plugin.getConfigGUI()),
            null);

        // SetSpawn
        registerCommand("setspawn", "Definir spawns do lobby e arenas",
            java.util.Arrays.asList("spawn"), "artixduels.admin",
            new SetSpawnCommand(plugin, plugin.getArenaManager()),
            new SetSpawnTabCompleter(plugin.getArenaManager()));

        // Arena
        registerCommand("arena", "Gerenciar arenas",
            java.util.Arrays.asList("arenas"), "artixduels.admin",
            new ArenaCommand(plugin.getArenaManager(), plugin.getKitManager()),
            new ArenaTabCompleter(plugin.getArenaManager(), plugin.getKitManager()));

        // Kit
        registerCommand("kit", "Gerenciar kits",
            java.util.Arrays.asList("kits"), "artixduels.admin",
            new KitCommand(plugin.getKitManager(), plugin.getConfigGUI()),
            new KitTabCompleter(plugin.getKitManager()));

        // NPC
        registerCommand("npc", "Gerenciar NPCs",
            java.util.Arrays.asList("npcs"), "artixduels.admin",
            new NPCCommand(plugin, plugin.getDuelNPC(), plugin.getMessageManager()),
            new NPCTabCompleter());

        // Spawn
        registerCommand("spawn", "Teleportar para o lobby",
            java.util.Arrays.asList("lobby"), null,
            new SpawnCommand(plugin), null);

        // Queue
        registerCommand("queue", "Entrar na fila de matchmaking",
            java.util.Arrays.asList("fila", "matchmaking"), null,
            new QueueCommand(plugin, plugin.getDuelManager(), plugin.getDuelModeSelectionGUI()),
            new QueueTabCompleter());

        // Artix-Holo
        registerCommand("artix-holo", "Gerenciar hologramas",
            java.util.Arrays.asList("hologram", "holograms", "holo"), "artixduels.admin",
            new HologramCommand(plugin),
            new HologramTabCompleter(plugin));
    }
}

