package com.example.security

import com.example.data.database.VaultItemEntity
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

object BackupManager {
    private const val BACKUP_VERSION = 1
    private const val APP_IDENTIFIER = "Cyber Guard"

    fun exportEncryptedBackup(items: List<VaultItemEntity>): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("appName", APP_IDENTIFIER)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("itemCount", items.size)

        val itemsArray = JSONArray()
        for (item in items) {
            val itemObj = JSONObject()
            itemObj.put("id", item.id)
            itemObj.put("type", item.type)
            itemObj.put("name", item.name)
            itemObj.put("accountTitle", item.accountTitle)
            itemObj.put("url", item.url)
            itemObj.put("username", item.username)
            itemObj.put("email", item.email)
            itemObj.put("encryptedPassword", item.encryptedPassword)
            itemObj.put("encryptedSensitiveData", item.encryptedSensitiveData)
            itemObj.put("encryptedCustomFields", item.encryptedCustomFields)
            itemObj.put("notes", item.notes)
            itemObj.put("category", item.category)
            itemObj.put("tags", item.tags)
            itemObj.put("favorite", item.favorite)
            itemObj.put("passwordLastChangedAt", item.passwordLastChangedAt)
            itemObj.put("createdAt", item.createdAt)
            itemObj.put("updatedAt", item.updatedAt)
            itemsArray.put(itemObj)
        }
        root.put("items", itemsArray)

        // Compute integrity checksum
        val rawData = itemsArray.toString()
        val checksum = sha256(rawData)
        root.put("checksum", checksum)

        return root.toString(2)
    }

    fun parseAndValidateBackup(jsonString: String): BackupValidationResult {
        return try {
            val root = JSONObject(jsonString)
            val version = root.optInt("version", -1)
            val appName = root.optString("appName", "")
            val itemCount = root.optInt("itemCount", 0)
            val checksum = root.optString("checksum", "")
            val itemsArray = root.optJSONArray("items")

            if (version != BACKUP_VERSION || (appName != APP_IDENTIFIER && appName != "Ahmad Guard") || itemsArray == null) {
                return BackupValidationResult.Invalid("Invalid backup format or incompatible version.")
            }

            // Verify checksum
            val computedChecksum = sha256(itemsArray.toString())
            if (checksum != computedChecksum) {
                return BackupValidationResult.Invalid("Backup integrity check failed: file may be corrupted or modified.")
            }

            val itemsList = mutableListOf<VaultItemEntity>()
            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                val entity = VaultItemEntity(
                    id = 0, // Auto-generate new IDs on restore to prevent collision
                    type = itemObj.optString("type", "LOGIN"),
                    name = itemObj.optString("name", "Untitled"),
                    accountTitle = itemObj.optString("accountTitle", ""),
                    url = itemObj.optString("url", ""),
                    username = itemObj.optString("username", ""),
                    email = itemObj.optString("email", ""),
                    encryptedPassword = itemObj.optString("encryptedPassword", ""),
                    encryptedSensitiveData = itemObj.optString("encryptedSensitiveData", ""),
                    encryptedCustomFields = itemObj.optString("encryptedCustomFields", ""),
                    notes = itemObj.optString("notes", ""),
                    category = itemObj.optString("category", "PERSONAL"),
                    tags = itemObj.optString("tags", ""),
                    favorite = itemObj.optBoolean("favorite", false),
                    passwordLastChangedAt = itemObj.optLong("passwordLastChangedAt", itemObj.optLong("updatedAt", System.currentTimeMillis())),
                    createdAt = itemObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = itemObj.optLong("updatedAt", System.currentTimeMillis())
                )
                itemsList.add(entity)
            }

            BackupValidationResult.Valid(
                itemCount = itemsList.size,
                exportTimestamp = root.optLong("exportTimestamp", System.currentTimeMillis()),
                items = itemsList
            )
        } catch (e: Exception) {
            BackupValidationResult.Invalid("Could not parse backup file: ${e.localizedMessage}")
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

sealed class BackupValidationResult {
    data class Valid(
        val itemCount: Int,
        val exportTimestamp: Long,
        val items: List<VaultItemEntity>
    ) : BackupValidationResult()

    data class Invalid(val reason: String) : BackupValidationResult()
}
