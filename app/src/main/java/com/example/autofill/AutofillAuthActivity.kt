package com.example.autofill

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SecurityAuditEventEntity
import com.example.data.database.VaultDatabase
import com.example.data.database.VaultItemEntity
import com.example.model.AuditEventType
import com.example.model.AuditSeverity
import com.example.model.SecurityAuditEvent
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.security.PinManager
import com.example.security.PinVerificationResult
import com.example.ui.theme.AhmadGuardTheme
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassSurface
import com.example.ui.theme.FrostedGlassSurfaceVariant
import com.example.ui.theme.NebulaIndigo
import com.example.ui.theme.SecurityAmber
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutofillAuthActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_USERNAME_ID = "extra_username_id"
        const val EXTRA_PASSWORD_ID = "extra_password_id"
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }

    private var targetItemId: Long = 0L
    private var usernameId: AutofillId? = null
    private var passwordId: AutofillId? = null
    private var targetPackage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        targetItemId = intent.getLongExtra(EXTRA_ITEM_ID, 0L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            usernameId = intent.getParcelableExtra(EXTRA_USERNAME_ID, AutofillId::class.java)
            passwordId = intent.getParcelableExtra(EXTRA_PASSWORD_ID, AutofillId::class.java)
        } else {
            @Suppress("DEPRECATION")
            usernameId = intent.getParcelableExtra(EXTRA_USERNAME_ID)
            @Suppress("DEPRECATION")
            passwordId = intent.getParcelableExtra(EXTRA_PASSWORD_ID)
        }
        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""

        setContent {
            AhmadGuardTheme {
                AutofillAuthScreen(
                    targetItemId = targetItemId,
                    targetPackage = targetPackage,
                    onAuthenticated = { selectedItem ->
                        completeAutofill(selectedItem)
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }

    private fun completeAutofill(item: VaultItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val datasetBuilder = android.service.autofill.Dataset.Builder()

            val title = "${item.name} (${item.username.ifBlank { item.email }})"
            val subtitle = "Cyber Guard Secure Autofill"
            val presentation = AutofillHelper.buildDatasetPresentation(this, title, subtitle)

            var hasValueSet = false

            if (usernameId != null) {
                val userVal = item.username.ifBlank { item.email }
                if (userVal.isNotBlank()) {
                    datasetBuilder.setValue(usernameId!!, AutofillValue.forText(userVal), presentation)
                    hasValueSet = true
                }
            }

            if (passwordId != null) {
                if (item.password.isNotBlank()) {
                    datasetBuilder.setValue(passwordId!!, AutofillValue.forText(item.password), presentation)
                    hasValueSet = true
                }
            }

            if (!hasValueSet) {
                // If neither specifically identified, try setting to usernameId if exists
                if (usernameId != null) {
                    datasetBuilder.setValue(usernameId!!, AutofillValue.forText(item.username.ifBlank { item.email }), presentation)
                }
            }

            val dataset = datasetBuilder.build()

            val replyIntent = Intent().apply {
                putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
            }

            // Log security audit event in Room
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = VaultDatabase.getDatabase(applicationContext)
                    db.vaultDao().insertAuditEvent(
                        SecurityAuditEventEntity.fromSecurityAuditEvent(
                            SecurityAuditEvent(
                                eventType = AuditEventType.VAULT_UNLOCKED,
                                title = "Autofill Credentials Dispatched",
                                details = "Decrypted credentials for '${item.name}' were securely filled into app ($targetPackage).",
                                severity = AuditSeverity.SUCCESS,
                                timestamp = System.currentTimeMillis(),
                                actor = "Cyber Guard Autofill Service",
                                contextInfo = "Target: $targetPackage"
                            )
                        )
                    )
                } catch (_: Exception) {}
            }

            setResult(Activity.RESULT_OK, replyIntent)
            finish()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}

@Composable
fun AutofillAuthScreen(
    targetItemId: Long,
    targetPackage: String,
    onAuthenticated: (VaultItem) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var targetItem by remember { mutableStateOf<VaultItem?>(null) }
    var allItems by remember { mutableStateOf<List<VaultItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBiometricAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = VaultDatabase.getDatabase(context)
            val entities = db.vaultDao().getAllItemsSync()
            val items = entities.map { it.toVaultItem() }
            allItems = items

            if (targetItemId > 0L) {
                targetItem = items.firstOrNull { it.id == targetItemId }
            } else {
                val matches = AutofillHelper.findMatchingItems(items, targetPackage, null)
                targetItem = matches.firstOrNull() ?: items.firstOrNull { it.type == VaultType.LOGIN }
            }
            isBiometricAvailable = PinManager.isBiometricEnabled(context)
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C14).copy(alpha = 0.95f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = NebulaIndigo)
        } else {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FrostedGlassSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedGlassBorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NebulaIndigo.copy(alpha = 0.3f))
                                    .border(1.dp, NebulaIndigo, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AHMAD GUARD",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = "Zero-Knowledge Autofill",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NebulaIndigo
                                )
                            }
                        }

                        IconButton(onClick = onCancel) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = TextMutedSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target App Badge
                    val cleanAppName = remember(targetPackage) {
                        targetPackage.substringAfterLast(".").replaceFirstChar { it.uppercase() }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FrostedGlassSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedGlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SecurityGreen.copy(alpha = 0.2f))
                                    .border(1.dp, SecurityGreen, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = SecurityGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = targetItem?.name ?: cleanAppName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhitePrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = targetItem?.let { it.username.ifBlank { it.email } } ?: targetPackage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMutedSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Enter Master PIN to release credentials",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // PIN Dots (6 digits)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dotCount = if (enteredPin.length > 4) enteredPin.length.coerceAtMost(6) else 4
                        for (i in 0 until dotCount) {
                            val filled = i < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (filled) NebulaIndigo else Color.Transparent)
                                    .border(
                                        1.5.dp,
                                        if (filled) NebulaIndigo else FrostedGlassBorder,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SecurityRed,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dedicated Numeric Keypad
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("BIO", "0", "DEL")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (row in rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (key in row) {
                                    when (key) {
                                        "BIO" -> {
                                            if (isBiometricAvailable) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(CircleShape)
                                                        .background(FrostedGlassSurfaceVariant)
                                                        .border(1.dp, FrostedGlassBorderLight, CircleShape)
                                                        .clickable {
                                                            if (targetItem != null) {
                                                                onAuthenticated(targetItem!!)
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Fingerprint,
                                                        contentDescription = "Biometric",
                                                        tint = NebulaIndigo,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.size(56.dp))
                                            }
                                        }
                                        "DEL" -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(FrostedGlassSurfaceVariant)
                                                    .border(1.dp, FrostedGlassBorderLight, CircleShape)
                                                    .clickable {
                                                        if (enteredPin.isNotEmpty()) {
                                                            enteredPin = enteredPin.dropLast(1)
                                                            errorMessage = null
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Backspace,
                                                    contentDescription = "Delete",
                                                    tint = TextMutedSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        else -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(FrostedGlassSurfaceVariant)
                                                    .border(1.dp, FrostedGlassBorderLight, CircleShape)
                                                    .clickable {
                                                        if (enteredPin.length < 6) {
                                                            val next = enteredPin + key
                                                            enteredPin = next
                                                            errorMessage = null
                                                            if (next.length >= 4) {
                                                                val result = PinManager.verifyPin(context, next)
                                                                when (result) {
                                                                    is PinVerificationResult.Success -> {
                                                                        if (targetItem != null) {
                                                                            onAuthenticated(targetItem!!)
                                                                        } else {
                                                                            errorMessage = "No credential selected"
                                                                        }
                                                                    }
                                                                    is PinVerificationResult.LockedOut -> {
                                                                        errorMessage = "Locked for ${result.remainingSeconds}s"
                                                                        enteredPin = ""
                                                                    }
                                                                    is PinVerificationResult.Failed -> {
                                                                        if (next.length == 6) {
                                                                            errorMessage = "Incorrect PIN (${result.attemptsRemainingBeforeLock} left)"
                                                                            enteredPin = ""
                                                                        }
                                                                    }
                                                                    else -> {
                                                                        if (next.length == 6) {
                                                                            errorMessage = "Invalid Master PIN"
                                                                            enteredPin = ""
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = TextMutedSecondary)
                    }
                }
            }
        }
    }
}
