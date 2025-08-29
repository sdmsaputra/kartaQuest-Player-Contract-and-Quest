package com.minekarta.karta.playercontract.command

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.config.MessageManager
import com.minekarta.karta.playercontract.gui.*
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class ContractCommand(
    private val plugin: KartaPlayerContract,
    private val guiConfig: GuiConfigManager,
    private val messageManager: MessageManager
) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage(messageManager.getMessage("command.player-only"))
                return true
            }
            openMainMenu(sender)
            return true
        }

        val player = sender as? Player
        when (args[0].lowercase()) {
            "list" -> {
                if (player == null) {
                    sender.sendMessage(messageManager.getMessage("command.player-only"))
                    return true
                }
                ContractListGui(plugin, player, guiConfig, plugin.contractService).open()
            }
            "inventory" -> {
                if (player == null) {
                    sender.sendMessage(messageManager.getMessage("command.player-only"))
                    return true
                }
                // InventoryGui(plugin, player, guiConfig, plugin.inventoryService).open()
            }
            "history" -> {
                if (player == null) {
                    sender.sendMessage(messageManager.getMessage("command.player-only"))
                    return true
                }
                // HistoryGui(plugin, player, guiConfig, plugin.historyService).open()
            }
            "stats" -> {
                if (player == null) {
                    sender.sendMessage(messageManager.getMessage("command.player-only"))
                    return true
                }
                // StatsGui(plugin, player, guiConfig, plugin.statsService).open()
            }
            "reload" -> handleReload(sender)
            "open" -> handleOpen(sender, args)
            "help" -> showHelp(sender)
            else -> {
                sender.sendMessage(messageManager.getMessage("command.unknown-subcommand"))
                return true
            }
        }

        return true
    }

    private fun handleReload(sender: CommandSender) {
        if (!sender.hasPermission("karta.contract.admin.reload")) {
            sender.sendMessage(messageManager.getMessage("command.no-permission"))
            return
        }
        guiConfig.reload()
        messageManager.reload()
        sender.sendMessage(messageManager.getMessage("command.reload-success"))
    }

    private fun handleOpen(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("karta.contract.admin.open")) {
            sender.sendMessage(messageManager.getMessage("command.no-permission"))
            return
        }

        if (args.size < 2) {
            sender.sendMessage(messageManager.getMessage("command.open-usage"))
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage(messageManager.getMessage("command.player-not-found", "player" to args[1]))
            return
        }

        openMainMenu(targetPlayer)
        sender.sendMessage(messageManager.getMessage("command.open-success", "player" to targetPlayer.name))
    }

    private fun showHelp(sender: CommandSender) {
        messageManager.getPrefixedMessage("command.help-header").let(sender::sendMessage)
        messageManager.getPrefixedMessage("command.help-base").let(sender::sendMessage)
        if (sender.hasPermission("karta.contract.list")) {
            messageManager.getPrefixedMessage("command.help-list").let(sender::sendMessage)
        }
        if (sender.hasPermission("karta.contract.inventory")) {
            messageManager.getPrefixedMessage("command.help-inventory").let(sender::sendMessage)
        }
        if (sender.hasPermission("karta.contract.history")) {
            messageManager.getPrefixedMessage("command.help-history").let(sender::sendMessage)
        }
        if (sender.hasPermission("karta.contract.stats")) {
            messageManager.getPrefixedMessage("command.help-stats").let(sender::sendMessage)
        }
        if (sender.hasPermission("karta.contract.admin.reload")) {
            messageManager.getPrefixedMessage("command.help-reload").let(sender::sendMessage)
        }
        if (sender.hasPermission("karta.contract.admin.open")) {
            messageManager.getPrefixedMessage("command.help-open").let(sender::sendMessage)
        }
    }

    private fun openMainMenu(player: Player) {
        // Here we create and open the Main Menu GUI for the player.
        val mainMenu = MainMenuGui(plugin, player, guiConfig)
        mainMenu.open()
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        val completions = mutableListOf<String>()
        when (args.size) {
            1 -> {
                val subcommands = mutableListOf("help", "list", "inventory", "history", "stats")
                if (sender.hasPermission("karta.contract.admin.reload")) subcommands.add("reload")
                if (sender.hasPermission("karta.contract.admin.open")) subcommands.add("open")
                StringUtil.copyPartialMatches(args[0], subcommands, completions)
            }
            2 -> {
                if (args[0].equals("open", ignoreCase = true) && sender.hasPermission("karta.contract.admin.open")) {
                    val playerNames = Bukkit.getOnlinePlayers().map { it.name }
                    StringUtil.copyPartialMatches(args[1], playerNames, completions)
                }
            }
        }
        return completions
    }
}
