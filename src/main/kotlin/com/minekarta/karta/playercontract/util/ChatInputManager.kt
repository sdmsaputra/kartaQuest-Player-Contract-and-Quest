package com.minekarta.karta.playercontract.util

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

class ChatInputManager {
    private val inputCallbacks = ConcurrentHashMap<UUID, Consumer<String>>()

    fun requestInput(player: Player, callback: Consumer<String>) {
        inputCallbacks[player.uniqueId] = callback
    }

    fun handleInput(player: Player, message: String): Boolean {
        val callback = inputCallbacks.remove(player.uniqueId)
        return if (callback != null) {
            callback.accept(message)
            true // Indicate that the input was handled
        } else {
            false
        }
    }

    fun hasPendingInput(player: Player): Boolean {
        return inputCallbacks.containsKey(player.uniqueId)
    }
}
