package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.TitleGUI;
import dev.artix.artixduels.managers.TitleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para gerenciar títulos.
 */
public class TitleCommand implements CommandExecutor {
    private final TitleManager titleManager;
    private final TitleGUI titleGUI;

    public TitleCommand(TitleManager titleManager, TitleGUI titleGUI) {
        this.titleManager = titleManager;
        this.titleGUI = titleGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            titleGUI.openTitleMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /title set <título>");
                    return true;
                }
                titleManager.setActiveTitle(player.getUniqueId(), args[1]);
                break;

            case "remove":
                titleManager.removeActiveTitle(player.getUniqueId());
                player.sendMessage("§cTítulo removido!");
                break;

            case "list":
                player.sendMessage("§6=== Títulos Desbloqueados ===");
                for (String titleId : titleManager.getUnlockedTitles(player.getUniqueId())) {
                    dev.artix.artixduels.models.Title title = titleManager.getTitles().get(titleId);
                    if (title != null) {
                        player.sendMessage("§e" + titleId + " §7- " + title.getDisplayName());
                    }
                }
                break;

            default:
                titleGUI.openTitleMenu(player);
                break;
        }

        return true;
    }
}

