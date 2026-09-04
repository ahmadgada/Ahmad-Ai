package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.security.RecoveryConfig
import com.example.security.RecoveryManager
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassSurface
import com.example.ui.theme.FrostedGlassSurfaceVariant
import com.example.ui.theme.NebulaFuchsia
import com.example.ui.theme.NebulaIndigo
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextIndigoSubtle
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary

/**
 * Dialog used on LockScreen to execute Emergency Account Recovery when PIN is forgotten.
 */
@Composable
fun EmergencyRecoveryDialog(
    recoveryConfig: RecoveryConfig,
    onVerifyQuestions: (String, String) -> Boolean,
    onVerifyRecoveryKey: (String) -> Boolean,
    onCompleteRecovery: (newPin: String) -> Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(if (recoveryConfig.isConfigured) 1 else 0) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Recovery Key

    var answer1 by remember { mutableStateOf("") }
    var showAnswer1 by remember { mutableStateOf(false) }
    var answer2 by remember { mutableStateOf("") }
    var showAnswer2 by remember { mutableStateOf(false) }

    var recoveryKeyInput by remember { mutableStateOf("") }

    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF0F111A))
                .border(1.5.dp, FrostedGlassBorderLight, RoundedCornerShape(26.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(NebulaIndigo, NebulaFuchsia))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Recovery Shield",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Emergency Recovery",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Zero-Knowledge Vault Rescue",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextIndigoSubtle
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_recovery_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMutedSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                when (step) {
                    0 -> {
                        // NOT CONFIGURED
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(FrostedGlassSurface)
                                .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
                                .padding(18.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SecurityRed,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Recovery Not Configured",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhitePrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Emergency Account Recovery was not set up for this vault. To protect your zero-knowledge encrypted credentials, please wait for the progressive lockout countdown to expire or use Biometric unlock if enabled.",
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                    color = TextMutedSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NebulaIndigo,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("recovery_not_configured_ok")
                        ) {
                            Text("Back to Lock Screen", fontWeight = FontWeight.Bold)
                        }
                    }

                    1 -> {
                        // STEP 1: VERIFY IDENTITY
                        Text(
                            text = "Authenticate using your configured recovery questions or emergency master key.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = TextMutedSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = FrostedGlassSurface,
                            contentColor = NebulaIndigo,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = NebulaIndigo
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0; errorMessage = null },
                                text = {
                                    Text(
                                        "Security Questions",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (selectedTab == 0) TextWhitePrimary else TextMutedSecondary
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1; errorMessage = null },
                                text = {
                                    Text(
                                        "Master Key",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (selectedTab == 1) TextWhitePrimary else TextMutedSecondary
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedTab == 0) {
                            // QUESTIONS
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Question 1: " + recoveryConfig.question1.ifBlank { "Configured Security Question 1" },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextIndigoSubtle
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = answer1,
                                        onValueChange = { answer1 = it; errorMessage = null },
                                        placeholder = { Text("Enter Answer 1", color = TextMutedSecondary) },
                                        singleLine = true,
                                        visualTransformation = if (showAnswer1) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showAnswer1 = !showAnswer1 }) {
                                                Icon(
                                                    imageVector = if (showAnswer1) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = TextMutedSecondary
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NebulaIndigo,
                                            unfocusedBorderColor = FrostedGlassBorder,
                                            focusedTextColor = TextWhitePrimary,
                                            unfocusedTextColor = TextWhitePrimary,
                                            focusedContainerColor = FrostedGlassSurface,
                                            unfocusedContainerColor = FrostedGlassSurface
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("recovery_answer_1_input")
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Question 2: " + recoveryConfig.question2.ifBlank { "Configured Security Question 2" },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextIndigoSubtle
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = answer2,
                                        onValueChange = { answer2 = it; errorMessage = null },
                                        placeholder = { Text("Enter Answer 2", color = TextMutedSecondary) },
                                        singleLine = true,
                                        visualTransformation = if (showAnswer2) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showAnswer2 = !showAnswer2 }) {
                                                Icon(
                                                    imageVector = if (showAnswer2) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = TextMutedSecondary
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NebulaIndigo,
                                            unfocusedBorderColor = FrostedGlassBorder,
                                            focusedTextColor = TextWhitePrimary,
                                            unfocusedTextColor = TextWhitePrimary,
                                            focusedContainerColor = FrostedGlassSurface,
                                            unfocusedContainerColor = FrostedGlassSurface
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("recovery_answer_2_input")
                                    )
                                }
                            }
                        } else {
                            // MASTER RECOVERY KEY
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Enter your 16-character Emergency Recovery Key (e.g. AGRD-XXXX-XXXX-XXXX):",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextIndigoSubtle
                                )
                                OutlinedTextField(
                                    value = recoveryKeyInput,
                                    onValueChange = {
                                        recoveryKeyInput = it.uppercase()
                                        errorMessage = null
                                    },
                                    placeholder = { Text("AGRD-XXXX-XXXX-XXXX", color = TextMutedSecondary) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NebulaIndigo,
                                        unfocusedBorderColor = FrostedGlassBorder,
                                        focusedTextColor = TextWhitePrimary,
                                        unfocusedTextColor = TextWhitePrimary,
                                        focusedContainerColor = FrostedGlassSurface,
                                        unfocusedContainerColor = FrostedGlassSurface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("recovery_key_input")
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecurityRed,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (selectedTab == 0) {
                                    if (answer1.isBlank() || answer2.isBlank()) {
                                        errorMessage = "Please provide answers to both security questions."
                                    } else {
                                        val valid = onVerifyQuestions(answer1, answer2)
                                        if (valid) {
                                            step = 2
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Incorrect security answers. Verification failed."
                                        }
                                    }
                                } else {
                                    if (recoveryKeyInput.isBlank()) {
                                        errorMessage = "Please enter your Emergency Master Recovery Key."
                                    } else {
                                        val valid = onVerifyRecoveryKey(recoveryKeyInput)
                                        if (valid) {
                                            step = 2
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Invalid Emergency Master Recovery Key."
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NebulaIndigo,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_recovery_button")
                        ) {
                            Text("Verify & Continue", fontWeight = FontWeight.Bold)
                        }
                    }

                    2 -> {
                        // STEP 2: ESTABLISH NEW MASTER PIN
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SecurityGreen.copy(alpha = 0.12f))
                                .border(1.dp, SecurityGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SecurityGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Recovery Verified! Set a new Master PIN to secure your vault.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = SecurityGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "New Master PIN (4 to 6 digits)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextIndigoSubtle
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = newPin,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                            newPin = it
                                            errorMessage = null
                                        }
                                    },
                                    placeholder = { Text("4 to 6 digits", color = TextMutedSecondary) },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NebulaIndigo,
                                        unfocusedBorderColor = FrostedGlassBorder,
                                        focusedTextColor = TextWhitePrimary,
                                        unfocusedTextColor = TextWhitePrimary,
                                        focusedContainerColor = FrostedGlassSurface,
                                        unfocusedContainerColor = FrostedGlassSurface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("new_master_pin_input")
                                )
                            }

                            Column {
                                Text(
                                    text = "Confirm New Master PIN",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextIndigoSubtle
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = confirmPin,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                            confirmPin = it
                                            errorMessage = null
                                        }
                                    },
                                    placeholder = { Text("Re-enter 4 to 6 digits", color = TextMutedSecondary) },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NebulaIndigo,
                                        unfocusedBorderColor = FrostedGlassBorder,
                                        focusedTextColor = TextWhitePrimary,
                                        unfocusedTextColor = TextWhitePrimary,
                                        focusedContainerColor = FrostedGlassSurface,
                                        unfocusedContainerColor = FrostedGlassSurface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("confirm_new_master_pin_input")
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecurityRed,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (newPin.length < 4) {
                                    errorMessage = "Master PIN must be between 4 and 6 digits."
                                } else if (newPin != confirmPin) {
                                    errorMessage = "New PIN and confirmation PIN do not match."
                                } else {
                                    val success = onCompleteRecovery(newPin)
                                    if (success) {
                                        onDismiss()
                                    } else {
                                        errorMessage = "Failed to update Master PIN. Please try again."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecurityGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_recovered_pin_button")
                        ) {
                            Text("Establish New PIN & Unlock", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog used in Settings to configure, update, or disable Emergency Account Recovery.
 */
@Composable
fun EmergencyRecoverySetupDialog(
    initialConfig: RecoveryConfig,
    onSave: (q1: String, a1: String, q2: String, a2: String, key: String) -> Boolean,
    onDisable: () -> Unit,
    onGenerateKey: () -> String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    var question1 by remember {
        mutableStateOf(
            if (initialConfig.question1.isNotBlank()) initialConfig.question1
            else RecoveryManager.PRESET_QUESTIONS[0]
        )
    }
    var q1Dropdown by remember { mutableStateOf(false) }
    var answer1 by remember { mutableStateOf("") }
    var showAnswer1 by remember { mutableStateOf(false) }

    var question2 by remember {
        mutableStateOf(
            if (initialConfig.question2.isNotBlank()) initialConfig.question2
            else RecoveryManager.PRESET_QUESTIONS[1]
        )
    }
    var q2Dropdown by remember { mutableStateOf(false) }
    var answer2 by remember { mutableStateOf("") }
    var showAnswer2 by remember { mutableStateOf(false) }

    var recoveryKey by remember { mutableStateOf(onGenerateKey()) }
    var copiedFeedback by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDisableConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF0F111A))
                .border(1.5.dp, FrostedGlassBorderLight, RoundedCornerShape(26.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(NebulaIndigo, NebulaFuchsia))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Recovery Settings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhitePrimary
                            )
                            Text(
                                text = if (initialConfig.isConfigured) "Configured & Active" else "Configure Vault Protection",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (initialConfig.isConfigured) SecurityGreen else TextIndigoSubtle
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_recovery_setup_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMutedSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Configure 2 security questions and save your Master Emergency Recovery Key. All answers are encrypted with salted PBKDF2 hashes.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = TextMutedSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // QUESTION 1
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Security Question 1",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextIndigoSubtle
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrostedGlassSurface)
                                .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                                .clickable { q1Dropdown = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = question1,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhitePrimary
                            )
                        }

                        DropdownMenu(
                            expanded = q1Dropdown,
                            onDismissRequest = { q1Dropdown = false }
                        ) {
                            RecoveryManager.PRESET_QUESTIONS.forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q) },
                                    onClick = {
                                        question1 = q
                                        q1Dropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = answer1,
                        onValueChange = { answer1 = it; errorMessage = null },
                        placeholder = { Text("Enter Answer 1 (Case-insensitive)", color = TextMutedSecondary) },
                        singleLine = true,
                        visualTransformation = if (showAnswer1) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showAnswer1 = !showAnswer1 }) {
                                Icon(
                                    imageVector = if (showAnswer1) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMutedSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaIndigo,
                            unfocusedBorderColor = FrostedGlassBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary,
                            focusedContainerColor = FrostedGlassSurface,
                            unfocusedContainerColor = FrostedGlassSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setup_answer_1_input")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QUESTION 2
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Security Question 2",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextIndigoSubtle
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrostedGlassSurface)
                                .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                                .clickable { q2Dropdown = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = question2,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhitePrimary
                            )
                        }

                        DropdownMenu(
                            expanded = q2Dropdown,
                            onDismissRequest = { q2Dropdown = false }
                        ) {
                            RecoveryManager.PRESET_QUESTIONS.forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q) },
                                    onClick = {
                                        question2 = q
                                        q2Dropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = answer2,
                        onValueChange = { answer2 = it; errorMessage = null },
                        placeholder = { Text("Enter Answer 2 (Case-insensitive)", color = TextMutedSecondary) },
                        singleLine = true,
                        visualTransformation = if (showAnswer2) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showAnswer2 = !showAnswer2 }) {
                                Icon(
                                    imageVector = if (showAnswer2) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMutedSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaIndigo,
                            unfocusedBorderColor = FrostedGlassBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary,
                            focusedContainerColor = FrostedGlassSurface,
                            unfocusedContainerColor = FrostedGlassSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setup_answer_2_input")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // MASTER RECOVERY KEY CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, NebulaIndigo.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Emergency Master Recovery Key",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextIndigoSubtle
                            )
                            IconButton(
                                onClick = { recoveryKey = onGenerateKey() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Regenerate Key",
                                    tint = NebulaIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recoveryKey,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp
                                ),
                                color = TextWhitePrimary
                            )

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(recoveryKey))
                                    copiedFeedback = true
                                },
                                modifier = Modifier.size(32.dp).testTag("copy_recovery_key_button")
                            ) {
                                Icon(
                                    imageVector = if (copiedFeedback) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                    contentDescription = "Copy Recovery Key",
                                    tint = if (copiedFeedback) SecurityGreen else TextMutedSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "Save this key in a secure physical location offline.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedSecondary
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecurityRed,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (answer1.trim().length < 2 || answer2.trim().length < 2) {
                            errorMessage = "Please enter valid answers (at least 2 characters each) for both questions."
                        } else {
                            val success = onSave(question1, answer1, question2, answer2, recoveryKey)
                            if (success) {
                                onDismiss()
                            } else {
                                errorMessage = "Failed to save recovery configuration."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NebulaIndigo,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_recovery_setup_button")
                ) {
                    Text("Save Emergency Recovery Setup", fontWeight = FontWeight.Bold)
                }

                if (initialConfig.isConfigured) {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!showDisableConfirm) {
                        OutlinedButton(
                            onClick = { showDisableConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("disable_recovery_button")
                        ) {
                            Text("Disable Emergency Recovery")
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SecurityRed.copy(alpha = 0.12f))
                                .border(1.dp, SecurityRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Are you sure you want to disable Emergency Recovery?",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = SecurityRed,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showDisableConfirm = false },
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text("Cancel", color = TextWhitePrimary)
                                }
                                Button(
                                    onClick = {
                                        onDisable()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SecurityRed,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text("Confirm")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
