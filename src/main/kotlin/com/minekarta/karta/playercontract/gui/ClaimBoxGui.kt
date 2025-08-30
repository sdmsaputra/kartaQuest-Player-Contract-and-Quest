package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class ClaimBoxGui(
    plugin: KartaPlayerContract,
    player: Player
) : BaseGui(plugin, player, 54, Component.text("Claim Box")) {

    override fun initializeItems() {
        // TODO: Implement Claim Box GUI
        // 1. Fetch all item packages waiting for the player from the Escrow/Claim repository.
        // 2. Display each package as a clickable item.
        // 3. Clicking a package attempts to add the items to the player's inventory.
        // 4. If inventory is full, inform the player.
        // 5. On successful claim, remove the package from the repository.
        fill(createFillerItem())
    }
}
