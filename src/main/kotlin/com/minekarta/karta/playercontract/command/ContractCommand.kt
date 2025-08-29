package com.minekarta.karta.playercontract.command

import com.minekarta.karta.playercontract.KartaPlayerContract
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.gui.MainMenuGui
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ContractCommand(
    private val plugin: KartaPlayerContract,
    private val guiConfig: GuiConfigManager
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }

        // For now, we only handle the base command which opens the menu.
        // Subcommands like 'reload' will be added later.
        if (args.isEmpty()) {
            openMainMenu(sender)
        } else {
            // Handle subcommands
            sender.sendMessage("Subcommands are not implemented yet.")
        }

        return true
    }

    private fun openMainMenu(player: Player) {
        // Here we create and open the Main Menu GUI for the player.
        val mainMenu = MainMenuGui(plugin, player, guiConfig)
        mainMenu.open()
    }
}
