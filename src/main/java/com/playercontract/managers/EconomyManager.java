package com.playercontract.managers;

import com.playercontract.PlayerContract;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final PlayerContract plugin;
    private Economy economy;

    public EconomyManager(PlayerContract plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault not found! Disabling KartaQuest.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().severe("No economy provider found! Disabling KartaQuest.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        economy = rsp.getProvider();
    }

    public boolean hasAccount(OfflinePlayer player) {
        return economy.hasAccount(player);
    }

    public double getBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return economy.has(player, amount);
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount);
    }

    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount);
    }

    public Economy getEconomy() {
        return economy;
    }
}
