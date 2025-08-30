package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class DeliveryInboxGui(
    plugin: KartaPlayerContract,
    player: Player
) : BaseGui(plugin, player, 54, Component.text("Delivery Inbox")) {

    override fun initializeItems() {
        // TODO: Implement Delivery Inbox GUI for contract issuers.
        // 1. Fetch all contracts for the player with state = DELIVERED.
        // 2. Display each delivery as a clickable item.
        // 3. Clicking a delivery opens a detail view with "Accept" and "Reject" buttons.
        // 4. "Accept" calls a service method to complete the contract, release reward, and give items to issuer.
        // 5. "Reject" calls a service method to return items to the contractor's claim box.
        fill(createFillerItem())
    }
}
