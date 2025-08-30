package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.util.FoliaScheduler
import net.milkbowl.vault.economy.Economy
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture

class EscrowServiceImpl(
    private val plugin: KartaPlayerContract,
    private val scheduler: FoliaScheduler
    // private val escrowRepository: EscrowRepository // TODO: Create and inject a repository for item persistence
) : EscrowService {

    private val economy: Economy? = plugin.server.servicesManager.getRegistration(Economy::class.java)?.provider

    override fun holdMoney(ownerUUID: UUID, amount: Double): CompletableFuture<Boolean> {
        return scheduler.supplyAsync {
            if (economy == null) {
                plugin.logger.severe("Vault not found, but a contract required a money reward.")
                return@supplyAsync false
            }
            val player = plugin.server.getOfflinePlayer(ownerUUID)
            if (!economy.has(player, amount)) {
                return@supplyAsync false // Not enough money
            }
            val result = economy.withdrawPlayer(player, amount)
            result.transactionSuccess()
        }
    }

    override fun releaseMoney(toUUID: UUID, amount: Double): CompletableFuture<Boolean> {
        return scheduler.supplyAsync {
            if (economy == null) return@supplyAsync false
            val player = plugin.server.getOfflinePlayer(toUUID)
            val result = economy.depositPlayer(player, amount)
            result.transactionSuccess()
        }
    }

    override fun holdItems(ownerUUID: UUID, contractId: UUID, items: List<ItemStack>): CompletableFuture<Boolean> {
        val player = plugin.server.getPlayer(ownerUUID)
            ?: return CompletableFuture.completedFuture(false) // Player must be online to take items

        // The inventory modification MUST happen on the main thread.
        val removalFuture = CompletableFuture<Boolean>()
        scheduler.runOnMainThread(player) {
            var hasAllItems = true
            val itemMap = items.groupingBy { it.type }.fold(0) { acc, item -> acc + item.amount }

            for ((type, amount) in itemMap) {
                if (!player.inventory.contains(type, amount)) {
                    hasAllItems = false
                    break
                }
            }

            if (hasAllItems) {
                items.forEach { player.inventory.removeItem(it) }
                removalFuture.complete(true)
            } else {
                removalFuture.complete(false)
            }
        }

        return removalFuture.thenComposeAsync { success ->
            if (success) {
                // Now, persist the items asynchronously.
                // TODO: Uncomment when EscrowRepository is created
                // escrowRepository.storeItems(contractId, items)
                CompletableFuture.completedFuture(true) // Placeholder
            } else {
                CompletableFuture.completedFuture(false)
            }
        }
    }

    override fun returnItems(contractId: UUID, toUUID: UUID): CompletableFuture<Boolean> {
        // TODO: Implement logic to retrieve items from DB and give to player or claim box
        return CompletableFuture.completedFuture(true)
    }

    override fun releaseItems(contractId: UUID, toUUID: UUID): CompletableFuture<Boolean> {
        // TODO: Implement logic to retrieve items from DB and give to player or claim box
        return CompletableFuture.completedFuture(true)
    }
}
