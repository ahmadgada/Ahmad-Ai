package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.animation.MotionDuration
import com.example.ui.animation.MotionEasing
import com.example.ui.animation.pressScale
import com.example.ui.animation.tactileClickable
import com.example.ui.animation.tactileUnboundedClickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditSeverity
import com.example.model.AutoLockTimeout
import com.example.model.ClipboardTimeout
import com.example.model.VaultCategory
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassSurface
import com.example.ui.theme.FrostedGlassSurfaceHighlight
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
import com.example.model.AppNavDestination
import com.example.model.FindingSeverity
import com.example.model.ScoreStatus
import com.example.model.SecurityScore
import com.example.model.ThemeMode
import com.example.model.WatchtowerFinding
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanAccentDark
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.FrostedBackground
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassSurface
import com.example.ui.theme.FrostedGlassSurfaceHighlight
import com.example.ui.theme.FrostedGlassSurfaceVariant
import com.example.ui.theme.NebulaBlue
import com.example.ui.theme.NebulaBlueGlow
import com.example.ui.theme.NebulaFuchsia
import com.example.ui.theme.NebulaFuchsiaDark
import com.example.ui.theme.NebulaFuchsiaGlow
import com.example.ui.theme.NebulaIndigo
import com.example.ui.theme.NebulaIndigoDark
import com.example.ui.theme.NebulaIndigoGlow
import com.example.ui.theme.SecurityAmber
import com.example.ui.theme.SecurityBlue
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.TextIndigoSubtle
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun FrostedGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FrostedBackground)
            .drawBehind {
                val w = size.width
                val h = size.height

                // Top-left glowing Indigo orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NebulaIndigoGlow,
                            NebulaIndigoDark.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.05f, h * 0.08f),
                        radius = w * 0.85f
                    ),
                    radius = w * 0.85f,
                    center = Offset(w * 0.05f, h * 0.08f)
                )

                // Bottom-right glowing Fuchsia orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NebulaFuchsiaGlow,
                            NebulaFuchsiaDark.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.95f, h * 0.82f),
                        radius = w * 0.90f
                    ),
                    radius = w * 0.90f,
                    center = Offset(w * 0.95f, h * 0.82f)
                )

                // Mid-right glowing Electric Blue orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NebulaBlueGlow,
                            Color.Transparent
                        ),
                        center = Offset(w * 0.88f, h * 0.35f),
                        radius = w * 0.60f
                    ),
                    radius = w * 0.60f,
                    center = Offset(w * 0.88f, h * 0.35f)
                )
            }
    ) {
        content()
    }
}

@Composable
fun AhmadGuardTopBar(
    title: String = "AHMAD GUARD",
    subtitle: String? = null,
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    onLockClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrostedGlassSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = FrostedGlassBorder
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Frosted glass avatar badge with glowing gradient
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FrostedGlassSurfaceVariant)
                        .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NebulaIndigo, NebulaFuchsia)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = TextWhitePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    if (subtitle != null) {
                        Text(
                            text = subtitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.8.sp
                            ),
                            color = TextIndigoSubtle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextWhitePrimary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedGlassSurfaceVariant)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                        .tactileClickable(targetScale = 0.90f, onClick = onThemeToggle)
                        .testTag("theme_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (themeMode) {
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.AMOLED -> Icons.Default.Shield
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                        },
                        contentDescription = "Toggle Theme",
                        tint = TextWhitePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedGlassSurfaceVariant)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                        .tactileClickable(targetScale = 0.90f, onClick = onLockClick)
                        .testTag("lock_vault_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Vault",
                        tint = NebulaIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AhmadGuardBottomNav(
    currentDestination: AppNavDestination,
    onNavigate: (AppNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 6.dp,
                bottom = 12.dp + navBarPadding.calculateBottomPadding()
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating pill frosted glass navigation container
        Surface(
            color = Color(0xD9101424),
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = FrostedGlassBorderLight,
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = currentDestination == AppNavDestination.HOME,
                    onClick = { onNavigate(AppNavDestination.HOME) },
                    testTag = "nav_home"
                )

                NavItem(
                    icon = Icons.Default.Key,
                    label = "Vault",
                    selected = currentDestination == AppNavDestination.VAULT,
                    onClick = { onNavigate(AppNavDestination.VAULT) },
                    testTag = "nav_vault"
                )

                // Glowing Center Add Button with vibrant gradient & tactile bounce
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NebulaIndigo, NebulaFuchsia)
                            )
                        )
                        .border(2.dp, Color(0x60FFFFFF), CircleShape)
                        .tactileClickable(targetScale = 0.88f) { onNavigate(AppNavDestination.ADD) }
                        .testTag("nav_add"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Secure Item",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                NavItem(
                    icon = Icons.Default.Refresh,
                    label = "Generator",
                    selected = currentDestination == AppNavDestination.GENERATOR,
                    onClick = { onNavigate(AppNavDestination.GENERATOR) },
                    testTag = "nav_generator"
                )

                NavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    selected = currentDestination == AppNavDestination.SETTINGS,
                    onClick = { onNavigate(AppNavDestination.SETTINGS) },
                    testTag = "nav_settings"
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val animatedColor by animateColorAsState(
        targetValue = if (selected) NebulaIndigo else TextMutedSecondary,
        animationSpec = tween(180),
        label = "nav_color"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "nav_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .tactileClickable(targetScale = 0.90f, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        // Active indicator pill
        if (selected) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(NebulaIndigo.copy(alpha = 0.20f))
                    .border(1.dp, NebulaIndigo.copy(alpha = 0.4f), RoundedCornerShape(15.dp))
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Glowing micro dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(NebulaIndigo)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = animatedColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier.size(width = 44.dp, height = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = animatedColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
            ),
            color = if (selected) TextIndigoSubtle else TextMutedSecondary
        )
    }
}

