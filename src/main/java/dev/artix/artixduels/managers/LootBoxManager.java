package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.LootBox;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de loot boxes.
 */
public class LootBoxManager {
    private final ArtixDuels plugin;
    private final RewardManager rewardManager;
    private Map<String, LootBox> lootBoxes;
    private Map<UUID, List<String>> playerLootBoxes;

    public LootBoxManager(ArtixDuels plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.lootBoxes = new HashMap<>();
        this.playerLootBoxes = new HashMap<>();
        
        loadLootBoxes();
        loadPlayerLootBoxes();
    }

    public void giveLootBox(UUID playerId, String lootBoxId) {
        playerLootBoxes.computeIfAbsent(playerId, k -> new ArrayList<>()).add(lootBoxId);
        savePlayerLootBoxes();
    }

    public void openLootBox(Player player, String lootBoxId) {
        LootBox lootBox = lootBoxes.get(lootBoxId);
        if (lootBox == null) return;

        List<String> playerBoxes = playerLootBoxes.get(player.getUniqueId());
        if (playerBoxes == null || !playerBoxes.contains(lootBoxId)) {
            player.sendMessage("§cVocê não possui esta loot box!");
            return;
        }

        playerBoxes.remove(lootBoxId);
        
        // Animar abertura
        animateOpening(player, lootBox);
        
        // Dar recompensas
        giveRewards(player, lootBox);
        
        savePlayerLootBoxes();
    }

    private void animateOpening(Player player, LootBox lootBox) {
        player.sendMessage("§6§l=== ABRINDO LOOT BOX ===");
        player.sendMessage(lootBox.getRarity().getDisplayName() + " " + lootBox.getName());
        
        // Animação simples
        for (int i = 3; i > 0; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§e" + count + "...");
            }, (3 - count) * 20L);
        }
    }

    private void giveRewards(Player player, LootBox lootBox) {
        if (lootBox.getRewards() == null) return;
        
        Random random = new Random();
        for (LootBox.LootBoxReward reward : lootBox.getRewards()) {
            if (random.nextDouble() * 100 < reward.getChance()) {
                giveReward(player, reward);
            }
        }
    }

    private void giveReward(Player player, LootBox.LootBoxReward reward) {
        String type = reward.getType();
        Map<String, Object> data = reward.getData();
        
        if (type.equals("money")) {
            double amount = ((Number) data.get("amount")).doubleValue();
            rewardManager.giveMoney(player, amount);
            player.sendMessage("§a+$" + amount);
        } else if (type.equals("item")) {
            String material = (String) data.get("material");
            int amount = ((Number) data.getOrDefault("amount", 1)).intValue();
            ItemStack item = new ItemStack(Material.valueOf(material), amount);
            player.getInventory().addItem(item);
            player.sendMessage("§aRecebeu: " + material);
        }
    }

    public List<String> getPlayerLootBoxes(UUID playerId) {
        return playerLootBoxes.getOrDefault(playerId, new ArrayList<>());
    }

    private void loadLootBoxes() {
        File lootBoxesFile = new File(plugin.getDataFolder(), "lootboxes.yml");
        if (!lootBoxesFile.exists()) {
            createDefaultLootBoxes(lootBoxesFile);
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(lootBoxesFile);
        // Carregar loot boxes do arquivo
    }

    private void createDefaultLootBoxes(File file) {
        try {
            file.createNewFile();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            config.set("lootboxes.common.name", "Loot Box Comum");
            config.set("lootboxes.common.rarity", "COMMON");
            config.set("lootboxes.common.drop-chance", 50.0);
            
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao criar loot boxes: " + e.getMessage());
        }
    }

    private void loadPlayerLootBoxes() {
        File playerBoxesFile = new File(plugin.getDataFolder(), "player_lootboxes.yml");
        if (!playerBoxesFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerBoxesFile);
        if (config.contains("lootboxes")) {
            for (String playerIdStr : config.getConfigurationSection("lootboxes").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    List<String> boxes = config.getStringList("lootboxes." + playerIdStr);
                    playerLootBoxes.put(playerId, boxes);
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }
    }

    private void savePlayerLootBoxes() {
        File playerBoxesFile = new File(plugin.getDataFolder(), "player_lootboxes.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerBoxesFile);

        for (Map.Entry<UUID, List<String>> entry : playerLootBoxes.entrySet()) {
            config.set("lootboxes." + entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(playerBoxesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar loot boxes: " + e.getMessage());
        }
    }
}

