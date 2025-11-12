package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.ArenaEditor;
import dev.artix.artixduels.gui.ArenaEditorGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Comando para o editor de arenas.
 */
public class ArenaEditorCommand implements CommandExecutor {
    private final ArenaEditor arenaEditor;
    private final ArenaEditorGUI editorGUI;

    public ArenaEditorCommand(ArenaEditor arenaEditor, ArenaEditorGUI editorGUI) {
        this.arenaEditor = arenaEditor;
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
                    player.sendMessage("§cUso: /arenaeditor create <nome>");
                    return true;
                }
                arenaEditor.startEditSession(player, args[1]);
                break;

            case "edit":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /arenaeditor edit <nome>");
                    return true;
                }
                arenaEditor.startEditSession(player, args[1]);
                break;

            case "cancel":
                arenaEditor.cancelEditSession(player.getUniqueId());
                player.sendMessage("§cEdição cancelada!");
                break;

            case "save":
                if (arenaEditor.saveArena(player.getUniqueId())) {
                    player.sendMessage("§aArena salva com sucesso!");
                } else {
                    player.sendMessage("§cErro ao salvar arena!");
                }
                break;

            case "test":
                if (args.length > 1 && args[1].equalsIgnoreCase("stop")) {
                    arenaEditor.stopTestMode(player);
                } else {
                    arenaEditor.startTestMode(player);
                }
                break;

            case "template":
                if (args.length < 3) {
                    player.sendMessage("§cUso: /arenaeditor template <template> <nome>");
                    player.sendMessage("§7Templates disponíveis: small, medium, large");
                    return true;
                }
                arenaEditor.createFromTemplate(player, args[1], args[2]);
                break;

            case "export":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /arenaeditor export <arena>");
                    return true;
                }
                File exportDir = new File(arenaEditor.getPlugin().getDataFolder(), "exports");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                File exportFile = new File(exportDir, args[1] + ".yml");
                if (arenaEditor.exportArena(args[1], exportFile)) {
                    player.sendMessage("§aArena exportada para: §e" + exportFile.getName());
                } else {
                    player.sendMessage("§cErro ao exportar arena!");
                }
                break;

            case "import":
                if (args.length < 3) {
                    player.sendMessage("§cUso: /arenaeditor import <arquivo> <nome>");
                    return true;
                }
                File importDir = new File(arenaEditor.getPlugin().getDataFolder(), "exports");
                File importFile = new File(importDir, args[1]);
                if (!importFile.exists()) {
                    player.sendMessage("§cArquivo não encontrado!");
                    return true;
                }
                if (arenaEditor.importArena(importFile, args[2])) {
                    player.sendMessage("§aArena importada com sucesso!");
                } else {
                    player.sendMessage("§cErro ao importar arena!");
                }
                break;

            default:
                editorGUI.openMainMenu(player);
                break;
        }

        return true;
    }
}

