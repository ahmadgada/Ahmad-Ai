package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AuditEventType
import com.example.model.AuditSeverity
import com.example.model.CustomField
import com.example.model.SecurityAuditEvent
import com.example.model.VaultCategory
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.security.EncryptionManager
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val name: String,
    val accountTitle: String = "",
    val url: String,
    val username: String,
    val email: String,
    val encryptedPassword: String,
    val encryptedSensitiveData: String,
    val encryptedCustomFields: String = "",
    val notes: String,
    val category: String,
    val tags: String, // comma-separated
    val favorite: Boolean,
    val passwordLastChangedAt: Long = 0L,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toVaultItem(): VaultItem {
        val decryptedCustomFieldsList = mutableListOf<CustomField>()
        if (encryptedCustomFields.isNotBlank()) {
            try {
                val decryptedJson = EncryptionManager.decrypt(encryptedCustomFields)
                if (decryptedJson.isNotBlank()) {
                    val jsonArray = JSONArray(decryptedJson)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        decryptedCustomFieldsList.add(
                            CustomField(
                                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                                label = obj.optString("label", ""),
                                value = obj.optString("value", ""),
                                isSecured = obj.optBoolean("isSecured", true)
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
        }

        val effectivePasswordChanged = if (passwordLastChangedAt > 0L) passwordLastChangedAt else updatedAt

        return VaultItem(
            id = id,
            type = try { VaultType.valueOf(type) } catch (e: Exception) { VaultType.LOGIN },
            name = name,
            accountTitle = accountTitle,
            url = url,
            username = username,
            email = email,
            password = EncryptionManager.decrypt(encryptedPassword),
            sensitiveData = EncryptionManager.decrypt(encryptedSensitiveData),
            customFields = decryptedCustomFieldsList,
            notes = notes,
            category = try { VaultCategory.valueOf(category) } catch (e: Exception) { VaultCategory.PERSONAL },
            tags = if (tags.isNotBlank()) tags.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
            favorite = favorite,
            passwordLastChangedAt = effectivePasswordChanged,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromVaultItem(item: VaultItem): VaultItemEntity {
            var encryptedFieldsPayload = ""
            if (item.customFields.isNotEmpty()) {
                try {
                    val array = JSONArray()
                    for (f in item.customFields) {
                        val obj = JSONObject()
                        obj.put("id", f.id)
                        obj.put("label", f.label)
                        obj.put("value", f.value)
                        obj.put("isSecured", f.isSecured)
                        array.put(obj)
                    }
                    encryptedFieldsPayload = EncryptionManager.encrypt(array.toString())
                } catch (_: Exception) {}
            }

            return VaultItemEntity(
                id = item.id,
                type = item.type.name,
                name = item.name,
                accountTitle = item.accountTitle,
                url = item.url,
                username = item.username,
                email = item.email,
                encryptedPassword = EncryptionManager.encrypt(item.password),
                encryptedSensitiveData = EncryptionManager.encrypt(item.sensitiveData),
                encryptedCustomFields = encryptedFieldsPayload,
                notes = item.notes,
                category = item.category.name,
                tags = item.tags.joinToString(","),
                favorite = item.favorite,
                passwordLastChangedAt = item.passwordLastChangedAt,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }
    }
}

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey
    val id: String,
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long,
    val isError: Boolean
)

@Entity(tableName = "security_audit_events")
data class SecurityAuditEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val title: String,
    val details: String,
    val severity: String,
    val timestamp: Long,
    val actor: String,
    val contextInfo: String
) {
    fun toSecurityAuditEvent(): SecurityAuditEvent {
        return SecurityAuditEvent(
            id = id,
            eventType = try { AuditEventType.valueOf(eventType) } catch (e: Exception) { AuditEventType.SYSTEM_INITIALIZED },
            title = title,
            details = details,
            severity = try { AuditSeverity.valueOf(severity) } catch (e: Exception) { AuditSeverity.INFO },
            timestamp = timestamp,
            actor = actor,
            contextInfo = contextInfo
        )
    }

    companion object {
        fun fromSecurityAuditEvent(event: SecurityAuditEvent): SecurityAuditEventEntity {
            return SecurityAuditEventEntity(
                id = event.id,
                eventType = event.eventType.name,
                title = event.title,
                details = event.details,
                severity = event.severity.name,
                timestamp = event.timestamp,
                actor = event.actor,
                contextInfo = event.contextInfo
            )
        }
    }
}

