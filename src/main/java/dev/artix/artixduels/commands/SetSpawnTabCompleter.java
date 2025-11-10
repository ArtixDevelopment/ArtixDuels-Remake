package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.ArenaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetSpawnTabCompleter implements TabCompleter {
    private ArenaManager arenaManager;

    public SetSpawnTabCompleter(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("lobby", "arena"));
        } else if (args.length == 2) {
            String type = args[0].toLowerCase();
            if (type.equals("arena")) {
                completions.addAll(arenaManager.getAllArenaNames());
            }
        } else if (args.length == 3) {
            String type = args[0].toLowerCase();
            if (type.equals("arena")) {
                completions.addAll(Arrays.asList("pos1", "pos2"));
            }
        }

        String current = args[args.length - 1].toLowerCase();
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(current))
            .collect(Collectors.toList());
    }
}

