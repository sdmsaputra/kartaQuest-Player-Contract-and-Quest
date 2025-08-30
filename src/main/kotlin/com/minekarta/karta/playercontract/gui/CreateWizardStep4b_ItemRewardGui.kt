package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class CreateWizardStep4b_ItemRewardGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val wizardManager: CreateWizardManager
) : BaseGui(plugin, player, 54, Component.text("Create Contract - Step 4: Item Reward")) {

    // The slots where the player puts their reward items
    private val stagingSlots = (10..43).toList()

    override fun initializeItems() {
        fill(createFillerItem())

        val instructions = ItemStack(Material.BOOK)
        instructions.itemMeta = instructions.itemMeta.also {
            it.displayName(Component.text("Set Item Reward", NamedTextColor.YELLOW))
            it.lore(listOf(
                Component.text("Place the item(s) you want to offer", NamedTextColor.GRAY),
                Component.text("as a reward in the empty slots.", NamedTextColor.GRAY)
            ))
        }
        setItem(4, instructions)

        // Allow players to place items in staging slots
        stagingSlots.forEach { slot ->
            setItem(slot, ItemStack(Material.AIR)) { event ->
                event.isCancelled = false
            }
        }

        // Confirm button
        val confirmItem = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
        confirmItem.itemMeta = confirmItem.itemMeta.also {
            it.displayName(Component.text("Confirm Reward", NamedTextColor.GREEN))
        }
        setItem(49, confirmItem, ::handleConfirmClick)
    }

    private fun handleConfirmClick(event: InventoryClickEvent) {
        val rewardItems = stagingSlots
            .mapNotNull { inventory.getItem(it) }
            .filter { !it.type.isAir }

        if (rewardItems.isEmpty()) {
            player.sendMessage(Component.text("You must offer at least one item as a reward!", NamedTextColor.RED))
            return
        }

        val state = wizardManager.getState(player) ?: return
        state.rewardItems = rewardItems.map { it.clone() }

        // Clear the staging slots so items aren't duplicated on close
        stagingSlots.forEach { inventory.setItem(it, null) }

        player.sendMessage(Component.text("Item reward set!"))
        CreateWizardStep5_ConfirmGui(plugin, player, wizardManager).open()
    }

    override fun handleClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
        // Return all items to the player if they close the GUI without confirming
        val itemsToReturn = stagingSlots
            .mapNotNull { inventory.getItem(it) }
            .filter { !it.type.isAir }

        if (itemsToReturn.isNotEmpty()) {
            player.inventory.addItem(*itemsToReturn.toTypedArray())
            player.sendMessage(Component.text("Your reward items have been returned to you.", NamedTextColor.YELLOW))
        }
    }
}
