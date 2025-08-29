package com.minekarta.karta.playercontract.persistence

import com.minekarta.karta.playercontract.domain.DeliveryPackage
import com.minekarta.karta.playercontract.domain.DeliveryStatus
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * An interface for data access operations related to Delivery Packages.
 */
interface DeliveryPackageRepository {

    /**
     * Finds a delivery package by its unique ID.
     */
    fun findById(id: UUID): CompletableFuture<DeliveryPackage?>

    /**
     * Finds all delivery packages for a given contract.
     */
    fun findByContractId(contractId: UUID): CompletableFuture<List<DeliveryPackage>>

    /**
     * Finds all packages with a given status belonging to a specific issuer (reviewer).
     */
    fun findByIssuerAndStatus(issuerUUID: UUID, status: DeliveryStatus): CompletableFuture<List<DeliveryPackage>>

    /**
     * Saves a new delivery package or updates an existing one.
     */
    fun save(deliveryPackage: DeliveryPackage): CompletableFuture<Void>
}
