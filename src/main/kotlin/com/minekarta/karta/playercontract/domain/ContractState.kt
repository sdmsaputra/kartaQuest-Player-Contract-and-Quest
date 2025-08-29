package com.minekarta.karta.playercontract.domain

/**
 * Represents the various states a contract can be in throughout its lifecycle.
 */
enum class ContractState {
    /**
     * The contract has been created and is available for players to accept.
     * It has an issuer but no contractor yet.
     */
    AVAILABLE,

    /**
     * The contract has been accepted by a player and is currently being worked on.
     */
    IN_PROGRESS,

    /**
     * The contractor has submitted the required items/proof for the contract.
     * The delivery is pending review from the issuer.
     */
    DELIVERED,

    /**
     * The issuer has accepted the delivery, the contractor has been paid,
     * and the contract is successfully finished.
     */
    COMPLETED,

    /**
     * The contract's deadline has passed before it could be completed.
     */
    EXPIRED,

    /**
     * The contract was cancelled by an admin or a system process.
     */
    CANCELLED,

    /**
     * The contract was abandoned by the contractor.
     */
    ABANDONED
}
