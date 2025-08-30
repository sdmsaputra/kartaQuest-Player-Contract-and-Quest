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
    protected val size: Int,
    protected var title: Component
) : InventoryHolder {

    private lateinit var inventory: Inventory
    private val clickActions: MutableMap<Int, (InventoryClickEvent) -> Unit> = mutableMapOf()

    /**
     * Populates the GUI with items. To be implemented by subclasses.
     * This method is responsible for setting the final title if it's dynamic.
     */
    protected abstract fun initializeItems()

    /**
     * Opens the inventory for the player.
     */
    fun open() {
        // Initialize items first, as it might calculate dynamic properties like page numbers
        initializeItems()

        // Now create the inventory with the potentially updated title
        inventory = Bukkit.createInventory(this, size, title)

        // Populate the now-created inventory
        populateInventory()

        player.openInventory(inventory)
        playSound("gui.open-sound")
    }

    /**
    * This new method will be called by initializeItems in subclasses to populate the inventory
    * after it has been created.
    */
    protected open fun populateInventory() {
        // Default implementation can be empty or subclasses can provide their own.
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
        if (soundName.isNotBlank() && !soundName.startsWith("Message not found:")) {
            try {
                val sound = Sound.valueOf(soundName.uppercase())
                player.playSound(player.location, sound, 1.0f, 1.0f)
            } catch (e: IllegalArgumentException) {
                plugin.logger.warning("Invalid sound name in messages.yml: $soundName")
            }
        }
    }
}
