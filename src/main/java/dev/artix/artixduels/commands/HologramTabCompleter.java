package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.HologramSystemManager;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HologramTabCompleter implements TabCompleter {
    private ArtixDuels plugin;

    public HologramTabCompleter(ArtixDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "remove", "list"));
        } else if (args.length == 2) {
            String action = args[0].toLowerCase();
            if (action.equals("remove")) {
                HologramSystemManager hologramManager = plugin.getHologramSystemManager();
                if (hologramManager != null) {
                    completions.addAll(hologramManager.getHologramNames());
                }
            } else if (action.equals("create")) {
                // Nome do holograma - sem sugestões
            } else if (action.equals("list")) {
                // Não precisa de argumentos
            }
        } else if (args.length == 3) {
            String action = args[0].toLowerCase();
            if (action.equals("create")) {
                completions.addAll(Arrays.asList("mode-selection", "top-wins", "top-streak"));
            }
        } else if (args.length == 4) {
            String action = args[0].toLowerCase();
            if (action.equals("create")) {
                completions.addAll(Arrays.stream(DuelMode.values())
                    .map(DuelMode::getName)
                    .collect(Collectors.toList()));
            }
        }

        String current = args[args.length - 1].toLowerCase();
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(current))
            .collect(Collectors.toList());
    }
}

