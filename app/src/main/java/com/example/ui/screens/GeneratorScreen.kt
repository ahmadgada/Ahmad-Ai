package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import com.example.ui.animation.pressScale
import com.example.ui.animation.tactileClickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VaultCategory
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.security.EncryptionManager
import com.example.security.PasswordStrengthResult
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassSurface
import com.example.ui.theme.FrostedGlassSurfaceVariant
import com.example.ui.theme.NebulaBlue
import com.example.ui.theme.NebulaFuchsia
import com.example.ui.theme.NebulaIndigo
import com.example.ui.theme.TextIndigoSubtle
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun GeneratorScreen(
    onCopyPassword: (String) -> Unit,
    onSaveToVault: (VaultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPassphraseMode by remember { mutableStateOf(false) }

    // Password Mode Options
    var passwordLength by remember { mutableFloatStateOf(16f) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }

    // Passphrase Mode Options
    var wordCount by remember { mutableFloatStateOf(4f) }
    var separator by remember { mutableStateOf("-") }
    var capitalize by remember { mutableStateOf(true) }
    var includeNumberInPassphrase by remember { mutableStateOf(true) }

    var generatedResult by remember { mutableStateOf("") }
    var strength by remember {
        mutableStateOf(
            PasswordStrengthResult(
                score = 0,
                label = "EMPTY",
                color = 0xFF757575,
                entropyBits = 0,
                feedback = ""
            )
        )
    }

    fun regenerate() {
        generatedResult = if (isPassphraseMode) {
            EncryptionManager.generatePassphrase(
                wordCount = wordCount.toInt(),
                separator = separator,
                capitalize = capitalize,
                includeNumber = includeNumberInPassphrase
            )
        } else {
            EncryptionManager.generatePassword(
                length = passwordLength.toInt(),
                includeUppercase = includeUpper,
                includeLowercase = includeLower,
                includeNumbers = includeNumbers,
                includeSymbols = includeSymbols
            )
        }
        strength = EncryptionManager.calculateStrength(generatedResult)
    }

    LaunchedEffect(
        isPassphraseMode,
        passwordLength,
        includeUpper,
        includeLower,
        includeNumbers,
        includeSymbols,
        wordCount,
        separator,
        capitalize,
        includeNumberInPassphrase
    ) {
        regenerate()
    }

    val strengthColor by animateColorAsState(
        targetValue = Color(strength.color),
        label = "strength_color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = "PASSWORD GENERATOR",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextWhitePrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Cryptographically fortified random entropy",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextIndigoSubtle
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Mode Tabs (Password / Passphrase)
            item {
                TabRow(
                    selectedTabIndex = if (isPassphraseMode) 1 else 0,
                    containerColor = FrostedGlassSurfaceVariant,
                    contentColor = NebulaIndigo,
                    indicator = { tabPositions ->
                        val index = if (isPassphraseMode) 1 else 0
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = NebulaIndigo,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(18.dp))
                ) {
                    Tab(
                        selected = !isPassphraseMode,
                        onClick = { isPassphraseMode = false },
                        text = {
                            Text(
                                text = "Password",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (!isPassphraseMode) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (!isPassphraseMode) TextWhitePrimary else TextMutedSecondary
                            )
                        }
                    )
                    Tab(
                        selected = isPassphraseMode,
                        onClick = { isPassphraseMode = true },
                        text = {
                            Text(
                                text = "Passphrase",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isPassphraseMode) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isPassphraseMode) TextWhitePrimary else TextMutedSecondary
                            )
                        }
                    )
                }
            }

            // Generated Result Display Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(FrostedGlassSurface)
                        .border(1.5.dp, strengthColor.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPassphraseMode) "GENERATED PASSPHRASE" else "GENERATED PASSWORD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp
                                ),
                                color = TextIndigoSubtle
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = strengthColor.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, strengthColor.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "${strength.label} (${strength.entropyBits} bits)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = strengthColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password text container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(FrostedGlassSurfaceVariant)
                                .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = generatedResult,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = TextWhitePrimary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Strength Progress Bar
                        LinearProgressIndicator(
                            progress = { strength.score / 100f },
                            color = strengthColor,
                            trackColor = FrostedGlassSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(FrostedGlassSurfaceVariant)
                                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(14.dp))
                                    .tactileClickable(targetScale = 0.95f) { regenerate() }
                                    .padding(vertical = 12.dp)
                                    .testTag("generator_regenerate_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = NebulaIndigo
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Regenerate", color = TextWhitePrimary, style = MaterialTheme.typography.labelLarge)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(listOf(NebulaIndigo, NebulaFuchsia))
                                    )
                                    .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(14.dp))
                                    .tactileClickable(targetScale = 0.95f) { onCopyPassword(generatedResult) }
                                    .padding(vertical = 12.dp)
                                    .testTag("generator_copy_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }

            // Controls & Options
            if (!isPassphraseMode) {
                // PASSWORD CONFIGURATION
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(FrostedGlassSurface)
                            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Password Length",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = "${passwordLength.toInt()} characters",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NebulaIndigo
                                )
                            }

                            Slider(
                                value = passwordLength,
                                onValueChange = { passwordLength = it },
                                valueRange = 8f..64f,
                                steps = 55,
                                colors = SliderDefaults.colors(
                                    thumbColor = NebulaIndigo,
                                    activeTrackColor = NebulaIndigo,
                                    inactiveTrackColor = FrostedGlassSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            ToggleOptionRow(
                                title = "Uppercase Letters (A-Z)",
                                checked = includeUpper,
                                onCheckedChange = {
                                    if (includeUpper && !includeLower && !includeNumbers && !includeSymbols) return@ToggleOptionRow
                                    includeUpper = it
                                }
                            )

                            ToggleOptionRow(
                                title = "Lowercase Letters (a-z)",
                                checked = includeLower,
                                onCheckedChange = {
                                    if (includeLower && !includeUpper && !includeNumbers && !includeSymbols) return@ToggleOptionRow
                                    includeLower = it
                                }
                            )

                            ToggleOptionRow(
                                title = "Numbers (0-9)",
                                checked = includeNumbers,
                                onCheckedChange = {
                                    if (includeNumbers && !includeUpper && !includeLower && !includeSymbols) return@ToggleOptionRow
                                    includeNumbers = it
                                }
                            )

                            ToggleOptionRow(
                                title = "Symbols (!@#$%^&*)",
                                checked = includeSymbols,
                                onCheckedChange = {
                                    if (includeSymbols && !includeUpper && !includeLower && !includeNumbers) return@ToggleOptionRow
                                    includeSymbols = it
                                }
                            )
                        }
                    }
                }
            } else {
                // PASSPHRASE CONFIGURATION
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(FrostedGlassSurface)
                            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Word Count",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = "${wordCount.toInt()} words",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NebulaIndigo
                                )
                            }

                            Slider(
                                value = wordCount,
                                onValueChange = { wordCount = it },
                                valueRange = 3f..8f,
                                steps = 4,
                                colors = SliderDefaults.colors(
                                    thumbColor = NebulaIndigo,
                                    activeTrackColor = NebulaIndigo,
                                    inactiveTrackColor = FrostedGlassSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Word Separator",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextWhitePrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("-", "_", ".", " ").forEach { sep ->
                                    val isSelected = separator == sep
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) NebulaIndigo.copy(alpha = 0.25f) else FrostedGlassSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) NebulaIndigo else FrostedGlassBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .pressScale(0.92f)
                                            .clickable { separator = sep }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (sep == " ") "Space" else sep,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) TextWhitePrimary else TextMutedSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            ToggleOptionRow(
                                title = "Capitalize Each Word",
                                checked = capitalize,
                                onCheckedChange = { capitalize = it }
                            )

                            ToggleOptionRow(
                                title = "Include Number in Passphrase",
                                checked = includeNumberInPassphrase,
                                onCheckedChange = { includeNumberInPassphrase = it }
                            )
                        }
                    }
                }
            }

            // Save to Vault Shortcut Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(18.dp))
                        .tactileClickable(targetScale = 0.96f) {
                            val newItem = VaultItem(
                                id = 0L,
                                type = VaultType.LOGIN,
                                name = "Generated Credential",
                                url = "",
                                username = "",
                                email = "",
                                password = generatedResult,
                                sensitiveData = "",
                                notes = "Created via Cyber Guard Generator (${if (isPassphraseMode) "Passphrase" else "Password"})",
                                category = VaultCategory.PERSONAL,
                                tags = listOf("generated"),
                                favorite = false,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSaveToVault(newItem)
                        }
                        .padding(16.dp)
                        .testTag("save_generated_to_vault_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = NebulaIndigo
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save directly to Vault",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = TextWhitePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
private fun ToggleOptionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhitePrimary
        )

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
