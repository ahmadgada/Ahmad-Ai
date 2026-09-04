package com.example.data.repository

import com.example.data.database.AiMessageEntity
import com.example.data.database.SecurityAuditEventEntity
import com.example.data.database.VaultDao
import com.example.data.database.VaultItemEntity
import com.example.model.AiMessage
import com.example.model.MessageSender
import com.example.model.SecurityAuditEvent
import com.example.model.VaultCategory
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.security.AuditLogManager
import com.example.security.BackupManager
import com.example.security.BackupValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VaultRepository(private val vaultDao: VaultDao) {

    val allVaultItemsFlow: Flow<List<VaultItem>> = vaultDao.getAllItemsFlow().map { entities ->
        entities.map { it.toVaultItem() }
    }

    val allAiMessagesFlow: Flow<List<AiMessage>> = vaultDao.getAllAiMessagesFlow().map { entities ->
        entities.map {
            AiMessage(
                id = it.id,
                sender = try { MessageSender.valueOf(it.sender) } catch (e: Exception) { MessageSender.AI },
                text = it.text,
                timestamp = it.timestamp,
                isError = it.isError
            )
        }
    }

    val allAuditEventsFlow: Flow<List<SecurityAuditEvent>> = vaultDao.getAllAuditEventsFlow().map { entities ->
        entities.map { it.toSecurityAuditEvent() }
    }

    suspend fun insertItem(item: VaultItem): Long = withContext(Dispatchers.IO) {
        val entity = VaultItemEntity.fromVaultItem(item)
        vaultDao.insertItem(entity)
    }

    suspend fun updateItem(item: VaultItem) = withContext(Dispatchers.IO) {
        val entity = VaultItemEntity.fromVaultItem(item)
        vaultDao.updateItem(entity)
    }

    suspend fun deleteItem(item: VaultItem) = withContext(Dispatchers.IO) {
        vaultDao.deleteItemById(item.id)
    }

    suspend fun deleteItemById(id: Long) = withContext(Dispatchers.IO) {
        vaultDao.deleteItemById(id)
    }

    suspend fun toggleFavorite(item: VaultItem) = withContext(Dispatchers.IO) {
        val updated = item.copy(favorite = !item.favorite, updatedAt = System.currentTimeMillis())
        vaultDao.updateItem(VaultItemEntity.fromVaultItem(updated))
    }

    suspend fun saveAiMessage(message: AiMessage) = withContext(Dispatchers.IO) {
        val entity = AiMessageEntity(
            id = message.id,
            sender = message.sender.name,
            text = message.text,
            timestamp = message.timestamp,
            isError = message.isError
        )
        vaultDao.insertAiMessage(entity)
    }

    suspend fun clearAiMessages() = withContext(Dispatchers.IO) {
        vaultDao.deleteAllAiMessages()
    }

    // Security Audit Log Operations
    suspend fun logSecurityEvent(event: SecurityAuditEvent): Long = withContext(Dispatchers.IO) {
        val entity = SecurityAuditEventEntity.fromSecurityAuditEvent(event)
        vaultDao.insertAuditEvent(entity)
    }

    suspend fun seedInitialAuditEventsIfEmpty() = withContext(Dispatchers.IO) {
        val existing = vaultDao.getAllAuditEventsSync()
        if (existing.isEmpty()) {
            val seedEvents = AuditLogManager.generateInitialSeedEvents()
            for (event in seedEvents) {
                vaultDao.insertAuditEvent(SecurityAuditEventEntity.fromSecurityAuditEvent(event))
            }
        }
    }

    suspend fun exportEncryptedAuditLog(): String = withContext(Dispatchers.IO) {
        val entities = vaultDao.getAllAuditEventsSync()
        val events = entities.map { it.toSecurityAuditEvent() }
        AuditLogManager.exportEncryptedAuditLog(events)
    }

    suspend fun clearAuditLog() = withContext(Dispatchers.IO) {
        vaultDao.deleteAllAuditEvents()
    }

    suspend fun exportEncryptedBackup(): String = withContext(Dispatchers.IO) {
        val entities = vaultDao.getAllItemsSync()
        BackupManager.exportEncryptedBackup(entities)
    }

    suspend fun restoreEncryptedBackup(json: String): BackupValidationResult = withContext(Dispatchers.IO) {
        val result = BackupManager.parseAndValidateBackup(json)
        if (result is BackupValidationResult.Valid) {
            for (entity in result.items) {
                vaultDao.insertItem(entity)
            }
        }
        result
    }

    suspend fun clearAllVaultData() = withContext(Dispatchers.IO) {
        vaultDao.deleteAllItems()
        vaultDao.deleteAllAiMessages()
        vaultDao.deleteAllAuditEvents()
    }
}

