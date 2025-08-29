package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.service.ContractService
import org.bukkit.entity.Player

class ContractListGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val guiConfig: GuiConfigManager,
    private val contractService: ContractService
) : BaseGui(
    plugin,
    player,
    guiConfig.getSize("contract-list.size", 54),
    guiConfig.getTitle("contract-list.title", "<blue>Contract List")
) {

    private var currentPage = 0
    private val contractsPerPage = 28 // 4 rows of 7

    override fun initializeItems() {
        contractService.getActiveContracts().thenAcceptAsync { contracts ->
            val paginatedContracts = contracts.drop(currentPage * contractsPerPage).take(contractsPerPage)

            // Populate with contract items
            paginatedContracts.forEachIndexed { index, contract ->
                val slot = (index / 7) * 9 + (index % 7) + 10 // Map to GUI slots
                val item = guiConfig.getButtonItem(
                    "contract-list.contract-item",
                    "title" to contract.description,
                    "issuer" to contract.issuerUUID.toString(), // Placeholder, should resolve to name
                    "reward" to contract.reward.toString(),
                    "time_left" to "N/A", // Placeholder
                    "status" to contract.state.name
                )
                setItem(slot, item) {
                    // TODO: Open contract detail view
                }
            }

            // Navigation buttons
            if (currentPage > 0) {
                setItem(48, guiConfig.getButtonItem("contract-list.navigation.previous-page")) {
                    currentPage--
                    refresh()
                }
            }
            if (contracts.size > (currentPage + 1) * contractsPerPage) {
                setItem(50, guiConfig.getButtonItem("contract-list.navigation.next-page")) {
                    currentPage++
                    refresh()
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
