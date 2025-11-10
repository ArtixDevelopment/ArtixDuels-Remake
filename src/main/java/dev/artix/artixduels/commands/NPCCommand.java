package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.MessageManager;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.npcs.DuelNPC;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Comando para gerenciar NPCs de duelos.
 * Subcomandos: set, edit, delete, list, reload
 */
public class NPCCommand implements CommandExecutor {
    private final ArtixDuels plugin;
    private final DuelNPC duelNPC;
    private final MessageManager messageManager;

    public NPCCommand(ArtixDuels plugin, DuelNPC duelNPC, MessageManager messageManager) {
        this.plugin = plugin;
        this.duelNPC = duelNPC;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
            case "criar":
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
                    return true;
                }
                handleSet((Player) sender, args);
                break;

            case "edit":
            case "editar":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
                    return true;
                }
                handleEdit((Player) sender, args);
                break;

            case "delete":
            case "deletar":
            case "remove":
            case "remover":
                handleDelete(sender, args);
                break;

            case "list":
            case "lista":
            case "listar":
                handleList(sender);
                break;

            case "reload":
            case "recarregar":
                handleReload(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== Comandos NPC ===");
        sender.sendMessage("§e/artix-npc set <nome> <modo> §7- Criar/setar um NPC");
        sender.sendMessage("§e/artix-npc edit <nome> §7- Editar um NPC existente");
        sender.sendMessage("§e/artix-npc delete <nome> §7- Deletar um NPC");
        sender.sendMessage("§e/artix-npc list §7- Listar todos os NPCs");
        sender.sendMessage("§e/artix-npc reload §7- Recarregar NPCs");
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUso: /artix-npc set <nome> <modo>");
            player.sendMessage("§7Modos disponíveis: BEDFIGHT, STICKFIGHT, SOUP, etc.");
            return;
        }

        String npcName = args[1];
        String modeString = args[2].toUpperCase();
        DuelMode mode = DuelMode.fromString(modeString);
        
        if (mode == null) {
            player.sendMessage("§cModo inválido! Modos disponíveis:");
            StringBuilder modes = new StringBuilder();
            for (DuelMode m : DuelMode.values()) {
                if (modes.length() > 0) modes.append(", ");
                modes.append(m.getName());
            }
            player.sendMessage("§7" + modes.toString());
            return;
        }

        Location loc = player.getLocation();
        FileConfiguration npcsConfig = plugin.getNPCsConfig();
        File npcsFile = new File(plugin.getDataFolder(), "npcs.yml");

        String path = "npcs." + npcName;
        
        // Verificar se já existe
        boolean exists = npcsConfig.contains(path);
        
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
            
            if (exists) {
                player.sendMessage("§aNPC §e" + npcName + " §aeditado com sucesso!");
            } else {
                player.sendMessage("§aNPC §e" + npcName + " §acriado com sucesso no modo §e" + mode.getDisplayName() + "§a!");
            }
        } catch (IOException e) {
            player.sendMessage("§cErro ao salvar NPC!");
            plugin.getLogger().severe("Erro ao salvar NPC: " + e.getMessage());
        }
    }

    private void handleEdit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUso: /artix-npc edit <nome>");
            player.sendMessage("§7Edite o NPC diretamente no arquivo npcs.yml");
            return;
        }

        String npcName = args[1];
        FileConfiguration npcsConfig = plugin.getNPCsConfig();
        String path = "npcs." + npcName;

        if (!npcsConfig.contains(path)) {
            player.sendMessage("§cNPC §e" + npcName + " §cnão encontrado!");
            return;
        }

        ConfigurationSection npcSection = npcsConfig.getConfigurationSection(path);
        if (npcSection == null) {
            player.sendMessage("§cErro ao carregar dados do NPC!");
            return;
        }

        // Mostrar informações do NPC
        player.sendMessage("§6§l=== Informações do NPC: " + npcName + " ===");
        player.sendMessage("§7Modo: §e" + npcSection.getString("mode", "N/A"));
        player.sendMessage("§7Display Name: §e" + npcSection.getString("display-name", "N/A"));
        player.sendMessage("§7Skin: §e" + npcSection.getString("skin", "N/A"));
        player.sendMessage("§7Localização: §e" + npcSection.getString("location", "N/A"));
        player.sendMessage("§7");
        player.sendMessage("§7Para editar, modifique o arquivo §enpcs.yml");
        player.sendMessage("§7Depois use §e/artix-npc reload §7para aplicar as mudanças");
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /artix-npc delete <nome>");
            return;
        }

        String npcName = args[1];
        FileConfiguration npcsConfig = plugin.getNPCsConfig();
        File npcsFile = new File(plugin.getDataFolder(), "npcs.yml");
        String path = "npcs." + npcName;

        if (!npcsConfig.contains(path)) {
            sender.sendMessage("§cNPC §e" + npcName + " §cnão encontrado!");
            return;
        }

        npcsConfig.set(path, null);

        try {
            npcsConfig.save(npcsFile);
            plugin.reloadNPCsConfig();
            sender.sendMessage("§aNPC §e" + npcName + " §adeletado com sucesso!");
        } catch (IOException e) {
            sender.sendMessage("§cErro ao deletar NPC!");
            plugin.getLogger().severe("Erro ao deletar NPC: " + e.getMessage());
        }
    }

    private void handleList(CommandSender sender) {
        FileConfiguration npcsConfig = plugin.getNPCsConfig();
        ConfigurationSection npcsSection = npcsConfig.getConfigurationSection("npcs");

        if (npcsSection == null || npcsSection.getKeys(false).isEmpty()) {
            sender.sendMessage("§cNenhum NPC encontrado!");
            return;
        }

        sender.sendMessage("§6§l=== NPCs Cadastrados ===");
        Set<String> npcNames = npcsSection.getKeys(false);
        for (String npcName : npcNames) {
            if (npcName.equals("enabled")) continue;
            
            ConfigurationSection npcSection = npcsSection.getConfigurationSection(npcName);
            if (npcSection == null) continue;

            String mode = npcSection.getString("mode", "N/A");
            String displayName = npcSection.getString("display-name", npcName);
            sender.sendMessage("§7- §e" + npcName + " §7(Modo: §b" + mode + "§7, Display: §f" + displayName + "§7)");
        }
        sender.sendMessage("§7Total: §e" + npcNames.size() + " NPC(s)");
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadNPCsConfig();
        sender.sendMessage("§aNPCs recarregados com sucesso!");
    }

    private String locationToString(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }
}
