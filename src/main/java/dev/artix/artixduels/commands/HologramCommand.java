package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.HologramSystemManager;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class HologramCommand implements CommandExecutor {
    private ArtixDuels plugin;

    public HologramCommand(ArtixDuels plugin) {
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
            player.sendMessage("§cUso: /hologram <create|remove|list> <nome> [tipo] [modo]");
            player.sendMessage("§7Tipos: mode-selection, top-wins, top-streak");
            return true;
        }

        HologramSystemManager hologramManager = plugin.getHologramSystemManager();
        if (hologramManager == null) {
            player.sendMessage("§cSistema de hologramas não está disponível!");
            return true;
        }

        String action = args[0].toLowerCase();
        String name = args[1];

        if (action.equals("create")) {
            if (args.length < 3) {
                player.sendMessage("§cUso: /hologram create <nome> <tipo> [modo]");
                player.sendMessage("§7Tipos: mode-selection, top-wins, top-streak");
                return true;
            }

            String typeString = args[2].toUpperCase().replace("-", "_");
            HologramSystemManager.HologramType type;

            try {
                type = HologramSystemManager.HologramType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cTipo inválido! Use: mode-selection, top-wins ou top-streak");
                return true;
            }

            DuelMode mode = DuelMode.BEDFIGHT;
            if (args.length >= 4) {
                mode = DuelMode.fromString(args[3]);
                if (mode == null) {
                    player.sendMessage("§cModo inválido!");
                    return true;
                }
            }

            hologramManager.createHologram(name, player.getLocation(), type, mode);
            player.sendMessage("§aHolograma '" + name + "' criado com sucesso!");
            
        } else if (action.equals("remove")) {
            hologramManager.removeHologram(name);
            player.sendMessage("§aHolograma '" + name + "' removido com sucesso!");
            
        } else if (action.equals("list")) {
            Set<String> hologramNames = hologramManager.getHologramNames();
            if (hologramNames.isEmpty()) {
                player.sendMessage("§7Nenhum holograma criado.");
            } else {
                player.sendMessage("§6Hologramas criados:");
                for (String holoName : hologramNames) {
                    String typeName = hologramManager.getHologramType(holoName);
                    String modeName = hologramManager.getHologramMode(holoName);
                    if (typeName != null && modeName != null) {
                        player.sendMessage("§7- §e" + holoName + " §7(Tipo: §b" + typeName + "§7, Modo: §b" + modeName + "§7)");
                    } else {
                        player.sendMessage("§7- §e" + holoName);
                    }
                }
                player.sendMessage("§7Use /hologram remove <nome> para remover");
            }
            
        } else {
            player.sendMessage("§cAção inválida! Use: create, remove ou list");
        }

        return true;
    }
}

