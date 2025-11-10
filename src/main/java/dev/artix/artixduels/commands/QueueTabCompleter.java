package dev.artix.artixduels.commands;

import dev.artix.artixduels.models.DuelMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QueueTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.stream(DuelMode.values())
                .map(DuelMode::getName)
                .collect(Collectors.toList()));
        }

        String current = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(current))
            .collect(Collectors.toList());
    }
}

