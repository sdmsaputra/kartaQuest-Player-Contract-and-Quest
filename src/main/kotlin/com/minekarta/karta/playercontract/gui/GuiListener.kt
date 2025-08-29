package com.minekarta.karta.playercontract.gui

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * A centralized listener for all GUI interactions.
 */
class GuiListener : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder

        // Check if the inventory is one of our custom GUIs
        if (holder is BaseGui) {
            // If it is, delegate the click handling to that specific GUI instance.
            holder.handleClick(event)
        }
    }
}
