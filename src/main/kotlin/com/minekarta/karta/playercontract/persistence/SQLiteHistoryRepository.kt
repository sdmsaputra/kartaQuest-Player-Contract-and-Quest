package com.minekarta.karta.playercontract.persistence

import com.minekarta.karta.playercontract.domain.HistoryEntry
import com.minekarta.karta.playercontract.domain.HistoryEventType
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class SQLiteHistoryRepository(private val dbManager: DatabaseManager) : HistoryRepository {

    override fun add(entry: HistoryEntry): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            val sql = """
                INSERT INTO history
                (id, contract_id, player_uuid, type, metadata, timestamp)
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, entry.id.toString())
                    stmt.setString(2, entry.contractId.toString())
                    stmt.setString(3, entry.playerUUID.toString())
                    stmt.setString(4, entry.type.name)
                    stmt.setString(5, entry.metadata)
                    stmt.setLong(6, entry.timestamp.toEpochMilli())
                    stmt.executeUpdate()
                }
            }
        }, dbManager.executor)
    }

    override fun findByPlayer(playerUUID: UUID, limit: Int, offset: Int): CompletableFuture<List<HistoryEntry>> {
        return CompletableFuture.supplyAsync({
            val entries = mutableListOf<HistoryEntry>()
            val sql = "SELECT * FROM history WHERE player_uuid = ? ORDER BY created_at DESC LIMIT ? OFFSET ?"
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, playerUUID.toString())
                    stmt.setInt(2, limit)
                    stmt.setInt(3, offset)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            entries.add(mapRowToHistoryEntry(rs))
                        }
                    }
                }
            }
            return@supplyAsync entries
        }, dbManager.executor)
    }

    override fun findByPlayerAndType(playerUUID: UUID, type: HistoryEventType, limit: Int, offset: Int): CompletableFuture<List<HistoryEntry>> {
        // Similar to findByPlayer, but with an additional WHERE clause
        TODO("Not yet implemented")
    }

    override fun findByContract(contractId: UUID): CompletableFuture<List<HistoryEntry>> {
        // Similar to findByPlayer, but with a different WHERE clause
        TODO("Not yet implemented")
    }

    private fun mapRowToHistoryEntry(rs: ResultSet): HistoryEntry {
        return HistoryEntry(
            id = UUID.fromString(rs.getString("id")),
            contractId = UUID.fromString(rs.getString("contract_id")),
            playerUUID = UUID.fromString(rs.getString("player_uuid")),
            type = HistoryEventType.valueOf(rs.getString("type")),
            metadata = rs.getString("metadata"),
            timestamp = Instant.ofEpochMilli(rs.getLong("timestamp"))
        )
    }
}
