package com.example.security

import com.example.data.database.SecurityAuditEventEntity
import com.example.model.AuditEventType
import com.example.model.AuditSeverity
import com.example.model.SecurityAuditEvent
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

object AuditLogManager {
    private const val AUDIT_EXPORT_VERSION = 1
    private const val APP_IDENTIFIER = "Cyber Guard Security Audit Trail"

    /**
     * Serializes and encrypts audit events into an official encrypted audit package.
     */
    fun exportEncryptedAuditLog(events: List<SecurityAuditEvent>): String {
        val root = JSONObject()
        root.put("version", AUDIT_EXPORT_VERSION)
        root.put("appName", APP_IDENTIFIER)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("totalEvents", events.size)
        root.put("encryptionAlgorithm", "AES-256-GCM / AndroidKeystore")

        // Build raw events payload
        val eventsArray = JSONArray()
        var criticalCount = 0
        var warningCount = 0
        var successCount = 0
        var infoCount = 0

        for (event in events) {
            val eventObj = JSONObject()
            eventObj.put("id", event.id)
            eventObj.put("eventType", event.eventType.name)
            eventObj.put("title", event.title)
            eventObj.put("details", event.details)
            eventObj.put("severity", event.severity.name)
            eventObj.put("timestamp", event.timestamp)
            eventObj.put("actor", event.actor)
            eventObj.put("contextInfo", event.contextInfo)
            eventsArray.put(eventObj)

            when (event.severity) {
                AuditSeverity.CRITICAL -> criticalCount++
                AuditSeverity.WARNING -> warningCount++
                AuditSeverity.SUCCESS -> successCount++
                AuditSeverity.INFO -> infoCount++
            }
        }

        val rawJson = eventsArray.toString()
        val checksum = sha256(rawJson)

        // Metadata summary visible in header
        val summaryObj = JSONObject()
        summaryObj.put("criticalEvents", criticalCount)
        summaryObj.put("warningEvents", warningCount)
        summaryObj.put("successEvents", successCount)
        summaryObj.put("infoEvents", infoCount)
        root.put("summary", summaryObj)

        // Hardware AES-256-GCM encryption of full audit event payload
        val encryptedPayload = EncryptionManager.encrypt(rawJson)
        root.put("encryptedPayload", encryptedPayload)
        root.put("checksumSha256", checksum)

        return root.toString(2)
    }

    /**
     * Generates initial seed events for when the app is first initialized.
     */
    fun generateInitialSeedEvents(): List<SecurityAuditEvent> {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)
        val twoHoursAgo = now - (2 * 60 * 60 * 1000)
        val yesterday = now - (24 * 60 * 60 * 1000)

        return listOf(
            SecurityAuditEvent(
                eventType = AuditEventType.SYSTEM_INITIALIZED,
                title = "Zero-Knowledge Vault Initialized",
                details = "Hardware Keystore alias created with AES-256-GCM master key and PBKDF2 Master PIN derivation.",
                severity = AuditSeverity.SUCCESS,
                timestamp = yesterday,
                actor = "Cyber Guard Security Core",
                contextInfo = "Android KeyStore / Hardware Enclave"
            ),
            SecurityAuditEvent(
                eventType = AuditEventType.WATCHTOWER_SCAN,
                title = "Initial Watchtower Defense Scan",
                details = "Local heuristic security analysis executed. Entropy calculations and password reuse checks completed.",
                severity = AuditSeverity.INFO,
                timestamp = twoHoursAgo,
                actor = "Watchtower Engine",
                contextInfo = "On-Device Local Memory"
            ),
            SecurityAuditEvent(
                eventType = AuditEventType.VAULT_UNLOCKED,
                title = "Master Vault Unlocked",
                details = "User successfully authenticated using 6-digit Master PIN. Session token authorized.",
                severity = AuditSeverity.SUCCESS,
                timestamp = oneHourAgo,
                actor = "Vault Master",
                contextInfo = "Local Biometric & PIN Verifier"
            ),
            SecurityAuditEvent(
                eventType = AuditEventType.BREACH_CHECK_RUN,
                title = "Privacy-Preserving Breach Verification",
                details = "K-Anonymity SHA-1 5-character prefix queries checked securely against exposed datasets.",
                severity = AuditSeverity.INFO,
                timestamp = now - (15 * 60 * 1000),
                actor = "Watchtower Breach Checker",
                contextInfo = "K-Anonymity Partial Hash Query"
            )
        )
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
