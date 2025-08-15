package com.playercontract.utils;

import com.playercontract.PlayerContract;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DataManager {

    private final PlayerContract plugin;
    private FileConfiguration contractsConfig;
    private File contractsFile;
    private FileConfiguration reputationsConfig;
    private File reputationsFile;

    public DataManager(PlayerContract plugin) {
        this.plugin = plugin;
        setup();
    }

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        contractsFile = new File(plugin.getDataFolder(), "contracts.yml");
        if (!contractsFile.exists()) {
            try {
                contractsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create contracts.yml!");
            }
        }
        contractsConfig = YamlConfiguration.loadConfiguration(contractsFile);

        reputationsFile = new File(plugin.getDataFolder(), "reputations.yml");
        if (!reputationsFile.exists()) {
            try {
                reputationsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create reputations.yml!");
            }
        }
        reputationsConfig = YamlConfiguration.loadConfiguration(reputationsFile);
    }

    public FileConfiguration getContractsConfig() {
        return contractsConfig;
    }

    public void saveContractsConfig() {
        try {
            contractsConfig.save(contractsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save to contracts.yml!");
        }
    }

    public void reloadContractsConfig() {
        contractsConfig = YamlConfiguration.loadConfiguration(contractsFile);
    }

    public FileConfiguration getReputationsConfig() {
        return reputationsConfig;
    }

    public void saveReputationsConfig() {
        try {
            reputationsConfig.save(reputationsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save to reputations.yml!");
        }
    }

    public void reloadReputationsConfig() {
        reputationsConfig = YamlConfiguration.loadConfiguration(reputationsFile);
    }
}
