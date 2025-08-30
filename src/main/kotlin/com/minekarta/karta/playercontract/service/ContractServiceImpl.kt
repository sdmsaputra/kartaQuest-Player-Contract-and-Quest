package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.domain.ContractState
import com.minekarta.karta.playercontract.events.*
import com.minekarta.karta.playercontract.persistence.ContractRepository
import com.minekarta.karta.playercontract.util.FoliaScheduler
import com.minekarta.karta.playercontract.util.Result
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

class ContractServiceImpl(
    private val contractRepository: ContractRepository,
    private val escrowService: EscrowService,
    private val scheduler: FoliaScheduler
) : ContractService {

    override fun createContract(contract: Contract): CompletableFuture<Result<Contract, Error>> {
        val player = Bukkit.getPlayer(contract.issuerUUID)
        if (player == null) {
            return CompletableFuture.completedFuture(Result.failure(Error("Player must be online to create a contract.")))
        }

        val event = PlayerContractCreatedEvent(contract.id, contract.issuerUUID)
        return scheduler.runOnMainThread(player) {
            Bukkit.getPluginManager().callEvent(event)
        }.thenComposeAsync {
            if (event.isCancelled) {
                return@thenComposeAsync CompletableFuture.completedFuture(
                    Result.failure(Error("Contract creation cancelled by another plugin."))
                )
            }

            val escrowFuture: CompletableFuture<Boolean> = when {
                contract.reward.money != null && contract.reward.money > 0 ->
                    escrowService.holdMoney(contract.issuerUUID, contract.reward.money)
                contract.reward.items != null && contract.reward.items.isNotEmpty() ->
                    escrowService.holdItems(contract.issuerUUID, contract.id, contract.reward.items)
                else -> CompletableFuture.completedFuture(true) // No reward
            }

            escrowFuture.thenCompose { success ->
                if (success) {
                    contractRepository.save(contract)
                        .thenApply<Result<Contract, Error>> { Result.success(contract) }
                } else {
                    CompletableFuture.completedFuture(
                        Result.failure(Error("Failed to secure contract reward in escrow."))
                    )
                }
            }
        }.exceptionally { ex ->
            Result.failure(Error("An unexpected error occurred: ${ex.message}"))
        }
    }

    override fun listOpenContracts(page: Int, pageSize: Int): CompletableFuture<List<Contract>> {
        return contractRepository.findByState(ContractState.AVAILABLE, page, pageSize)
    }

    override fun countOpenContracts(): CompletableFuture<Int> {
        return contractRepository.countByState(ContractState.AVAILABLE)
    }

    override fun getContract(id: UUID): CompletableFuture<Contract?> {
        return contractRepository.findById(id)
    }

    override fun takeContract(playerUUID: UUID, contractId: UUID): CompletableFuture<Result<Unit, Error>> {
        return contractRepository.findById(contractId).thenCompose { contract ->
            if (contract == null) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Contract not found.")))
            }
            if (contract.state != ContractState.AVAILABLE) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Contract is not available.")))
            }
            if (contract.issuerUUID == playerUUID) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("You cannot take your own contract.")))
            }

            contract.contractorUUID = playerUUID
            contract.state = ContractState.IN_PROGRESS
            contract.updatedAt = Instant.now()

            contractRepository.save(contract).thenApply {
                val player = Bukkit.getPlayer(playerUUID)
                if (player != null) {
                    scheduler.runOnMainThread(player) {
                        Bukkit.getPluginManager().callEvent(PlayerContractTakenEvent(contract.id, playerUUID))
                    }
                }
                Result.success(Unit)
            }
        }
    }

    override fun cancelContract(ownerUUID: UUID, contractId: UUID): CompletableFuture<Result<Unit, Error>> {
        return contractRepository.findById(contractId).thenCompose { contract ->
            if (contract == null) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Contract not found.")))
            }
            if (contract.issuerUUID != ownerUUID) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("You are not the owner of this contract.")))
            }
            if (contract.state != ContractState.AVAILABLE) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Only available contracts can be cancelled.")))
            }

            val refundFuture = when {
                contract.reward.money != null && contract.reward.money > 0 ->
                    escrowService.releaseMoney(contract.issuerUUID, contract.reward.money)
                contract.reward.items != null && contract.reward.items.isNotEmpty() ->
                    escrowService.returnItems(contract.id, contract.issuerUUID)
                else -> CompletableFuture.completedFuture(true)
            }

            refundFuture.thenCompose { success ->
                if (!success) {
                    return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Failed to refund escrow.")))
                }
                contract.state = ContractState.CANCELLED
                contract.updatedAt = Instant.now()
                contractRepository.save(contract).thenApply {
                    val player = Bukkit.getPlayer(ownerUUID)
                    if (player != null) {
                        scheduler.runOnMainThread(player) {
                            Bukkit.getPluginManager().callEvent(PlayerContractCancelledEvent(contract.id, ownerUUID))
                        }
                    }
                    Result.success(Unit)
                }
            }
        }
    }

    override fun completeContract(contractId: UUID): CompletableFuture<Result<Unit, Error>> {
        return contractRepository.findById(contractId).thenCompose { contract ->
            val contractorId = contract?.contractorUUID
            if (contract == null || contractorId == null) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Contract not found or not taken.")))
            }
            if (contract.state != ContractState.DELIVERED) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Contract items have not been fully delivered yet.")))
            }

            val rewardFuture = when {
                contract.reward.money != null && contract.reward.money > 0 ->
                    escrowService.releaseMoney(contractorId, contract.reward.money)
                contract.reward.items != null && contract.reward.items.isNotEmpty() ->
                    escrowService.releaseItems(contract.id, contractorId)
                else -> CompletableFuture.completedFuture(true)
            }

            rewardFuture.thenCompose { success ->
                if (!success) {
                    return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Failed to release reward from escrow.")))
                }
                contract.state = ContractState.COMPLETED
                contract.updatedAt = Instant.now()
                contractRepository.save(contract).thenApply {
                    val player = Bukkit.getPlayer(contract.issuerUUID)
                    if (player != null) {
                        scheduler.runOnMainThread(player) {
                            Bukkit.getPluginManager().callEvent(PlayerContractAcceptedEvent(contract.id, contract.issuerUUID, contractorId))
                        }
                    }
                    Result.success(Unit)
                }
            }
        }
    }

    override fun deliverItems(contractId: UUID, contractorUUID: UUID, items: List<ItemStack>): CompletableFuture<Result<Unit, Error>> {
        return contractRepository.findById(contractId).thenCompose { contract ->
            if (contract == null) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("Contract not found.")))
            }
            if (contract.contractorUUID != contractorUUID) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("You are not the contractor for this contract.")))
            }
            if (contract.state != ContractState.IN_PROGRESS) {
                return@thenCompose CompletableFuture.completedFuture(Result.failure(Error("This contract is not in progress.")))
            }

            // This is a simplified approach. A robust solution needs to handle item metadata.
            val newProgress = contract.deliveryProgress.toMutableMap()
            items.forEach { item ->
                val key = item.type.name // Simplified key
                newProgress[key] = newProgress.getOrDefault(key, 0) + item.amount
            }

            val requirementsMet = contract.requirements.all { requiredItem ->
                val key = requiredItem.type.name // Simplified key
                newProgress.getOrDefault(key, 0) >= requiredItem.amount
            }

            if (requirementsMet) {
                contract.state = ContractState.DELIVERED
            }
            contract.updatedAt = Instant.now()
            contract.deliveryProgress = newProgress

            contractRepository.save(contract).thenApply {
                val player = Bukkit.getPlayer(contractorUUID)
                if (player != null) {
                    scheduler.runOnMainThread(player) {
                        Bukkit.getPluginManager().callEvent(PlayerContractDeliveredEvent(contract.id, contractorUUID))
                    }
                }
                Result.success(Unit)
            }
        }
    }

    override fun expireContracts(): CompletableFuture<Int> {
        val now = Instant.now()
        val findAvailable = contractRepository.findByState(ContractState.AVAILABLE)
        val findInProgress = contractRepository.findByState(ContractState.IN_PROGRESS)

        return findAvailable.thenCombine(findInProgress) { available, inProgress ->
            available + inProgress
        }.thenCompose { contracts ->
            val expiredContracts = contracts.filter {
                it.deadline != null && it.deadline.isBefore(now)
            }

            if (expiredContracts.isEmpty()) {
                return@thenCompose CompletableFuture.completedFuture(0)
            }

            val expirationFutures = expiredContracts.map { contract ->
                contract.state = ContractState.EXPIRED
                contract.updatedAt = now

                val refundFuture: CompletableFuture<*> = when {
                    contract.reward.money != null && contract.reward.money > 0 ->
                        // Assuming releaseMoney can be used to refund the issuer
                        escrowService.releaseMoney(contract.issuerUUID, contract.reward.money)
                    contract.reward.items != null && contract.reward.items.isNotEmpty() ->
                        escrowService.returnItems(contract.id, contract.issuerUUID)
                    else -> CompletableFuture.completedFuture(true) // No reward to refund
                }

                refundFuture.thenCompose {
                    contractRepository.save(contract)
                }
            }

            CompletableFuture.allOf(*expirationFutures.toTypedArray())
                .thenApply { expiredContracts.size }
        }
    }
}
