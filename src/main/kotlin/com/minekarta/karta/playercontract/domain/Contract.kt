package com.minekarta.karta.playercontract.domain

import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

/**
 * Represents a single contract between two parties.
 *
 * @property id The unique identifier for this contract.
 * @property templateId An optional ID from a template this contract was created from.
 * @property title The user-facing title of the contract.
 * @property description A detailed description of the contract's objectives.
 * @property issuerUUID The UUID of the player who created the contract.
 * @property contractorUUID The UUID of the player who accepted the contract. Null if AVAILABLE.
 * @property requirements A list of items that must be submitted to complete the contract.
 * @property reward The reward given to the contractor upon completion.
 * @property deadline The time by which the contract must be completed. Null for no time limit.
 * @property state The current state of the contract (e.g., AVAILABLE, IN_PROGRESS).
 * @property createdAt The timestamp when the contract was created.
 * @property updatedAt The timestamp when the contract was last modified.
 */
data class Contract(
    val id: UUID,
    val templateId: String?,
    val title: String,
    val description: String,
    val issuerUUID: UUID,
    var contractorUUID: UUID?,
    val requirements: List<ItemStack>, // Using ItemStack directly for now, will need a robust spec class for serialization
    val reward: Reward,
    val deadline: Instant?,
    var state: ContractState,
    val createdAt: Instant,
    var updatedAt: Instant
)

/**
 * Defines the reward for completing a contract.
 *
 * @property money The amount of money (Vault economy) to be paid.
 * @property items A list of ItemStacks to be given as a reward.
 */
data class Reward(
    val money: Double?,
    val items: List<ItemStack>?
)

// Note: Storing raw ItemStacks is not ideal for long-term persistence across MC versions.
// A more robust solution would be an "ItemStackSpec" that stores material, amount, and NBT data as separate fields.
// For this implementation, we will rely on Bukkit's built-in ItemStack serialization,
// but this is a key area for future improvement.
