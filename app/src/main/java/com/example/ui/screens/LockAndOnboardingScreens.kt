package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.PinVerificationResult
import com.example.security.RecoveryConfig
import com.example.security.RecoveryManager
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassSurface
import com.example.ui.theme.FrostedGlassSurfaceVariant
import com.example.ui.theme.NebulaBlue
import com.example.ui.theme.NebulaFuchsia
import com.example.ui.theme.NebulaIndigo
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextIndigoSubtle
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun LockScreen(
    lockoutSeconds: Int,
    biometricEnabled: Boolean,
    recoveryConfig: RecoveryConfig,
    onVerifyPin: (String) -> PinVerificationResult,
    onBiometricUnlock: () -> Unit,
    onVerifyRecoveryQuestions: (String, String) -> Boolean,
    onVerifyRecoveryKey: (String) -> Boolean,
    onCompleteRecovery: (newPin: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryDialog by remember { mutableStateOf(false) }

    fun formatLockoutTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return if (mins > 0) {
            String.format("%02d:%02d (%dm %ds)", mins, secs, mins, secs)
        } else {
            "${secs}s"
        }
    }

    fun handleDigit(digit: String) {
        if (lockoutSeconds > 0) return
        if (enteredPin.length < 6) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null
            if (newPin.length >= 4) {
                val result = onVerifyPin(newPin)
                when (result) {
                    is PinVerificationResult.Success -> {
                        enteredPin = ""
                        errorMessage = null
                    }
                    is PinVerificationResult.Failed -> {
                        if (newPin.length == 6) {
                            val attempts = result.attemptCount
                            val lockTimeDesc = when (attempts) {
                                in 1..4 -> "1-minute"
                                5 -> "5-minute"
                                6 -> "15-minute"
                                7 -> "30-minute"
                                else -> "extended"
                            }
                            errorMessage = "Incorrect Master PIN. ${result.attemptsRemainingBeforeLock} attempt(s) remaining before $lockTimeDesc lockout."
                            enteredPin = ""
                        }
                    }
                    is PinVerificationResult.LockedOut -> {
                        val duration = formatLockoutTime(result.remainingSeconds)
                        errorMessage = "Vault locked for $duration due to repeated failed attempts."
                        enteredPin = ""
                    }
                    else -> {}
                }
            }
        }
    }

    fun handleDelete() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Surface(
        color = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Logo & Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FrostedGlassSurface)
                        .border(1.5.dp, FrostedGlassBorderLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NebulaIndigo.copy(alpha = 0.35f), NebulaFuchsia.copy(alpha = 0.25f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Cyber Guard Shield",
                            tint = NebulaIndigo,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CYBER GUARD",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.4.sp
                    ),
                    color = TextWhitePrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Personal Cyber Vault & Defense",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextIndigoSubtle
                )
            }

            // PIN Dots & Lockout Countdown Area
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (lockoutSeconds > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(SecurityRed.copy(alpha = 0.14f))
                            .border(1.dp, SecurityRed.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = SecurityRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Progressive Vault Lockout",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SecurityRed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try again in ${formatLockoutTime(lockoutSeconds)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Protection active against repeated incorrect attempts.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Enter Master PIN to Unlock",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextWhitePrimary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val maxDots = if (enteredPin.length > 4) enteredPin.length.coerceAtMost(6) else 4
                    for (i in 0 until maxDots) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) NebulaIndigo else FrostedGlassSurfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isFilled) NebulaIndigo else FrostedGlassBorderLight,
                                    CircleShape
                                )
                        )
                    }
                }

                if (errorMessage != null && lockoutSeconds == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecurityRed,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Numeric Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            when (key) {
                                "BIO" -> {
                                    if (biometricEnabled) {
                                        KeypadButton(
                                            content = {
                                                Icon(
                                                    imageVector = Icons.Default.Fingerprint,
                                                    contentDescription = "Biometric Unlock",
                                                    tint = NebulaIndigo,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            },
                                            onClick = onBiometricUnlock,
                                            testTag = "keypad_biometric"
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.size(64.dp))
                                    }
                                }
                                "DEL" -> {
                                    KeypadButton(
                                        content = {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Delete",
                                                tint = TextMutedSecondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        onClick = { handleDelete() },
                                        testTag = "keypad_del"
                                    )
                                }
                                else -> {
                                    KeypadButton(
                                        content = {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (lockoutSeconds > 0) TextMutedSecondary.copy(alpha = 0.4f) else TextWhitePrimary
                                            )
                                        },
                                        onClick = { handleDigit(key) },
                                        testTag = "keypad_$key"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Forgot PIN / Emergency Account Recovery Action
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showRecoveryDialog = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("forgot_pin_recovery_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = NebulaIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Forgot PIN? / Account Recovery",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = NebulaIndigo
                    )
                }
            }
        }
    }

    if (showRecoveryDialog) {
        EmergencyRecoveryDialog(
            recoveryConfig = recoveryConfig,
            onVerifyQuestions = onVerifyRecoveryQuestions,
            onVerifyRecoveryKey = onVerifyRecoveryKey,
            onCompleteRecovery = onCompleteRecovery,
            onDismiss = { showRecoveryDialog = false }
        )
    }
}

