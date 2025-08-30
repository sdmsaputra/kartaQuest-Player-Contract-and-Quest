package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID

class DeliveryStagingGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val contractId: UUID
) : BaseGui(plugin, player, 54, Component.text("Deliver Items")) {

    override fun initializeItems() {
        // TODO: Implement Delivery Staging GUI
        // 1. Load contract details.
        // 2. Display required items and amounts.
        // 3. Provide staging slots for the player to put items in.
        // 4. Add a "Confirm Delivery" button.
        // 5. On confirm, call the ContractService.deliverItems method.
        // 6. On close, return any staged items to the player.
        fill(createFillerItem())
    }
}
