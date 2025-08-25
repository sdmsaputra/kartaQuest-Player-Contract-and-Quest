package com.playercontract.managers;

import com.playercontract.PlayerContract;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {

    private final PlayerContract plugin;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private final MiniMessage miniMessage;
    private final boolean isPapiEnabled;
    private static final Pattern LEGACY_PLACEHOLDER_PATTERN = Pattern.compile("[{$]([^{}$]+)}");
    private String adminPermission;


    public ConfigManager(PlayerContract plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.isPapiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        loadConfig();
        loadMessages();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();

        adminPermission = config.getString("admin-permission", "karta.admin");
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "message.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("message.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Load default messages from the JAR
        try (InputStream defaultConfigStream = plugin.getResource("message.yml")) {
            if (defaultConfigStream != null) {
                messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream)));
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load default messages from JAR.");
            e.printStackTrace();
        }
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadMessages();
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
        String messageFormat = messagesConfig.getString(key, "<red>Missing message for key: " + key + "</red>");
        String prefix = withPrefix ? messagesConfig.getString("prefix", "") : "";

        String formattedMessage = formatString(player, prefix + messageFormat);

        // Remove all MiniMessage tags that remove italics
        formattedMessage = formattedMessage.replace("<!italic>", "");

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

    public String getRawMessage(String key) {
        return messagesConfig.getString(key, "Unknown");
    }
}
