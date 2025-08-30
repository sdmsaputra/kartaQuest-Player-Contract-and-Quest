package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.service.ContractService
import org.bukkit.entity.Player

class ContractListGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val guiConfig: GuiConfigManager,
    private val contractService: ContractService,
    private var currentPage: Int = 0
) : BaseGui(
    plugin,
    player,
    guiConfig.getSize("contract-list.size", 54),
    guiConfig.getTitle("contract-list.title", "<blue>Contract List") // Initial title, will be updated
) {
    private val contractsPerPage = guiConfig.getSlots("contract-list.item_slots").size
    private var maxPages = 0
    private var contracts: List<Contract> = emptyList()

    override fun initializeItems() {
        // This method now handles fetching data and setting up properties BEFORE inventory creation.
        // It's a blocking call for simplicity here, but in a real scenario, this would be async.
        val totalContracts = contractService.countOpenContracts().join()
        maxPages = if (totalContracts == 0) 0 else (totalContracts - 1) / contractsPerPage
        if (currentPage > maxPages) {
            currentPage = maxPages
        }

        contracts = contractService.listOpenContracts(currentPage, contractsPerPage).join()

        // Now, update the title with the correct page numbers
        title = guiConfig.getTitle(
            "contract-list.title",
            "<blue>Contract List",
            "page" to (currentPage + 1).toString(),
            "max_pages" to (maxPages + 1).toString()
        )
    }

    override fun populateInventory() {
        // This method populates the inventory AFTER it has been created with the correct title.
        val itemSlots = guiConfig.getSlots("contract-list.item_slots")

        contracts.forEachIndexed { index, contract ->
            if (index < itemSlots.size) {
                val slot = itemSlots[index]
                val item = guiConfig.getButtonItem(
                    "contract-list.contract_item",
                    "owner_name" to contract.ownerName,
                    "item_name" to contract.requestedItem.type.name,
                    "amount" to contract.requestedAmount.toString(),
                    "reward_preview" to contract.rewardMoney.toPlainString(), // Simplified
                    "time_left" to "N/A" // Placeholder
                )
                setItem(slot, item) {
                    // TODO: Open contract detail view
                }
            }
        }

        // Navigation buttons
        if (currentPage > 0) {
            val prevButton = guiConfig.getButton("contract-list.buttons.previous_page")
            setItem(prevButton.slot, prevButton.item) {
                ContractListGui(plugin, player, guiConfig, contractService, currentPage - 1).open()
            }
        }
        if (currentPage < maxPages) {
            val nextButton = guiConfig.getButton("contract-list.buttons.next_page")
            setItem(nextButton.slot, nextButton.item) {
                ContractListGui(plugin, player, guiConfig, contractService, currentPage + 1).open()
            }
        }

        val backButton = guiConfig.getButton("contract-list.buttons.back")
        setItem(backButton.slot, backButton.item) {
            MainMenuGui(plugin, player, guiConfig).open()
        }

        fill(guiConfig.getFillerItem())
    }
}
