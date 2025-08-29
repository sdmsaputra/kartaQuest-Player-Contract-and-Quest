package com.minekarta.karta.playercontract.domain

import java.time.Instant
import java.util.UUID

/**
 * Represents a single entry in a player's or contract's history log.
 *
 * @property id The unique identifier for this history entry.
 * @property type The type of event that occurred.
 * @property playerUUID The primary player associated with this event (e.g., the one who accepted, delivered, etc.).
 * @property contractId The ID of the contract this event relates to.
 * @property timestamp The exact time the event occurred.
 * @property metadata Additional, semi-structured data about the event, stored as JSON.
 *                  For example, for a REJECT event, this could contain the reason.
 *                  For a COMPLETE event, it could contain the final payout amount.
 */
data class HistoryEntry(
    val id: UUID,
    val type: HistoryEventType,
    val playerUUID: UUID,
    val contractId: UUID,
    val timestamp: Instant,
    val metadata: String // JSON string for flexibility
)

/**
 * Defines the types of events that can be recorded in the history.
 */
enum class HistoryEventType {
    CREATED,    // Contract was created by an issuer.
    ACCEPTED,   // Contract was accepted by a contractor.
    DELIVERED,  // Contractor delivered the items.
    COMPLETED,  // Issuer accepted the delivery, contract is complete.
    EXPIRED,    // Contract expired due to time limit.
    CANCELLED,  // Contract was cancelled by an admin.
    ABANDONED,  // Contractor abandoned the contract.
    REJECTED,   // Issuer rejected the delivery.
    PAID_OUT    // A payout was processed.
}
