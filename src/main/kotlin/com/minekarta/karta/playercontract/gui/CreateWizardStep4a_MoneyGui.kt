package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.function.Consumer

class CreateWizardStep4a_MoneyGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val wizardManager: CreateWizardManager
) : BaseGui(plugin, player, 27, Component.text("Create Contract - Enter Amount")) {

    override fun initializeItems() {
        // This GUI doesn't actually display. It just triggers the chat input process.
        player.closeInventory()
        player.sendMessage(Component.text("Please type the reward amount in chat.", NamedTextColor.YELLOW))
        player.sendMessage(Component.text("Type 'cancel' to abort.", NamedTextColor.GRAY))

        plugin.chatInputManager.requestInput(player, Consumer { message ->
            handleChatInput(message)
        })
    }

    private fun handleChatInput(message: String) {
        if (message.equals("cancel", ignoreCase = true)) {
            wizardManager.cancelWizard(player)
            player.sendMessage(Component.text("Contract creation cancelled.", NamedTextColor.RED))
            return
        }

        val amount = message.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            player.sendMessage(Component.text("Invalid amount. Please enter a positive number.", NamedTextColor.RED))
            // Ask for input again
            plugin.chatInputManager.requestInput(player, Consumer { msg -> handleChatInput(msg) })
            return
        }

        val state = wizardManager.getState(player)
        if (state == null) {
            player.sendMessage(Component.text("An error occurred. Wizard state lost.", NamedTextColor.RED))
            return
        }

        state.rewardMoney = amount
        player.sendMessage(Component.text("Reward amount set to: ", NamedTextColor.GREEN).append(Component.text(amount)))

        // Since we are now outside of a GUI context, we need to run the next GUI opening
        // on the main server thread.
        plugin.scheduler.runOnMainThread(player) {
            CreateWizardStep5_ConfirmGui(plugin, player, wizardManager).open()
        }
    }
}
