package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.domain.ContractState
import com.minekarta.karta.playercontract.domain.Reward
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class CreateWizardStep5_ConfirmGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val wizardManager: CreateWizardManager
) : BaseGui(plugin, player, 27, Component.text("Create Contract - Final Step")) {

    override fun initializeItems() {
        fill(createFillerItem())

        val state = wizardManager.getState(player)
        if (state == null) {
            player.closeInventory()
            return
        }

        // Summary Item
        val summaryItem = ItemStack(Material.PAPER)
        summaryItem.itemMeta = summaryItem.itemMeta.also {
            val lore = mutableListOf<Component>()
            lore.add(Component.text("Requesting: ", NamedTextColor.GRAY).append(Component.text("${state.requestAmount}x ${state.requestItem?.type}", NamedTextColor.AQUA)))
            val rewardStr = if (state.rewardType == RewardType.MONEY) "\$${state.rewardMoney}" else "${state.rewardItems.size} items"
            lore.add(Component.text("Reward: ", NamedTextColor.GRAY).append(Component.text(rewardStr, NamedTextColor.GOLD)))
            lore.add(Component.text("Expires in: ", NamedTextColor.GRAY).append(Component.text("${state.expiryDays} days", NamedTextColor.WHITE)))
            it.displayName(Component.text("Contract Summary", NamedTextColor.YELLOW))
            it.lore(lore)
        }
        setItem(13, summaryItem)

        // Confirm Button
        val confirmButton = ItemStack(Material.LIME_CONCRETE)
        confirmButton.itemMeta = confirmButton.itemMeta.also {
            it.displayName(Component.text("CREATE CONTRACT", NamedTextColor.GREEN))
        }
        setItem(11, confirmButton) {
            createContract(state)
        }

        // Cancel Button
        val cancelButton = ItemStack(Material.RED_CONCRETE)
        cancelButton.itemMeta = cancelButton.itemMeta.also {
            it.displayName(Component.text("CANCEL", NamedTextColor.RED))
        }
        setItem(15, cancelButton) {
            player.closeInventory() // The listener will handle cleanup
        }
    }

    private fun createContract(state: WizardState) {
        player.closeInventory()
        player.sendMessage(Component.text("Creating contract...", NamedTextColor.YELLOW))

        // This is where we finally build the contract object
        val contract = Contract(
            id = UUID.randomUUID(),
            templateId = null,
            title = "Request for ${state.requestAmount} ${state.requestItem?.type}", // Simple title
            description = "", // Can be expanded later
            issuerUUID = player.uniqueId,
            contractorUUID = null,
            requirements = listOf(state.requestItem!!.clone().apply { amount = state.requestAmount }),
            reward = Reward(
                money = if (state.rewardType == RewardType.MONEY) state.rewardMoney else null,
                items = if (state.rewardType == RewardType.ITEM) state.rewardItems else null
            ),
            deadline = Instant.now().plus(state.expiryDays.toLong(), ChronoUnit.DAYS),
            state = ContractState.AVAILABLE,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            deliveryProgress = emptyMap()
        )

        plugin.contractService.createContract(contract).whenComplete { newContract, error ->
            if (error != null) {
                player.sendMessage(Component.text("Error creating contract: ${error.message}", NamedTextColor.RED))
                // TODO: Return reward items if they were taken for escrow
            } else {
                player.sendMessage(Component.text("Contract created successfully!", NamedTextColor.GREEN))
                wizardManager.cancelWizard(player) // Clean up the state
            }
        }
    }
}
