package dev.artix.artixduels.utils;

import dev.artix.artixduels.ArtixDuels;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Gerenciador de integrações com plugins externos.
 * Verifica e gerencia integrações com PlaceholderAPI e Vault.
 */
public class IntegrationManager {
    private final ArtixDuels plugin;
    private boolean placeholderAPIEnabled;
    private boolean vaultEnabled;
    private Object vaultEconomy;

    public IntegrationManager(ArtixDuels plugin) {
        this.plugin = plugin;
        this.placeholderAPIEnabled = false;
        this.vaultEnabled = false;
        checkIntegrations();
    }

    /**
     * Verifica todas as integrações disponíveis.
     */
    public void checkIntegrations() {
        checkPlaceholderAPI();
        checkVault();
        logIntegrationStatus();
    }

    /**
     * Verifica se PlaceholderAPI está disponível.
     */
    private void checkPlaceholderAPI() {
        try {
            Plugin placeholderPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
            if (placeholderPlugin != null && placeholderPlugin.isEnabled()) {
                placeholderAPIEnabled = true;
                plugin.getLogger().info("§a[Integração] PlaceholderAPI encontrado e habilitado!");
            } else {
                placeholderAPIEnabled = false;
                plugin.getLogger().info("§e[Integração] PlaceholderAPI não encontrado. Placeholders externos não estarão disponíveis.");
            }
        } catch (Exception e) {
            placeholderAPIEnabled = false;
            plugin.getLogger().warning("§c[Integração] Erro ao verificar PlaceholderAPI: " + e.getMessage());
        }
    }

    /**
     * Verifica se Vault está disponível.
     */
    private void checkVault() {
        try {
            Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
            if (vaultPlugin != null && vaultPlugin.isEnabled()) {
                // Verificar se há um provider de economia registrado
                try {
                    Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
                    org.bukkit.plugin.ServicesManager servicesManager = Bukkit.getServicesManager();
                    org.bukkit.plugin.RegisteredServiceProvider<?> provider = 
                        (org.bukkit.plugin.RegisteredServiceProvider<?>) servicesManager.getRegistration(economyClass);
                    
                    if (provider != null) {
                        vaultEnabled = true;
                        vaultEconomy = provider.getProvider();
                        plugin.getLogger().info("§a[Integração] Vault encontrado com provider de economia!");
                    } else {
                        vaultEnabled = false;
                        plugin.getLogger().warning("§e[Integração] Vault encontrado, mas nenhum provider de economia está registrado.");
                    }
                } catch (ClassNotFoundException e) {
                    vaultEnabled = false;
                    plugin.getLogger().warning("§e[Integração] Vault encontrado, mas classes de economia não estão disponíveis.");
                }
            } else {
                vaultEnabled = false;
                plugin.getLogger().info("§e[Integração] Vault não encontrado. Sistema de economia não estará disponível.");
            }
        } catch (Exception e) {
            vaultEnabled = false;
            plugin.getLogger().warning("§c[Integração] Erro ao verificar Vault: " + e.getMessage());
        }
    }

    /**
     * Registra o status de todas as integrações no log.
     */
    private void logIntegrationStatus() {
        plugin.getLogger().info("§6=== Status de Integrações ===");
        plugin.getLogger().info("§7PlaceholderAPI: " + (placeholderAPIEnabled ? "§aHabilitado" : "§cDesabilitado"));
        plugin.getLogger().info("§7Vault: " + (vaultEnabled ? "§aHabilitado" : "§cDesabilitado"));
        plugin.getLogger().info("§6==============================");
    }

    /**
     * Processa placeholders usando PlaceholderAPI se disponível.
     * @param text Texto com placeholders
     * @param player Jogador para processar placeholders
     * @return Texto processado
     */
    public String processPlaceholders(String text, org.bukkit.entity.Player player) {
        if (!placeholderAPIEnabled || text == null || player == null) {
            return text;
        }

        try {
            Class<?> placeholderAPIClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            java.lang.reflect.Method setPlaceholdersMethod = placeholderAPIClass.getMethod("setPlaceholders", org.bukkit.entity.Player.class, String.class);
            return (String) setPlaceholdersMethod.invoke(null, player, text);
        } catch (Exception e) {
            // Se PlaceholderAPI não estiver disponível ou houver erro, retorna o texto original
            return text;
        }
    }

    /**
     * Verifica se PlaceholderAPI está habilitado.
     * @return true se PlaceholderAPI está habilitado
     */
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    /**
     * Verifica se Vault está habilitado.
     * @return true se Vault está habilitado
     */
    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    /**
     * Obtém a instância do Economy do Vault.
     * @return Instância do Economy ou null se não disponível
     */
    public Object getVaultEconomy() {
        return vaultEconomy;
    }

    /**
     * Recarrega as verificações de integrações.
     */
    public void reload() {
        checkIntegrations();
    }
}

