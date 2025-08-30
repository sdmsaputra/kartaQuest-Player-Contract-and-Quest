package com.minekarta.karta.playercontract.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

/**
 * Fired when a player creates a new contract.
 * Can be cancelled to prevent contract creation.
 */
class PlayerContractCreatedEvent(val contractId: UUID, val ownerUUID: UUID) : Event(), Cancellable {
    private var isCancelled = false

    override fun isCancelled(): Boolean = isCancelled
    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        val HANDLERS = HandlerList()
    }
}

/**
 * Fired when a player takes an open contract.
 */
class PlayerContractTakenEvent(val contractId: UUID, val takerUUID: UUID) : Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        val HANDLERS = HandlerList()
    }
}

/**
 * Fired when a contractor delivers items for a contract.
 */
class PlayerContractDeliveredEvent(val contractId: UUID, val contractorUUID: UUID) : Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        val HANDLERS = HandlerList()
    }
}

/**
 * Fired when an issuer accepts a delivery, completing the contract.
 */
class PlayerContractAcceptedEvent(val contractId: UUID, val ownerUUID: UUID, val contractorUUID: UUID) : Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        val HANDLERS = HandlerList()
    }
}

/**
 * Fired when a contract is cancelled by its owner.
 */
class PlayerContractCancelledEvent(val contractId: UUID, val ownerUUID: UUID) : Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        val HANDLERS = HandlerList()
    }
}
