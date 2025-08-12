package com.kartaquest;

import com.kartaquest.commands.ReputationsCommand;
import com.kartaquest.data.Contract;
import com.kartaquest.expansion.KartaQuestExpansion;
import com.kartaquest.listeners.GUIListener;
import com.kartaquest.managers.ConfigManager;
import com.kartaquest.managers.ContractManager;
import com.kartaquest.managers.EconomyManager;
import com.kartaquest.managers.ReputationManager;
import com.kartaquest.utils.DataManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class KartaQuest extends JavaPlugin {

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private ContractManager contractManager;
    private ReputationManager reputationManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        // Initialize Managers
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        economyManager = new EconomyManager(this);
        contractManager = new ContractManager(this);
        reputationManager = new ReputationManager(this);

        // Register commands
        getCommand("kartaquest").setExecutor(new com.kartaquest.commands.KartaQuestCommand(this));
        getCommand("kartaquest").setTabCompleter(new com.kartaquest.commands.KartaQuestCommand(this));
        getCommand("reputations").setExecutor(new ReputationsCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // Register PlaceholderAPI expansion
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KartaQuestExpansion(this).register();
            getLogger().info("Successfully registered PlaceholderAPI expansion.");
        }

        startExpirationTask();

        getLogger().info("KartaQuest has been enabled!");
    }

    private void startExpirationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                getContractManager().getActiveContracts().values().stream()
                        .filter(c -> c.status() == Contract.ContractStatus.AVAILABLE || c.status() == Contract.ContractStatus.IN_PROGRESS)
                        .filter(c -> c.timeLimit() > 0 && c.timeLimit() < now)
                        .forEach(c -> {
                            getLogger().info("Contract " + c.contractId() + " has expired.");
                            getContractManager().expireContract(c.contractId());
                        });
            }
        }.runTaskTimer(this, 0L, 20L * 60 * 5); // Check every 5 minutes
    }

    @Override
    public void onDisable() {
        // Save data on disable
        if (contractManager != null) {
            contractManager.saveContractsSync();
        }
        if (reputationManager != null) {
            reputationManager.saveReputationsSync();
        }
        getLogger().info("KartaQuest has been disabled!");
    }

    // Getters for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ContractManager getContractManager() {
        return contractManager;
    }

    public ReputationManager getReputationManager() {
        return reputationManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
