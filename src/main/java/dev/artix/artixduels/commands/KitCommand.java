package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.ConfigGUI;
import dev.artix.artixduels.managers.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private KitManager kitManager;
    private ConfigGUI configGUI;

    public KitCommand(KitManager kitManager, ConfigGUI configGUI) {
        this.kitManager = kitManager;
        this.configGUI = configGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cUso: /kit <manage|create|delete> [nome]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("manage")) {
            configGUI.openKitsMenu(player);
            return true;
        }

        if (subCommand.equals("create")) {
            if (args.length < 2) {
                player.sendMessage("§cUso: /kit create <nome>");
                return true;
            }

            String kitName = args[1];
            if (kitManager.kitExists(kitName)) {
                player.sendMessage("§cKit já existe! Use /kit manage para editá-lo.");
                return true;
            }

            dev.artix.artixduels.models.Kit kit = new dev.artix.artixduels.models.Kit(kitName, kitName, dev.artix.artixduels.models.DuelMode.BEDFIGHT);
            kit.setContents(player.getInventory().getContents().clone());
            kit.setArmor(player.getInventory().getArmorContents().clone());

            kitManager.addKit(kitName, kit);
            kitManager.saveKit(kitName, kit);
            player.sendMessage("§aKit §e" + kitName + " §acriado com sucesso!");
            return true;
        }

        if (subCommand.equals("delete")) {
            if (args.length < 2) {
                player.sendMessage("§cUso: /kit delete <nome>");
                return true;
            }

            String kitName = args[1];
            if (!kitManager.kitExists(kitName)) {
                player.sendMessage("§cKit não encontrado!");
                return true;
            }

            kitManager.removeKit(kitName);
            player.sendMessage("§aKit §e" + kitName + " §aremovido com sucesso!");
            return true;
        }

        player.sendMessage("§cSubcomando inválido! Use: manage, create ou delete");
        return true;
    }
}

