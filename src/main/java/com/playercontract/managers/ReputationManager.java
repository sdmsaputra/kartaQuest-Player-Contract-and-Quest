package com.playercontract.managers;

import com.playercontract.PlayerContract;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReputationManager {

    private final PlayerContract plugin;
    private final Map<UUID, Integer> reputationCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> completedContractsCache = new ConcurrentHashMap<>();

    public ReputationManager(PlayerContract plugin) {
        this.plugin = plugin;
        loadReputations(); // This will be expanded to load completed counts too
    }

    public void loadReputations() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getDataManager().reloadReputationsConfig();

                // Load reputations
                ConfigurationSection repSection = plugin.getDataManager().getReputationsConfig().getConfigurationSection("reputations");
                if (repSection != null) {
                    for (String key : repSection.getKeys(false)) {
                        UUID playerUuid = UUID.fromString(key);
                        int reputation = plugin.getDataManager().getReputationsConfig().getInt("reputations." + key);
                        reputationCache.put(playerUuid, reputation);
                    }
                }

                // Load completed counts
                ConfigurationSection completedSection = plugin.getDataManager().getReputationsConfig().getConfigurationSection("completed-contracts");
                if (completedSection != null) {
                    for (String key : completedSection.getKeys(false)) {
                        UUID playerUuid = UUID.fromString(key);
                        int count = plugin.getDataManager().getReputationsConfig().getInt("completed-contracts." + key);
                        completedContractsCache.put(playerUuid, count);
                    }
                }
                plugin.getLogger().info("Loaded " + reputationCache.size() + " reputation entries and " + completedContractsCache.size() + " completion records.");
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveReputations() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Save reputations
                plugin.getDataManager().getReputationsConfig().set("reputations", null);
                for (Map.Entry<UUID, Integer> entry : reputationCache.entrySet()) {
                    plugin.getDataManager().getReputationsConfig().set("reputations." + entry.getKey().toString(), entry.getValue());
                }

                // Save completed counts
                plugin.getDataManager().getReputationsConfig().set("completed-contracts", null);
                for (Map.Entry<UUID, Integer> entry : completedContractsCache.entrySet()) {
                    plugin.getDataManager().getReputationsConfig().set("completed-contracts." + entry.getKey().toString(), entry.getValue());
                }

                plugin.getDataManager().saveReputationsConfig();
                plugin.getLogger().info("Saved " + reputationCache.size() + " reputation entries and " + completedContractsCache.size() + " completion records.");
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveReputationsSync() {
        // Save reputations
        plugin.getDataManager().getReputationsConfig().set("reputations", null);
        for (Map.Entry<UUID, Integer> entry : reputationCache.entrySet()) {
            plugin.getDataManager().getReputationsConfig().set("reputations." + entry.getKey().toString(), entry.getValue());
        }

        // Save completed counts
        plugin.getDataManager().getReputationsConfig().set("completed-contracts", null);
        for (Map.Entry<UUID, Integer> entry : completedContractsCache.entrySet()) {
            plugin.getDataManager().getReputationsConfig().set("completed-contracts." + entry.getKey().toString(), entry.getValue());
        }

        plugin.getDataManager().saveReputationsConfig();
        plugin.getLogger().info("Saved " + reputationCache.size() + " reputation entries and " + completedContractsCache.size() + " completion records.");
    }

    public int getReputation(UUID playerUuid) {
        return reputationCache.getOrDefault(playerUuid, 0);
    }

    public void addReputation(UUID playerUuid, int amount) {
        reputationCache.put(playerUuid, getReputation(playerUuid) + amount);
    }

    public void removeReputation(UUID playerUuid, int amount) {
        reputationCache.put(playerUuid, getReputation(playerUuid) - amount);
    }

    public int getCompletedContracts(UUID playerUuid) {
        return completedContractsCache.getOrDefault(playerUuid, 0);
    }

    public void incrementCompletedContracts(UUID playerUuid) {
        completedContractsCache.put(playerUuid, getCompletedContracts(playerUuid) + 1);
    }
}
