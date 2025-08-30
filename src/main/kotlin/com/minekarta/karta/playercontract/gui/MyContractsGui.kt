package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class MyContractsGui(
    plugin: KartaPlayerContract,
    player: Player
) : BaseGui(plugin, player, 54, Component.text("My Active Contracts")) {

    override fun initializeItems() {
        // TODO: Implement My Contracts GUI
        // 1. Fetch all contracts where the player is either the issuer or the contractor and state is IN_PROGRESS.
        // 2. Display the contracts in a list.
        // 3. If player is owner, show a "Cancel" button.
        // 4. If player is contractor, show a "Deliver" button (opens DeliveryStagingGui) and an "Abandon" button.
        fill(createFillerItem())
    }
}
