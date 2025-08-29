package com.minekarta.karta.playercontract.service

import com.minekarta.karta.playercontract.domain.HistoryEntry
import com.minekarta.karta.playercontract.persistence.HistoryRepository
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface HistoryService {
    fun getHistory(playerId: UUID, page: Int, limit: Int): CompletableFuture<List<HistoryEntry>>
}

class HistoryServiceImpl(private val historyRepository: HistoryRepository) : HistoryService {
    override fun getHistory(playerId: UUID, page: Int, limit: Int): CompletableFuture<List<HistoryEntry>> {
        val offset = page * limit
        return historyRepository.findByPlayer(playerId, limit, offset)
    }
}
