package com.minekarta.karta.playercontract.listeners

import com.minekarta.karta.playercontract.util.ChatInputManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerChatListener(private val chatInputManager: ChatInputManager) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        if (chatInputManager.hasPendingInput(player)) {
            // This player has a pending chat input request.
            event.isCancelled = true

            // Pass the message to the manager.
            // We need to do this on the main thread if the callback touches Bukkit API.
            val message = Component.textOfChildren(event.message()).content()
            chatInputManager.handleInput(player, message)
        }
    }
}
