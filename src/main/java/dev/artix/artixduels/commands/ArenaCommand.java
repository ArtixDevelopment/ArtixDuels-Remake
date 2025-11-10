package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.models.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ArenaCommand implements CommandExecutor {
    private ArenaManager arenaManager;
    private KitManager kitManager;

    public ArenaCommand(ArenaManager arenaManager, KitManager kitManager) {
        this.arenaManager = arenaManager;
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUso: /arena <toggle|setkit> <arena> [função|kit]");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String arenaName = args[1];

        if (!arenaManager.arenaExists(arenaName)) {
            sender.sendMessage("§cArena não encontrada!");
            return true;
        }

        Arena arena = arenaManager.getArena(arenaName);

        if (subCommand.equals("toggle")) {
            if (args.length < 3) {
                sender.sendMessage("§cUso: /arena toggle <arena> <enabled|kits|rules>");
                return true;
            }

            String function = args[2].toLowerCase();

            switch (function) {
                case "enabled":
                    boolean currentEnabled = arena.isEnabled();
                    arena.setEnabled(!currentEnabled);
                    arenaManager.saveArena(arenaName, arena);
                    sender.sendMessage("§aArena §e" + arenaName + " §a" + (!currentEnabled ? "ativada" : "desativada") + " com sucesso!");
                    break;
                case "kits":
                    boolean currentKits = arena.isKitsEnabled();
                    arena.setKitsEnabled(!currentKits);
                    arenaManager.saveArena(arenaName, arena);
                    sender.sendMessage("§aKits da arena §e" + arenaName + " §a" + (!currentKits ? "ativados" : "desativados") + " com sucesso!");
                    break;
                case "rules":
                    boolean currentRules = arena.isRulesEnabled();
                    arena.setRulesEnabled(!currentRules);
                    arenaManager.saveArena(arenaName, arena);
                    sender.sendMessage("§aRegras da arena §e" + arenaName + " §a" + (!currentRules ? "ativadas" : "desativadas") + " com sucesso!");
                    break;
                default:
                    sender.sendMessage("§cFunção inválida! Use: enabled, kits ou rules");
                    break;
            }
            return true;
        }

        if (subCommand.equals("setkit")) {
            if (args.length < 3) {
                sender.sendMessage("§cUso: /arena setkit <arena> <kit>");
                return true;
            }

            String kitName = args[2];

            if (!kitManager.kitExists(kitName)) {
                sender.sendMessage("§cKit não encontrado!");
                return true;
            }

            arena.setDefaultKit(kitName);
            arenaManager.saveArena(arenaName, arena);
            sender.sendMessage("§aKit padrão da arena §e" + arenaName + " §adefinido para §e" + kitName + "§a!");
            return true;
        }

        sender.sendMessage("§cSubcomando inválido! Use: toggle ou setkit");
        return true;
    }
}

