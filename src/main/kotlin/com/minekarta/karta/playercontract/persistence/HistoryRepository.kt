package com.minekarta.karta.playercontract.persistence

import com.minekarta.karta.playercontract.domain.HistoryEntry
import com.minekarta.karta.playercontract.domain.HistoryEventType
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * An interface for data access operations related to History Entries.
 */
interface HistoryRepository {

    /**
     * Adds a new entry to the history log.
     */
    fun add(entry: HistoryEntry): CompletableFuture<Void>

    /**
     * Retrieves all history entries for a specific player.
     * @param playerUUID The UUID of the player.
     * @param limit The maximum number of entries to return.
     * @param offset The number of entries to skip (for pagination).
     */
    fun findByPlayer(playerUUID: UUID, limit: Int, offset: Int): CompletableFuture<List<HistoryEntry>>

    /**
     * Retrieves history entries for a player, filtered by event type.
     */
    fun findByPlayerAndType(playerUUID: UUID, type: HistoryEventType, limit: Int, offset: Int): CompletableFuture<List<HistoryEntry>>

    /**
     * Retrieves all history for a specific contract.
     */
    fun findByContract(contractId: UUID): CompletableFuture<List<HistoryEntry>>
}
