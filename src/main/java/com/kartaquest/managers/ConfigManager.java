package com.kartaquest.managers;

import com.kartaquest.KartaQuest;
import org.bukkit.configuration.file.FileConfiguration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final KartaQuest plugin;
    private FileConfiguration config;
    private MiniMessage miniMessage;

    public ConfigManager(KartaQuest plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public Component getMessage(String key, TagResolver... placeholders) {
        String messageFormat = config.getString("messages." + key, "<red>Missing message for key: " + key + "</red>");
        String prefix = config.getString("messages.prefix", "");

        return miniMessage.deserialize(prefix + messageFormat, placeholders);
    }

    public int getMaxContractsPerPlayer() {
        return config.getInt("max-contracts-per-player", 5);
    }

    public double getContractCreationTaxPercent() {
        return config.getDouble("contract-creation-tax-percent", 5.0);
    }
}
