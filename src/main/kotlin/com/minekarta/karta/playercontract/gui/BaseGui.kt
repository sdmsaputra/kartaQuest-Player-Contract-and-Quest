package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

/**
 * An abstract base class for all GUIs in the plugin.
 * It handles the basic setup of an inventory and provides a structured way to handle clicks.
 *
 * Each GUI instance is its own InventoryHolder, which is the modern, safe way to handle GUIs.
 */
abstract class BaseGui(
    protected val plugin: KartaPlayerContract,
    protected val player: Player,
    private val size: Int,
    private val title: Component
) : InventoryHolder {

    private val inventory: Inventory = Bukkit.createInventory(this, size, title)
    private val clickActions: MutableMap<Int, (InventoryClickEvent) -> Unit> = mutableMapOf()

    /**
     * Populates the GUI with items. To be implemented by subclasses.
     */
    protected abstract fun initializeItems()

    /**
     * Opens the inventory for the player.
     */
    fun open() {
        initializeItems()
        player.openInventory(inventory)
        playSound("gui.open-sound")
    }

    /**
     * Handles a click event within this GUI.
     * This method is called by a central GUIListener.
     */
    fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true // Prevent players from taking items by default
        if (clickActions.containsKey(event.rawSlot)) {
            playSound("gui.click-sound")
            clickActions[event.rawSlot]?.invoke(event)
        }
    }

    /**
     * Handles a close event for this GUI.
     * Can be overridden by subclasses to perform cleanup.
     */
    open fun handleClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
        // By default, do nothing.
    }

    /**
     * Sets an item in a specific slot of the inventory.
     */
    protected fun setItem(slot: Int, item: ItemStack, onClick: ((InventoryClickEvent) -> Unit)? = null) {
        inventory.setItem(slot, item)
        if (onClick != null) {
            clickActions[slot] = onClick
        }
    }

    /**
     * Fills the border or empty slots of the GUI with a filler item.
     */
    protected fun fill(item: ItemStack) {
        for (i in 0 until size) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item)
            }
        }
    }

    /**
     * Creates a simple, non-functional item, typically for decoration.
     */
    protected fun createFillerItem(material: Material = Material.GRAY_STAINED_GLASS_PANE, name: String = " "): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(Component.text(name))
        item.itemMeta = meta
        return item
    }

    override fun getInventory(): Inventory = inventory

    private fun playSound(soundKey: String) {
        val soundName = plugin.messageManager.getRawMessage(soundKey)
        try {
            val sound = Sound.valueOf(soundName.uppercase())
            player.playSound(player.location, sound, 1.0f, 1.0f)
        } catch (e: IllegalArgumentException) {
            plugin.logger.warning("Invalid sound name in messages.yml: $soundName")
        }
    }
}
