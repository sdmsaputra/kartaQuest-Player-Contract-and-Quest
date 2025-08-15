package com.playercontract.managers;

import com.playercontract.PlayerContract;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {

    private final PlayerContract plugin;
    private FileConfiguration config;
    private final MiniMessage miniMessage;
    private final boolean isPapiEnabled;
    private static final Pattern LEGACY_PLACEHOLDER_PATTERN = Pattern.compile("[{$]([^{}$]+)}");
    private String adminPermission;


    public ConfigManager(PlayerContract plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.isPapiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();

        adminPermission = config.getString("admin-permission", "karta.admin");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    private String formatString(OfflinePlayer player, String message) {
        if (message == null) {
            return "";
        }
        // First, parse PAPI placeholders if PAPI is enabled
        if (isPapiEnabled && player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        // Then, convert our custom {placeholder} and ${placeholder} formats to MiniMessage format <placeholder>
        Matcher matcher = LEGACY_PLACEHOLDER_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<" + matcher.group(1) + ">");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public Component getMessage(String key, OfflinePlayer player, TagResolver... placeholders) {
        return getMessage(key, player, true, placeholders);
    }

    public Component getMessage(String key, TagResolver... placeholders) {
        return getMessage(key, null, true, placeholders);
    }

    public Component getMessage(String key, OfflinePlayer player, boolean withPrefix, TagResolver... placeholders) {
        String messageFormat = config.getString("messages." + key, "<red>Missing message for key: " + key + "</red>");
        String prefix = withPrefix ? config.getString("messages.prefix", "") : "";

        String formattedMessage = formatString(player, prefix + messageFormat);

        return miniMessage.deserialize(formattedMessage, placeholders);
    }

    public int getMaxContractsPerPlayer() {
        return config.getInt("max-contracts-per-player", 5);
    }

    public double getContractCreationTaxPercent() {
        return config.getDouble("contract-creation-tax-percent", 5.0);
    }

    public Component parse(String message, OfflinePlayer player, TagResolver... placeholders) {
        String formattedMessage = formatString(player, message);
        return miniMessage.deserialize(formattedMessage, placeholders);
    }

    public String getAdminPermission() {
        return adminPermission;
    }
}
