package dev.artix.artixduels.commands;

import dev.artix.artixduels.managers.AntiCheatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para gerenciar anti-cheat.
 */
public class AntiCheatCommand implements CommandExecutor {
    private final AntiCheatManager antiCheatManager;

    public AntiCheatCommand(AntiCheatManager antiCheatManager) {
        this.antiCheatManager = antiCheatManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6=== Anti-Cheat ===");
            sender.sendMessage("§7Status: " + (antiCheatManager.isEnabled() ? "§aAtivado" : "§cDesativado"));
            sender.sendMessage("§7Auto-Click: " + (antiCheatManager.isAutoClickDetectionEnabled() ? "§aAtivado" : "§cDesativado"));
            sender.sendMessage("§7Reach: " + (antiCheatManager.isReachDetectionEnabled() ? "§aAtivado" : "§cDesativado"));
            sender.sendMessage("§7Movimento: " + (antiCheatManager.isMovementDetectionEnabled() ? "§aAtivado" : "§cDesativado"));
            sender.sendMessage("§7");
            sender.sendMessage("§eUso: /anticheat <toggle|autoclick|reach|movement>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle":
                boolean newState = !antiCheatManager.isEnabled();
                antiCheatManager.setEnabled(newState);
                sender.sendMessage("§aAnti-Cheat " + (newState ? "ativado" : "desativado") + "!");
                break;

            case "autoclick":
                boolean autoClickState = !antiCheatManager.isAutoClickDetectionEnabled();
                antiCheatManager.setAutoClickDetection(autoClickState);
                sender.sendMessage("§aDetecção de Auto-Click " + (autoClickState ? "ativada" : "desativada") + "!");
                break;

            case "reach":
                boolean reachState = !antiCheatManager.isReachDetectionEnabled();
                antiCheatManager.setReachDetection(reachState);
                sender.sendMessage("§aDetecção de Reach " + (reachState ? "ativada" : "desativada") + "!");
                break;

            case "movement":
                boolean movementState = !antiCheatManager.isMovementDetectionEnabled();
                antiCheatManager.setMovementDetection(movementState);
                sender.sendMessage("§aDetecção de Movimento " + (movementState ? "ativada" : "desativada") + "!");
                break;

            default:
                sender.sendMessage("§cUso: /anticheat <toggle|autoclick|reach|movement>");
                break;
        }

        return true;
    }
}

