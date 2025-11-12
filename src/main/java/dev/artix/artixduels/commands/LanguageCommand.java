package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.LanguageSelectionGUI;
import dev.artix.artixduels.managers.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para gerenciar idiomas.
 */
public class LanguageCommand implements CommandExecutor {
    private final LanguageManager languageManager;
    private final LanguageSelectionGUI languageGUI;

    public LanguageCommand(LanguageManager languageManager, LanguageSelectionGUI languageGUI) {
        this.languageManager = languageManager;
        this.languageGUI = languageGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            languageGUI.openLanguageMenu(player);
            return true;
        }

        String langCode = args[0].toLowerCase();
        String[] supportedLanguages = languageManager.getSupportedLanguages();
        
        boolean isValid = false;
        for (String lang : supportedLanguages) {
            if (lang.equals(langCode)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            player.sendMessage("§cIdioma inválido! Idiomas disponíveis: pt, en, es, fr, de");
            return true;
        }

        languageManager.setPlayerLanguage(player.getUniqueId(), langCode);
        languageManager.savePlayerLanguages();
        
        java.util.HashMap<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("language", languageManager.getLanguageName(langCode));
        String message = languageManager.getMessage(player, "messages.language.changed", placeholders);
        if (message.isEmpty()) {
            message = "§aIdioma alterado para: §e" + languageManager.getLanguageName(langCode);
        }
        player.sendMessage(message);
        
        return true;
    }
}

