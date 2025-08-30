package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.domain.ContractState
import com.minekarta.karta.playercontract.domain.Reward
import com.minekarta.karta.playercontract.events.*
import com.minekarta.karta.playercontract.persistence.ContractRepository
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

import com.minekarta.karta.playercontract.util.FoliaScheduler

class ContractServiceImpl(
    private val contractRepository: ContractRepository,
    private val escrowService: EscrowService,
    private val scheduler: FoliaScheduler
) : ContractService {

    override fun createContract(contract: Contract): CompletableFuture<Contract> {
        // Run the whole creation process asynchronously
    override fun createContract(contract: Contract): CompletableFuture<Contract> {
        val player = Bukkit.getPlayer(contract.issuerUUID)
            ?: return CompletableFuture.failedFuture(IllegalStateException("Player must be online to create a contract."))

        // 1. Fire the event on the main thread and get the result.
        val event = PlayerContractCreatedEvent(contract.id, contract.issuerUUID)
        return scheduler.runOnMainThread(player) {
            Bukkit.getPluginManager().callEvent(event)
        }.thenComposeAsync { // This block runs async after the event is fired
            if (event.isCancelled) {
                return@thenComposeAsync CompletableFuture.failedFuture<Contract>(
                    IllegalStateException("Contract creation cancelled by another plugin.")
                )
            }

            // 2. Hold the reward in escrow. This is already an async operation.
            val escrowFuture: CompletableFuture<Boolean> = when {
                contract.reward.money != null && contract.reward.money > 0 ->
                    escrowService.holdMoney(contract.issuerUUID, contract.reward.money)
                contract.reward.items != null && contract.reward.items.isNotEmpty() ->
                    escrowService.holdItems(contract.issuerUUID, contract.id, contract.reward.items)
                else -> CompletableFuture.completedFuture(true) // No reward
            }

            val escrowSuccess = escrowFuture.join() // We are in an async block, so .join() is safe here.
            if (!escrowSuccess) {
                throw IllegalStateException("Failed to secure contract reward in escrow.")
            }

            // 3. Save contract to database (already returns a future)
            contractRepository.save(contract).thenApply { contract }
        }.thenCompose { it } // Flatten the CompletableFuture<CompletableFuture<Contract>>
    }

    override fun listOpenContracts(page: Int, pageSize: Int): CompletableFuture<List<Contract>> {
        // TODO: Implement pagination in the repository layer
        return contractRepository.findByState(ContractState.AVAILABLE)
    }

    override fun getContract(id: UUID): CompletableFuture<Contract?> {
        return contractRepository.findById(id)
    }

    override fun takeContract(playerUUID: UUID, contractId: UUID): CompletableFuture<Result<Unit, Error>> {
        return CompletableFuture.supplyAsync {
            // TODO: Implement atomic operation in repository
            val contract = contractRepository.findById(contractId).join()
                ?: return@supplyAsync Result.failure(Error("Contract not found."))

            if (contract.state != ContractState.AVAILABLE) {
                return@supplyAsync Result.failure(Error("Contract is not available."))
            }
            if (contract.issuerUUID == playerUUID) {
                return@supplyAsync Result.failure(Error("You cannot take your own contract."))
            }

            contract.contractorUUID = playerUUID
            contract.state = ContractState.IN_PROGRESS
            contract.updatedAt = Instant.now()
            contractRepository.save(contract).join()

            Bukkit.getPluginManager().callEvent(PlayerContractTakenEvent(contractId, playerUUID))
            Result.success(Unit)
        }
    }

    override fun cancelContract(ownerUUID: UUID, contractId: UUID): CompletableFuture<Result<Unit, Error>> {
        // TODO: Implement cancellation logic
        // 1. Get contract, check owner and state
        // 2. Return funds/items from escrow
        // 3. Update contract state to CANCELLED
        // 4. Fire event
        TODO("Not yet implemented")
    }

    override fun completeContract(contractId: UUID): CompletableFuture<Result<Unit, Error>> {
        // TODO: Implement completion logic
        // 1. Get contract, check state (should be DELIVERED)
        // 2. Release reward from escrow to contractor
        // 3. Update contract state to COMPLETED
        // 4. Fire event
        TODO("Not yet implemented")
    }

    override fun deliverItems(contractId: UUID, contractorUUID: UUID, items: List<ItemStack>): CompletableFuture<Result<Unit, Error>> {
        // TODO: Implement delivery logic
        // 1. Get contract, check contractor and state
        // 2. Validate delivered items against requirements
        // 3. Update deliveryProgress map
        // 4. If all requirements met, change state to DELIVERED
        // 5. Fire event
        TODO("Not yet implemented")
    }

    override fun expireContracts(): CompletableFuture<Int> {
        // This logic was already partially implemented, can be reused/refined.
        TODO("Not yet implemented")
    }
}
