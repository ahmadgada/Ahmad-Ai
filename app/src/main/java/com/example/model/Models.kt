package com.example.model

enum class VaultType(val displayName: String) {
    LOGIN("Login"),
    CARD("Card"),
    IDENTITY("Identity"),
    SECURE_NOTE("Secure Note")
}

enum class VaultCategory(val displayName: String) {
    ALL("All"),
    FAVORITES("Favorites"),
    SOCIAL("Social"),
    BANKING("Banking"),
    GAMING("Gaming"),
    SHOPPING("Shopping"),
    EMAIL("Email"),
    WORK("Work"),
    ENTERTAINMENT("Entertainment"),
    FINANCE("Finance"),
    PERSONAL("Personal"),
    MESSAGING("Messaging"),
    OTHER("Other")
}

data class CustomField(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String = "",
    val value: String = "",
    val isSecured: Boolean = true
)

data class VaultItem(
    val id: Long = 0,
    val type: VaultType = VaultType.LOGIN,
    val name: String = "",
    val accountTitle: String = "", // Subtitle / Profile / Account variant (e.g. "Personal", "Work", "Gaming")
    val url: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "", // Decrypted in memory when unlocked
    val sensitiveData: String = "", // Card Number, SSN/ID, CVV, Note Body
    val notes: String = "",
    val category: VaultCategory = VaultCategory.PERSONAL,
    val tags: List<String> = emptyList(),
    val customFields: List<CustomField> = emptyList(),
    val favorite: Boolean = false,
    val passwordLastChangedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ScoreStatus(val label: String, val colorHex: Long) {
    EXCELLENT("EXCELLENT", 0xFF00E676),
    STRONG("STRONG", 0xFF00B0FF),
    GOOD("GOOD", 0xFF00E5FF),
    NEEDS_ACTION("NEEDS ACTION", 0xFFFFB300),
    CRITICAL("CRITICAL", 0xFFFF5252)
}

data class SecurityScore(
    val score: Int = 100,
    val status: ScoreStatus = ScoreStatus.EXCELLENT,
    val totalPasswords: Int = 0,
    val strongCount: Int = 0,
    val weakCount: Int = 0,
    val reusedCount: Int = 0,
    val shortCount: Int = 0,
    val oldPasswordCount: Int = 0,
    val missing2FaCount: Int = 0,
    val complexityScore: Int = 100,
    val uniquenessScore: Int = 100,
    val twoFactorScore: Int = 100,
    val breachSafetyScore: Int = 100,
    val freshnessScore: Int = 100,
    val lastBackupDate: String = "Today",
    val generatedAt: Long = System.currentTimeMillis()
)

enum class FindingSeverity {
    CRITICAL,
    WARNING,
    INFO
}

data class WatchtowerFinding(
    val id: String,
    val title: String,
    val explanation: String,
    val recommendation: String,
    val severity: FindingSeverity,
    val itemId: Long? = null,
    val itemName: String? = null,
    val affectedItemIds: List<Long> = emptyList(),
    val affectedAccountNames: List<String> = emptyList()
)

enum class MessageSender {
    USER,
    AI
}

data class AiMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

enum class AiLanguage(val displayName: String) {
    AUTO("Auto Detect"),
    ENGLISH("English"),
    HINDI("हिंदी (Hindi)"),
    HINGLISH("Hinglish")
}

enum class ThemeMode(val displayName: String) {
    DARK("Cyber Dark"),
    AMOLED("AMOLED Black"),
    LIGHT("Clean Light")
}

enum class AutoLockTimeout(val displayName: String, val millis: Long) {
    IMMEDIATELY("Immediately", 0L),
    THIRTY_SEC("30 Seconds", 30_000L),
    ONE_MIN("1 Minute", 60_000L),
    FIVE_MIN("5 Minutes", 300_000L),
    FIFTEEN_MIN("15 Minutes", 900_000L),
    NEVER("Never (Not Recommended)", Long.MAX_VALUE)
}

enum class ClipboardTimeout(val displayName: String, val seconds: Int) {
    FIFTEEN_SEC("15 Seconds", 15),
    THIRTY_SEC("30 Seconds", 30),
    SIXTY_SEC("60 Seconds", 60),
    OFF("Never", 0)
}

enum class AppNavDestination {
    HOME,
    VAULT,
    ADD,
    GENERATOR,
    SETTINGS,
    WATCHTOWER,
    AI_ASSISTANT,
    SECURITY_SCANNER,
    EDIT_ITEM,
    SECURITY_TIPS,
    AUDIT_LOG
}

enum class AuditSeverity(val displayName: String, val colorHex: Long) {
    INFO("INFO", 0xFF00B0FF),
    SUCCESS("SUCCESS", 0xFF00E676),
    WARNING("WARNING", 0xFFFFB300),
    CRITICAL("CRITICAL", 0xFFFF5252)
}

enum class AuditEventType(val displayName: String, val defaultSeverity: AuditSeverity) {
    VAULT_UNLOCKED("Vault Unlocked", AuditSeverity.SUCCESS),
    VAULT_LOCKED("Vault Locked", AuditSeverity.INFO),
    AUTH_FAILED("Authentication Failed", AuditSeverity.CRITICAL),
    ITEM_CREATED("Credential Created", AuditSeverity.SUCCESS),
    ITEM_UPDATED("Credential Modified", AuditSeverity.INFO),
    ITEM_DELETED("Credential Deleted", AuditSeverity.WARNING),
    PASSWORD_COPIED("Secret Copied to Clipboard", AuditSeverity.INFO),
    PASSWORD_GENERATED("Password Generated", AuditSeverity.INFO),
    WATCHTOWER_SCAN("Watchtower Defense Scan", AuditSeverity.INFO),
    BREACH_CHECK_RUN("Breach Check Executed", AuditSeverity.INFO),
    BACKUP_EXPORTED("Encrypted Backup Exported", AuditSeverity.SUCCESS),
    BACKUP_RESTORED("Backup Restored", AuditSeverity.SUCCESS),
    AUDIT_LOG_EXPORTED("Encrypted Audit Log Exported", AuditSeverity.SUCCESS),
    MASTER_PIN_CHANGED("Master PIN Changed", AuditSeverity.WARNING),
    BIOMETRIC_TOGGLED("Biometric Authentication Updated", AuditSeverity.INFO),
    ACCOUNT_RECOVERY_CONFIGURED("Emergency Recovery Configured", AuditSeverity.SUCCESS),
    ACCOUNT_RECOVERY_DISABLED("Emergency Recovery Disabled", AuditSeverity.WARNING),
    ACCOUNT_RECOVERED("Vault Recovered via Emergency Protocol", AuditSeverity.WARNING),
    AUTOFILL_REQUEST_SERVED("Autofill Request Processed", AuditSeverity.INFO),
    AUTOFILL_CREDENTIAL_RELEASED("Autofill Credential Released", AuditSeverity.SUCCESS),
    SYSTEM_INITIALIZED("Vault Security Initialized", AuditSeverity.SUCCESS)
}

data class SecurityAuditEvent(
    val id: Long = 0,
    val eventType: AuditEventType = AuditEventType.SYSTEM_INITIALIZED,
    val title: String = "",
    val details: String = "",
    val severity: AuditSeverity = AuditSeverity.INFO,
    val timestamp: Long = System.currentTimeMillis(),
    val actor: String = "Vault Master (Local Hardware Keystore)",
    val contextInfo: String = "On-Device Keystore / Hardware Protected"
)

