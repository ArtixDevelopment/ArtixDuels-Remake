package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.MessageManager;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.npcs.DuelNPC;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class NPCCommand implements CommandExecutor {
    private ArtixDuels plugin;

    public NPCCommand(ArtixDuels plugin, DuelNPC duelNPC, MessageManager messageManager) {
        this.plugin = plugin;
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

        if (args.length < 2) {
            player.sendMessage("§cUso: /npc set <nome> <modo>");
            return true;
        }

        if (!args[0].equalsIgnoreCase("set")) {
            player.sendMessage("§cUso: /npc set <nome> <modo>");
            return true;
        }

        String npcName = args[1];
        if (args.length < 3) {
            player.sendMessage("§cUso: /npc set <nome> <modo>");
            return true;
        }

        String modeString = args[2].toUpperCase();
        DuelMode mode = DuelMode.fromString(modeString);
        if (mode == null) {
            player.sendMessage("§cModo inválido! Modos disponíveis: BEDFIGHT, STICKFIGHT, SOUP, etc.");
            return true;
        }

        Location loc = player.getLocation();
        FileConfiguration npcsConfig = plugin.getNPCsConfig();
        File npcsFile = new File(plugin.getDataFolder(), "npcs.yml");

        String path = "npcs." + npcName;
        npcsConfig.set(path + ".mode", modeString);
        npcsConfig.set(path + ".display-name", "&6&l" + mode.getDisplayName());
        npcsConfig.set(path + ".skin", "");
        npcsConfig.set(path + ".location", locationToString(loc));
        npcsConfig.set(path + ".look-close.enabled", true);
        npcsConfig.set(path + ".look-close.range", 5);
        npcsConfig.set(path + ".equipment.hand", null);
        npcsConfig.set(path + ".equipment.off-hand", null);
        npcsConfig.set(path + ".equipment.helmet", null);
        npcsConfig.set(path + ".equipment.chestplate", null);
        npcsConfig.set(path + ".equipment.leggings", null);
        npcsConfig.set(path + ".equipment.boots", null);
        npcsConfig.set(path + ".hologram.enabled", true);
        npcsConfig.set(path + ".hologram.height", 2.5);
        npcsConfig.set(path + ".hologram.offset-x", 0.0);
        npcsConfig.set(path + ".hologram.offset-z", 0.0);
        npcsConfig.set(path + ".hologram.lines", java.util.Arrays.asList(
            "&6&l" + mode.getDisplayName().toUpperCase(),
            " ",
            "&eClique aqui!"
        ));

        try {
            npcsConfig.save(npcsFile);
            plugin.reloadNPCsConfig();
            player.sendMessage("§aNPC §e" + npcName + " §adefinido com sucesso no modo §e" + mode.getDisplayName() + "§a!");
        } catch (IOException e) {
            player.sendMessage("§cErro ao salvar NPC!");
            plugin.getLogger().severe("Erro ao salvar NPC: " + e.getMessage());
        }

        return true;
    }

    private String locationToString(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }
}

