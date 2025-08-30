package com.minekarta.karta.playercontract.persistence

import com.minekarta.karta.playercontract.domain.Contract
import com.minekarta.karta.playercontract.domain.ContractState
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class SQLiteContractRepository(private val dbManager: DatabaseManager) : ContractRepository {

    override fun findById(id: UUID): CompletableFuture<Contract?> {
        return CompletableFuture.supplyAsync({
            val sql = "SELECT * FROM contracts WHERE id = ?"
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, id.toString())
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            return@supplyAsync mapRowToContract(rs)
                        }
                        return@supplyAsync null
                    }
                }
            }
        }, dbManager.executor)
    }

    override fun findByState(state: ContractState): CompletableFuture<List<Contract>> {
        return CompletableFuture.supplyAsync({
            val contracts = mutableListOf<Contract>()
            val sql = "SELECT * FROM contracts WHERE state = ?"
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, state.name)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            contracts.add(mapRowToContract(rs))
                        }
                    }
                }
            }
            return@supplyAsync contracts
        }, dbManager.executor)
    }

    override fun findByState(state: ContractState, page: Int, pageSize: Int): CompletableFuture<List<Contract>> {
        return CompletableFuture.supplyAsync({
            val contracts = mutableListOf<Contract>()
            val sql = "SELECT * FROM contracts WHERE state = ? ORDER BY createdAt DESC LIMIT ? OFFSET ?"
            val offset = (page - 1) * pageSize
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, state.name)
                    stmt.setInt(2, pageSize)
                    stmt.setInt(3, offset)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            contracts.add(mapRowToContract(rs))
                        }
                    }
                }
            }
            return@supplyAsync contracts
        }, dbManager.executor)
    }

    override fun findByPlayer(playerUUID: UUID): CompletableFuture<List<Contract>> {
        // Implementation would be similar to findByState, with a different WHERE clause
        // WHERE issuerUUID = ? OR contractorUUID = ?
        TODO("Not yet implemented")
    }

    override fun save(contract: Contract): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            val sql = """
                INSERT OR REPLACE INTO contracts
                (id, templateId, title, description, issuerUUID, contractorUUID, requirements, reward, deadline, state, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, contract.id.toString())
                    stmt.setString(2, contract.templateId)
                    stmt.setString(3, contract.title)
                    stmt.setString(4, contract.description)
                    stmt.setString(5, contract.issuerUUID.toString())
                    stmt.setString(6, contract.contractorUUID?.toString())
                    stmt.setBytes(7, SerializationUtil.serializeItemList(contract.requirements))
                    stmt.setString(8, SerializationUtil.toJson(contract.reward))
                    stmt.setObject(9, contract.deadline?.toEpochMilli())
                    stmt.setString(10, contract.state.name)
                    stmt.setLong(11, contract.createdAt.toEpochMilli())
                    stmt.setLong(12, contract.updatedAt.toEpochMilli())
                    stmt.executeUpdate()
                }
            }
        }, dbManager.executor)
    }

    override fun delete(id: UUID): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            val sql = "DELETE FROM contracts WHERE id = ?"
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, id.toString())
                    stmt.executeUpdate()
                }
            }
        }, dbManager.executor)
    }

    override fun findAll(): CompletableFuture<List<Contract>> {
        // Implementation would be similar to findByState, without a WHERE clause
        TODO("Not yet implemented")
    }

    private fun mapRowToContract(rs: ResultSet): Contract {
        val deadlineMillis = rs.getLong("deadline")
        return Contract(
            id = UUID.fromString(rs.getString("id")),
            templateId = rs.getString("templateId"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            issuerUUID = UUID.fromString(rs.getString("issuerUUID")),
            contractorUUID = rs.getString("contractorUUID")?.let { UUID.fromString(it) },
            requirements = SerializationUtil.deserializeItemList(rs.getBytes("requirements")),
            reward = SerializationUtil.fromJsonToReward(rs.getString("reward")),
            deadline = if (deadlineMillis > 0) Instant.ofEpochMilli(deadlineMillis) else null,
            state = ContractState.valueOf(rs.getString("state")),
            createdAt = Instant.ofEpochMilli(rs.getLong("createdAt")),
            updatedAt = Instant.ofEpochMilli(rs.getLong("updatedAt"))
        )
    }
}
