package com.minekarta.karta.playercontract.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.Connection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DatabaseManager(private val plugin: JavaPlugin) {

    lateinit var dataSource: HikariDataSource
        private set

    // A dedicated thread pool for all database operations.
    val executor: ExecutorService = Executors.newFixedThreadPool(4)

    fun initialize() {
        plugin.logger.info("Initializing database connection...")
        val dbFile = File(plugin.dataFolder, plugin.config.getString("database.sqlite.file", "data.db"))
        if (!dbFile.exists()) {
            plugin.dataFolder.mkdirs()
            try {
                dbFile.createNewFile()
            } catch (e: Exception) {
                plugin.logger.severe("Failed to create database file!")
                e.printStackTrace()
                return
            }
        }

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
            connectionTestQuery = "SELECT 1"
            maximumPoolSize = 8
            poolName = "KartaContract-DB"
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }

        dataSource = HikariDataSource(config)
        plugin.logger.info("Database connection pool established.")

        // Run table creation asynchronously.
        executor.submit { createTables() }
    }

    private fun createTables() {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                plugin.logger.info("Verifying database schema...")

                // Contracts Table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS contracts (
                        id TEXT PRIMARY KEY,
                        templateId TEXT,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        issuerUUID TEXT NOT NULL,
                        contractorUUID TEXT,
                        requirements BLOB NOT NULL, -- Serialized List<ItemStack>
                        reward TEXT NOT NULL,       -- JSON Serialized Reward object
                        deadline INTEGER,           -- Unix timestamp in millis
                        state TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    );
                """.trimIndent())

                // Delivery Packages Table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS delivery_packages (
                        id TEXT PRIMARY KEY,
                        contractId TEXT NOT NULL,
                        contractorUUID TEXT NOT NULL,
                        contents BLOB NOT NULL,     -- Serialized List<ItemStack>
                        deliveredAt INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        reviewedAt INTEGER,
                        reviewerUUID TEXT,
                        rejectReason TEXT,
                        FOREIGN KEY (contractId) REFERENCES contracts(id)
                    );
                """.trimIndent())

                // History Table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS history (
                        id TEXT PRIMARY KEY,
                        type TEXT NOT NULL,
                        playerUUID TEXT NOT NULL,
                        contractId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        metadata TEXT NOT NULL      -- JSON string
                    );
                """.trimIndent())

                // Player Stats Table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        playerUUID TEXT PRIMARY KEY,
                        contractsCompleted INTEGER NOT NULL DEFAULT 0,
                        contractsFailed INTEGER NOT NULL DEFAULT 0,
                        totalEarned REAL NOT NULL DEFAULT 0.0,
                        totalPaid REAL NOT NULL DEFAULT 0.0,
                        avgCompletionTimeMs INTEGER NOT NULL DEFAULT 0,
                        lastUpdated INTEGER NOT NULL
                    );
                """.trimIndent())

                plugin.logger.info("Database tables verified/created successfully.")
            }
        }
    }

    fun close() {
        if (::dataSource.isInitialized && !dataSource.isClosed) {
            dataSource.close()
        }
        executor.shutdown()
        plugin.logger.info("Database connection closed.")
    }

    fun getConnection(): Connection = dataSource.connection
}
