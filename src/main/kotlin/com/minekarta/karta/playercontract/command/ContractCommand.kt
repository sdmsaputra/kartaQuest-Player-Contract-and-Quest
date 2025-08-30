package com.minekarta.karta.playercontract.command

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.MessageManager
import com.minekarta.karta.playercontract.gui.*
import com.minekarta.karta.playercontract.util.Result
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.util.UUID

class ContractCommand(
    private val plugin: KartaPlayerContract,
    private val messageManager: MessageManager,
    private val wizardManager: CreateWizardManager
) : CommandExecutor, TabCompleter {

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
            "create" -> handleCreate(sender, args.drop(1))
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

    private fun handleCreate(player: Player, args: List<String>) {
        if (!player.hasPermission("karta.contract.create")) {
            player.sendMessage(messageManager.getPrefixedMessage("command.no-permission"))
            return
        }
        if (args.isEmpty()) {
            wizardManager.startWizard(player)
        } else {
            // TODO: Implement quick-create command parsing
            // e.g. /contract create item DIAMOND 10 reward money 500
            player.sendMessage(messageManager.getPrefixedMessage("command.not-implemented"))
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
        if (args.size == 1) {
            val subcommands = listOf("create", "list", "take", "deliver", "inbox", "history", "stats", "claims", "cancel")
            val completions = StringUtil.copyPartialMatches(args[0], subcommands, mutableListOf())
            if (sender.hasPermission("karta.contract.admin")) {
                completions.add("reload")
            }
            return completions
        }

        if (args.size == 2) {
            when (args[0].lowercase()) {
                "take", "deliver", "cancel" -> {
                    // TODO: Suggest active contract IDs
                    return listOf("[contract_id]")
                }
            }
        }

        if (args.size > 1 && args[0].equals("create", ignoreCase = true)) {
             // /contract create item <MATERIAL> <amount> reward <money|item> <value>
            return when(args.size) {
                2 -> StringUtil.copyPartialMatches(args[1], listOf("item"), mutableListOf())
                3 -> StringUtil.copyPartialMatches(args[2], Material.entries.map { it.name }, mutableListOf())
                5 -> StringUtil.copyPartialMatches(args[4], listOf("reward"), mutableListOf())
                6 -> StringUtil.copyPartialMatches(args[5], listOf("money", "item"), mutableListOf())
                else -> emptyList()
            }
        }

        return emptyList()
    }
}
