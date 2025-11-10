package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RewardManager {
    private ArtixDuels plugin;
    private List<Reward> winRewards;
    private List<Reward> lossRewards;
    private boolean rewardsEnabled;
    private double moneyReward;
    private int expReward;

    public RewardManager(ArtixDuels plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.winRewards = new ArrayList<>();
        this.lossRewards = new ArrayList<>();
        loadRewards(config);
    }

    private void loadRewards(FileConfiguration config) {
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        if (rewardsSection == null) {
            rewardsEnabled = false;
            return;
        }

        rewardsEnabled = rewardsSection.getBoolean("enabled", true);
        moneyReward = rewardsSection.getDouble("money", 0.0);
        expReward = rewardsSection.getInt("exp", 0);

        winRewards.clear();
        lossRewards.clear();

        ConfigurationSection winSection = rewardsSection.getConfigurationSection("win");
        if (winSection != null) {
            loadRewardList(winSection, winRewards);
        }

        ConfigurationSection lossSection = rewardsSection.getConfigurationSection("loss");
        if (lossSection != null) {
            loadRewardList(lossSection, lossRewards);
        }
    }

    public void reload(FileConfiguration config) {
        loadRewards(config);
    }

    private void loadRewardList(ConfigurationSection section, List<Reward> rewards) {
        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSection = section.getConfigurationSection(key);
            if (rewardSection == null) continue;

            String type = rewardSection.getString("type", "ITEM");
            double chance = rewardSection.getDouble("chance", 100.0);
            int minAmount = rewardSection.getInt("min-amount", 1);
            int maxAmount = rewardSection.getInt("max-amount", 1);

            Reward reward = new Reward(type, chance, minAmount, maxAmount);

            if (type.equals("ITEM")) {
                String materialName = rewardSection.getString("material", "DIAMOND");
                Material material = Material.valueOf(materialName);
                reward.setMaterial(material);
            } else if (type.equals("COMMAND")) {
                String command = rewardSection.getString("command", "");
                reward.setCommand(command);
            }

            rewards.add(reward);
        }
    }

    public void giveWinRewards(Player player) {
        if (!rewardsEnabled) return;

        if (moneyReward > 0) {
            giveMoney(player, moneyReward);
        }

        if (expReward > 0) {
            player.giveExp(expReward);
        }

        for (Reward reward : winRewards) {
            if (shouldGiveReward(reward.getChance())) {
                giveReward(player, reward);
            }
        }

        player.sendMessage("§aVocê recebeu recompensas pela vitória!");
    }

    public void giveLossRewards(Player player) {
        if (!rewardsEnabled) return;

        for (Reward reward : lossRewards) {
            if (shouldGiveReward(reward.getChance())) {
                giveReward(player, reward);
            }
        }
    }

    private void giveReward(Player player, Reward reward) {
        switch (reward.getType()) {
            case "ITEM":
                int amount = getRandomAmount(reward.getMinAmount(), reward.getMaxAmount());
                ItemStack item = new ItemStack(reward.getMaterial(), amount);
                player.getInventory().addItem(item);
                break;
            case "COMMAND":
                String command = reward.getCommand().replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                break;
        }
    }

    private void giveMoney(Player player, double amount) {
        try {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
                Object economy = Bukkit.getServicesManager().getRegistration(economyClass).getProvider();
                if (economy != null) {
                    economyClass.getMethod("depositPlayer", Player.class, double.class).invoke(economy, player, amount);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Vault não está disponível. Recompensas em dinheiro não serão dadas.");
        }
    }

    private boolean shouldGiveReward(double chance) {
        return new Random().nextDouble() * 100 < chance;
    }

    private int getRandomAmount(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private static class Reward {
        private String type;
        private double chance;
        private int minAmount;
        private int maxAmount;
        private Material material;
        private String command;

        public Reward(String type, double chance, int minAmount, int maxAmount) {
            this.type = type;
            this.chance = chance;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        public String getType() {
            return type;
        }

        public double getChance() {
            return chance;
        }

        public int getMinAmount() {
            return minAmount;
        }

        public int getMaxAmount() {
            return maxAmount;
        }

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }
    }
}

