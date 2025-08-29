package com.minekarta.karta.playercontract.config

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class MessageManager(private val plugin: KartaPlayerContract) {

    private lateinit var messageConfig: FileConfiguration

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val configFile = File(plugin.dataFolder, "messages.yml")
        if (!configFile.exists()) {
            plugin.saveResource("messages.yml", false)
        }
        messageConfig = YamlConfiguration.loadConfiguration(configFile)
    }

    fun reload() {
        loadConfig()
        plugin.logger.info("messages.yml has been reloaded.")
    }

    fun getMessage(path: String, vararg placeholders: Pair<String, String>): Component {
        var message = messageConfig.getString(path, "Message not found: $path")!!
        for ((key, value) in placeholders) {
            message = message.replace("<$key>", value)
        }
        return MiniMessage.miniMessage().deserialize(message)
    }

    fun getPrefixedMessage(path: String, vararg placeholders: Pair<String, String>): Component {
        val prefix = messageConfig.getString("prefix", "")!!
        var message = messageConfig.getString(path, "Message not found: $path")!!
        for ((key, value) in placeholders) {
            message = message.replace("<$key>", value)
        }
        return MiniMessage.miniMessage().deserialize(prefix + message)
    }

    fun getRawMessage(path: String): String {
        return messageConfig.getString(path, "Message not found: $path")!!
    }
}
