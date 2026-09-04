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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FindingSeverity
import com.example.model.SecurityScore
import com.example.model.VaultItem
import com.example.model.WatchtowerFinding
import com.example.ui.components.SecurityHealthInteractiveCard
import com.example.ui.components.SecurityScoreCard
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
fun WatchtowerScreen(
    securityScore: SecurityScore,
    findings: List<WatchtowerFinding>,
    allItems: List<VaultItem>,
    onFixItem: (VaultItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSeverityFilter by remember { mutableStateOf<FindingSeverity?>(null) }

    val filteredFindings = if (selectedSeverityFilter == null) {
        findings
    } else {
        findings.filter { it.severity == selectedSeverityFilter }
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
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedGlassSurfaceVariant)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NebulaIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "WATCHTOWER DEFENSE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextWhitePrimary
                    )
                    Text(
                        text = "${findings.size} security assessment finding${if (findings.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (findings.isNotEmpty()) SecurityAmber else SecurityGreen
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Overview Interactive Security Health Gauge Card
            item {
                SecurityHealthInteractiveCard(
                    score = securityScore,
                    onWatchtowerClick = {}
                )
            }

            // Severity Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeverityFilterChip(
                        label = "All (${findings.size})",
                        selected = selectedSeverityFilter == null,
                        color = NebulaIndigo,
                        onClick = { selectedSeverityFilter = null },
                        modifier = Modifier.weight(1f)
                    )

                    val criticalCount = findings.count { it.severity == FindingSeverity.CRITICAL }
                    SeverityFilterChip(
                        label = "Critical ($criticalCount)",
                        selected = selectedSeverityFilter == FindingSeverity.CRITICAL,
                        color = SecurityRed,
                        onClick = { selectedSeverityFilter = FindingSeverity.CRITICAL },
                        modifier = Modifier.weight(1f)
                    )

                    val warningCount = findings.count { it.severity == FindingSeverity.WARNING }
                    SeverityFilterChip(
                        label = "Warning ($warningCount)",
                        selected = selectedSeverityFilter == FindingSeverity.WARNING,
                        color = SecurityAmber,
                        onClick = { selectedSeverityFilter = FindingSeverity.WARNING },
                        modifier = Modifier.weight(1f)
                    )

                    val infoCount = findings.count { it.severity == FindingSeverity.INFO }
                    SeverityFilterChip(
                        label = "2FA ($infoCount)",
                        selected = selectedSeverityFilter == FindingSeverity.INFO,
                        color = NebulaBlue,
                        onClick = { selectedSeverityFilter = FindingSeverity.INFO },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Findings List or All Clear State
            if (filteredFindings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(FrostedGlassSurface)
                            .border(1.dp, SecurityGreen.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(SecurityGreen.copy(alpha = 0.18f))
                                    .border(1.dp, SecurityGreen.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SecurityGreen,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "ALL DEFENSES ACTIVE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhitePrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (findings.isEmpty())
                                    "No weak passwords, reused credentials, or missing 2FA detected. Great job maintaining cyber hygiene!"
                                else
                                    "No findings under the selected filter category.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredFindings, key = { it.id }) { finding ->
                    WatchtowerFindingCard(
                        finding = finding,
                        onFix = {
                            val targetItem = allItems.find { it.id == finding.itemId }
                            if (targetItem != null) {
                                onFixItem(targetItem)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun WatchtowerFindingCard(
    finding: WatchtowerFinding,
    onFix: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (severityColor, severityLabel) = when (finding.severity) {
        FindingSeverity.CRITICAL -> SecurityRed to "CRITICAL RISK"
        FindingSeverity.WARNING -> SecurityAmber to "WARNING"
        FindingSeverity.INFO -> NebulaBlue to "RECOMMENDATION"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, severityColor.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .testTag("finding_${finding.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = severityColor.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, severityColor.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = severityLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = finding.itemName ?: "Vault Item",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = NebulaIndigo
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = finding.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextWhitePrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = finding.explanation,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                color = TextMutedSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FrostedGlassSurfaceVariant)
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NebulaIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finding.recommendation,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                        color = TextWhitePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(severityColor.copy(alpha = 0.18f))
                    .border(1.dp, severityColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onFix)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = severityColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fix '${finding.itemName ?: "Item"}' in Vault",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = severityColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityFilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) color.copy(alpha = 0.25f) else FrostedGlassSurfaceVariant)
            .border(
                1.dp,
                if (selected) color.copy(alpha = 0.7f) else FrostedGlassBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (selected) TextWhitePrimary else TextMutedSecondary,
            textAlign = TextAlign.Center
        )
    }
}
