package com.minekarta.karta.playercontract.domain

import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

/**
 * Represents a package of items delivered by a contractor for a specific contract.
 * This package is held in a "limbo" state until it is reviewed by the issuer.
 *
 * @property id The unique identifier for this delivery.
 * @property contractId The ID of the contract this delivery is for.
 * @property contractorUUID The UUID of the player who made the delivery.
 * @property contents The list of items that were delivered.
 * @property deliveredAt The timestamp when the delivery was made.
 * @property status The current review status of the delivery.
 * @property reviewedAt The timestamp when the review was completed. Null if pending.
 * @property reviewerUUID The UUID of the issuer who reviewed the delivery. Null if pending.
 * @property rejectReason A reason provided by the issuer if the delivery was rejected. Null if accepted or pending.
 */
data class DeliveryPackage(
    val id: UUID,
    val contractId: UUID,
    val contractorUUID: UUID,
    val contents: List<ItemStack>, // Like in Contract, relies on ItemStack serialization
    val deliveredAt: Instant,
    var status: DeliveryStatus,
    var reviewedAt: Instant?,
    var reviewerUUID: UUID?,
    var rejectReason: String?
)

/**
 * Represents the review status of a delivery package.
 */
enum class DeliveryStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