@Composable
fun SecurityScoreCard(
    score: SecurityScore,
    onWatchtowerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score.score / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "score_anim"
    )

    val statusColor = Color(score.status.colorHex)

    // Frosted glass hero card with subtle glow ring
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(FrostedGlassSurface)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        FrostedGlassBorderLight,
                        statusColor.copy(alpha = 0.4f),
                        FrostedGlassBorder
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .testTag("security_score_card")
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NebulaIndigo.copy(alpha = 0.2f))
                            .border(1.dp, NebulaIndigo.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Watchtower",
                            tint = NebulaIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "WATCHTOWER DEFENSE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = TextIndigoSubtle
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = statusColor.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = score.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Ring Gauge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(92.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        strokeWidth = 8.dp,
                        color = Color(0x1FFFFFFF),
                        modifier = Modifier.size(92.dp)
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        strokeWidth = 8.dp,
                        color = statusColor,
                        modifier = Modifier.size(92.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${score.score}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = TextWhitePrimary
                        )
                        Text(
                            text = "HEALTH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            ),
                            color = TextMutedSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vault Protection Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhitePrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (score.status) {
                            ScoreStatus.EXCELLENT -> "Vault credentials are fully fortified and encrypted."
                            ScoreStatus.STRONG -> "Good defense posture. Minor hygiene updates recommended."
                            ScoreStatus.GOOD -> "Vault is reasonably protected. Review recommendations."
                            ScoreStatus.NEEDS_ACTION -> "Vulnerabilities detected. Review weak or reused secrets."
                            ScoreStatus.CRITICAL -> "Critical security risks found. Immediate action required."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextMutedSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Frosted glass statistics pills in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    label = "Total",
                    value = score.totalPasswords.toString(),
                    color = NebulaIndigo,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Strong",
                    value = score.strongCount.toString(),
                    color = SecurityGreen,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Weak",
                    value = score.weakCount.toString(),
                    color = SecurityAmber,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Reused",
                    value = score.reusedCount.toString(),
                    color = SecurityRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glowing frosted action button with tactile press
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FrostedGlassSurfaceVariant)
                    .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(16.dp))
                    .tactileClickable(targetScale = 0.96f, onClick = onWatchtowerClick)
                    .padding(vertical = 12.dp)
                    .testTag("open_watchtower_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NebulaIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View Watchtower Analysis",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextWhitePrimary
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedGlassSurfaceVariant)
            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                ),
                color = TextMutedSecondary
            )
        }
    }
}

