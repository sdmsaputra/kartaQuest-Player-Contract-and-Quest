package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.domain.PlayerStats
import com.minekarta.karta.playercontract.persistence.PlayerStatsRepository
import java.util.UUID
import java.util.concurrent.CompletableFuture

import java.time.Instant

interface PlayerStatsService {
    fun getStats(playerId: UUID): CompletableFuture<PlayerStats>
}

class PlayerStatsServiceImpl(private val statsRepository: PlayerStatsRepository) : PlayerStatsService {
    override fun getStats(playerId: UUID): CompletableFuture<PlayerStats> {
        return statsRepository.getByPlayer(playerId).thenApply { stats ->
            stats ?: PlayerStats(playerId, 0, 0, 0.0, 0.0, 0L, Instant.now())
        }
    }
}
