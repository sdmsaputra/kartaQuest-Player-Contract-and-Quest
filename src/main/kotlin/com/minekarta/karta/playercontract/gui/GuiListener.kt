package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * A centralized listener for all GUI interactions.
 */
class GuiListener(private val plugin: KartaPlayerContract) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder

        // Check if the inventory is one of our custom GUIs
        if (holder is BaseGui) {
            // If it is, delegate the click handling to that specific GUI instance.
            holder.handleClick(event)
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder
        if (holder is BaseGui) {
            holder.handleClose(event)
        }
    }
}
