package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY updatedAt DESC")
    fun getAllItemsFlow(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items ORDER BY updatedAt DESC")
    suspend fun getAllItemsSync(): List<VaultItemEntity>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): VaultItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity): Long

    @Update
    suspend fun updateItem(item: VaultItemEntity)

    @Delete
    suspend fun deleteItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM vault_items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM ai_messages ORDER BY timestamp ASC")
    fun getAllAiMessagesFlow(): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(message: AiMessageEntity)

    @Query("DELETE FROM ai_messages")
    suspend fun deleteAllAiMessages()

    // Security Audit Log Queries
    @Query("SELECT * FROM security_audit_events ORDER BY timestamp DESC")
    fun getAllAuditEventsFlow(): Flow<List<SecurityAuditEventEntity>>

    @Query("SELECT * FROM security_audit_events ORDER BY timestamp DESC")
    suspend fun getAllAuditEventsSync(): List<SecurityAuditEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditEvent(event: SecurityAuditEventEntity): Long

    @Query("DELETE FROM security_audit_events")
    suspend fun deleteAllAuditEvents()
}

