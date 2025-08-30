package com.minekarta.karta.playercontract.listeners

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.util.ChatInputManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerChatListener(private val plugin: KartaPlayerContract) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        if (plugin.chatInputManager.hasPendingInput(player)) {
            // This player has a pending chat input request.
            event.isCancelled = true

            // Extract plain text from the message component
            val message = PlainTextComponentSerializer.plainText().serialize(event.message())

            // Pass the message to the manager on the main server thread, as it will likely
            // interact with Bukkit API (sending messages, opening GUIs, etc.)
            plugin.scheduler.runOnMainThread(player) {
                plugin.chatInputManager.handleInput(player, message)
            }
        }
    }
}
