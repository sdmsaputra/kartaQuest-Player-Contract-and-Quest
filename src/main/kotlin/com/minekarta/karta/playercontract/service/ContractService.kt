package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.domain.Contract
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Service layer for handling business logic related to contracts.
 */
interface ContractService {

    /**
     * A player attempts to accept a contract.
     * This involves checking limits, updating state, and notifying parties.
     * @param player The player accepting the contract.
     * @param contractId The ID of the contract to accept.
     * @return A CompletableFuture that completes with the updated Contract if successful, or an exception.
     */
    fun acceptContract(player: Player, contractId: UUID): CompletableFuture<Contract>

    /**
     * A player attempts to abandon a contract they are working on.
     * @param player The player abandoning the contract.
     * @param contractId The ID of the contract to abandon.
     * @return A CompletableFuture that completes when the operation is done.
     */
    fun abandonContract(player: Player, contractId: UUID): CompletableFuture<Void>

    /**
     * Periodically checks for and expires contracts that have passed their deadline.
     * This should be called by a scheduler.
     * @return A CompletableFuture that completes with the number of expired contracts.
     */
    fun expireContracts(): CompletableFuture<Int>

    // Other methods like createContract, deliverToContract, etc. will be added here.
}
