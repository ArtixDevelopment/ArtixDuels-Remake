package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.KitEditorGUI;
import dev.artix.artixduels.managers.KitEditor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Comando para o editor de kits.
 */
public class KitEditorCommand implements CommandExecutor {
    private final KitEditor kitEditor;
    private final KitEditorGUI editorGUI;

    public KitEditorCommand(KitEditor kitEditor, KitEditorGUI editorGUI) {
        this.kitEditor = kitEditor;
        this.editorGUI = editorGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("artixduels.admin")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (args.length == 0) {
            editorGUI.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /kiteditor create <nome>");
                    return true;
                }
                String kitName = args[1];
                dev.artix.artixduels.models.Kit kit = new dev.artix.artixduels.models.Kit(
                    kitName, kitName, dev.artix.artixduels.models.DuelMode.BEDFIGHT);
                kit.setContents(player.getInventory().getContents().clone());
                kit.setArmor(player.getInventory().getArmorContents().clone());
                kitEditor.getKitManager().addKit(kitName, kit);
                kitEditor.getKitManager().saveKit(kitName, kit);
                player.sendMessage("§aKit criado com sucesso!");
                break;

            case "edit":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /kiteditor edit <nome>");
                    return true;
                }
                kitEditor.startEditSession(player, args[1]);
                break;

            case "save":
                kitEditor.saveKit(player);
                break;

            case "cancel":
                kitEditor.cancelEditSession(player.getUniqueId());
                break;

            case "preview":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /kiteditor preview <kit>");
                    return true;
                }
                if (args[1].equalsIgnoreCase("stop")) {
                    kitEditor.stopPreview(player);
                } else {
                    kitEditor.startPreview(player, args[1]);
                }
                break;

            case "template":
                if (args.length < 3) {
                    player.sendMessage("§cUso: /kiteditor template <template> <nome>");
                    player.sendMessage("§7Templates disponíveis: pvp_basic, soup, nodebuff");
                    return true;
                }
                kitEditor.createFromTemplate(player, args[1], args[2]);
                break;

            case "favorite":
                if (args.length < 3) {
                    player.sendMessage("§cUso: /kiteditor favorite <add|remove> <kit>");
                    return true;
                }
                if (args[1].equalsIgnoreCase("add")) {
                    kitEditor.addFavorite(player.getUniqueId(), args[2]);
                    player.sendMessage("§aKit adicionado aos favoritos!");
                } else if (args[1].equalsIgnoreCase("remove")) {
                    kitEditor.removeFavorite(player.getUniqueId(), args[2]);
                    player.sendMessage("§cKit removido dos favoritos!");
                }
                break;

            case "export":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /kiteditor export <kit>");
                    return true;
                }
                File exportDir = new File(kitEditor.getPlugin().getDataFolder(), "exports");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                File exportFile = new File(exportDir, args[1] + ".yml");
                if (kitEditor.exportKit(args[1], exportFile)) {
                    player.sendMessage("§aKit exportado para: §e" + exportFile.getName());
                } else {
                    player.sendMessage("§cErro ao exportar kit!");
                }
                break;

            case "import":
                if (args.length < 3) {
                    player.sendMessage("§cUso: /kiteditor import <arquivo> <nome>");
                    return true;
                }
                File importDir = new File(kitEditor.getPlugin().getDataFolder(), "exports");
                File importFile = new File(importDir, args[1]);
                if (!importFile.exists()) {
                    player.sendMessage("§cArquivo não encontrado!");
                    return true;
                }
                if (kitEditor.importKit(importFile, args[2])) {
                    player.sendMessage("§aKit importado com sucesso!");
                } else {
                    player.sendMessage("§cErro ao importar kit!");
                }
                break;

            default:
                editorGUI.openMainMenu(player);
                break;
        }

        return true;
    }
}

