package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.util.Result
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Service layer for handling business logic related to contracts.
 */
interface ContractService {

    /**
     * Creates a new contract and holds the reward in escrow.
     * @param contract The contract object to create.
     * @return A future that completes with a Result containing the created contract or an error.
     */
    fun createContract(contract: Contract): CompletableFuture<Result<Contract, Error>>

    /**
     * Retrieves a paginated list of open contracts.
     * @param page The page number to retrieve.
     * @param pageSize The number of contracts per page.
     * @return A future that completes with a list of open contracts for the given page.
     */
    fun listOpenContracts(page: Int, pageSize: Int): CompletableFuture<List<Contract>>

    /**
     * Counts the total number of open contracts.
     * @return A future that completes with the total count.
     */
    fun countOpenContracts(): CompletableFuture<Int>

    /**
     * Gets a single contract by its unique ID.
     * @param id The UUID of the contract.
     * @return A future that completes with the Contract, or null if not found.
     */
    fun getContract(id: UUID): CompletableFuture<Contract?>

    /**
     * Allows a player to take an open contract.
     * @param playerUUID The UUID of the player taking the contract.
     * @param contractId The ID of the contract to take.
     * @return A future that completes with a Result indicating success or an Error.
     */
    fun takeContract(playerUUID: UUID, contractId: UUID): CompletableFuture<Result<Unit, Error>>

    /**
     * Allows the owner to cancel a contract they created.
     * @param ownerUUID The UUID of the contract owner.
     * @param contractId The ID of the contract to cancel.
     * @return A future that completes with a Result indicating success or an Error.
     */
    fun cancelContract(ownerUUID: UUID, contractId: UUID): CompletableFuture<Result<Unit, Error>>

    /**
     * Marks a contract as successfully completed and triggers reward distribution.
     * @param contractId The ID of the contract to complete.
     * @return A future that completes with a Result indicating success or an Error.
     */
    fun completeContract(contractId: UUID): CompletableFuture<Result<Unit, Error>>

    /**
     * Allows a contractor to deliver items for a contract they have taken.
     * @param contractId The ID of the contract.
     * @param contractorUUID The UUID of the player delivering the items.
     * @param items The items being delivered.
     * @return A future that completes with a Result indicating the delivery status or an Error.
     */
    fun deliverItems(contractId: UUID, contractorUUID: UUID, items: List<ItemStack>): CompletableFuture<Result<Unit, Error>> // Simplified result for now

    /**
     * Periodically checks for and expires contracts that have passed their deadline.
     * @return A future that completes with the number of expired contracts.
     */
    fun expireContracts(): CompletableFuture<Int>
}
