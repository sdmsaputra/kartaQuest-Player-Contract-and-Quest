package com.minekarta.karta.playercontract.util

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.MessageManager
import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.domain.ContractState
import com.minekarta.karta.playercontract.gui.MainMenuGui
import com.minekarta.karta.playercontract.service.ContractService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class ChatInputManager(
    private val plugin: KartaPlayerContract,
    private val messageManager: MessageManager,
    private val contractService: ContractService
) {
    private val conversations = ConcurrentHashMap<UUID, ConversationContext>()

    private enum class ConversationState {
        AWAITING_ITEM_NAME,
        AWAITING_AMOUNT,
        AWAITING_PRICE
    }

    private data class ConversationContext(
        var state: ConversationState,
        var material: Material? = null,
        var amount: Int? = null
    )

    private val nonSurvivalItems = setOf(
        Material.BEDROCK, Material.BARRIER, Material.LIGHT, Material.COMMAND_BLOCK,
        Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK, Material.STRUCTURE_BLOCK,
        Material.STRUCTURE_VOID, Material.JIGSAW, Material.DEBUG_STICK, Material.KNOWLEDGE_BOOK,
        Material.PLAYER_HEAD, Material.SPAWNER, Material.FARMLAND, Material.DIRT_PATH,
        Material.END_PORTAL_FRAME, Material.END_PORTAL, Material.NETHER_PORTAL,
        Material.ATTACHED_MELON_STEM, Material.ATTACHED_PUMPKIN_STEM, Material.PISTON_HEAD,
        Material.MOVING_PISTON, Material.PETRIFIED_OAK_SLAB
    )

    fun startContractCreation(player: Player) {
        conversations[player.uniqueId] = ConversationContext(ConversationState.AWAITING_ITEM_NAME)
        player.sendMessage(messageManager.getPrefixedMessage("chat_input.prompt_item_name"))
    }

    fun cancelConversation(player: Player) {
        if (conversations.remove(player.uniqueId) != null) {
            player.sendMessage(messageManager.getPrefixedMessage("chat_input.cancelled"))
        }
    }

    fun handleInput(player: Player, message: String) {
        val context = conversations[player.uniqueId] ?: return

        when (context.state) {
            ConversationState.AWAITING_ITEM_NAME -> handleItemNameInput(player, context, message)
            ConversationState.AWAITING_AMOUNT -> handleAmountInput(player, context, message)
            ConversationState.AWAITING_PRICE -> handlePriceInput(player, context, message)
        }
    }

    private fun handleItemNameInput(player: Player, context: ConversationContext, message: String) {
        if (message.equals("cancel", ignoreCase = true)) {
            cancelConversation(player)
            return
        }
        val materialName = message.uppercase()
        val material = Material.matchMaterial(materialName)
        if (material == null || !material.isItem || nonSurvivalItems.contains(material)) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-material", "material" to materialName))
            player.sendMessage(messageManager.getPrefixedMessage("chat_input.prompt_item_name")) // Re-prompt
            return
        }
        context.material = material
        context.state = ConversationState.AWAITING_AMOUNT
        player.sendMessage(messageManager.getPrefixedMessage("chat_input.item_name_set", "item" to material.name.lowercase()))
        player.sendMessage(messageManager.getPrefixedMessage("chat_input.prompt_amount"))
    }

    private fun handleAmountInput(player: Player, context: ConversationContext, message: String) {
        if (message.equals("cancel", ignoreCase = true)) {
            cancelConversation(player)
            return
        }
        val amount = message.toIntOrNull()
        if (amount == null || amount <= 0) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-amount", "amount" to message))
            player.sendMessage(messageManager.getPrefixedMessage("chat_input.prompt_amount")) // Re-prompt
            return
        }
        context.amount = amount
        context.state = ConversationState.AWAITING_PRICE
        player.sendMessage(messageManager.getPrefixedMessage("chat_input.amount_set", "amount" to amount.toString()))
        player.sendMessage(messageManager.getPrefixedMessage("chat_input.prompt_price"))
    }

    private fun handlePriceInput(player: Player, context: ConversationContext, message: String) {
        if (message.equals("cancel", ignoreCase = true)) {
            cancelConversation(player)
            return
        }
        val price = message.toBigDecimalOrNull()
        if (price == null || price <= BigDecimal.ZERO) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-price", "price" to message))
            player.sendMessage(messageManager.getPrefixedMessage("chat_input.prompt_price")) // Re-prompt
            return
        }

        // We have all the data, let's create the contract
        val material = context.material!!
        val amount = context.amount!!

        val requestedItem = ItemStack(material)
        val contract = Contract(
            id = UUID.randomUUID(),
            ownerId = player.uniqueId,
            ownerName = player.name,
            requestedItem = requestedItem,
            requestedAmount = amount,
            rewardMoney = price,
            rewardItems = emptyList(), // No item rewards
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(TimeUnit.DAYS.toSeconds(plugin.config.getLong("contract_defaults.default_expiry_days", 7))),
            state = ContractState.AVAILABLE
        )

        // End the conversation
        conversations.remove(player.uniqueId)

        contractService.createContract(contract).whenComplete { result, error ->
            plugin.scheduler.runOnMainThread(player) {
                if (error != null) {
                    player.sendMessage(messageManager.getPrefixedMessage("command.generic-error"))
                    plugin.logger.warning("Error creating contract from chat: ${error.message}")
                } else {
                    when (result) {
                        is Result.Success -> player.sendMessage(messageManager.getPrefixedMessage("command.create-success", "id" to contract.id.toString()))
                        is Result.Failure -> player.sendMessage(messageManager.getPrefixedMessage("command.create-failure", "reason" to result.error.toString()))
                        null -> {}
                    }
                }
                // Re-open the main menu for the player
                MainMenuGui(plugin, player, plugin.guiConfigManager).open()
            }
        }
    }

    fun hasPendingInput(player: Player): Boolean {
        return conversations.containsKey(player.uniqueId)
    }
}
