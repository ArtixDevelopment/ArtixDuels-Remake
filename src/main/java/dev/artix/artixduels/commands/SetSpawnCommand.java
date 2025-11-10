package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.ArenaManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class SetSpawnCommand implements CommandExecutor {
    private ArtixDuels plugin;
    private ArenaManager arenaManager;

    public SetSpawnCommand(ArtixDuels plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cUso: /setspawn <lobby|arena> [arena] [pos1|pos2]");
            return true;
        }

        String type = args[0].toLowerCase();

        if (type.equals("lobby")) {
            Location loc = player.getLocation();
            FileConfiguration config = plugin.getConfig();
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            
            config.set("lobby-spawn", locationToString(loc));
            try {
                config.save(configFile);
                player.sendMessage("§aSpawn do lobby definido com sucesso!");
            } catch (IOException e) {
                player.sendMessage("§cErro ao salvar spawn do lobby!");
                plugin.getLogger().severe("Erro ao salvar spawn do lobby: " + e.getMessage());
            }
            return true;
        }

        if (type.equals("arena")) {
            if (args.length < 3) {
                player.sendMessage("§cUso: /setspawn arena <arena> <pos1|pos2>");
                return true;
            }

            String arenaName = args[1];
            String posType = args[2].toLowerCase();

            if (!arenaManager.arenaExists(arenaName)) {
                player.sendMessage("§cArena não encontrada!");
                return true;
            }

            Location loc = player.getLocation();
            dev.artix.artixduels.models.Arena arena = arenaManager.getArena(arenaName);

            if (posType.equals("pos1")) {
                arena.setPlayer1Spawn(loc);
                player.sendMessage("§aPosição 1 da arena §e" + arenaName + " §adefinida com sucesso!");
            } else if (posType.equals("pos2")) {
                arena.setPlayer2Spawn(loc);
                player.sendMessage("§aPosição 2 da arena §e" + arenaName + " §adefinida com sucesso!");
            } else {
                player.sendMessage("§cTipo de posição inválido! Use pos1 ou pos2.");
                return true;
            }

            arenaManager.saveArena(arenaName, arena);
            return true;
        }

        player.sendMessage("§cTipo inválido! Use lobby ou arena.");
        return true;
    }

    private String locationToString(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }
}

