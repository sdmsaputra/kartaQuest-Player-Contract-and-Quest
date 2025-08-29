package com.minekarta.karta.playercontract.domain

import java.time.Instant
import java.util.UUID

/**
 * Holds aggregated statistics for a single player.
 * Can be used for both their role as a contractor and as an issuer.
 *
 * @property playerUUID The UUID of the player these stats belong to.
 * @property contractsCompleted The total number of contracts this player has successfully completed as a contractor.
 * @property contractsFailed The total number of contracts this player has failed (expired, abandoned) as a contractor.
 * @property totalEarned The total amount of money this player has earned as a contractor.
 * @property totalPaid The total amount of money this player has paid out as an issuer.
 * @property avgCompletionTimeMs The average time in milliseconds it takes for this player to complete a contract.
 * @property lastUpdated The timestamp when these stats were last calculated and saved.
 */
data class PlayerStats(
    val playerUUID: UUID,
    var contractsCompleted: Int,
    var contractsFailed: Int,
    var totalEarned: Double,
    var totalPaid: Double,
    var avgCompletionTimeMs: Long,
    var lastUpdated: Instant
)
