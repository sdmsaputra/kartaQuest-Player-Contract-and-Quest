package com.minekarta.karta.playercontract

import com.minekarta.karta.playercontract.command.ContractCommand
import com.minekarta.karta.playercontract.config.GuiConfigManager
import com.minekarta.karta.playercontract.config.MessageManager
import com.minekarta.karta.playercontract.gui.CreateWizardManager
import com.minekarta.karta.playercontract.gui.GuiListener
import com.minekarta.karta.playercontract.listeners.PlayerChatListener
import com.minekarta.karta.playercontract.persistence.*
import com.minekarta.karta.playercontract.service.*
import com.minekarta.karta.playercontract.util.ChatInputManager
import com.minekarta.karta.playercontract.util.FoliaScheduler
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

/**
 * Main class for the KartaPlayerContract plugin.
 * Initializes all managers, services, and commands.
 */
class KartaPlayerContract : JavaPlugin() {

    lateinit var dbManager: DatabaseManager
        private set
    lateinit var contractService: ContractService
        private set
    lateinit var escrowService: EscrowService
        private set
    lateinit var inventoryService: InventoryService
        private set
    lateinit var historyService: HistoryService
        private set
    lateinit var playerStatsService: PlayerStatsService
        private set
    lateinit var guiConfigManager: GuiConfigManager
        private set
    lateinit var messageManager: MessageManager
        private set
    lateinit var wizardManager: CreateWizardManager
        private set
    lateinit var scheduler: FoliaScheduler
        private set
    lateinit var chatInputManager: ChatInputManager
        private set

    override fun onEnable() {
        // 1. Load Configurations
        saveDefaultConfig()
        guiConfigManager = GuiConfigManager(this)
        messageManager = MessageManager(this)
        wizardManager = CreateWizardManager(this)
        scheduler = FoliaScheduler(this)
        chatInputManager = ChatInputManager()

        // 2. Initialize Database
        dbManager = DatabaseManager(this)
        dbManager.initialize()

        // 3. Initialize Repositories
        val contractRepository = SQLiteContractRepository(dbManager)
        val playerStatsRepository = SQLitePlayerStatsRepository(dbManager)
        val historyRepository = SQLiteHistoryRepository(dbManager)
        val deliveryPackageRepository = SQLiteDeliveryPackageRepository(dbManager)


        // 4. Initialize Services
        escrowService = EscrowServiceImpl(this, scheduler)
        contractService = ContractServiceImpl(contractRepository, escrowService, scheduler)
        inventoryService = InventoryServiceImpl(deliveryPackageRepository)
        historyService = HistoryServiceImpl(historyRepository)
        playerStatsService = PlayerStatsServiceImpl(playerStatsRepository)


        // 5. Register Commands
        val contractCommand = ContractCommand(this, messageManager, wizardManager)
        getCommand("contract")?.let {
            it.setExecutor(contractCommand)
            it.tabCompleter = contractCommand
        }

        // 6. Register Listeners
        server.pluginManager.registerEvents(GuiListener(wizardManager), this)
        server.pluginManager.registerEvents(PlayerChatListener(chatInputManager), this)

        // 8. Register PlaceholderAPI Expansion
        // TODO: Register PAPI expansion if PlaceholderAPI is enabled

        // 8. Schedule Tasks
        scheduleContractExpirationTask()

        logger.info("KartaPlayerContract has been enabled successfully.")
    }

    private fun scheduleContractExpirationTask() {
        // Use the modern async scheduler, which is safe for both Spigot/Paper and Folia.
        server.asyncScheduler.runAtFixedRate(this, { task ->
            logger.info("Running scheduled task to expire contracts...")
            contractService.expireContracts().whenComplete { count, error ->
                if (error != null) {
                    logger.severe("Error occurred while expiring contracts: ${error.message}")
                    error.printStackTrace()
                } else {
                    if (count > 0) {
                        logger.info("Expired $count contracts.")
                    }
                }
            }
        }, 5, 5, TimeUnit.MINUTES) // Initial delay of 5 mins, repeat every 5 mins.
    }

    override fun onDisable() {
        // Gracefully shut down the database connection pool
        if (::dbManager.isInitialized) {
            dbManager.close()
        }
        logger.info("KartaPlayerContract has been disabled.")
    }
}
