package com.minekarta.karta.playercontract.command

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.MessageManager
import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.gui.*
import com.minekarta.karta.playercontract.service.ContractService
import com.minekarta.karta.playercontract.util.Result
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.StringUtil
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

class ContractCommand(
    private val plugin: KartaPlayerContract,
    private val messageManager: MessageManager,
    private val contractService: ContractService
) : CommandExecutor, TabCompleter {

    private val nonSurvivalItems = setOf(
        Material.BEDROCK, Material.BARRIER, Material.LIGHT, Material.COMMAND_BLOCK,
        Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK, Material.STRUCTURE_BLOCK,
        Material.STRUCTURE_VOID, Material.JIGSAW, Material.DEBUG_STICK, Material.KNOWLEDGE_BOOK,
        Material.PLAYER_HEAD, Material.SPAWNER, Material.FARMLAND, Material.DIRT_PATH,
        Material.END_PORTAL_FRAME, Material.END_PORTAL, Material.NETHER_PORTAL,
        Material.ATTACHED_MELON_STEM, Material.ATTACHED_PUMPKIN_STEM, Material.PISTON_HEAD,
        Material.MOVING_PISTON, Material.PETRIFIED_OAK_SLAB
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }

        if (args.isEmpty()) {
            MainMenuGui(plugin, sender, plugin.guiConfigManager).open()
            return true
        }

        when (args[0].lowercase()) {
            "create" -> handleCreate(sender, args.drop(1).toTypedArray())
            "list" -> ContractListGui(plugin, sender, plugin.guiConfigManager, plugin.contractService).open()
            "take" -> handleTake(sender, args.getOrNull(1))
            "deliver" -> handleDeliver(sender, args.getOrNull(1))
            "inbox" -> DeliveryInboxGui(plugin, sender).open()
            "history" -> HistoryGui(plugin, sender, plugin.guiConfigManager, plugin.historyService).open()
            "stats" -> StatsGui(plugin, sender, plugin.guiConfigManager, plugin.playerStatsService).open()
            "claims" -> ClaimBoxGui(plugin, sender).open()
            "cancel" -> handleCancel(sender, args.getOrNull(1))
            "reload" -> handleReload(sender)
            else -> sender.sendMessage(messageManager.getPrefixedMessage("command.unknown-subcommand"))
        }

        return true
    }

    private fun handleCreate(player: Player, args: Array<String>) {
        if (!player.hasPermission("karta.contract.create")) {
            player.sendMessage(messageManager.getPrefixedMessage("command.no-permission"))
            return
        }

        if (args.size != 3) {
            player.sendMessage(messageManager.getPrefixedMessage("command.create-usage"))
            return
        }

        val materialName = args[0].uppercase()
        val material = Material.matchMaterial(materialName)
        if (material == null || !material.isItem || nonSurvivalItems.contains(material)) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-material", "material" to materialName))
            return
        }

        val amount = args[1].toIntOrNull()
        if (amount == null || amount <= 0) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-amount", "amount" to args[1]))
            return
        }

        val price = args[2].toBigDecimalOrNull()
        if (price == null || price <= BigDecimal.ZERO) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-price", "price" to args[2]))
            return
        }

        // Create a dummy item stack for serialization. We only care about the material type.
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
            state = com.minekarta.karta.playercontract.domain.ContractState.AVAILABLE
        )

        contractService.createContract(contract).whenComplete { result, error ->
            plugin.scheduler.runOnMainThread(player) {
                if (error != null) {
                    player.sendMessage(messageManager.getPrefixedMessage("command.generic-error"))
                    plugin.logger.warning("Error creating contract: ${error.message}")
                    return@runOnMainThread
                }
                when (result) {
                    is Result.Success -> player.sendMessage(messageManager.getPrefixedMessage("command.create-success", "id" to contract.id.toString()))
                    is Result.Failure -> player.sendMessage(messageManager.getPrefixedMessage("command.create-failure", "reason" to result.error.toString()))
                    null -> {}
                }
            }
        }
    }

    private fun handleTake(player: Player, contractIdStr: String?) {
        if (!player.hasPermission("karta.contract.take")) {
            player.sendMessage(messageManager.getPrefixedMessage("command.no-permission"))
            return
        }
        val contractId = try {
            UUID.fromString(contractIdStr)
        } catch (e: Exception) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-uuid", "uuid" to (contractIdStr ?: "")))
            return
        }

        plugin.contractService.takeContract(player.uniqueId, contractId).whenComplete { result, error ->
            plugin.scheduler.runOnMainThread(player) {
                if (error != null) {
                    player.sendMessage(messageManager.getPrefixedMessage("command.generic-error"))
                    plugin.logger.warning("Error taking contract: ${error.message}")
                    return@runOnMainThread
                }
                when (result) {
                    is Result.Success -> player.sendMessage(messageManager.getPrefixedMessage("command.take-success", "id" to contractId.toString()))
                    is Result.Failure -> player.sendMessage(messageManager.getPrefixedMessage("command.take-failure", "reason" to result.error.toString()))
                    null -> {}
                }
            }
        }
    }

    private fun handleDeliver(player: Player, contractIdStr: String?) {
        if (contractIdStr == null) {
            player.sendMessage(messageManager.getPrefixedMessage("command.id-required"))
            return
        }
        val contractId = try {
            UUID.fromString(contractIdStr)
        } catch (e: Exception) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-uuid", "uuid" to contractIdStr))
            return
        }
        DeliveryStagingGui(plugin, player, contractId).open()
    }

    private fun handleCancel(player: Player, contractIdStr: String?) {
         if (!player.hasPermission("karta.contract.manage")) {
            player.sendMessage(messageManager.getPrefixedMessage("command.no-permission"))
            return
        }
        val contractId = try {
            UUID.fromString(contractIdStr)
        } catch (e: Exception) {
            player.sendMessage(messageManager.getPrefixedMessage("command.invalid-uuid", "uuid" to (contractIdStr ?: "")))
            return
        }
        plugin.contractService.cancelContract(player.uniqueId, contractId).whenComplete { result, error ->
            plugin.scheduler.runOnMainThread(player) {
                if (error != null) {
                    player.sendMessage(messageManager.getPrefixedMessage("command.generic-error"))
                    plugin.logger.warning("Error cancelling contract: ${error.message}")
                    return@runOnMainThread
                }
                when (result) {
                    is Result.Success -> player.sendMessage(messageManager.getPrefixedMessage("command.cancel-success", "id" to contractId.toString()))
                    is Result.Failure -> player.sendMessage(messageManager.getPrefixedMessage("command.cancel-failure", "reason" to result.error.toString()))
                    null -> {}
                }
            }
        }
    }

    private fun handleReload(sender: Player) {
        if (!sender.hasPermission("karta.contract.admin")) {
            sender.sendMessage(messageManager.getPrefixedMessage("command.no-permission"))
            return
        }
        plugin.guiConfigManager.reload()
        plugin.messageManager.reload()
        sender.sendMessage(messageManager.getPrefixedMessage("command.reload-success"))
    }


    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) {
            val subcommands = listOf("create", "list", "take", "deliver", "inbox", "history", "stats", "claims", "cancel")
            StringUtil.copyPartialMatches(args[0], subcommands, completions)
            if (sender.hasPermission("karta.contract.admin")) {
                if ("reload".startsWith(args[0], ignoreCase = true)) {
                    completions.add("reload")
                }
            }
        } else if (args.size > 1 && args[0].equals("create", ignoreCase = true)) {
            when (args.size) {
                2 -> { // Suggesting item names
                    val itemNames = Material.entries
                        .filter { it.isItem && !nonSurvivalItems.contains(it) }
                        .map { it.name.lowercase() }
                    StringUtil.copyPartialMatches(args[1], itemNames, completions)
                }
                3 -> completions.add("<amount>") // Suggesting amount
                4 -> completions.add("<price>")  // Suggesting price
            }
        } else if (args.size == 2) {
            when (args[0].lowercase()) {
                "take", "deliver", "cancel" -> {
                    // TODO: Suggest active contract IDs
                    completions.add("[contract_id]")
                }
            }
        }

        return completions
    }
}
