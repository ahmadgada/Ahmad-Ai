package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNavDestination
import com.example.model.SecurityScore
import com.example.model.ThemeMode
import com.example.model.VaultItem
import com.example.ui.components.AhmadGuardTopBar
import com.example.ui.components.SecurityHealthInteractiveCard
import com.example.ui.components.SecurityScoreCard
import com.example.ui.components.VaultItemCard
import com.example.ui.animation.pressScale
import com.example.ui.animation.tactileClickable
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

@Composable
fun HomeScreen(
    securityScore: SecurityScore,
    vaultItems: List<VaultItem>,
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    onLockClick: () -> Unit,
    onNavigate: (AppNavDestination) -> Unit,
    onItemClick: (VaultItem) -> Unit,
    onFavoriteToggle: (VaultItem) -> Unit,
    onCopyPassword: (VaultItem) -> Unit,
    onCopyUsername: (VaultItem) -> Unit,
    onEditItem: (VaultItem) -> Unit,
    onDeleteItem: (VaultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = vaultItems.size
    val subtitleText = "$totalCount item${if (totalCount == 1) "" else "s"} fortified"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        AhmadGuardTopBar(
            title = "AHMAD GUARD",
            subtitle = subtitleText,
            themeMode = themeMode,
            onThemeToggle = onThemeToggle,
            onLockClick = onLockClick,
            onInfoClick = { onNavigate(AppNavDestination.SETTINGS) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Watchtower Security Health Interactive Custom Canvas Card
            item {
                SecurityHealthInteractiveCard(
                    score = securityScore,
                    onWatchtowerClick = { onNavigate(AppNavDestination.WATCHTOWER) }
                )
            }

            // Quick Actions Section
            item {
                Column {
                    Text(
                        text = "QUICK ACTIONS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp
                        ),
                        color = TextIndigoSubtle
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Add Item",
                            subtitle = "Password / Note",
                            icon = Icons.Default.Add,
                            accentColor = NebulaIndigo,
                            onClick = { onNavigate(AppNavDestination.ADD) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_add"
                        )

                        QuickActionCard(
                            title = "AI Assistant",
                            subtitle = "Security Expert",
                            icon = Icons.Default.AutoAwesome,
                            accentColor = NebulaFuchsia,
                            onClick = { onNavigate(AppNavDestination.AI_ASSISTANT) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_ai"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Generator",
                            subtitle = "Passphrase / Key",
                            icon = Icons.Default.Refresh,
                            accentColor = NebulaBlue,
                            onClick = { onNavigate(AppNavDestination.GENERATOR) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_generator"
                        )

                        QuickActionCard(
                            title = "Scanner",
                            subtitle = "QR / 2FA Keys",
                            icon = Icons.Default.QrCodeScanner,
                            accentColor = SecurityAmber,
                            onClick = { onNavigate(AppNavDestination.SECURITY_SCANNER) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_scanner"
                        )
                    }
                }
            }

            // Favorites & Recent Section
            item {
                val recentOrFavs = vaultItems.filter { it.favorite }.ifEmpty { vaultItems.take(3) }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (vaultItems.any { it.favorite }) "FAVORITES" else "RECENT VAULT ITEMS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.6.sp
                            ),
                            color = TextIndigoSubtle
                        )

                        if (vaultItems.isNotEmpty()) {
                            TextButton(onClick = { onNavigate(AppNavDestination.VAULT) }) {
                                Text(
                                    text = "View All (${vaultItems.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = NebulaIndigo
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (recentOrFavs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(FrostedGlassSurface)
                                .border(1.dp, FrostedGlassBorder, RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(NebulaIndigo.copy(alpha = 0.18f))
                                        .border(1.dp, NebulaIndigo.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = NebulaIndigo,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Your Vault is Empty",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextWhitePrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Add your first credential to activate Watchtower protection",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMutedSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { onNavigate(AppNavDestination.ADD) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NebulaIndigo,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Add Secure Item", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (item in recentOrFavs) {
                                VaultItemCard(
                                    item = item,
                                    onItemClick = { onItemClick(item) },
                                    onFavoriteToggle = { onFavoriteToggle(item) },
                                    onCopyPassword = { onCopyPassword(item) },
                                    onCopyUsername = { onCopyUsername(item) },
                                    onEditClick = { onEditItem(item) },
                                    onDeleteClick = { onDeleteItem(item) }
                                )
                            }
                        }
                    }
                }
            }

            // Security Recommendations / Tips Carousel
            item {
                Column {
                    Text(
                        text = "SECURITY ADVISORIES",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp
                        ),
                        color = TextIndigoSubtle
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            SecurityTipCard(
                                title = "End-to-End Encryption",
                                description = "Credentials are encrypted with AES-256-GCM and PBKDF2 key derivation.",
                                icon = Icons.Default.Shield,
                                tag = "CRYPTOGRAPHY",
                                accentColor = NebulaIndigo
                            )
                        }
                        item {
                            SecurityTipCard(
                                title = "Eliminate Password Reuse",
                                description = "Watchtower identifies duplicate credentials across distinct websites.",
                                icon = Icons.Default.Key,
                                tag = "WATCHTOWER",
                                accentColor = NebulaFuchsia
                            )
                        }
                        item {
                            SecurityTipCard(
                                title = "Hardware-Backed Security",
                                description = "Master secrets are pinned to Android Keystore cryptographic keys.",
                                icon = Icons.Default.Security,
                                tag = "RECOVERY",
                                accentColor = NebulaBlue
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
            .tactileClickable(targetScale = 0.96f, onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhitePrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SecurityTipCard(
    title: String,
    description: String,
    icon: ImageVector,
    tag: String,
    accentColor: Color = NebulaIndigo
) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(22.dp))
            .pressScale(0.98f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextWhitePrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                color = TextMutedSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
