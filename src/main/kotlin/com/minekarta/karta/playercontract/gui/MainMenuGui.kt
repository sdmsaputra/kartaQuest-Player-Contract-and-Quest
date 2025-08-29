package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class MainMenuGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val guiConfig: GuiConfigManager
) : BaseGui(plugin, player, guiConfig.getMainMenuSize(), guiConfig.getMainMenuTitle()) {

    override fun initializeItems() {
        // --- Main Navigation Buttons ---

        // Contract List Button
        val contractListPath = "main-menu.buttons.contract-list"
        setItem(guiConfig.getButtonSlot(contractListPath), guiConfig.getButtonItem(contractListPath)) {
            player.sendMessage(Component.text("You clicked the Contract List button!"))
            // Future: GuiRouter.open(ContractListGui(plugin, player, guiConfig, 0))
        }

        // Delivery Inbox Button
        val deliveryInboxPath = "main-menu.buttons.delivery-inbox"
        setItem(guiConfig.getButtonSlot(deliveryInboxPath), guiConfig.getButtonItem(deliveryInboxPath)) {
            player.sendMessage(Component.text("You clicked the Delivery Inbox button!"))
            // Future: GuiRouter.open(DeliveryInboxGui(...))
        }

        // History Button
        val historyPath = "main-menu.buttons.history"
        setItem(guiConfig.getButtonSlot(historyPath), guiConfig.getButtonItem(historyPath)) {
            player.sendMessage(Component.text("You clicked the History button!"))
            // Future: GuiRouter.open(HistoryGui(...))
        }

        // Statistics Button
        val statsPath = "main-menu.buttons.statistics"
        setItem(guiConfig.getButtonSlot(statsPath), guiConfig.getButtonItem(statsPath)) {
            player.sendMessage(Component.text("You clicked the Statistics button!"))
            // Future: GuiRouter.open(StatisticsGui(...))
        }

        // --- Profile Header ---
        // This will require fetching player stats asynchronously. For now, it's a placeholder.
        val profileHeaderPath = "main-menu.buttons.profile-header"
        val profileItem = guiConfig.getButtonItem(profileHeaderPath,
            "player_name" to player.name,
            "active_contracts" to "0",
            "completed_contracts" to "0",
            "total_earned" to "0.00"
        )
        // TODO: Replace placeholder item with actual player head
        setItem(guiConfig.getButtonSlot(profileHeaderPath), profileItem)

        // --- Decorative Items ---
        fill(guiConfig.getFillerItem())
    }
}
