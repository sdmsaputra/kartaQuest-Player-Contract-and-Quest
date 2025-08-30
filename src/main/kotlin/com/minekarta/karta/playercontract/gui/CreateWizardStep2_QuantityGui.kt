package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CreateWizardStep2_QuantityGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val wizardManager: CreateWizardManager
) : BaseGui(plugin, player, 54, Component.text("Create Contract - Step 2: Quantity")) {

    override fun initializeItems() {
        fill(createFillerItem())

        val state = wizardManager.getState(player)
        val requestItem = state?.requestItem
        if (requestItem == null) {
            player.closeInventory()
            player.sendMessage(Component.text("Error: Item not set. Please start over.", NamedTextColor.RED))
            return
        }

        // Display the selected item
        val itemPreview = requestItem.clone()
        itemPreview.itemMeta = itemPreview.itemMeta.also {
            it.displayName(Component.text("Requesting Item", NamedTextColor.YELLOW))
        }
        setItem(4, itemPreview)

        // Number pad buttons
        setQuantityButton(1, 29)
        setQuantityButton(16, 30)
        setQuantityButton(64, 31)
        setQuantityButton(128, 32)
        setQuantityButton(256, 33)

        // TODO: Implement custom amount input (e.g., Anvil GUI)
        val customButton = ItemStack(Material.ANVIL)
        customButton.itemMeta = customButton.itemMeta.also {
            it.displayName(Component.text("Custom Amount", NamedTextColor.AQUA))
            it.lore(listOf(Component.text("Not yet implemented.", NamedTextColor.RED)))
        }
        setItem(40, customButton)
    }

    private fun setQuantityButton(amount: Int, slot: Int) {
        val item = ItemStack(Material.STONE_BUTTON, amount)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text("$amount", NamedTextColor.GREEN))
        }
        setItem(slot, item) {
            val state = wizardManager.getState(player) ?: return@setItem
            state.requestAmount = amount
            player.sendMessage(Component.text("Quantity set to: $amount"))
            CreateWizardStep3_RewardTypeGui(plugin, player, wizardManager).open()
        }
    }
}
