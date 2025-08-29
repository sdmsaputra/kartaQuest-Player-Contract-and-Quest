package com.minekarta.karta.playercontract.persistence

import com.minekarta.karta.playercontract.domain.PlayerStats
import java.sql.ResultSet
import java.util.UUID
import java.util.concurrent.CompletableFuture

class SQLitePlayerStatsRepository(private val dbManager: DatabaseManager) : PlayerStatsRepository {

    override fun getByPlayer(playerUUID: UUID): CompletableFuture<PlayerStats?> {
        return CompletableFuture.supplyAsync({
            val sql = "SELECT * FROM player_stats WHERE player_uuid = ?"
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, playerUUID.toString())
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            return@supplyAsync mapRowToPlayerStats(rs)
                        }
                        return@supplyAsync null
                    }
                }
            }
        }, dbManager.executor)
    }

    override fun save(stats: PlayerStats): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            val sql = """
                INSERT OR REPLACE INTO player_stats
                (player_uuid, contracts_completed, contracts_failed, total_earned, total_paid, avg_completion_time_ms, last_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, stats.playerUUID.toString())
                    stmt.setInt(2, stats.contractsCompleted)
                    stmt.setInt(3, stats.contractsFailed)
                    stmt.setDouble(4, stats.totalEarned)
                    stmt.setDouble(5, stats.totalPaid)
                    stmt.setLong(6, stats.avgCompletionTimeMs)
                    stmt.setLong(7, stats.lastUpdated.toEpochMilli())
                    stmt.executeUpdate()
                }
            }
        }, dbManager.executor)
    }

    override fun getLeaderboard(statistic: String, limit: Int): CompletableFuture<List<PlayerStats>> {
        return CompletableFuture.supplyAsync({
            val stats = mutableListOf<PlayerStats>()
            // Sanitize the statistic column name to prevent SQL injection
            val sanitizedStatistic = when (statistic) {
                "contractsCompleted" -> "contracts_completed"
                "totalEarned" -> "total_earned"
                else -> throw IllegalArgumentException("Invalid statistic for leaderboard")
            }
            val sql = "SELECT * FROM player_stats ORDER BY $sanitizedStatistic DESC LIMIT ?"
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, limit)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            stats.add(mapRowToPlayerStats(rs))
                        }
                    }
                }
            }
            return@supplyAsync stats
        }, dbManager.executor)
    }

    private fun mapRowToPlayerStats(rs: ResultSet): PlayerStats {
        return PlayerStats(
            playerUUID = UUID.fromString(rs.getString("player_uuid")),
            contractsCompleted = rs.getInt("contracts_completed"),
            contractsFailed = rs.getInt("contracts_failed"),
            totalEarned = rs.getDouble("total_earned"),
            totalPaid = rs.getDouble("total_paid"),
            avgCompletionTimeMs = rs.getLong("avg_completion_time_ms"),
            lastUpdated = java.time.Instant.ofEpochMilli(rs.getLong("last_updated"))
        )
    }
}
