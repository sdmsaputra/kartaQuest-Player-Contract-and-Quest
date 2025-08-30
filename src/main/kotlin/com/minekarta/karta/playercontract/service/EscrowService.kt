package com.minekarta.karta.playercontract.service

import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Manages the holding and releasing of contract rewards.
 * This ensures that rewards are safely stored while a contract is active.
 */
interface EscrowService {

    /**
     * Holds a specified amount of money from a player's account for a contract.
     *
     * @param ownerUUID The UUID of the player providing the money.
     * @param amount The amount of money to hold.
     * @return A future that completes with true if the money was successfully held, false otherwise.
     */
    fun holdMoney(ownerUUID: UUID, amount: Double): CompletableFuture<Boolean>

    /**
     * Releases a specified amount of money to a player's account.
     *
     * @param toUUID The UUID of the player receiving the money.
     * @param amount The amount of money to release.
     * @return A future that completes with true if the money was successfully released, false otherwise.
     */
    fun releaseMoney(toUUID: UUID, amount: Double): CompletableFuture<Boolean>

    /**
     * Holds a list of items in a secure, persistent escrow.
     * These items are removed from the owner's inventory.
     *
     * @param ownerUUID The player providing the items.
     * @param contractId The ID of the contract the items are for.
     * @param items The list of ItemStacks to hold.
     * @return A future that completes with true if the items were successfully held.
     */
    fun holdItems(ownerUUID: UUID, contractId: UUID, items: List<ItemStack>): CompletableFuture<Boolean>

    /**
     * Returns escrowed items to a player, either directly to their inventory or a claim box.
     *
     * @param contractId The ID of the contract whose items are being returned.
     * @param toUUID The UUID of the player to return the items to.
     * @return A future that completes with true if the items were successfully returned or placed in a claim box.
     */
    fun returnItems(contractId: UUID, toUUID: UUID): CompletableFuture<Boolean>

    /**
     * Releases escrowed items to a player.
     *
     * @param contractId The ID of the contract whose reward items are being released.
     * @param toUUID The UUID of the player receiving the items.
     * @return A future that completes with true if the items were successfully given.
     */
    fun releaseItems(contractId: UUID, toUUID: UUID): CompletableFuture<Boolean>
}
