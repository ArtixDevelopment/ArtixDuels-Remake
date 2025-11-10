package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * TabCompleter para o comando /artix-npc
 */
public class NPCTabCompleter implements TabCompleter {
    private final ArtixDuels plugin;

    public NPCTabCompleter(ArtixDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcomandos
            List<String> subCommands = Arrays.asList("set", "edit", "delete", "list", "reload");
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("set") || subCommand.equals("create") || subCommand.equals("criar")) {
                // Sugerir nomes de NPCs ou deixar o usuário digitar
                completions.add("<nome>");
            } else if (subCommand.equals("edit") || subCommand.equals("editar") || 
                       subCommand.equals("delete") || subCommand.equals("deletar") ||
                       subCommand.equals("remove") || subCommand.equals("remover")) {
                // Sugerir NPCs existentes
                completions.addAll(getNPCNames());
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("set") || subCommand.equals("create") || subCommand.equals("criar")) {
                // Sugerir modos de duelo
                for (DuelMode mode : DuelMode.values()) {
                    if (mode.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(mode.getName());
                    }
                }
            }
        }

        return completions.isEmpty() ? null : completions;
    }

    private List<String> getNPCNames() {
        List<String> names = new ArrayList<>();
        try {
            org.bukkit.configuration.file.FileConfiguration npcsConfig = plugin.getNPCsConfig();
            ConfigurationSection npcsSection = npcsConfig.getConfigurationSection("npcs");
            
            if (npcsSection != null) {
                Set<String> npcNames = npcsSection.getKeys(false);
                for (String npcName : npcNames) {
                    if (!npcName.equals("enabled")) {
                        names.add(npcName);
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar erros
        }
        return names;
    }
}
