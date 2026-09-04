package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.ui.animation.pressScale
import com.example.ui.animation.tactileClickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiLanguage
import com.example.model.AutoLockTimeout
import com.example.model.ClipboardTimeout
import com.example.model.ThemeMode
import com.example.security.BackupValidationResult
import com.example.security.PinManager
import com.example.security.RecoveryConfig
import com.example.security.RecoveryManager
import com.example.ui.components.ConfirmationDialog
import com.example.ui.screens.EmergencyRecoverySetupDialog
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassSurface
import com.example.ui.theme.FrostedGlassSurfaceVariant
import com.example.ui.theme.NebulaBlue
import com.example.ui.theme.NebulaFuchsia
import com.example.ui.theme.NebulaIndigo
import com.example.ui.theme.SecurityAmber
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextIndigoSubtle
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    aiLanguage: AiLanguage,
    biometricEnabled: Boolean,
    autoLockTimeout: AutoLockTimeout,
    clipboardTimeout: ClipboardTimeout,
    isAutofillEnabled: Boolean,
    isAutofillAuthRequired: Boolean,
    isSystemAutofillActive: Boolean,
    isAutofillSupported: Boolean,
    isRecoveryConfigured: Boolean,
    recoveryConfig: RecoveryConfig,
    onThemeChange: (ThemeMode) -> Unit,
    onAiLanguageChange: (AiLanguage) -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onAutoLockChange: (AutoLockTimeout) -> Unit,
    onClipboardTimeoutChange: (ClipboardTimeout) -> Unit,
    onAutofillEnabledChange: (Boolean) -> Unit,
    onAutofillAuthRequiredChange: (Boolean) -> Unit,
    onOpenSystemAutofillSettings: () -> Unit,
    onRefreshAutofillStatus: () -> Unit,
    onChangeMasterPin: (oldPin: String, newPin: String) -> Boolean,
    onSaveRecovery: (q1: String, a1: String, q2: String, a2: String, key: String) -> Boolean,
    onDisableRecovery: () -> Unit,
    onGenerateRecoveryKey: () -> String,
    onExportBackup: suspend () -> String,
    onRestoreBackup: suspend (String) -> BackupValidationResult,
    onNavigateToAuditLog: () -> Unit,
    onClearAllVault: () -> Unit,
    onClearAiHistory: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showRecoverySetupDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showClearVaultDialog by remember { mutableStateOf(false) }
    var showAutofillGuideDialog by remember { mutableStateOf(false) }
    var exportedBackupJson by remember { mutableStateOf("") }
    var restoreJsonInput by remember { mutableStateOf("") }
    var restoreValidationResult by remember { mutableStateOf<BackupValidationResult?>(null) }

    // Change Master PIN Dialog
    if (showChangePinDialog) {
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = {
                Text(
                    text = "CHANGE MASTER PIN",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhitePrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { oldPin = it },
                        label = { Text("Current Master PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaIndigo,
                            unfocusedBorderColor = FrostedGlassBorderLight,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("New Master PIN (4-6 digits)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaIndigo,
                            unfocusedBorderColor = FrostedGlassBorderLight,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it },
                        label = { Text("Confirm New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaIndigo,
                            unfocusedBorderColor = FrostedGlassBorderLight,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMsg != null) {
                        Text(text = errorMsg!!, style = MaterialTheme.typography.bodySmall, color = SecurityRed)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!PinManager.isPinCorrect(context, oldPin)) {
                            errorMsg = "Current PIN is incorrect."
                        } else if (newPin.length < 4) {
                            errorMsg = "New PIN must be at least 4 digits."
                        } else if (newPin != confirmPin) {
                            errorMsg = "New PINs do not match."
                        } else {
                            val success = onChangeMasterPin(oldPin, newPin)
                            if (success) {
                                showChangePinDialog = false
                            } else {
                                errorMsg = "Could not update PIN."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaIndigo, contentColor = Color.White)
                ) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel", color = TextMutedSecondary)
                }
            },
            containerColor = Color(0xFF141624),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Emergency Account Recovery Setup Dialog
    if (showRecoverySetupDialog) {
        EmergencyRecoverySetupDialog(
            initialConfig = recoveryConfig,
            onSave = { q1, a1, q2, a2, key ->
                val success = onSaveRecovery(q1, a1, q2, a2, key)
                if (success) {
                    onShowSnackbar("Emergency Account Recovery configured.")
                }
                success
            },
            onDisable = {
                onDisableRecovery()
                onShowSnackbar("Emergency Account Recovery disabled.")
            },
            onGenerateKey = onGenerateRecoveryKey,
            onDismiss = { showRecoverySetupDialog = false }
        )
    }

    // Export Backup Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = NebulaIndigo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENCRYPTED BACKUP",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhitePrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your vault has been serialized into an encrypted format protected with checksum integrity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FrostedGlassSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedGlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = exportedBackupJson.take(180) + if (exportedBackupJson.length > 180) "\n... [Encrypted Backup Data]" else "",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            color = TextWhitePrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Cyber Guard Backup", exportedBackupJson))
                        onShowSnackbar("Encrypted backup payload copied to clipboard")
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaIndigo, contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Backup JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Done", color = TextMutedSecondary)
                }
            },
            containerColor = Color(0xFF141624),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = "RESTORE ENCRYPTED BACKUP",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhitePrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Paste the Cyber Guard backup JSON string to restore your vault items.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedSecondary
                    )

                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = {
                            restoreJsonInput = it
                            restoreValidationResult = null
                        },
                        placeholder = { Text("Paste JSON here...", color = TextMutedSecondary) },
                        minLines = 4,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaIndigo,
                            unfocusedBorderColor = FrostedGlassBorderLight,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (restoreValidationResult is BackupValidationResult.Invalid) {
                        Text(
                            text = (restoreValidationResult as BackupValidationResult.Invalid).reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecurityRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = onRestoreBackup(restoreJsonInput.trim())
                            restoreValidationResult = result
                            if (result is BackupValidationResult.Valid) {
                                showRestoreDialog = false
                            }
                        }
                    },
                    enabled = restoreJsonInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaIndigo, contentColor = Color.White)
                ) {
                    Text("Validate & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel", color = TextMutedSecondary)
                }
            },
            containerColor = Color(0xFF141624),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Clear Vault Confirm Dialog
    if (showClearVaultDialog) {
        ConfirmationDialog(
            title = "ERASE ALL VAULT DATA?",
            message = "This will permanently delete all passwords, cards, identities, notes, and AI messages. This action CANNOT be undone.",
            confirmText = "Erase Everything",
            isDestructive = true,
            onConfirm = {
                showClearVaultDialog = false
                onClearAllVault()
            },
            onDismiss = { showClearVaultDialog = false }
        )
    }

    // Autofill Security & Instructions Dialog
    if (showAutofillGuideDialog) {
        AlertDialog(
            onDismissRequest = { showAutofillGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NebulaIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = NebulaIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NATIVE AUTOFILL GUIDE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhitePrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Cyber Guard integrates directly with the official Android Autofill framework (API 26+) for seamless, zero-knowledge credential insertion.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextMutedSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FrostedGlassSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedGlassBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "HOW IT WORKS IN 4 STEPS:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NebulaIndigo
                            )
                            Text(
                                text = "1. Save login accounts in Cyber Guard (e.g. Instagram, Google, Facebook).\n" +
                                        "2. Open the app (like Instagram) and tap the username/email or password field.\n" +
                                        "3. Tap 'Cyber Guard' from the keyboard or dropdown autofill prompt.\n" +
                                        "4. Enter your Master PIN or use Biometric unlock to securely inject credentials.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = TextWhitePrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SecurityGreen.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecurityGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SecurityGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Zero-Knowledge: Passwords stay hardware-encrypted at rest and are only decrypted upon explicit PIN/Biometric authorization.",
                                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp),
                                color = TextWhitePrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAutofillGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaIndigo, contentColor = Color.White)
                ) {
                    Text("Got It")
                }
            },
            containerColor = Color(0xFF141624),
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Top Bar
        Surface(
            color = FrostedGlassSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FrostedGlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTINGS & SECURITY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextWhitePrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // SECTION: SECURITY & ACCESS
            item {
                SettingsSectionHeader(title = "SECURITY & ACCESS")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(22.dp))
                ) {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Default.Key,
                            title = "Change Master PIN",
                            subtitle = "Update your vault's primary unlocking PIN",
                            onClick = { showChangePinDialog = true }
                        )

                        SettingsDivider()

                        SettingsSwitchRow(
                            icon = Icons.Default.Fingerprint,
                            title = "Biometric Unlock",
                            subtitle = "Unlock with Fingerprint or Face ID",
                            checked = biometricEnabled,
                            onCheckedChange = onBiometricChange
                        )

                        SettingsDivider()

                        var autoLockDropdown by remember { mutableStateOf(false) }
                        Box {
                            SettingsClickableRow(
                                icon = Icons.Default.Timer,
                                title = "Auto-Lock Timeout",
                                subtitle = autoLockTimeout.displayName,
                                onClick = { autoLockDropdown = true }
                            )
                            DropdownMenu(
                                expanded = autoLockDropdown,
                                onDismissRequest = { autoLockDropdown = false }
                            ) {
                                AutoLockTimeout.entries.forEach { timeout ->
                                    DropdownMenuItem(
                                        text = { Text(timeout.displayName) },
                                        onClick = {
                                            onAutoLockChange(timeout)
                                            autoLockDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        SettingsDivider()

                        var clipDropdown by remember { mutableStateOf(false) }
                        Box {
                            SettingsClickableRow(
                                icon = Icons.Default.ContentCopy,
                                title = "Clipboard Auto-Clear",
                                subtitle = clipboardTimeout.displayName,
                                onClick = { clipDropdown = true }
                            )
                            DropdownMenu(
                                expanded = clipDropdown,
                                onDismissRequest = { clipDropdown = false }
                            ) {
                                ClipboardTimeout.entries.forEach { timeout ->
                                    DropdownMenuItem(
                                        text = { Text(timeout.displayName) },
                                        onClick = {
                                            onClipboardTimeoutChange(timeout)
                                            clipDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        SettingsDivider()

                        SettingsClickableRow(
                            icon = Icons.Default.Key,
                            title = "Emergency Account Recovery",
                            subtitle = if (isRecoveryConfigured) "Configured (2 Questions & Recovery Key)" else "Not configured (Tap to setup)",
                            onClick = { showRecoverySetupDialog = true }
                        )

                        SettingsDivider()

                        SettingsClickableRow(
                            icon = Icons.Default.Shield,
                            title = "Security Audit Log",
                            subtitle = "View & export tamper-evident event log (Room DB)",
                            onClick = onNavigateToAuditLog
                        )
                    }
                }
            }

            // SECTION: ANDROID AUTOFILL SERVICE
            item {
                SettingsSectionHeader(title = "NATIVE ANDROID AUTOFILL")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(22.dp))
                ) {
                    Column {
                        // System Status Card
                        Surface(
                            color = if (isSystemAutofillActive) SecurityGreen.copy(alpha = 0.12f) else SecurityAmber.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isSystemAutofillActive) SecurityGreen else SecurityAmber)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isSystemAutofillActive) "System Autofill Active" else "Autofill Not Enabled in System",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSystemAutofillActive) SecurityGreen else SecurityAmber
                                        )
                                        Text(
                                            text = if (isSystemAutofillActive) "Cyber Guard is selected as default provider" else "Tap Configure to select Cyber Guard in Android Settings",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMutedSecondary
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        onOpenSystemAutofillSettings()
                                        onRefreshAutofillStatus()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSystemAutofillActive) SecurityGreen else SecurityAmber)
                                ) {
                                    Text(
                                        text = if (isSystemAutofillActive) "System Settings" else "Configure",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSystemAutofillActive) SecurityGreen else SecurityAmber
                                    )
                                }
                            }
                        }

                        SettingsDivider()

                        SettingsSwitchRow(
                            icon = Icons.Default.Key,
                            title = "Enable Autofill Engine",
                            subtitle = "Detect and autofill credentials in Instagram, Google, Facebook & apps",
                            checked = isAutofillEnabled,
                            onCheckedChange = onAutofillEnabledChange
                        )

                        SettingsDivider()

                        SettingsSwitchRow(
                            icon = Icons.Default.Lock,
                            title = "Require Authentication for Autofill",
                            subtitle = "Prompt for Master PIN / Biometric before injecting credentials",
                            checked = isAutofillAuthRequired,
                            onCheckedChange = onAutofillAuthRequiredChange
                        )

                        SettingsDivider()

                        SettingsClickableRow(
                            icon = Icons.Default.Security,
                            title = "Autofill Security Guide & Setup",
                            subtitle = "Learn how zero-knowledge autofill works with third-party apps",
                            onClick = { showAutofillGuideDialog = true }
                        )
                    }
                }
            }

            // SECTION: BACKUP & DATA
            item {
                SettingsSectionHeader(title = "ENCRYPTED BACKUP & AUDIT")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(22.dp))
                ) {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Default.CloudUpload,
                            title = "Export Encrypted Backup",
                            subtitle = "Generate a secure JSON backup of your vault",
                            onClick = {
                                coroutineScope.launch {
                                    exportedBackupJson = onExportBackup()
                                    showExportDialog = true
                                }
                            }
                        )

                        SettingsDivider()

                        SettingsClickableRow(
                            icon = Icons.Default.CloudDownload,
                            title = "Restore from Encrypted Backup",
                            subtitle = "Import and merge credentials from a backup file",
                            onClick = {
                                restoreJsonInput = ""
                                restoreValidationResult = null
                                showRestoreDialog = true
                            }
                        )

                        SettingsDivider()

                        SettingsClickableRow(
                            icon = Icons.Default.Security,
                            title = "Export Security Audit Trail",
                            subtitle = "AES-256-GCM sealed Room database security events",
                            onClick = onNavigateToAuditLog
                        )
                    }
                }
            }

            // SECTION: AI & APPEARANCE
            item {
                SettingsSectionHeader(title = "AI & APPEARANCE")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(22.dp))
                ) {
                    Column {
                        var themeDropdown by remember { mutableStateOf(false) }
                        Box {
                            SettingsClickableRow(
                                icon = Icons.Default.Palette,
                                title = "Appearance Theme",
                                subtitle = "Frosted Glass (Active)",
                                onClick = { themeDropdown = true }
                            )
                            DropdownMenu(
                                expanded = themeDropdown,
                                onDismissRequest = { themeDropdown = false }
                            ) {
                                ThemeMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (mode) {
                                                    ThemeMode.DARK -> "Frosted Glass Nebula"
                                                    ThemeMode.AMOLED -> "AMOLED Pure Black"
                                                    ThemeMode.LIGHT -> "Frosted Glass Light"
                                                }
                                            )
                                        },
                                        onClick = {
                                            onThemeChange(mode)
                                            themeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        SettingsDivider()

                        var aiLangDropdown by remember { mutableStateOf(false) }
                        Box {
                            SettingsClickableRow(
                                icon = Icons.Default.Language,
                                title = "AI Assistant Language",
                                subtitle = aiLanguage.displayName,
                                onClick = { aiLangDropdown = true }
                            )
                            DropdownMenu(
                                expanded = aiLangDropdown,
                                onDismissRequest = { aiLangDropdown = false }
                            ) {
                                AiLanguage.entries.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang.displayName) },
                                        onClick = {
                                            onAiLanguageChange(lang)
                                            aiLangDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        SettingsDivider()

                        SettingsClickableRow(
                            icon = Icons.Default.AutoAwesome,
                            title = "Clear AI Chat History",
                            subtitle = "Erase past questions and security advice",
                            onClick = onClearAiHistory
                        )
                    }
                }
            }

            // SECTION: ABOUT & CONNECT WITH AHMAD
            item {
                SettingsSectionHeader(title = "ABOUT & DEVELOPER")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, NebulaIndigo.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(NebulaIndigo.copy(alpha = 0.2f))
                                    .border(1.dp, NebulaIndigo.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = NebulaIndigo,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Cyber Guard v1.0.0",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = "Zero-Knowledge Personal Cyber Vault",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = NebulaIndigo
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Engineered with AES-256-GCM hardware key encryption, PBKDF2 Master PIN hashing, on-device Watchtower cybersecurity scoring, and real-time AI security guidance.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = TextMutedSecondary
                        )

                    }
                }
            }

            // SECTION: DANGER ZONE
            item {
                SettingsSectionHeader(title = "DANGER ZONE")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, SecurityRed.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SecurityRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Erase All Vault Data",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SecurityRed
                                )
                                Text(
                                    text = "Permanently delete all stored credentials and history",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SecurityRed.copy(alpha = 0.15f))
                                .border(1.dp, SecurityRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { showClearVaultDialog = true }
                                .padding(vertical = 11.dp)
                                .testTag("erase_vault_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Erase All Data Permanently",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = SecurityRed
                            )
                        }
                    }
                }
            }

            // SECTION: CONNECT WITH AHMAD (Absolute Bottom of Settings)
            item {
                Spacer(modifier = Modifier.height(14.dp))

                SettingsSectionHeader(title = "CONNECT WITH AHMAD")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Connect with Ahmad",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhitePrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Official channels for security advisories, release notes, and community updates.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = TextMutedSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Row 1: YouTube & Instagram
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // YouTube Button
                            SocialChannelButton(
                                name = "YouTube",
                                handle = "Watch Channel",
                                accentColor = Color(0xFFFF0000),
                                icon = {
                                    YouTubeIcon(modifier = Modifier.size(24.dp))
                                },
                                onClick = {
                                    openSocialLink(
                                        context = context,
                                        appUriString = "vnd.youtube:https://youtube.com/@codevision-m5t?si=UnLrGIW0fobetbFtH",
                                        webUrlString = "https://youtube.com/@codevision-m5t?si=UnLrGIW0fobetbFtH",
                                        appPackage = "com.google.android.youtube",
                                        onShowSnackbar = onShowSnackbar
                                    )
                                },
                                testTag = "connect_youtube_button"
                            )

                            // Instagram Button
                            SocialChannelButton(
                                name = "Instagram",
                                handle = "@ahmad__gada_",
                                accentColor = Color(0xFFE1306C),
                                icon = {
                                    InstagramIcon(modifier = Modifier.size(24.dp))
                                },
                                onClick = {
                                    openSocialLink(
                                        context = context,
                                        appUriString = "http://instagram.com/_u/ahmad__gada_",
                                        webUrlString = "https://www.instagram.com/ahmad__gada_?igsh=dHZqamdmOHNmc3U3",
                                        appPackage = "com.instagram.android",
                                        onShowSnackbar = onShowSnackbar
                                    )
                                },
                                testTag = "connect_instagram_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Row 2: Telegram Button Centered
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SocialChannelButton(
                                name = "Telegram",
                                handle = "Join Community",
                                accentColor = Color(0xFF229ED9),
                                icon = {
                                    TelegramIcon(modifier = Modifier.size(24.dp))
                                },
                                onClick = {
                                    openSocialLink(
                                        context = context,
                                        appUriString = "tg://join?invite=H6NI94pCNwY4OWNl",
                                        webUrlString = "https://t.me/+H6NI94pCNwY4OWNl",
                                        appPackage = "org.telegram.messenger",
                                        onShowSnackbar = onShowSnackbar
                                    )
                                },
                                testTag = "connect_telegram_button"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        ),
        color = TextIndigoSubtle
    )
}

