package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.model.AppNavDestination
import com.example.model.ThemeMode
import com.example.model.VaultItem
import com.example.ui.components.AhmadGuardBottomNav
import com.example.ui.components.FrostedGlassBackground
import com.example.ui.screens.AddEditItemScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SecurityScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.screens.WatchtowerScreen
import com.example.ui.theme.AhmadGuardTheme
import com.example.viewmodel.VaultViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val allItems by viewModel.allItems.collectAsState()
            val filteredItems by viewModel.filteredItems.collectAsState()
            val watchtowerAnalysis by viewModel.watchtowerAnalysis.collectAsState()
            val aiMessages by viewModel.allAiMessages.collectAsState()
            val auditEvents by viewModel.allAuditEvents.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearSnackbar()
                }
            }

            AhmadGuardTheme(themeMode = uiState.themeMode) {
                FrostedGlassBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        bottomBar = {
                            if (uiState.isOnboardingDone && uiState.isUnlocked &&
                                uiState.currentDestination != AppNavDestination.ADD &&
                                uiState.currentDestination != AppNavDestination.EDIT_ITEM &&
                                uiState.currentDestination != AppNavDestination.AI_ASSISTANT &&
                                uiState.currentDestination != AppNavDestination.SECURITY_SCANNER &&
                                uiState.currentDestination != AppNavDestination.WATCHTOWER &&
                                uiState.currentDestination != AppNavDestination.AUDIT_LOG
                            ) {
                                AhmadGuardBottomNav(
                                    currentDestination = uiState.currentDestination,
                                    onNavigate = { destination ->
                                        if (destination == AppNavDestination.ADD) {
                                            viewModel.setEditingItem(null)
                                            viewModel.navigateTo(AppNavDestination.ADD)
                                        } else {
                                            viewModel.navigateTo(destination)
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                        AnimatedContent(
                            targetState = Triple(uiState.isOnboardingDone, uiState.isUnlocked, uiState.currentDestination),
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                        scaleIn(initialScale = 0.985f, animationSpec = tween(200, easing = FastOutSlowInEasing)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                                                scaleOut(targetScale = 1.01f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                                    )
                            },
                            label = "main_nav"
                        ) { (onboarded, unlocked, destination) ->
                            when {
                                !onboarded -> {
                                    OnboardingScreen(
                                        onComplete = { pin, bio, q1, a1, q2, a2, key ->
                                            viewModel.completeOnboarding(pin, bio, q1, a1, q2, a2, key)
                                        }
                                    )
                                }
                                !unlocked -> {
                                    LockScreen(
                                        lockoutSeconds = uiState.lockoutRemainingSeconds,
                                        biometricEnabled = uiState.biometricEnabled,
                                        recoveryConfig = viewModel.getRecoveryConfig(),
                                        onVerifyPin = { pin -> viewModel.verifyPin(pin) },
                                        onBiometricUnlock = { viewModel.unlockVault() },
                                        onVerifyRecoveryQuestions = { a1, a2 -> viewModel.verifyRecoveryQuestions(a1, a2) },
                                        onVerifyRecoveryKey = { key -> viewModel.verifyRecoveryKey(key) },
                                        onCompleteRecovery = { newPin -> viewModel.completeEmergencyRecovery(newPin) }
                                    )
                                }
                                else -> {
                                    when (destination) {
                                        AppNavDestination.HOME -> {
                                            HomeScreen(
                                                securityScore = watchtowerAnalysis.first,
                                                vaultItems = allItems,
                                                themeMode = uiState.themeMode,
                                                onThemeToggle = {
                                                    val next = when (uiState.themeMode) {
                                                        ThemeMode.DARK -> ThemeMode.AMOLED
                                                        ThemeMode.AMOLED -> ThemeMode.LIGHT
                                                        ThemeMode.LIGHT -> ThemeMode.DARK
                                                    }
                                                    viewModel.setThemeMode(next)
                                                },
                                                onLockClick = { viewModel.lockVault() },
                                                onNavigate = { dest ->
                                                    if (dest == AppNavDestination.ADD) {
                                                        viewModel.setEditingItem(null)
                                                    }
                                                    viewModel.navigateTo(dest)
                                                },
                                                onItemClick = { item ->
                                                    viewModel.setEditingItem(item)
                                                },
                                                onFavoriteToggle = { item ->
                                                    viewModel.toggleFavorite(item)
                                                },
                                                onCopyPassword = { item ->
                                                    val secret = if (item.password.isNotBlank()) item.password else item.sensitiveData
                                                    viewModel.copyField("${item.name} Secret", secret)
                                                },
                                                onCopyUsername = { item ->
                                                    val user = if (item.username.isNotBlank()) item.username else item.email
                                                    viewModel.copyField("${item.name} Username", user)
                                                },
                                                onEditItem = { item ->
                                                    viewModel.setEditingItem(item)
                                                },
                                                onDeleteItem = { item ->
                                                    viewModel.deleteItem(item)
                                                }
                                            )
                                        }

                                        AppNavDestination.VAULT -> {
                                            VaultScreen(
                                                items = filteredItems,
                                                searchQuery = uiState.searchQuery,
                                                selectedCategory = uiState.selectedCategory,
                                                selectedTypeFilter = uiState.selectedTypeFilter,
                                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                                onCategorySelect = { viewModel.setSelectedCategory(it) },
                                                onTypeFilterSelect = { viewModel.setSelectedTypeFilter(it) },
                                                onItemClick = { item -> viewModel.setEditingItem(item) },
                                                onFavoriteToggle = { item -> viewModel.toggleFavorite(item) },
                                                onCopyPassword = { item ->
                                                    val secret = if (item.password.isNotBlank()) item.password else item.sensitiveData
                                                    viewModel.copyField("${item.name} Secret", secret)
                                                },
                                                onCopyUsername = { item ->
                                                    val user = if (item.username.isNotBlank()) item.username else item.email
                                                    viewModel.copyField("${item.name} Username", user)
                                                },
                                                onEditItem = { item -> viewModel.setEditingItem(item) },
                                                onDeleteItem = { item -> viewModel.deleteItem(item) },
                                                onAddNewItem = {
                                                    viewModel.setEditingItem(null)
                                                    viewModel.navigateTo(AppNavDestination.ADD)
                                                }
                                            )
                                        }

                                        AppNavDestination.ADD, AppNavDestination.EDIT_ITEM -> {
                                            AddEditItemScreen(
                                                initialItem = uiState.editingItem,
                                                onSave = { item -> viewModel.saveItem(item) },
                                                onDelete = { item -> viewModel.deleteItem(item) },
                                                onCancel = {
                                                    viewModel.setEditingItem(null)
                                                    viewModel.navigateTo(AppNavDestination.VAULT)
                                                }
                                            )
                                        }

                                        AppNavDestination.GENERATOR -> {
                                            GeneratorScreen(
                                                onCopyPassword = { pass ->
                                                    viewModel.copyField("Generated Password", pass)
                                                },
                                                onSaveToVault = { item ->
                                                    viewModel.setEditingItem(item)
                                                    viewModel.navigateTo(AppNavDestination.ADD)
                                                }
                                            )
                                        }

                                        AppNavDestination.WATCHTOWER -> {
                                            WatchtowerScreen(
                                                securityScore = watchtowerAnalysis.first,
                                                findings = watchtowerAnalysis.second,
                                                allItems = allItems,
                                                onFixItem = { item ->
                                                    viewModel.setEditingItem(item)
                                                },
                                                onBack = {
                                                    viewModel.navigateTo(AppNavDestination.HOME)
                                                }
                                            )
                                        }

                                        AppNavDestination.AI_ASSISTANT -> {
                                            AiAssistantScreen(
                                                messages = aiMessages,
                                                isLoading = uiState.isAiLoading,
                                                errorMessage = uiState.aiErrorMessage,
                                                aiLanguage = uiState.aiLanguage,
                                                onSendMessage = { prompt ->
                                                    viewModel.sendAiMessage(prompt)
                                                },
                                                onClearHistory = {
                                                    viewModel.clearAiHistory()
                                                },
                                                onLanguageChange = { lang ->
                                                    viewModel.setAiLanguage(lang)
                                                },
                                                onCopyText = { text ->
                                                    viewModel.copyField("AI Response", text)
                                                },
                                                onBack = {
                                                    viewModel.navigateTo(AppNavDestination.HOME)
                                                }
                                            )
                                        }

                                        AppNavDestination.SECURITY_SCANNER -> {
                                            SecurityScannerScreen(
                                                onSaveToVault = { item ->
                                                    viewModel.setEditingItem(item)
                                                    viewModel.navigateTo(AppNavDestination.ADD)
                                                },
                                                onBack = {
                                                    viewModel.navigateTo(AppNavDestination.HOME)
                                                }
                                            )
                                        }

                                        AppNavDestination.SETTINGS -> {
                                            SettingsScreen(
                                                themeMode = uiState.themeMode,
                                                aiLanguage = uiState.aiLanguage,
                                                biometricEnabled = uiState.biometricEnabled,
                                                autoLockTimeout = uiState.autoLockTimeout,
                                                clipboardTimeout = uiState.clipboardTimeout,
                                                isAutofillEnabled = uiState.isAutofillEnabled,
                                                isAutofillAuthRequired = uiState.isAutofillAuthRequired,
                                                isSystemAutofillActive = uiState.isSystemAutofillActive,
                                                isAutofillSupported = uiState.isAutofillSupported,
                                                isRecoveryConfigured = uiState.isRecoveryConfigured,
                                                recoveryConfig = viewModel.getRecoveryConfig(),
                                                onThemeChange = { viewModel.setThemeMode(it) },
                                                onAiLanguageChange = { viewModel.setAiLanguage(it) },
                                                onBiometricChange = { viewModel.setBiometricEnabled(it) },
                                                onAutoLockChange = { viewModel.setAutoLockTimeout(it) },
                                                onClipboardTimeoutChange = { viewModel.setClipboardTimeout(it) },
                                                onAutofillEnabledChange = { viewModel.setAutofillEnabled(it) },
                                                onAutofillAuthRequiredChange = { viewModel.setAutofillAuthRequired(it) },
                                                onOpenSystemAutofillSettings = { viewModel.openSystemAutofillSettings() },
                                                onRefreshAutofillStatus = { viewModel.refreshAutofillStatus() },
                                                onChangeMasterPin = { old, new -> viewModel.changeMasterPin(new) },
                                                onSaveRecovery = { q1, a1, q2, a2, key ->
                                                    viewModel.saveEmergencyRecovery(q1, a1, q2, a2, key)
                                                },
                                                onDisableRecovery = { viewModel.disableEmergencyRecovery() },
                                                onGenerateRecoveryKey = { viewModel.generateRecoveryKey() },
                                                onExportBackup = { viewModel.exportBackupJson() },
                                                onRestoreBackup = { json -> viewModel.restoreBackupJson(json) },
                                                onNavigateToAuditLog = { viewModel.navigateTo(AppNavDestination.AUDIT_LOG) },
                                                onClearAllVault = { viewModel.clearAllVault() },
                                                onClearAiHistory = { viewModel.clearAiHistory() },
                                                onShowSnackbar = { viewModel.showSnackbar(it) }
                                            )
                                        }

                                        AppNavDestination.AUDIT_LOG -> {
                                            AuditLogScreen(
                                                auditEvents = auditEvents,
                                                onExportAuditLog = { viewModel.exportAuditLogJson() },
                                                onClearAuditLog = { viewModel.clearAuditLog() },
                                                onShowSnackbar = { viewModel.showSnackbar(it) },
                                                onBack = { viewModel.navigateTo(AppNavDestination.SETTINGS) }
                                            )
                                        }

                                        else -> {
                                            viewModel.navigateTo(AppNavDestination.HOME)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
