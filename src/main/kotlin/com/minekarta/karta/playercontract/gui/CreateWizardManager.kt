package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the state of the multi-step contract creation wizard for all players.
 */
class CreateWizardManager(private val plugin: KartaPlayerContract) {

    private val wizardStates = ConcurrentHashMap<UUID, WizardState>()

    /**
     * Starts the wizard for a player.
     */
    fun startWizard(player: Player) {
        val state = WizardState(player.uniqueId)
        wizardStates[player.uniqueId] = state
        // Open the first step of the wizard
        CreateWizardStep1_ItemGui(plugin, player, this).open()
    }

    /**
     * Retrieves the current wizard state for a player.
     */
    fun getState(player: Player): WizardState? {
        return wizardStates[player.uniqueId]
    }

    /**
     * Advances the wizard to the next step.
     */
    fun nextStep(player: Player) {
        val state = getState(player) ?: return
        // TODO: Implement logic to move to the next step's GUI
    }

    /**
     * Cancels the wizard and cleans up the state.
     */
    fun cancelWizard(player: Player) {
        wizardStates.remove(player.uniqueId)
        // TODO: Inform the player
    }
}

/**
 * Holds the data for a contract being created via the GUI wizard.
 */
data class WizardState(
    val playerUUID: UUID,
    var requestItem: ItemStack? = null,
    var requestAmount: Int = 0,
    var rewardType: RewardType? = null,
    var rewardMoney: Double = 0.0,
    var rewardItems: List<ItemStack> = emptyList(),
    var expiryDays: Int = 7 // Default expiry
)

enum class RewardType {
    MONEY, ITEM
}
