package com.minekarta.karta.playercontract.config

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

data class GuiButton(val slot: Int, val item: ItemStack)

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

    fun getTitle(path: String, default: String, vararg placeholders: Pair<String, String>): Component {
        var title = guiConfig.getString(path, default)!!
        for ((key, value) in placeholders) {
            title = title.replace("<$key>", value, ignoreCase = true)
        }
        return deserialize(title)
    }

    fun getSize(path: String, default: Int): Int = guiConfig.getInt(path, default)

    fun getSlots(path: String): List<Int> = guiConfig.getIntegerList(path)

    fun getMainMenuTitle(): Component = getTitle("main-menu.title", "<blue>Main Menu")

    fun getMainMenuSize(): Int = getSize("main-menu.size", 54)

    fun getButtonItem(path: String, vararg placeholders: Pair<String, String>): ItemStack {
        // Inherit from common buttons if applicable
        val commonButtonName = guiConfig.getString("$path.inherit")
        val finalPath = if (commonButtonName != null) "common-buttons.$commonButtonName" else path

        val materialName = guiConfig.getString("$finalPath.material", "STONE")!!
        val material = Material.matchMaterial(materialName) ?: Material.STONE
        val customModelData = guiConfig.getInt("$finalPath.custom_model_data", 0)

        var name = guiConfig.getString("$finalPath.name", " ")!!
        var lore = guiConfig.getStringList("$finalPath.lore")

        for ((key, value) in placeholders) {
            name = name.replace("<$key>", value, ignoreCase = true)
            lore = lore.map { it.replace("<$key>", value, ignoreCase = true) }
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

    fun getButton(path: String, vararg placeholders: Pair<String, String>): GuiButton {
        val slot = guiConfig.getInt("$path.slot")
        val item = getButtonItem(path, *placeholders)
        return GuiButton(slot, item)
    }

    fun getFillerItem(): ItemStack {
        val path = "main-menu.filler"
        val materialName = guiConfig.getString("$path.material", "GRAY_STAINED_GLASS_PANE")!!
        val material = Material.matchMaterial(materialName) ?: Material.GRAY_STAINED_GLASS_PANE
        val name = guiConfig.getString("$path.name", " ")!!

        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(deserialize(name))
        item.itemMeta = meta
        return item
    }
}
