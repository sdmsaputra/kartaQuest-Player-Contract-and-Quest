package com.minekarta.karta.playercontract.gui

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.service.PlayerStatsService
import org.bukkit.entity.Player

class StatsGui(
    plugin: KartaPlayerContract,
    player: Player,
    private val guiConfig: GuiConfigManager,
    private val statsService: PlayerStatsService
) : BaseGui(
    plugin,
    player,
    guiConfig.getSize("stats.size", 27),
    guiConfig.getTitle("stats.title", "<green>Player Statistics")
) {

    override fun initializeItems() {
        statsService.getStats(player.uniqueId).thenAcceptAsync { stats ->
            val successRate = if (stats.contractsCompleted > 0) {
                (stats.contractsCompleted.toDouble() / (stats.contractsCompleted + stats.contractsFailed)) * 100
            } else {
                0.0
            }

            val totalCompletedItem = guiConfig.getButtonItem("stats.items.total-completed", "value" to stats.contractsCompleted.toString())
            setItem(guiConfig.getButtonSlot("stats.items.total-completed"), totalCompletedItem)

            val successRateItem = guiConfig.getButtonItem("stats.items.success-rate", "value" to String.format("%.2f", successRate))
            setItem(guiConfig.getButtonSlot("stats.items.success-rate"), successRateItem)

            val totalEarnedItem = guiConfig.getButtonItem("stats.items.total-earned", "value" to stats.totalEarned.toString())
            setItem(guiConfig.getButtonSlot("stats.items.total-earned"), totalEarnedItem)

            fill(guiConfig.getFillerItem())
        }
    }
}
