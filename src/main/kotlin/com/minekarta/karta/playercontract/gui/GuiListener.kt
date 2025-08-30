package com.minekarta.karta.playercontract.gui

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * A centralized listener for all GUI interactions.
 */
class GuiListener(private val wizardManager: CreateWizardManager) : Listener {

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

            // If a player closes a wizard GUI, we should probably cancel the wizard process
            // to prevent them from getting stuck in a state.
            if (holder is CreateWizardStep1_ItemGui) { // TODO: Add other wizard steps here
                wizardManager.getState(event.player as Player)?.let {
                    wizardManager.cancelWizard(event.player as Player)
                }
            }
        }
    }
}
