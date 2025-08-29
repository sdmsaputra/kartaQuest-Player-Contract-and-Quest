package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.service.HistoryService
import org.bukkit.entity.Player

class HistoryGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val guiConfig: GuiConfigManager,
    private val historyService: HistoryService
) : BaseGui(
    plugin,
    player,
    guiConfig.getSize("history.size", 54),
    guiConfig.getTitle("history.title", "<yellow>Contract History")
) {

    private var currentPage = 0
    private val itemsPerPage = 28

    override fun initializeItems() {
        historyService.getHistory(player.uniqueId, currentPage, itemsPerPage).thenAcceptAsync { historyEntries ->
            historyEntries.forEachIndexed { index, entry ->
                val slot = (index / 7) * 9 + (index % 7) + 10
                val item = guiConfig.getButtonItem(
                    "history.history-item",
                    "contract_name" to entry.contractId.toString(), // Placeholder
                    "status" to entry.type.name,
                    "date_completed" to entry.timestamp.toString()
                )
                setItem(slot, item) // No click action for read-only
            }

            // Navigation
            if (currentPage > 0) {
                setItem(48, guiConfig.getButtonItem("history.navigation.previous-page")) {
                    currentPage--
                    refresh()
                }
            }
            // We don't know the total size, so we can only show next if we got a full page
            if (historyEntries.size == itemsPerPage) {
                setItem(50, guiConfig.getButtonItem("history.navigation.next-page")) {
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
