package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.service.InventoryService
import org.bukkit.entity.Player

class InventoryGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val guiConfig: GuiConfigManager,
    private val inventoryService: InventoryService
) : BaseGui(
    plugin,
    player,
    guiConfig.getSize("inventory.size", 54),
    guiConfig.getTitle("inventory.title", "<gold>Reward Inventory")
) {

    override fun initializeItems() {
        inventoryService.getClaimablePackages(player.uniqueId).thenAcceptAsync { packages ->
            packages.forEachIndexed { index, pkg ->
                val item = guiConfig.getButtonItem(
                    "inventory.claimable-item",
                    "contract_name" to pkg.contractId.toString(), // Placeholder
                    "reward_description" to pkg.contents.joinToString(", ") { "${it.amount}x ${it.type}" }
                )
                setItem(index, item) {
                    inventoryService.claimPackage(player, pkg.id).thenAccept { success ->
                        if (success) {
                            player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
                            refresh()
                        } else {
                            player.sendMessage(plugin.messageManager.getPrefixedMessage("contract.inventory-full"))
                            player.playSound(player.location, org.bukkit.Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f)
                        }
                    }
                }
            }
            fill(guiConfig.getFillerItem())
        }
    }

    private fun refresh() {
        inventory.clear()
        initializeItems()
    }
}