@Composable
fun VaultItemCard(
    item: VaultItem,
    onItemClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCopyPassword: () -> Unit,
    onCopyUsername: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordCopied by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val starScale by animateFloatAsState(
        targetValue = if (item.favorite) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "star_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FrostedGlassSurface)
            .border(
                1.dp,
                if (isPasswordVisible) NebulaIndigo.copy(alpha = 0.55f) else FrostedGlassBorder,
                RoundedCornerShape(24.dp)
            )
            .pressScale(0.985f)
            .clickable {
                if (isPasswordVisible) {
                    // Tapping outside the password area hides the revealed password and Copy button
                    isPasswordVisible = false
                    isPasswordCopied = false
                } else {
                    onItemClick()
                }
            }
            .testTag("vault_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val (icon, tint) = when (item.type) {
                        VaultType.LOGIN -> Icons.Default.Key to NebulaIndigo
                        VaultType.CARD -> Icons.Default.CreditCard to NebulaFuchsia
                        VaultType.IDENTITY -> Icons.Default.Person to NebulaBlue
                        VaultType.SECURE_NOTE -> Icons.Default.Notes to SecurityAmber
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tint.copy(alpha = 0.18f))
                            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.type.displayName,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextWhitePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (item.accountTitle.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NebulaIndigo.copy(alpha = 0.20f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NebulaIndigo.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = item.accountTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = NebulaIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FrostedGlassSurfaceHighlight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedGlassBorder)
                            ) {
                                Text(
                                    text = item.category.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextIndigoSubtle,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            if (item.username.isNotBlank() || item.email.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.username.ifBlank { item.email },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMutedSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (item.customFields.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${item.customFields.size} field${if (item.customFields.size == 1) "" else "s"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextIndigoSubtle
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            scaleX = starScale
                            scaleY = starScale
                        }
                ) {
                    Icon(
                        imageVector = if (item.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (item.favorite) SecurityAmber else TextMutedSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Password / Sensitive field preview pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FrostedGlassSurfaceVariant)
                    .border(
                        1.dp,
                        if (isPasswordVisible) NebulaIndigo.copy(alpha = 0.45f) else FrostedGlassBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Intentional: Never copy the password when simply tapping the password field.
                        // Prevent accidental clipboard triggers.
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displaySecret = when (item.type) {
                        VaultType.LOGIN -> if (isPasswordVisible) item.password else "••••••••••••"
                        VaultType.CARD -> if (isPasswordVisible) item.sensitiveData else "•••• •••• •••• ••••"
                        VaultType.IDENTITY -> if (isPasswordVisible) item.sensitiveData else "••••••••••••"
                        VaultType.SECURE_NOTE -> if (isPasswordVisible) item.sensitiveData.take(40) else "•••• Encrypted Note ••••"
                    }

                    Text(
                        text = displaySecret,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            letterSpacing = if (!isPasswordVisible) 2.sp else 0.sp
                        ),
                        color = if (isPasswordVisible) TextWhitePrimary else TextMutedSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Eye (Show/Hide) button - Always visible
                        IconButton(
                            onClick = {
                                isPasswordVisible = !isPasswordVisible
                                if (!isPasswordVisible) {
                                    isPasswordCopied = false
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .tactileUnboundedClickable(targetScale = 0.88f) {
                                    isPasswordVisible = !isPasswordVisible
                                    if (!isPasswordVisible) {
                                        isPasswordCopied = false
                                    }
                                }
                                .testTag("vault_item_eye_${item.id}")
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide Password" else "Reveal Password",
                                tint = if (isPasswordVisible) NebulaIndigo else TextMutedSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Copy button - Smoothly reveals ONLY when password is shown
                        AnimatedVisibility(
                            visible = isPasswordVisible,
                            enter = fadeIn(tween(180, easing = MotionEasing.Standard)) +
                                    expandHorizontally(tween(220, easing = MotionEasing.Standard)),
                            exit = fadeOut(tween(140, easing = MotionEasing.Standard)) +
                                    shrinkHorizontally(tween(180, easing = MotionEasing.Standard))
                        ) {
                            IconButton(
                                onClick = {
                                    onCopyPassword()
                                    isPasswordCopied = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        isPasswordCopied = false
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .tactileUnboundedClickable(targetScale = 0.88f) {
                                        onCopyPassword()
                                        isPasswordCopied = true
                                        coroutineScope.launch {
                                            delay(2000)
                                            isPasswordCopied = false
                                        }
                                    }
                                    .testTag("vault_item_copy_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy Password",
                                    tint = if (isPasswordCopied) SecurityGreen else NebulaIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.username.isNotBlank() || item.email.isNotBlank()) {
                    TextButton(
                        onClick = onCopyUsername,
                        modifier = Modifier.pressScale(0.94f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Copy ${if (item.username.isNotBlank()) "User" else "Email"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = NebulaIndigo
                        )
                    }
                }

                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.pressScale(0.94f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = TextMutedSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMutedSecondary
                    )
                }

                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.pressScale(0.94f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = SecurityRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecurityRed
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isDestructive) SecurityRed else TextWhitePrimary
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) SecurityRed else NebulaIndigo,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(confirmText, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    dismissText,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextMutedSecondary
                )
            }
        },
        containerColor = Color(0xF0131728),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(28.dp))
    )
}

