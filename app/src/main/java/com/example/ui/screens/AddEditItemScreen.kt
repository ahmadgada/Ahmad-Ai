package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CustomField
import com.example.model.VaultCategory
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.security.EncryptionManager
import com.example.security.ClipboardHelper
import com.example.ui.components.ConfirmationDialog
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    initialItem: VaultItem?,
    onSave: (VaultItem) -> Unit,
    onDelete: (VaultItem) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = initialItem != null

    var selectedType by remember { mutableStateOf(initialItem?.type ?: VaultType.LOGIN) }
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var accountTitle by remember { mutableStateOf(initialItem?.accountTitle ?: "") }
    var url by remember { mutableStateOf(initialItem?.url ?: "") }
    var username by remember { mutableStateOf(initialItem?.username ?: "") }
    var email by remember { mutableStateOf(initialItem?.email ?: "") }
    var password by remember { mutableStateOf(initialItem?.password ?: "") }
    var sensitiveData by remember { mutableStateOf(initialItem?.sensitiveData ?: "") }
    var customFields by remember { mutableStateOf(initialItem?.customFields ?: emptyList()) }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.category ?: VaultCategory.PERSONAL) }
    var tagsString by remember { mutableStateOf(initialItem?.tags?.joinToString(", ") ?: "") }
    var favorite by remember { mutableStateOf(initialItem?.favorite ?: false) }
    val passwordLastChangedAt = remember { initialItem?.passwordLastChangedAt ?: System.currentTimeMillis() }

    // Custom Field Add state
    var newFieldLabel by remember { mutableStateOf("") }
    var newFieldValue by remember { mutableStateOf("") }
    var newFieldSecured by remember { mutableStateOf(false) }
    var isAddingCustomField by remember { mutableStateOf(false) }

    // Card Specific
    var cardholderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    // Identity Specific
    var identityIdNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Parsing initial sensitive data for Card / Identity
    remember(initialItem) {
        if (initialItem != null) {
            when (initialItem.type) {
                VaultType.CARD -> {
                    val parts = initialItem.sensitiveData.split("|")
                    if (parts.size >= 3) {
                        expiryDate = parts.getOrNull(1) ?: ""
                        cvv = parts.getOrNull(2) ?: ""
                        pin = parts.getOrNull(3) ?: ""
                        cardholderName = parts.getOrNull(4) ?: ""
                    }
                }
                VaultType.IDENTITY -> {
                    val parts = initialItem.sensitiveData.split("|")
                    identityIdNumber = parts.getOrNull(0) ?: ""
                    phone = parts.getOrNull(1) ?: ""
                    address = parts.getOrNull(2) ?: ""
                }
                else -> {}
            }
        }
    }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordCopied by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog && initialItem != null) {
        ConfirmationDialog(
            title = "DELETE ITEM?",
            message = "Are you sure you want to permanently delete '${initialItem.name}'? This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirmDialog = false
                onDelete(initialItem)
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Top App Bar
        Surface(
            color = FrostedGlassSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FrostedGlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NebulaIndigo
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditMode) "EDIT SECURE ITEM" else "ADD SECURE ITEM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextWhitePrimary
                    )
                }

                if (isEditMode) {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("delete_item_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = SecurityRed
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector Tabs (Only active in Add mode or can be changed)
            item {
                TabRow(
                    selectedTabIndex = VaultType.entries.indexOf(selectedType),
                    containerColor = FrostedGlassSurface,
                    contentColor = NebulaIndigo,
                    indicator = { tabPositions ->
                        val index = VaultType.entries.indexOf(selectedType)
                        if (index in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = NebulaIndigo,
                                height = 3.dp
                            )
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
                ) {
                    VaultType.entries.forEach { type ->
                        val selected = selectedType == type
                        Tab(
                            selected = selected,
                            onClick = { selectedType = type },
                            text = {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selected) NebulaIndigo else TextMutedSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Primary Item Name Field
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = {
                        Text(
                            when (selectedType) {
                                VaultType.LOGIN -> "Website / App Name *"
                                VaultType.CARD -> "Card Name / Bank *"
                                VaultType.IDENTITY -> "Identity Name / Title *"
                                VaultType.SECURE_NOTE -> "Note Title *"
                            }
                        )
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("Name cannot be empty", color = SecurityRed)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NebulaIndigo,
                        unfocusedBorderColor = FrostedGlassBorderLight,
                        focusedContainerColor = FrostedGlassSurface,
                        unfocusedContainerColor = FrostedGlassSurface,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_name_input")
                )
            }

            // Account Title / Variant (Allows multiple accounts per service)
            item {
                OutlinedTextField(
                    value = accountTitle,
                    onValueChange = { accountTitle = it },
                    label = { Text("Account Variant / Label (e.g. Personal, Work, Gaming)") },
                    placeholder = { Text("Primary Account", color = TextMutedSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NebulaIndigo,
                        unfocusedBorderColor = FrostedGlassBorderLight,
                        focusedContainerColor = FrostedGlassSurface,
                        unfocusedContainerColor = FrostedGlassSurface,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_account_title_input")
                )
            }

            // DYNAMIC FORM BY TYPE
            when (selectedType) {
                VaultType.LOGIN -> {
                    item {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("Website URL") },
                            placeholder = { Text("https://example.com", color = TextMutedSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Column {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        IconButton(onClick = {
                                            isPasswordVisible = !isPasswordVisible
                                            if (!isPasswordVisible) isPasswordCopied = false
                                        }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isPasswordVisible) "Hide Password" else "Reveal Password",
                                                tint = NebulaIndigo
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = isPasswordVisible && password.isNotBlank(),
                                            enter = fadeIn() + expandHorizontally(),
                                            exit = fadeOut() + shrinkHorizontally()
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    ClipboardHelper.copyToClipboard(context, "Password", password, 30)
                                                    isPasswordCopied = true
                                                    coroutineScope.launch {
                                                        delay(2000)
                                                        isPasswordCopied = false
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isPasswordCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Password",
                                                    tint = if (isPasswordCopied) SecurityGreen else NebulaIndigo
                                                )
                                            }
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NebulaIndigo,
                                    unfocusedBorderColor = FrostedGlassBorderLight,
                                    focusedContainerColor = FrostedGlassSurface,
                                    unfocusedContainerColor = FrostedGlassSurface,
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("item_password_input")
                            )

                            // Password Age Banner if in Edit Mode
                            if (isEditMode && passwordLastChangedAt > 0) {
                                val ageDays = ((System.currentTimeMillis() - passwordLastChangedAt) / (24 * 60 * 60 * 1000)).toInt()
                                val isOld = ageDays > 90
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isOld) SecurityAmber.copy(alpha = 0.12f) else FrostedGlassSurfaceVariant)
                                        .border(1.dp, if (isOld) SecurityAmber.copy(alpha = 0.35f) else FrostedGlassBorderLight, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (isOld) SecurityAmber else TextMutedSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (ageDays == 0) "Password changed today" else "Password age: $ageDays days old" + (if (isOld) " • Rotation advised" else ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOld) SecurityAmber else TextMutedSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Password Generator Preset Chips
                            Text(
                                text = "GENERATE SECURE PRESETS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextIndigoSubtle
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FrostedGlassSurfaceVariant)
                                        .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(10.dp))
                                        .clickable {
                                            password = EncryptionManager.generateStrongPreset()
                                            isPasswordVisible = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Strong 16", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = NebulaIndigo)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FrostedGlassSurfaceVariant)
                                        .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(10.dp))
                                        .clickable {
                                            password = EncryptionManager.generateMaxSecurityPreset()
                                            isPasswordVisible = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Max 24", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = NebulaFuchsia)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FrostedGlassSurfaceVariant)
                                        .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(10.dp))
                                        .clickable {
                                            password = EncryptionManager.generatePin(6)
                                            isPasswordVisible = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("6-Digit PIN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = NebulaBlue)
                                }
                            }
                        }
                    }
                }

                VaultType.CARD -> {
                    item {
                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it },
                            label = { Text("Cardholder Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = sensitiveData,
                            onValueChange = { sensitiveData = it },
                            label = { Text("Card Number") },
                            placeholder = { Text("4111 2222 3333 4444", color = TextMutedSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { expiryDate = it },
                                label = { Text("Expiry (MM/YY)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NebulaIndigo,
                                    unfocusedBorderColor = FrostedGlassBorderLight,
                                    focusedContainerColor = FrostedGlassSurface,
                                    unfocusedContainerColor = FrostedGlassSurface,
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { cvv = it },
                                label = { Text("CVV / CVC") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NebulaIndigo,
                                    unfocusedBorderColor = FrostedGlassBorderLight,
                                    focusedContainerColor = FrostedGlassSurface,
                                    unfocusedContainerColor = FrostedGlassSurface,
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it },
                            label = { Text("ATM / Card PIN (Optional)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                VaultType.IDENTITY -> {
                    item {
                        OutlinedTextField(
                            value = identityIdNumber,
                            onValueChange = { identityIdNumber = it },
                            label = { Text("ID / Passport / SSN / License Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                VaultType.SECURE_NOTE -> {
                    item {
                        OutlinedTextField(
                            value = sensitiveData,
                            onValueChange = { sensitiveData = it },
                            label = { Text("Encrypted Note Content") },
                            minLines = 5,
                            maxLines = 10,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NebulaIndigo,
                                unfocusedBorderColor = FrostedGlassBorderLight,
                                focusedContainerColor = FrostedGlassSurface,
                                unfocusedContainerColor = FrostedGlassSurface,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Category Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaIndigo,
                            unfocusedBorderColor = FrostedGlassBorderLight,
                            focusedContainerColor = FrostedGlassSurface,
                            unfocusedContainerColor = FrostedGlassSurface,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        VaultCategory.entries.filter { it != VaultCategory.ALL && it != VaultCategory.FAVORITES }.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Tags Field
            item {
                OutlinedTextField(
                    value = tagsString,
                    onValueChange = { tagsString = it },
                    label = { Text("Tags (comma-separated)") },
                    placeholder = { Text("finance, primary, 2fa", color = TextMutedSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NebulaIndigo,
                        unfocusedBorderColor = FrostedGlassBorderLight,
                        focusedContainerColor = FrostedGlassSurface,
                        unfocusedContainerColor = FrostedGlassSurface,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Custom Fields Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = NebulaIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CUSTOM FIELDS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextIndigoSubtle
                            )
                        }

                        if (!isAddingCustomField) {
                            TextButton(
                                onClick = { isAddingCustomField = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = NebulaIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Field", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = NebulaIndigo)
                            }
                        }
                    }

                    // Existing Custom Fields List
                    if (customFields.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            customFields.forEachIndexed { index, field ->
                                var isFieldRevealed by remember { mutableStateOf(!field.isSecured) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FrostedGlassSurfaceVariant)
                                        .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = field.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextIndigoSubtle
                                        )
                                        Text(
                                            text = if (field.isSecured && !isFieldRevealed) "••••••••••••" else field.value,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextWhitePrimary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (field.isSecured) {
                                            IconButton(
                                                onClick = { isFieldRevealed = !isFieldRevealed },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isFieldRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Toggle Reveal",
                                                    tint = NebulaIndigo,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                ClipboardHelper.copyToClipboard(context, field.label, field.value, 30)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Field",
                                                tint = TextMutedSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                customFields = customFields.filterIndexed { i, _ -> i != index }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove Field",
                                                tint = SecurityRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add Custom Field Inline Box
                    if (isAddingCustomField) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrostedGlassSurfaceVariant)
                                .border(1.dp, NebulaIndigo.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            OutlinedTextField(
                                value = newFieldLabel,
                                onValueChange = { newFieldLabel = it },
                                label = { Text("Field Name / Label (e.g. PIN, Security Answer, Recovery Code)") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NebulaIndigo,
                                    unfocusedBorderColor = FrostedGlassBorderLight,
                                    focusedContainerColor = FrostedGlassSurface,
                                    unfocusedContainerColor = FrostedGlassSurface,
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newFieldValue,
                                onValueChange = { newFieldValue = it },
                                label = { Text("Field Value") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NebulaIndigo,
                                    unfocusedBorderColor = FrostedGlassBorderLight,
                                    focusedContainerColor = FrostedGlassSurface,
                                    unfocusedContainerColor = FrostedGlassSurface,
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = newFieldSecured,
                                        onCheckedChange = { newFieldSecured = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = NebulaIndigo,
                                            uncheckedThumbColor = TextMutedSecondary,
                                            uncheckedTrackColor = FrostedGlassSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mask / Hide by Default", style = MaterialTheme.typography.labelMedium, color = TextWhitePrimary)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = {
                                            isAddingCustomField = false
                                            newFieldLabel = ""
                                            newFieldValue = ""
                                            newFieldSecured = false
                                        }
                                    ) {
                                        Text("Cancel", color = TextMutedSecondary)
                                    }

                                    Button(
                                        onClick = {
                                            if (newFieldLabel.isNotBlank()) {
                                                customFields = customFields + CustomField(
                                                    id = System.currentTimeMillis().toString(),
                                                    label = newFieldLabel.trim(),
                                                    value = newFieldValue.trim(),
                                                    isSecured = newFieldSecured
                                                )
                                                isAddingCustomField = false
                                                newFieldLabel = ""
                                                newFieldValue = ""
                                                newFieldSecured = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NebulaIndigo),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Add", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Notes
            if (selectedType != VaultType.SECURE_NOTE) {
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional Notes / Security Reminders") },
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaIndigo,
                            unfocusedBorderColor = FrostedGlassBorderLight,
                            focusedContainerColor = FrostedGlassSurface,
                            unfocusedContainerColor = FrostedGlassSurface,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Favorite Toggle
            item {
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
                                imageVector = if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (favorite) SecurityAmber else TextMutedSecondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Add to Favorites",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextWhitePrimary
                            )
                        }

                        Switch(
                            checked = favorite,
                            onCheckedChange = { favorite = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SecurityAmber,
                                uncheckedThumbColor = TextMutedSecondary,
                                uncheckedTrackColor = FrostedGlassSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Save & Cancel Buttons
            item {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Cancel", color = TextWhitePrimary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(listOf(NebulaIndigo, NebulaFuchsia))
                            )
                            .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(14.dp))
                            .clickable {
                                if (name.isBlank()) {
                                    nameError = true
                                    return@clickable
                                }

                                val payloadSensitiveData = when (selectedType) {
                                    VaultType.CARD -> "$sensitiveData|$expiryDate|$cvv|$pin|$cardholderName"
                                    VaultType.IDENTITY -> "$identityIdNumber|$phone|$address"
                                    else -> sensitiveData
                                }

                                val tagsList = tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                                val updatedPasswordTime = if (initialItem == null || initialItem.password != password) {
                                    System.currentTimeMillis()
                                } else {
                                    initialItem.passwordLastChangedAt
                                }

                                val newItem = VaultItem(
                                    id = initialItem?.id ?: 0L,
                                    type = selectedType,
                                    name = name.trim(),
                                    accountTitle = accountTitle.trim(),
                                    url = url.trim(),
                                    username = username.trim(),
                                    email = email.trim(),
                                    password = password,
                                    sensitiveData = payloadSensitiveData,
                                    customFields = customFields,
                                    notes = notes.trim(),
                                    category = selectedCategory,
                                    tags = tagsList,
                                    favorite = favorite,
                                    passwordLastChangedAt = updatedPasswordTime,
                                    createdAt = initialItem?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )

                                onSave(newItem)
                            }
                            .testTag("save_item_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEditMode) "Update Item" else "Save to Vault",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}