@Composable
private fun SettingsDivider() {
    Surface(
        color = FrostedGlassBorder,
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
    ) {}
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tactileClickable(targetScale = 0.98f, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NebulaIndigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NebulaIndigo,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhitePrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMutedSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NebulaIndigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NebulaIndigo,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhitePrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NebulaIndigo,
                uncheckedThumbColor = TextMutedSecondary,
                uncheckedTrackColor = FrostedGlassSurfaceVariant
            )
        )
    }
}

@Composable
private fun SocialChannelButton(
    name: String,
    handle: String,
    accentColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .tactileClickable(targetScale = 0.92f, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(FrostedGlassSurfaceVariant)
                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextWhitePrimary
        )

        Text(
            text = handle,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMutedSecondary
        )
    }
}

@Composable
private fun YouTubeIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Red rounded rectangle background
        drawRoundRect(
            color = Color(0xFFFF0000),
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)
        )
        // White play triangle in center
        val trianglePath = Path().apply {
            moveTo(w * 0.38f, h * 0.28f)
            lineTo(w * 0.72f, h * 0.50f)
            lineTo(w * 0.38f, h * 0.72f)
            close()
        }
        drawPath(trianglePath, color = Color.White, style = Fill)
    }
}

@Composable
private fun InstagramIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF833AB4),
                Color(0xFFFD1D1D),
                Color(0xFFFCAF45)
            ),
            start = Offset(0f, h),
            end = Offset(w, 0f)
        )
        // Gradient rounded squircle background
        drawRoundRect(
            brush = gradient,
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.26f, h * 0.26f)
        )
        // Camera outer rounded stroke
        val strokeWidth = w * 0.08f
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.20f, h * 0.20f),
            size = Size(w * 0.60f, h * 0.60f),
            cornerRadius = CornerRadius(w * 0.16f, h * 0.16f),
            style = Stroke(width = strokeWidth)
        )
        // Center lens circle
        drawCircle(
            color = Color.White,
            radius = w * 0.14f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )
        // Flash dot
        drawCircle(
            color = Color.White,
            radius = w * 0.038f,
            center = Offset(w * 0.68f, h * 0.32f),
            style = Fill
        )
    }
}

@Composable
private fun TelegramIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Circular Telegram blue background
        drawCircle(
            color = Color(0xFF229ED9),
            radius = w / 2f,
            center = Offset(w / 2f, h / 2f)
        )
        // Paper plane icon
        val planePath = Path().apply {
            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.78f, h * 0.24f)
            lineTo(w * 0.65f, h * 0.76f)
            lineTo(w * 0.48f, h * 0.62f)
            lineTo(w * 0.40f, h * 0.72f)
            lineTo(w * 0.38f, h * 0.58f)
            close()
        }
        drawPath(planePath, color = Color.White, style = Fill)
    }
}

private fun openSocialLink(
    context: Context,
    appUriString: String,
    webUrlString: String,
    appPackage: String? = null,
    onShowSnackbar: (String) -> Unit
) {
    try {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appUriString)).apply {
            if (appPackage != null) setPackage(appPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(appIntent)
    } catch (e: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrlString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            onShowSnackbar("Unable to open browser: $webUrlString")
        }
    }
}

