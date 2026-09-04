package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.database.VaultDatabase
import com.example.data.repository.VaultRepository
import com.example.domain.WatchtowerEngine
import com.example.model.AiLanguage
import com.example.model.AiMessage
import com.example.model.AuditEventType
import com.example.model.AuditSeverity
import com.example.model.AppNavDestination
import com.example.model.AutoLockTimeout
import com.example.model.ClipboardTimeout
import com.example.model.FindingSeverity
import com.example.model.MessageSender
import com.example.security.PinVerificationResult
import com.example.model.SecurityAuditEvent
import com.example.model.SecurityScore
import com.example.model.ThemeMode
import com.example.model.VaultCategory
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.model.WatchtowerFinding
import com.example.autofill.AutofillHelper
import com.example.network.GeminiApiClient
import com.example.network.GeminiContent
import com.example.network.GeminiGenerationConfig
import com.example.network.GeminiPart
import com.example.network.GeminiRequest
import com.example.security.BackupValidationResult
import com.example.security.ClipboardHelper
import com.example.security.PinManager
import com.example.security.RecoveryConfig
import com.example.security.RecoveryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class VaultUiState(
    val isUnlocked: Boolean = false,
    val isOnboardingDone: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val aiLanguage: AiLanguage = AiLanguage.AUTO,
    val currentDestination: AppNavDestination = AppNavDestination.HOME,
    val searchQuery: String = "",
    val selectedCategory: VaultCategory = VaultCategory.ALL,
    val selectedTypeFilter: VaultType? = null,
    val editingItem: VaultItem? = null,
    val isAiLoading: Boolean = false,
    val aiErrorMessage: String? = null,
    val biometricEnabled: Boolean = false,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.ONE_MIN,
    val clipboardTimeout: ClipboardTimeout = ClipboardTimeout.THIRTY_SEC,
    val lockoutRemainingSeconds: Int = 0,
    val isRecoveryConfigured: Boolean = false,
    val isAutofillEnabled: Boolean = true,
    val isAutofillAuthRequired: Boolean = true,
    val isSystemAutofillActive: Boolean = false,
    val isAutofillSupported: Boolean = true,
    val snackbarMessage: String? = null
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VaultRepository
    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    val allItems: StateFlow<List<VaultItem>>
    val allAiMessages: StateFlow<List<AiMessage>>
    val allAuditEvents: StateFlow<List<SecurityAuditEvent>>

    private var autoLockJob: Job? = null
    private var lockoutTimerJob: Job? = null
    private var lastUserActivityTimestamp = System.currentTimeMillis()

    init {
        val database = VaultDatabase.getDatabase(application)
        repository = VaultRepository(database.vaultDao())

        allItems = repository.allVaultItemsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

        allAiMessages = repository.allAiMessagesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

        allAuditEvents = repository.allAuditEventsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedInitialAuditEventsIfEmpty()
        }

        // Load initial preferences
        val onboardingDone = PinManager.isOnboardingCompleted(context)
        val theme = try { ThemeMode.valueOf(PinManager.getThemeMode(context)) } catch (e: Exception) { ThemeMode.DARK }
        val lang = try { AiLanguage.valueOf(PinManager.getAiLanguage(context)) } catch (e: Exception) { AiLanguage.AUTO }
        val bio = PinManager.isBiometricEnabled(context)
        val autoLockMillis = PinManager.getAutoLockTimeout(context)
        val autoLock = AutoLockTimeout.entries.find { it.millis == autoLockMillis } ?: AutoLockTimeout.ONE_MIN
        val clipSecs = PinManager.getClipboardTimeoutSeconds(context)
        val clip = ClipboardTimeout.entries.find { it.seconds == clipSecs } ?: ClipboardTimeout.THIRTY_SEC
        val lockoutSecs = PinManager.getRemainingLockoutSeconds(context)
        val recoveryConfigured = RecoveryManager.isRecoveryConfigured(context)
        val autofillEnabled = PinManager.isAutofillEnabled(context)
        val autofillAuthReq = PinManager.isAutofillAuthRequired(context)
        val systemAutofillActive = AutofillHelper.isAhmadGuardAutofillEnabled(context)
        val autofillSupported = AutofillHelper.isAutofillSupported(context)

        _uiState.update {
            it.copy(
                isOnboardingDone = onboardingDone,
                isUnlocked = false,
                themeMode = theme,
                aiLanguage = lang,
                biometricEnabled = bio,
                autoLockTimeout = autoLock,
                clipboardTimeout = clip,
                lockoutRemainingSeconds = lockoutSecs,
                isRecoveryConfigured = recoveryConfigured,
                isAutofillEnabled = autofillEnabled,
                isAutofillAuthRequired = autofillAuthReq,
                isSystemAutofillActive = systemAutofillActive,
                isAutofillSupported = autofillSupported
            )
        }

        if (lockoutSecs > 0) {
            startLockoutCountdown(lockoutSecs)
        }
    }


    // Dynamic Filtered Items - Offloaded to Dispatchers.Default for lag-free UI scrolling & search
    val filteredItems: StateFlow<List<VaultItem>> = combine(
        allItems,
        _uiState
    ) { items, state ->
        items.filter { item ->
            val matchesQuery = if (state.searchQuery.isBlank()) true else {
                val q = state.searchQuery.trim().lowercase()
                item.name.lowercase().contains(q) ||
                        item.url.lowercase().contains(q) ||
                        item.username.lowercase().contains(q) ||
                        item.email.lowercase().contains(q) ||
                        item.category.displayName.lowercase().contains(q) ||
                        item.tags.any { it.lowercase().contains(q) } ||
                        item.notes.lowercase().contains(q)
            }

            val matchesCategory = when (state.selectedCategory) {
                VaultCategory.ALL -> true
                VaultCategory.FAVORITES -> item.favorite
                else -> item.category == state.selectedCategory
            }

            val matchesType = state.selectedTypeFilter == null || item.type == state.selectedTypeFilter

            matchesQuery && matchesCategory && matchesType
        }
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Dynamic Watchtower Analysis - Background computed on Dispatchers.Default to prevent main-thread stutter
    val watchtowerAnalysis: StateFlow<Pair<SecurityScore, List<WatchtowerFinding>>> = combine(
        allItems
    ) { itemsArray ->
        val items = itemsArray[0]
        WatchtowerEngine.analyze(items)
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Pair(SecurityScore(), emptyList())
    )

    // Navigation
    fun navigateTo(destination: AppNavDestination) {
        recordUserActivity()
        _uiState.update { it.copy(currentDestination = destination) }
    }

    // Activity & Auto Lock
    fun recordUserActivity() {
        lastUserActivityTimestamp = System.currentTimeMillis()
        scheduleAutoLock()
    }

    private fun scheduleAutoLock() {
        autoLockJob?.cancel()
        val timeout = _uiState.value.autoLockTimeout
        if (timeout == AutoLockTimeout.NEVER || !_uiState.value.isUnlocked) return

        autoLockJob = viewModelScope.launch {
            delay(timeout.millis)
            lockVault()
        }
    }

    fun logSecurityEvent(
        type: AuditEventType,
        title: String,
        details: String,
        severity: AuditSeverity = type.defaultSeverity,
        contextInfo: String = "On-Device Keystore / Hardware Protected"
    ) {
        viewModelScope.launch {
            repository.logSecurityEvent(
                SecurityAuditEvent(
                    eventType = type,
                    title = title,
                    details = details,
                    severity = severity,
                    timestamp = System.currentTimeMillis(),
                    actor = "Vault Master",
                    contextInfo = contextInfo
                )
            )
        }
    }

    fun lockVault() {
        logSecurityEvent(AuditEventType.VAULT_LOCKED, "Vault Session Locked", "Vault automatically or manually locked.")
        _uiState.update {
            it.copy(
                isUnlocked = false,
                editingItem = null
            )
        }
    }

    fun unlockVault() {
        PinManager.resetFailedAttempts(context)
        lockoutTimerJob?.cancel()
        logSecurityEvent(AuditEventType.VAULT_UNLOCKED, "Vault Unlocked", "Session authorized via PIN / Biometrics.")
        _uiState.update { it.copy(isUnlocked = true, lockoutRemainingSeconds = 0) }
        recordUserActivity()
    }

    // PIN Operations
    fun verifyPin(pin: String): PinVerificationResult {
        val result = PinManager.verifyPin(context, pin)
        when (result) {
            is PinVerificationResult.Success -> {
                logSecurityEvent(AuditEventType.VAULT_UNLOCKED, "Master PIN Verified", "User authenticated with 6-digit Master PIN.", AuditSeverity.SUCCESS)
                unlockVault()
            }
            is PinVerificationResult.LockedOut -> {
                logSecurityEvent(AuditEventType.AUTH_FAILED, "Progressive Lockout Triggered", "Account locked for ${result.remainingSeconds}s after repeated failed attempts.", AuditSeverity.CRITICAL)
                startLockoutCountdown(result.remainingSeconds)
            }
            is PinVerificationResult.Failed -> {
                logSecurityEvent(AuditEventType.AUTH_FAILED, "PIN Authentication Failed", "Incorrect Master PIN entered (${result.attemptsRemainingBeforeLock} attempts left).", AuditSeverity.CRITICAL)
            }
            else -> {}
        }
        return result
    }

    fun completeOnboarding(
        pin: String,
        enableBiometric: Boolean,
        recoveryQ1: String? = null,
        recoveryA1: String? = null,
        recoveryQ2: String? = null,
        recoveryA2: String? = null,
        recoveryKey: String? = null
    ) {
        PinManager.setMasterPin(context, pin)
        PinManager.setBiometricEnabled(context, enableBiometric)
        PinManager.setOnboardingCompleted(context, true)

        var recoverySet = false
        if (!recoveryQ1.isNullOrBlank() && !recoveryA1.isNullOrBlank() &&
            !recoveryQ2.isNullOrBlank() && !recoveryA2.isNullOrBlank()
        ) {
            recoverySet = RecoveryManager.saveRecoverySetup(
                context,
                recoveryQ1,
                recoveryA1,
                recoveryQ2,
                recoveryA2,
                recoveryKey ?: RecoveryManager.generateNewRecoveryKey()
            )
            if (recoverySet) {
                logSecurityEvent(
                    AuditEventType.ACCOUNT_RECOVERY_CONFIGURED,
                    "Emergency Recovery Enabled",
                    "Security questions and recovery key configured during vault onboarding.",
                    AuditSeverity.SUCCESS
                )
            }
        }

        logSecurityEvent(AuditEventType.SYSTEM_INITIALIZED, "Master Vault Created", "Master PIN and Keystore initialized.", AuditSeverity.SUCCESS)
        _uiState.update {
            it.copy(
                isOnboardingDone = true,
                isUnlocked = true,
                biometricEnabled = enableBiometric,
                isRecoveryConfigured = recoverySet || RecoveryManager.isRecoveryConfigured(context)
            )
        }
        recordUserActivity()
    }

    fun changeMasterPin(newPin: String): Boolean {
        val success = PinManager.setMasterPin(context, newPin)
        if (success) {
            logSecurityEvent(AuditEventType.MASTER_PIN_CHANGED, "Master PIN Changed", "Primary authentication secret was rotated.", AuditSeverity.WARNING)
            showSnackbar("Master PIN updated successfully")
        }
        return success
    }

    // Emergency Account Recovery
    fun getRecoveryConfig(): RecoveryConfig {
        return RecoveryManager.getRecoveryConfig(context)
    }

    fun generateRecoveryKey(): String {
        return RecoveryManager.generateNewRecoveryKey()
    }

    fun saveEmergencyRecovery(
        q1: String,
        a1: String,
        q2: String,
        a2: String,
        key: String
    ): Boolean {
        val success = RecoveryManager.saveRecoverySetup(context, q1, a1, q2, a2, key)
        if (success) {
            _uiState.update { it.copy(isRecoveryConfigured = true) }
            logSecurityEvent(
                AuditEventType.ACCOUNT_RECOVERY_CONFIGURED,
                "Emergency Recovery Configured",
                "Recovery questions and emergency key saved with PBKDF2 salted hashes.",
                AuditSeverity.SUCCESS
            )
            showSnackbar("Emergency Account Recovery configured successfully")
        }
        return success
    }

    fun disableEmergencyRecovery() {
        RecoveryManager.disableRecovery(context)
        _uiState.update { it.copy(isRecoveryConfigured = false) }
        logSecurityEvent(
            AuditEventType.ACCOUNT_RECOVERY_DISABLED,
            "Emergency Recovery Disabled",
            "Security recovery questions and recovery key purged from device.",
            AuditSeverity.WARNING
        )
        showSnackbar("Emergency Account Recovery disabled")
    }

    fun verifyRecoveryQuestions(a1: String, a2: String): Boolean {
        return RecoveryManager.verifyQuestions(context, a1, a2)
    }

    fun verifyRecoveryKey(key: String): Boolean {
        return RecoveryManager.verifyRecoveryKey(context, key)
    }

    fun completeEmergencyRecovery(newPin: String): Boolean {
        val success = PinManager.setMasterPin(context, newPin)
        if (success) {
            PinManager.resetFailedAttempts(context)
            lockoutTimerJob?.cancel()
            _uiState.update { it.copy(lockoutRemainingSeconds = 0) }
            logSecurityEvent(
                AuditEventType.ACCOUNT_RECOVERED,
                "Vault Recovered",
                "Master PIN was securely reset via Emergency Account Recovery.",
                AuditSeverity.WARNING
            )
            showSnackbar("New Master PIN set successfully. Please unlock your vault.")
        }
        return success
    }

    private fun startLockoutCountdown(seconds: Int) {
        lockoutTimerJob?.cancel()
        _uiState.update { it.copy(lockoutRemainingSeconds = seconds) }
        lockoutTimerJob = viewModelScope.launch {
            var s = seconds
            while (s > 0) {
                delay(1000)
                s--
                _uiState.update { it.copy(lockoutRemainingSeconds = s) }
            }
        }
    }

    // Vault CRUD
    fun saveItem(item: VaultItem) {
        recordUserActivity()
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertItem(item)
                logSecurityEvent(AuditEventType.ITEM_CREATED, "Credential Added", "${item.type.displayName} '${item.name}' added to secure vault.", AuditSeverity.SUCCESS)
                showSnackbar("${item.type.displayName} '${item.name}' added to secure vault")
            } else {
                repository.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
                logSecurityEvent(AuditEventType.ITEM_UPDATED, "Credential Updated", "${item.type.displayName} '${item.name}' secret modified.", AuditSeverity.INFO)
                showSnackbar("${item.type.displayName} '${item.name}' updated")
            }
            _uiState.update { it.copy(editingItem = null, currentDestination = AppNavDestination.VAULT) }
        }
    }

    fun deleteItem(item: VaultItem) {
        recordUserActivity()
        viewModelScope.launch {
            repository.deleteItem(item)
            logSecurityEvent(AuditEventType.ITEM_DELETED, "Credential Deleted", "'${item.name}' permanently deleted from vault.", AuditSeverity.WARNING)
            showSnackbar("'${item.name}' removed from vault")
            if (_uiState.value.editingItem?.id == item.id) {
                _uiState.update { it.copy(editingItem = null, currentDestination = AppNavDestination.VAULT) }
            }
        }
    }

    fun toggleFavorite(item: VaultItem) {
        recordUserActivity()
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun setEditingItem(item: VaultItem?) {
        recordUserActivity()
        _uiState.update {
            it.copy(
                editingItem = item,
                currentDestination = if (item != null) AppNavDestination.EDIT_ITEM else it.currentDestination
            )
        }
    }

    fun setSearchQuery(query: String) {
        recordUserActivity()
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: VaultCategory) {
        recordUserActivity()
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSelectedTypeFilter(type: VaultType?) {
        recordUserActivity()
        _uiState.update { it.copy(selectedTypeFilter = type) }
    }

    // Copy to Clipboard
    fun copyField(label: String, value: String) {
        recordUserActivity()
        val timeoutSecs = _uiState.value.clipboardTimeout.seconds
        val success = ClipboardHelper.copyToClipboard(context, label, value, timeoutSecs)
        if (success) {
            logSecurityEvent(AuditEventType.PASSWORD_COPIED, "Clipboard Decryption Access", "Secret field '$label' copied with ${timeoutSecs}s auto-clear timer.", AuditSeverity.INFO)
            val timeoutNotice = if (timeoutSecs > 0) " • Auto-clears in ${timeoutSecs}s" else ""
            val message = if (label.contains("Secret", ignoreCase = true) || label.contains("Password", ignoreCase = true)) {
                "Password Copied$timeoutNotice"
            } else {
                "$label Copied$timeoutNotice"
            }
            showSnackbar(message)
        }
    }


    // Gemini AI Integration
    fun sendAiMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isEmpty()) return

        recordUserActivity()
        val userMsgId = UUID.randomUUID().toString()
        val userMsg = AiMessage(
            id = userMsgId,
            sender = MessageSender.USER,
            text = trimmed,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.saveAiMessage(userMsg)
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val currentLang = _uiState.value.aiLanguage
                val systemPrompt = GeminiApiClient.getSystemPrompt(currentLang)

                // Build context from recent messages
                val recentMessages = allAiMessages.value.takeLast(6)
                val contentsList = mutableListOf<GeminiContent>()

                for (msg in recentMessages) {
                    val role = if (msg.sender == MessageSender.USER) "user" else "model"
                    contentsList.add(
                        GeminiContent(
                            role = role,
                            parts = listOf(GeminiPart(text = msg.text))
                        )
                    )
                }

                // Append the new message
                contentsList.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = trimmed))
                    )
                )

                val request = GeminiRequest(
                    contents = contentsList,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.7f,
                        topP = 0.95f,
                        topK = 40
                    )
                )

                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!responseText.isNullOrBlank()) {
                    val aiMsg = AiMessage(
                        id = UUID.randomUUID().toString(),
                        sender = MessageSender.AI,
                        text = responseText.trim(),
                        timestamp = System.currentTimeMillis()
                    )
                    repository.saveAiMessage(aiMsg)
                    _uiState.update { it.copy(isAiLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            isAiLoading = false,
                            aiErrorMessage = "I couldn't generate a response right now. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                        "No Internet Connection"
                    e.message?.contains("400") == true || e.message?.contains("403") == true ->
                        "AI service configuration is unavailable."
                    else ->
                        "AI service is temporarily unavailable. Please try again later."
                }
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        aiErrorMessage = errorMsg
                    )
                }
            }
        }
    }

    fun clearAiHistory() {
        recordUserActivity()
        viewModelScope.launch {
            repository.clearAiMessages()
            showSnackbar("AI conversation history cleared")
        }
    }

    // Settings
    fun setThemeMode(mode: ThemeMode) {
        PinManager.setThemeMode(context, mode.name)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setAiLanguage(language: AiLanguage) {
        PinManager.setAiLanguage(context, language.name)
        _uiState.update { it.copy(aiLanguage = language) }
        showSnackbar("AI Language set to ${language.displayName}")
    }

    fun setBiometricEnabled(enabled: Boolean) {
        PinManager.setBiometricEnabled(context, enabled)
        _uiState.update { it.copy(biometricEnabled = enabled) }
        showSnackbar(if (enabled) "Biometric unlock enabled" else "Biometric unlock disabled")
    }

    fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        PinManager.setAutoLockTimeout(context, timeout.millis)
        _uiState.update { it.copy(autoLockTimeout = timeout) }
        scheduleAutoLock()
        showSnackbar("Auto Lock set to ${timeout.displayName}")
    }

    fun setClipboardTimeout(timeout: ClipboardTimeout) {
        PinManager.setClipboardTimeoutSeconds(context, timeout.seconds)
        _uiState.update { it.copy(clipboardTimeout = timeout) }
        showSnackbar("Clipboard Auto Clear set to ${timeout.displayName}")
    }

    // Autofill Controls
    fun refreshAutofillStatus() {
        val systemActive = AutofillHelper.isAhmadGuardAutofillEnabled(context)
        _uiState.update { it.copy(isSystemAutofillActive = systemActive) }
    }

    fun setAutofillEnabled(enabled: Boolean) {
        PinManager.setAutofillEnabled(context, enabled)
        _uiState.update { it.copy(isAutofillEnabled = enabled) }
        showSnackbar(if (enabled) "Cyber Guard Autofill service enabled" else "Autofill service disabled")
    }

    fun setAutofillAuthRequired(required: Boolean) {
        PinManager.setAutofillAuthRequired(context, required)
        _uiState.update { it.copy(isAutofillAuthRequired = required) }
        showSnackbar(if (required) "Master PIN authentication required before filling credentials" else "Quick fill without Master PIN verification")
    }

    fun openSystemAutofillSettings() {
        AutofillHelper.openAutofillSettings(context)
    }

    suspend fun exportBackupJson(): String {
        return repository.exportEncryptedBackup()
    }

    suspend fun restoreBackupJson(json: String): BackupValidationResult {
        val result = repository.restoreEncryptedBackup(json)
        if (result is BackupValidationResult.Valid) {
            showSnackbar("Successfully restored ${result.itemCount} items to vault")
        }
        return result
    }

    suspend fun exportAuditLogJson(): String {
        val result = repository.exportEncryptedAuditLog()
        logSecurityEvent(
            AuditEventType.AUDIT_LOG_EXPORTED,
            "Encrypted Audit Log Exported",
            "Security events archive generated with SHA-256 HMAC and AES-256-GCM encryption.",
            AuditSeverity.SUCCESS
        )
        return result
    }

    fun clearAuditLog() {
        viewModelScope.launch {
            repository.clearAuditLog()
            // Immediately log the clearing event
            logSecurityEvent(
                AuditEventType.SYSTEM_INITIALIZED,
                "Audit Log Purged",
                "Historical security audit records were cleared by the vault master.",
                AuditSeverity.WARNING
            )
            showSnackbar("Security audit log cleared")
        }
    }

    fun clearAllVault() {
        viewModelScope.launch {
            repository.clearAllVaultData()
            showSnackbar("All vault data cleared permanently")
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
