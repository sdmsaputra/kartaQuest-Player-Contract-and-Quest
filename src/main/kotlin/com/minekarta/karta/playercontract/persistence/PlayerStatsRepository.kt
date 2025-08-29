package com.minekarta.karta.playercontract.persistence

import com.minekarta.karta.playercontract.domain.PlayerStats
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * An interface for data access operations related to Player Statistics.
 */
interface PlayerStatsRepository {

    /**
     * Retrieves the statistics for a single player.
     * If the player has no stats, it may return null or a default object.
     */
    fun getByPlayer(playerUUID: UUID): CompletableFuture<PlayerStats?>

    /**
     * Saves or updates the statistics for a player.
     */
    fun save(stats: PlayerStats): CompletableFuture<Void>

    /**
     * Retrieves a leaderboard of top players based on a specific statistic.
     * @param statistic The column/statistic to sort by (e.g., "contractsCompleted", "totalEarned").
     * @param limit The number of players to return.
     * @return A CompletableFuture that completes with a list of PlayerStats objects.
     */
    fun getLeaderboard(statistic: String, limit: Int): CompletableFuture<List<PlayerStats>>
}
