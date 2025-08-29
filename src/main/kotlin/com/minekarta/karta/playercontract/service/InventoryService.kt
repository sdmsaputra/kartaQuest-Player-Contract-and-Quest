package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.domain.DeliveryPackage
import com.minekarta.karta.playercontract.domain.DeliveryStatus
import com.minekarta.karta.playercontract.persistence.DeliveryPackageRepository
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface InventoryService {
    fun getClaimablePackages(playerId: UUID): CompletableFuture<List<DeliveryPackage>>
    fun claimPackage(player: Player, packageId: UUID): CompletableFuture<Boolean>
}

class InventoryServiceImpl(
    private val deliveryPackageRepository: DeliveryPackageRepository
) : InventoryService {

    override fun getClaimablePackages(playerId: UUID): CompletableFuture<List<DeliveryPackage>> {
        return deliveryPackageRepository.findByIssuerAndStatus(playerId, DeliveryStatus.PENDING)
    }

    override fun claimPackage(player: Player, packageId: UUID): CompletableFuture<Boolean> {
        return deliveryPackageRepository.findById(packageId).thenCompose { deliveryPackage ->
            if (deliveryPackage == null || deliveryPackage.reviewerUUID != player.uniqueId) {
                return@thenCompose CompletableFuture.completedFuture(false)
            }

            // Attempt to add items to inventory
            val remaining = player.inventory.addItem(*deliveryPackage.contents.toTypedArray())

            if (remaining.isNotEmpty()) {
                // Not all items could be added
                // Drop the items that couldn't be added
                remaining.values.forEach { item ->
                    player.world.dropItem(player.location, item)
                }
            }

            // Update status and save
            val updatedPackage = deliveryPackage.copy(status = DeliveryStatus.ACCEPTED) // Should be something like CLAIMED
            deliveryPackageRepository.save(updatedPackage).thenApply { true }
        }
    }
}