@Composable
fun KeypadButton(
    content: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(FrostedGlassSurfaceVariant)
            .border(1.dp, FrostedGlassBorderLight, CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun OnboardingScreen(
    onComplete: (
        pin: String,
        enableBiometric: Boolean,
        q1: String?,
        a1: String?,
        q2: String?,
        a2: String?,
        recoveryKey: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    var step by remember { mutableIntStateOf(1) }
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var enableBiometric by remember { mutableStateOf(true) }

    // Step 4: Recovery Setup
    var question1 by remember { mutableStateOf(RecoveryManager.PRESET_QUESTIONS[0]) }
    var q1Dropdown by remember { mutableStateOf(false) }
    var answer1 by remember { mutableStateOf("") }
    var showAnswer1 by remember { mutableStateOf(false) }

    var question2 by remember { mutableStateOf(RecoveryManager.PRESET_QUESTIONS[1]) }
    var q2Dropdown by remember { mutableStateOf(false) }
    var answer2 by remember { mutableStateOf("") }
    var showAnswer2 by remember { mutableStateOf(false) }

    var recoveryKey by remember { mutableStateOf(RecoveryManager.generateNewRecoveryKey()) }
    var copiedKey by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        color = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 28.dp else 10.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) NebulaIndigo else FrostedGlassSurfaceVariant
                            )
                            .border(1.dp, if (i <= step) NebulaIndigo else FrostedGlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (i == step) {
                            Text(
                                text = "$i",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                    if (i < 4) {
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            }

            // Step Content
            when (step) {
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(FrostedGlassSurface)
                                .border(1.5.dp, FrostedGlassBorderLight, RoundedCornerShape(26.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NebulaIndigo,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Welcome to Cyber Guard",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhitePrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Your zero-knowledge personal cybersecurity vault. Keep passwords, cards, and notes fortified on-device with Watchtower defense and AI intelligence.",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = TextMutedSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Features checklist
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FeatureRow(title = "AES-256-GCM Hardware Key Encryption")
                            FeatureRow(title = "Progressive Lockout Brute-Force Defense")
                            FeatureRow(title = "Emergency Account Recovery with Master Key")
                            FeatureRow(title = "AI Cyber Defense & Breach Watchtower")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(NebulaIndigo, NebulaFuchsia))
                            )
                            .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(16.dp))
                            .clickable { step = 2 }
                            .testTag("onboarding_get_started"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Set Up Master PIN",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }

                2 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NebulaIndigo,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Create Master PIN",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextWhitePrimary
                        )

                        Text(
                            text = "Enter a 4 to 6 digit Master PIN to protect your vault",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // PIN Display Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val maxDots = if (pin1.length > 4) pin1.length.coerceAtMost(6) else 4
                            for (i in 0 until maxDots) {
                                val isFilled = i < pin1.length
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isFilled) NebulaIndigo else FrostedGlassSurfaceVariant)
                                        .border(1.dp, if (isFilled) NebulaIndigo else FrostedGlassBorderLight, CircleShape)
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = errorMessage!!, style = MaterialTheme.typography.bodySmall, color = SecurityRed)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        NumericKeypad(
                            onDigit = {
                                if (pin1.length < 6) {
                                    pin1 += it
                                    errorMessage = null
                                }
                            },
                            onDelete = {
                                if (pin1.isNotEmpty()) pin1 = pin1.dropLast(1)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1; pin1 = "" },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Back", color = TextWhitePrimary)
                        }

                        Button(
                            onClick = {
                                if (pin1.length < 4) {
                                    errorMessage = "Please enter at least 4 digits."
                                } else {
                                    step = 3
                                    errorMessage = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NebulaIndigo,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("confirm_pin_step_button")
                        ) {
                            Text("Next")
                        }
                    }
                }

                3 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = NebulaIndigo,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Confirm Master PIN",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextWhitePrimary
                        )

                        Text(
                            text = "Re-enter your Master PIN to confirm",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // PIN Display Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val maxDots = if (pin2.length > 4) pin2.length.coerceAtMost(6) else 4
                            for (i in 0 until maxDots) {
                                val isFilled = i < pin2.length
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isFilled) NebulaIndigo else FrostedGlassSurfaceVariant)
                                        .border(1.dp, if (isFilled) NebulaIndigo else FrostedGlassBorderLight, CircleShape)
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = errorMessage!!, style = MaterialTheme.typography.bodySmall, color = SecurityRed)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Biometric Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(FrostedGlassSurface)
                                .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = NebulaIndigo,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Biometric Unlock",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = TextWhitePrimary
                                        )
                                        Text(
                                            text = "Unlock with fingerprint or face",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMutedSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = enableBiometric,
                                    onCheckedChange = { enableBiometric = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = NebulaIndigo,
                                        uncheckedThumbColor = TextMutedSecondary,
                                        uncheckedTrackColor = FrostedGlassSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        NumericKeypad(
                            onDigit = {
                                if (pin2.length < 6) {
                                    pin2 += it
                                    errorMessage = null
                                }
                            },
                            onDelete = {
                                if (pin2.isNotEmpty()) pin2 = pin2.dropLast(1)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 2; pin2 = "" },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Back", color = TextWhitePrimary)
                        }

                        Button(
                            onClick = {
                                if (pin1 != pin2) {
                                    errorMessage = "PINs do not match. Please re-enter."
                                    pin2 = ""
                                } else {
                                    step = 4
                                    errorMessage = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NebulaIndigo,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("goto_recovery_step_button")
                        ) {
                            Text("Next: Recovery", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                4 -> {
                    // STEP 4: EMERGENCY ACCOUNT RECOVERY SETUP
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(NebulaIndigo, NebulaFuchsia))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Emergency Account Recovery",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextWhitePrimary
                        )

                        Text(
                            text = "Recommended: Set up recovery questions to restore your vault if you ever forget your Master PIN.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                            color = TextMutedSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Question 1
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Security Question 1",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextIndigoSubtle
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FrostedGlassSurface)
                                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(10.dp))
                                        .clickable { q1Dropdown = true }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = question1,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextWhitePrimary
                                    )
                                }
                                DropdownMenu(expanded = q1Dropdown, onDismissRequest = { q1Dropdown = false }) {
                                    RecoveryManager.PRESET_QUESTIONS.forEach { q ->
                                        DropdownMenuItem(
                                            text = { Text(q) },
                                            onClick = { question1 = q; q1Dropdown = false }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = answer1,
                                onValueChange = { answer1 = it; errorMessage = null },
                                placeholder = { Text("Answer 1 (Case-insensitive)", color = TextMutedSecondary) },
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("onboarding_answer_1_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Question 2
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Security Question 2",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextIndigoSubtle
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FrostedGlassSurface)
                                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(10.dp))
                                        .clickable { q2Dropdown = true }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = question2,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextWhitePrimary
                                    )
                                }
                                DropdownMenu(expanded = q2Dropdown, onDismissRequest = { q2Dropdown = false }) {
                                    RecoveryManager.PRESET_QUESTIONS.forEach { q ->
                                        DropdownMenuItem(
                                            text = { Text(q) },
                                            onClick = { question2 = q; q2Dropdown = false }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = answer2,
                                onValueChange = { answer2 = it; errorMessage = null },
                                placeholder = { Text("Answer 2 (Case-insensitive)", color = TextMutedSecondary) },
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("onboarding_answer_2_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Emergency Key
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrostedGlassSurface)
                                .border(1.dp, NebulaIndigo.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Emergency Recovery Key",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextIndigoSubtle
                                    )
                                    IconButton(
                                        onClick = { recoveryKey = RecoveryManager.generateNewRecoveryKey() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Regenerate",
                                            tint = NebulaIndigo,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = recoveryKey,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        ),
                                        color = TextWhitePrimary
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(recoveryKey))
                                            copiedKey = true
                                        },
                                        modifier = Modifier.size(28.dp).testTag("onboarding_copy_key")
                                    ) {
                                        Icon(
                                            imageVector = if (copiedKey) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = if (copiedKey) SecurityGreen else TextMutedSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = errorMessage!!, style = MaterialTheme.typography.bodySmall, color = SecurityRed)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (answer1.isNotBlank() && answer1.trim().length < 2) {
                                    errorMessage = "Answer 1 must be at least 2 characters."
                                } else if (answer2.isNotBlank() && answer2.trim().length < 2) {
                                    errorMessage = "Answer 2 must be at least 2 characters."
                                } else {
                                    val hasBothAnswers = answer1.trim().length >= 2 && answer2.trim().length >= 2
                                    onComplete(
                                        pin1,
                                        enableBiometric,
                                        if (hasBothAnswers) question1 else null,
                                        if (hasBothAnswers) answer1 else null,
                                        if (hasBothAnswers) question2 else null,
                                        if (hasBothAnswers) answer2 else null,
                                        if (hasBothAnswers) recoveryKey else null
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecurityGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("finish_onboarding_with_recovery")
                        ) {
                            Text("Save & Fortify Vault", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onComplete(pin1, enableBiometric, null, null, null, null, null)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("skip_recovery_button")
                        ) {
                            Text("Skip Recovery (Set up later in Settings)", color = TextMutedSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureRow(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(SecurityGreen.copy(alpha = 0.2f))
                .border(1.dp, SecurityGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SecurityGreen,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhitePrimary
        )
    }
}

@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (key in row) {
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(60.dp))
                    } else if (key == "DEL") {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(FrostedGlassSurfaceVariant)
                                .border(1.dp, FrostedGlassBorderLight, CircleShape)
                                .clickable(onClick = onDelete),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Delete",
                                tint = TextMutedSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(FrostedGlassSurfaceVariant)
                                .border(1.dp, FrostedGlassBorderLight, CircleShape)
                                .clickable { onDigit(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = TextWhitePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
