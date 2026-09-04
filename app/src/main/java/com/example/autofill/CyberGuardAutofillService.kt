package com.example.autofill

import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveRequest
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import androidx.annotation.RequiresApi
import com.example.data.database.SecurityAuditEventEntity
import com.example.data.database.VaultDatabase
import com.example.data.database.VaultItemEntity
import com.example.model.AuditEventType
import com.example.model.AuditSeverity
import com.example.model.SecurityAuditEvent
import com.example.model.VaultCategory
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.security.EncryptionManager
import com.example.security.PinManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger

@RequiresApi(Build.VERSION_CODES.O)
class CyberGuardAutofillService : AutofillService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val requestCodeGenerator = AtomicInteger(1000)

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        try {
            if (!PinManager.isAutofillEnabled(this)) {
                callback.onSuccess(null)
                return
            }

            val structure = request.fillContexts.lastOrNull()?.structure
            if (structure == null) {
                callback.onSuccess(null)
                return
            }

            val parsed = AutofillStructureParser.parse(structure)
            val usernameField = parsed.usernameField
            val passwordField = parsed.passwordField

            if (usernameField == null && passwordField == null) {
                callback.onSuccess(null)
                return
            }

            val targetPackage = parsed.packageName
            val webDomain = parsed.webDomain

            // Query vault database
            val db = VaultDatabase.getDatabase(this)
            val entities = runBlocking(Dispatchers.IO) {
                db.vaultDao().getAllItemsSync()
            }
            val allItems = entities.map { it.toVaultItem() }

            val matchedItems = AutofillHelper.findMatchingItems(allItems, targetPackage, webDomain)
            if (matchedItems.isEmpty()) {
                callback.onSuccess(null)
                return
            }

            val responseBuilder = FillResponse.Builder()
            val isAuthRequired = PinManager.isAutofillAuthRequired(this)

            // Limit to top 5 most relevant credentials
            val itemsToPresent = matchedItems.take(5)

            for (item in itemsToPresent) {
                val datasetBuilder = Dataset.Builder()
                val userLabel = item.username.ifBlank { item.email.ifBlank { item.name } }
                val title = "${item.name} ($userLabel)"
                val subtitle = if (isAuthRequired) "Requires Master PIN • Cyber Guard" else "Cyber Guard Secure Fill"
                val presentation = AutofillHelper.buildDatasetPresentation(this, title, subtitle)

                if (isAuthRequired) {
                    val reqCode = requestCodeGenerator.incrementAndGet()
                    val authPendingIntent = AutofillHelper.createAuthPendingIntent(
                        context = this,
                        itemId = item.id,
                        usernameId = usernameField?.autofillId,
                        passwordId = passwordField?.autofillId,
                        targetPackageName = targetPackage,
                        requestCode = reqCode
                    )
                    datasetBuilder.setAuthentication(authPendingIntent.intentSender)

                    if (usernameField != null) {
                        datasetBuilder.setValue(
                            usernameField.autofillId,
                            AutofillValue.forText(userLabel),
                            presentation
                        )
                    }
                    if (passwordField != null) {
                        datasetBuilder.setValue(
                            passwordField.autofillId,
                            AutofillValue.forText("••••••••"),
                            presentation
                        )
                    }
                } else {
                    if (usernameField != null) {
                        datasetBuilder.setValue(
                            usernameField.autofillId,
                            AutofillValue.forText(userLabel),
                            presentation
                        )
                    }
                    if (passwordField != null) {
                        datasetBuilder.setValue(
                            passwordField.autofillId,
                            AutofillValue.forText(item.password),
                            presentation
                        )
                    }
                }

                responseBuilder.addDataset(datasetBuilder.build())
            }

            // Configure SaveInfo so Android prompts to save new/updated credentials
            val saveIds = mutableListOf<AutofillId>()
            passwordField?.autofillId?.let { saveIds.add(it) }
            usernameField?.autofillId?.let { saveIds.add(it) }

            if (saveIds.isNotEmpty()) {
                val saveInfoBuilder = SaveInfo.Builder(
                    SaveInfo.SAVE_DATA_TYPE_PASSWORD or SaveInfo.SAVE_DATA_TYPE_USERNAME,
                    saveIds.toTypedArray()
                )
                responseBuilder.setSaveInfo(saveInfoBuilder.build())
            }

            // Log security audit event for detection
            serviceScope.launch {
                try {
                    db.vaultDao().insertAuditEvent(
                        SecurityAuditEventEntity.fromSecurityAuditEvent(
                            SecurityAuditEvent(
                                eventType = AuditEventType.AUTOFILL_REQUEST_SERVED,
                                title = "Autofill Suggestions Provided",
                                details = "Autofill query processed for app '$targetPackage' (${itemsToPresent.size} credentials matched).",
                                severity = AuditSeverity.INFO,
                                timestamp = System.currentTimeMillis(),
                                actor = "Cyber Guard Autofill Engine",
                                contextInfo = "Package: $targetPackage | Web: ${webDomain ?: "N/A"}"
                            )
                        )
                    )
                } catch (_: Exception) {}
            }

            callback.onSuccess(responseBuilder.build())
        } catch (e: Exception) {
            callback.onSuccess(null)
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        serviceScope.launch {
            try {
                val structure = request.fillContexts.lastOrNull()?.structure
                if (structure != null) {
                    val parsed = AutofillStructureParser.parse(structure)
                    val usernameVal = parsed.usernameField?.value?.trim() ?: ""
                    val passwordVal = parsed.passwordField?.value?.trim() ?: ""
                    val targetPackage = parsed.packageName
                    val webDomain = parsed.webDomain

                    if (passwordVal.isNotBlank()) {
                        val cleanAppName = when {
                            !webDomain.isNullOrBlank() -> webDomain.removePrefix("www.").substringBefore(".")
                            targetPackage.isNotBlank() -> targetPackage.substringAfterLast(".")
                            else -> "Saved App"
                        }.replaceFirstChar { it.uppercase() }

                        val db = VaultDatabase.getDatabase(applicationContext)
                        val existingItems = db.vaultDao().getAllItemsSync().map { it.toVaultItem() }

                        val existing = existingItems.firstOrNull {
                            it.name.equals(cleanAppName, ignoreCase = true) &&
                                    (it.username.equals(usernameVal, ignoreCase = true) || it.email.equals(usernameVal, ignoreCase = true))
                        }

                        if (existing != null) {
                            // Update existing password
                            val updated = existing.copy(
                                password = passwordVal,
                                updatedAt = System.currentTimeMillis()
                            )
                            db.vaultDao().updateItem(VaultItemEntity.fromVaultItem(updated))
                        } else {
                            // Create new item
                            val newItem = VaultItem(
                                type = VaultType.LOGIN,
                                name = cleanAppName,
                                url = webDomain ?: targetPackage,
                                username = if (usernameVal.contains("@")) "" else usernameVal,
                                email = if (usernameVal.contains("@")) usernameVal else "",
                                password = passwordVal,
                                notes = "Captured securely via Cyber Guard Autofill",
                                category = VaultCategory.PERSONAL,
                                tags = listOf("Autofill", cleanAppName),
                                favorite = false,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            db.vaultDao().insertItem(VaultItemEntity.fromVaultItem(newItem))
                        }

                        // Log audit event
                        db.vaultDao().insertAuditEvent(
                            SecurityAuditEventEntity.fromSecurityAuditEvent(
                                SecurityAuditEvent(
                                    eventType = AuditEventType.ITEM_CREATED,
                                    title = "Autofill Credential Saved",
                                    details = "New login credentials for '$cleanAppName' were captured and encrypted into vault.",
                                    severity = AuditSeverity.SUCCESS,
                                    timestamp = System.currentTimeMillis(),
                                    actor = "Cyber Guard Autofill Save",
                                    contextInfo = "Package: $targetPackage"
                                )
                            )
                        )
                    }
                }
                callback.onSuccess()
            } catch (e: Exception) {
                callback.onSuccess()
            }
        }
    }
}
