package com.minekarta.karta.playercontract.persistence

import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.domain.ContractState
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * An interface for data access operations related to Contracts.
 * All operations are asynchronous and return a CompletableFuture to prevent blocking the server thread.
 */
interface ContractRepository {

    /**
     * Finds a contract by its unique ID.
     * @param id The UUID of the contract.
     * @return A CompletableFuture that will complete with the Contract, or null if not found.
     */
    fun findById(id: UUID): CompletableFuture<Contract?>

    /**
     * Retrieves all contracts currently in a specific state.
     * @param state The state to filter by.
     * @return A CompletableFuture that will complete with a list of contracts.
     */
    fun findByState(state: ContractState): CompletableFuture<List<Contract>>

    /**
     * Retrieves a paginated list of contracts in a specific state.
     * @param state The state to filter by.
     * @param page The page number (1-indexed).
     * @param pageSize The number of items per page.
     * @return A CompletableFuture that will complete with a list of contracts for the given page.
     */
    fun findByState(state: ContractState, page: Int, pageSize: Int): CompletableFuture<List<Contract>>

    /**
     * Counts the number of contracts in a specific state.
     * @param state The state to count.
     * @return A CompletableFuture that will complete with the count.
     */
    fun countByState(state: ContractState): CompletableFuture<Int>

    /**
     * Retrieves all contracts associated with a specific player, either as issuer or contractor.
     * @param playerUUID The UUID of the player.
     * @return A CompletableFuture that will complete with a list of contracts.
     */
    fun findByPlayer(playerUUID: UUID): CompletableFuture<List<Contract>>

    /**
     * Saves a new contract or updates an existing one.
     * @param contract The contract object to save.
     * @return A CompletableFuture that completes when the operation is finished.
     */
    fun save(contract: Contract): CompletableFuture<Void>

    /**
     * Deletes a contract by its unique ID.
     * @param id The UUID of the contract to delete.
     * @return A CompletableFuture that completes when the operation is finished.
     */
    fun delete(id: UUID): CompletableFuture<Void>

    /**
     * Retrieves all contracts from the database.
     * Primarily for admin or diagnostic purposes.
     * @return A CompletableFuture that will complete with a list of all contracts.
     */
    fun findAll(): CompletableFuture<List<Contract>>
}
