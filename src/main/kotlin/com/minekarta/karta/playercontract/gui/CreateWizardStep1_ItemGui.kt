package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class CreateWizardStep1_ItemGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val wizardManager: CreateWizardManager
) : BaseGui(plugin, player, 54, Component.text("Create Contract - Step 1: Item")) {

    private val stagingSlot = 22 // The slot where the player puts their item

    override fun initializeItems() {
        // Fill border with glass
        fill(createFillerItem())

        // Instruction Item
        val instructions = ItemStack(Material.BOOK)
        instructions.itemMeta = instructions.itemMeta.also {
            it.displayName(Component.text("Select Item", NamedTextColor.YELLOW))
            it.lore(listOf(
                Component.text("Place one item you want to request", NamedTextColor.GRAY),
                Component.text("in the empty slot below.", NamedTextColor.GRAY)
            ))
        }
        setItem(13, instructions)

        // Set the staging slot to be initially empty
        setItem(stagingSlot, ItemStack(Material.AIR)) { event ->
            // Allow player to place items in this slot
            event.isCancelled = false
        }

        // Confirm button
        val confirmItem = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
        confirmItem.itemMeta = confirmItem.itemMeta.also {
            it.displayName(Component.text("Confirm Item", NamedTextColor.GREEN))
        }
        setItem(49, confirmItem, ::handleConfirmClick)
    }

    private fun handleConfirmClick(event: InventoryClickEvent) {
        val stagedItem = inventory.getItem(stagingSlot)
        if (stagedItem == null || stagedItem.type.isAir) {
            player.sendMessage(Component.text("You must place an item in the slot first!", NamedTextColor.RED))
            return
        }

        val wizardState = wizardManager.getState(player)
        if (wizardState == null) {
            player.closeInventory()
            player.sendMessage(Component.text("An error occurred. Please start over.", NamedTextColor.RED))
            return
        }

        // Save the item and move to the next step
        wizardState.requestItem = stagedItem.clone()
        player.sendMessage(Component.text("Item set to: ${stagedItem.type}"))

        // Move to the next wizard GUI (Step 2: Quantity)
        CreateWizardStep2_QuantityGui(plugin, player, wizardManager).open()
    }

    override fun handleClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
        // Give the item back to the player if they close the GUI without confirming
        val stagedItem = inventory.getItem(stagingSlot)
        if (stagedItem != null && !stagedItem.type.isAir) {
            player.inventory.addItem(stagedItem)
        }
    }
}
