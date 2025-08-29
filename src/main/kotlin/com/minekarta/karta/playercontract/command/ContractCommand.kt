package com.minekarta.karta.playercontract.command

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.config.MessageManager
import com.minekarta.karta.playercontract.gui.MainMenuGui
import org.bukkit.Bukkit
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

        when (args[0].lowercase()) {
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
        // This could also be moved to a multi-line message in messages.yml
        sender.sendMessage(messageManager.getMessage("command.help-header"))
        sender.sendMessage(messageManager.getMessage("command.help-base"))
        if (sender.hasPermission("karta.contract.admin.reload")) {
            sender.sendMessage(messageManager.getMessage("command.help-reload"))
        }
        if (sender.hasPermission("karta.contract.admin.open")) {
            sender.sendMessage(messageManager.getMessage("command.help-open"))
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
                val subcommands = mutableListOf("help")
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
