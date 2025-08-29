package com.minekarta.karta.playercontract.config

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

class GuiConfigManager(private val plugin: KartaPlayerContract) {

    private lateinit var guiConfig: FileConfiguration

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val configFile = File(plugin.dataFolder, "gui.yml")
        if (!configFile.exists()) {
            plugin.saveResource("gui.yml", false)
        }
        guiConfig = YamlConfiguration.loadConfiguration(configFile)
    }

    fun reload() {
        loadConfig()
    }

    private fun deserialize(text: String): Component {
        return MiniMessage.miniMessage().deserialize("<italic:false>$text")
    }

    private fun deserialize(lore: List<String>): List<Component> {
        return lore.map { MiniMessage.miniMessage().deserialize("<italic:false>$it") }
    }

    fun getTitle(path: String, default: String): Component {
        val title = guiConfig.getString(path, default)!!
        return deserialize(title)
    }

    fun getSize(path: String, default: Int): Int = guiConfig.getInt(path, default)

    fun getMainMenuTitle(): Component = getTitle("main-menu.title", "<blue>Main Menu")

    fun getMainMenuSize(): Int = getSize("main-menu.size", 54)

    fun getButtonItem(path: String, vararg placeholders: Pair<String, String>): ItemStack {
        val materialName = guiConfig.getString("$path.item.material", "STONE")!!
        val material = Material.matchMaterial(materialName) ?: Material.STONE
        val customModelData = guiConfig.getInt("$path.item.custom_model_data", 0)

        var name = guiConfig.getString("$path.name", " ")!!
        var lore = guiConfig.getStringList("$path.lore")

        for ((key, value) in placeholders) {
            name = name.replace("<$key>", value)
            lore = lore.map { it.replace("<$key>", value) }
        }

        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(deserialize(name))
        meta.lore(deserialize(lore))
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData)
        }
        item.itemMeta = meta
        return item
    }

    fun getButtonSlot(path: String): Int = guiConfig.getInt("$path.slot", 0)

    fun getFillerItem(): ItemStack {
        val materialName = guiConfig.getString("main-menu.decoration.border-pane.item.material", "GRAY_STAINED_GLASS_PANE")!!
        val material = Material.matchMaterial(materialName) ?: Material.GRAY_STAINED_GLASS_PANE
        val name = guiConfig.getString("main-menu.decoration.border-pane.name", " ")!!

        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(MiniMessage.miniMessage().deserialize(name))
        item.itemMeta = meta
        return item
    }
}
