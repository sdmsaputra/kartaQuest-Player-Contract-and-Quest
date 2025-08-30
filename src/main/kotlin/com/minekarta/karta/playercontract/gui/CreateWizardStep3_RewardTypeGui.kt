package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CreateWizardStep3_RewardTypeGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val wizardManager: CreateWizardManager
) : BaseGui(plugin, player, 27, Component.text("Create Contract - Step 3: Reward")) {

    override fun initializeItems() {
        fill(createFillerItem())

        val moneyButton = ItemStack(Material.EMERALD)
        moneyButton.itemMeta = moneyButton.itemMeta.also {
            it.displayName(Component.text("Money Reward", NamedTextColor.GREEN))
            it.lore(listOf(Component.text("Offer a cash reward via Vault.", NamedTextColor.GRAY)))
        }
        setItem(11, moneyButton) {
            val state = wizardManager.getState(player) ?: return@setItem
            state.rewardType = RewardType.MONEY
            CreateWizardStep4a_MoneyGui(plugin, player, wizardManager).open()
        }

        val itemButton = ItemStack(Material.DIAMOND)
        itemButton.itemMeta = itemButton.itemMeta.also {
            it.displayName(Component.text("Item Reward", NamedTextColor.AQUA))
            it.lore(listOf(Component.text("Offer items as a reward.", NamedTextColor.GRAY)))
        }
        setItem(15, itemButton) {
            val state = wizardManager.getState(player) ?: return@setItem
            state.rewardType = RewardType.ITEM
            CreateWizardStep4b_ItemRewardGui(plugin, player, wizardManager).open()
        }
    }
}
