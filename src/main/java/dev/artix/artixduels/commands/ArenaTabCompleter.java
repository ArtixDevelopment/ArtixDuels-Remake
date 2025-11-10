package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaTabCompleter implements TabCompleter {
    private ArenaManager arenaManager;
    private KitManager kitManager;

    public ArenaTabCompleter(ArenaManager arenaManager, KitManager kitManager) {
        this.arenaManager = arenaManager;
        this.kitManager = kitManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("toggle", "setkit"));
        } else if (args.length == 2) {
            completions.addAll(arenaManager.getAllArenaNames());
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("toggle")) {
                completions.addAll(Arrays.asList("enabled", "kits", "rules"));
            } else if (subCommand.equals("setkit")) {
                completions.addAll(kitManager.getAllKitNames());
            }
        }

        String current = args[args.length - 1].toLowerCase();
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(current))
            .collect(Collectors.toList());
    }
}

