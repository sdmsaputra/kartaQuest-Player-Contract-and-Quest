package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.domain.ContractState
import com.minekarta.karta.playercontract.persistence.ContractRepository
import org.bukkit.entity.Player
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ContractServiceImpl(
    private val contractRepository: ContractRepository
    // private val notificationService: NotificationService // Will be added later
) : ContractService {

    override fun acceptContract(player: Player, contractId: UUID): CompletableFuture<Contract> {
        // TODO: Implement logic for accepting a contract.
        // 1. Fetch contract by ID.
        // 2. Check if it's AVAILABLE.
        // 3. Check if player is not the issuer.
        // 4. Check player's active contract limit.
        // 5. Update contract state to IN_PROGRESS, set contractorUUID.
        // 6. Save the updated contract.
        // 7. Fire ContractAcceptedEvent.
        // 8. Send notifications.
        TODO("Not yet implemented")
    }

    override fun abandonContract(player: Player, contractId: UUID): CompletableFuture<Void> {
        // TODO: Implement logic for abandoning a contract.
        TODO("Not yet implemented")
    }

    /**
     * This is the refactored, asynchronous version of the old expiration task.
     */
    override fun expireContracts(): CompletableFuture<Int> {
        val now = Instant.now()

        // Find all contracts that are currently in progress.
        val inProgressFuture = contractRepository.findByState(ContractState.IN_PROGRESS)
        // Also find contracts that are still available but have a deadline.
        val availableFuture = contractRepository.findByState(ContractState.AVAILABLE)

        return inProgressFuture.thenCombine(availableFuture) { inProgress, available ->
            val allCheckable = inProgress + available
            val expiredContracts = allCheckable.filter { contract ->
                contract.deadline != null && now.isAfter(contract.deadline)
            }

            if (expiredContracts.isEmpty()) {
                return@thenCombine 0
            }

            val saveFutures = expiredContracts.map { contract ->
                contract.state = ContractState.EXPIRED
                contract.updatedAt = now
                // In a real implementation, we'd also fire an event and send a notification here.
                contractRepository.save(contract)
            }

            // Wait for all save operations to complete.
            CompletableFuture.allOf(*saveFutures.toTypedArray()).join()

            return@thenCombine expiredContracts.size
        }
    }
}
