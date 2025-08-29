package com.minekarta.karta.playercontract.persistence

import com.minekarta.karta.playercontract.domain.DeliveryPackage
import com.minekarta.karta.playercontract.domain.DeliveryStatus
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class SQLiteDeliveryPackageRepository(private val dbManager: DatabaseManager) : DeliveryPackageRepository {

    override fun findById(id: UUID): CompletableFuture<DeliveryPackage?> {
        return CompletableFuture.supplyAsync({
            val sql = "SELECT * FROM delivery_packages WHERE id = ?"
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, id.toString())
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            return@supplyAsync mapRowToDeliveryPackage(rs)
                        }
                        return@supplyAsync null
                    }
                }
            }
        }, dbManager.executor)
    }

    override fun findByContractId(contractId: UUID): CompletableFuture<List<DeliveryPackage>> {
        // Implementation would be similar to findById, with a different WHERE clause
        TODO("Not yet implemented")
    }

    override fun findByIssuerAndStatus(issuerUUID: UUID, status: DeliveryStatus): CompletableFuture<List<DeliveryPackage>> {
        return CompletableFuture.supplyAsync({
            val packages = mutableListOf<DeliveryPackage>()
            val sql = "SELECT * FROM delivery_packages WHERE reviewer_uuid = ? AND status = ?"
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, issuerUUID.toString())
                    stmt.setString(2, status.name)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            packages.add(mapRowToDeliveryPackage(rs))
                        }
                    }
                }
            }
            return@supplyAsync packages
        }, dbManager.executor)
    }

    override fun save(deliveryPackage: DeliveryPackage): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            val sql = """
                INSERT OR REPLACE INTO delivery_packages
                (id, contract_id, contractor_uuid, contents, delivered_at, status, reviewed_at, reviewer_uuid, reject_reason)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, deliveryPackage.id.toString())
                    stmt.setString(2, deliveryPackage.contractId.toString())
                    stmt.setString(3, deliveryPackage.contractorUUID.toString())
                    stmt.setBytes(4, SerializationUtil.serializeItemList(deliveryPackage.contents))
                    stmt.setLong(5, deliveryPackage.deliveredAt.toEpochMilli())
                    stmt.setString(6, deliveryPackage.status.name)
                    stmt.setObject(7, deliveryPackage.reviewedAt?.toEpochMilli())
                    stmt.setString(8, deliveryPackage.reviewerUUID?.toString())
                    stmt.setString(9, deliveryPackage.rejectReason)
                    stmt.executeUpdate()
                }
            }
        }, dbManager.executor)
    }

    private fun mapRowToDeliveryPackage(rs: ResultSet): DeliveryPackage {
        val reviewedAtMillis = rs.getLong("reviewed_at")
        return DeliveryPackage(
            id = UUID.fromString(rs.getString("id")),
            contractId = UUID.fromString(rs.getString("contract_id")),
            contractorUUID = UUID.fromString(rs.getString("contractor_uuid")),
            contents = SerializationUtil.deserializeItemList(rs.getBytes("contents")),
            deliveredAt = Instant.ofEpochMilli(rs.getLong("delivered_at")),
            status = DeliveryStatus.valueOf(rs.getString("status")),
            reviewedAt = if (reviewedAtMillis > 0) Instant.ofEpochMilli(reviewedAtMillis) else null,
            reviewerUUID = rs.getString("reviewer_uuid")?.let { UUID.fromString(it) },
            rejectReason = rs.getString("reject_reason")
        )
    }
}
