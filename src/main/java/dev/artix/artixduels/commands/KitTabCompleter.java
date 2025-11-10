package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KitTabCompleter implements TabCompleter {
    private KitManager kitManager;

    public KitTabCompleter(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("manage", "create", "delete"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("delete")) {
                completions.addAll(kitManager.getAllKitNames());
            } else if (subCommand.equals("create")) {
                // Nome do kit - sem sugestões
            }
        }

        String current = args[args.length - 1].toLowerCase();
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(current))
            .collect(Collectors.toList());
    }
}

