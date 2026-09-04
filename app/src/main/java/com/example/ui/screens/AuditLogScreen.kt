package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditEventType
import com.example.model.AuditSeverity
import com.example.model.SecurityAuditEvent
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogScreen(
    auditEvents: List<SecurityAuditEvent>,
    onExportAuditLog: suspend () -> String,
    onClearAuditLog: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSeverityFilter by remember { mutableStateOf<AuditSeverity?>(null) }
    var selectedEventForDetail by remember { mutableStateOf<SecurityAuditEvent?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedAuditJson by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Filtered Events
    val filteredEvents = remember(auditEvents, searchQuery, selectedSeverityFilter) {
        auditEvents.filter { event ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                event.title.lowercase().contains(q) ||
                        event.details.lowercase().contains(q) ||
                        event.eventType.displayName.lowercase().contains(q) ||
                        event.actor.lowercase().contains(q) ||
                        event.contextInfo.lowercase().contains(q)
            }
            val matchesSeverity = selectedSeverityFilter == null || event.severity == selectedSeverityFilter
            matchesSearch && matchesSeverity
        }
    }

    // Counts
    val criticalCount = remember(auditEvents) { auditEvents.count { it.severity == AuditSeverity.CRITICAL } }
    val warningCount = remember(auditEvents) { auditEvents.count { it.severity == AuditSeverity.WARNING } }
    val successCount = remember(auditEvents) { auditEvents.count { it.severity == AuditSeverity.SUCCESS } }

    // Clear confirmation dialog
    if (showClearConfirmDialog) {
        ConfirmationDialog(
            title = "PURGE SECURITY AUDIT TRAIL?",
            message = "This will permanently delete all logged authentication and security events. A new cryptographic initialization event will be created.",
            confirmText = "Purge Audit Log",
            isDestructive = true,
            onConfirm = {
                showClearConfirmDialog = false
                onClearAuditLog()
            },
            onDismiss = { showClearConfirmDialog = false }
        )
    }

    // Event Detail Dialog
    if (selectedEventForDetail != null) {
        val event = selectedEventForDetail!!
        val dateFormatted = remember(event.timestamp) {
            SimpleDateFormat("MMMM dd, yyyy • hh:mm:ss a (z)", Locale.getDefault()).format(Date(event.timestamp))
        }

        AlertDialog(
            onDismissRequest = { selectedEventForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(event.severity.colorHex).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getSeverityIcon(event.severity),
                            contentDescription = null,
                            tint = Color(event.severity.colorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = event.eventType.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhitePrimary
                        )
                        Text(
                            text = event.severity.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(event.severity.colorHex)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FrostedGlassSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedGlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "EVENT DETAILS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = NebulaIndigo
                            )
                            Text(
                                text = event.details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextWhitePrimary
                            )
                        }
                    }

                    AuditDetailRow(label = "Timestamp", value = dateFormatted)
                    AuditDetailRow(label = "Security Principal", value = event.actor)
                    AuditDetailRow(label = "Context & Enclave", value = event.contextInfo)
                    AuditDetailRow(label = "Record ID (Room DB)", value = "#${event.id}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = "Cyber Guard Security Event:\nEvent: ${event.title}\nType: ${event.eventType.displayName}\nSeverity: ${event.severity}\nTime: $dateFormatted\nDetails: ${event.details}\nContext: ${event.contextInfo}"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Security Event", shareText))
                        onShowSnackbar("Event details copied to clipboard")
                        selectedEventForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaIndigo, contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Details")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEventForDetail = null }) {
                    Text("Close", color = TextMutedSecondary)
                }
            },
            containerColor = Color(0xFF141624),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Export Audit Log Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = SecurityGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENCRYPTED AUDIT EXPORT",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhitePrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your Room database audit log has been sealed into an encrypted ledger archive using AES-256-GCM with SHA-256 integrity verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FrostedGlassSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedGlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = exportedAuditJson.take(220) + if (exportedAuditJson.length > 220) "\n... [AES-256-GCM Encrypted Payload]" else "",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            color = TextWhitePrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NebulaIndigo.copy(alpha = 0.15f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Algorithm: AES-256-GCM", style = MaterialTheme.typography.labelSmall, color = NebulaIndigo)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecurityGreen.copy(alpha = 0.15f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Events: ${auditEvents.size}", style = MaterialTheme.typography.labelSmall, color = SecurityGreen)
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Cyber Guard Encrypted Audit Log", exportedAuditJson))
                            onShowSnackbar("Encrypted audit ledger copied to clipboard")
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaIndigo, contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }

                    Button(
                        onClick = {
                            try {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, exportedAuditJson)
                                    putExtra(Intent.EXTRA_SUBJECT, "Cyber Guard - Encrypted Security Audit Log")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Export Encrypted Audit Log")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                onShowSnackbar("Unable to open share sheet")
                            }
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen, contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = TextMutedSecondary)
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
        // Screen Top Bar
        Surface(
            color = FrostedGlassSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FrostedGlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhitePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "SECURITY AUDIT LOG",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextWhitePrimary
                        )
                        Text(
                            text = "Hardware-Encrypted Room Event Ledger",
                            style = MaterialTheme.typography.labelSmall,
                            color = NebulaIndigo
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Export Button
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                exportedAuditJson = onExportAuditLog()
                                showExportDialog = true
                            }
                        },
                        modifier = Modifier.testTag("export_audit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export Encrypted Audit Log",
                            tint = SecurityGreen
                        )
                    }

                    // Purge Button
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.testTag("purge_audit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Audit Log",
                            tint = SecurityRed
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AuditMetricCard(
                        title = "TOTAL EVENTS",
                        value = "${auditEvents.size}",
                        accentColor = NebulaIndigo,
                        modifier = Modifier.weight(1f)
                    )
                    AuditMetricCard(
                        title = "CRITICAL",
                        value = "$criticalCount",
                        accentColor = SecurityRed,
                        modifier = Modifier.weight(1f)
                    )
                    AuditMetricCard(
                        title = "SUCCESSFUL",
                        value = "$successCount",
                        accentColor = SecurityGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Export Banner Action Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    NebulaIndigo.copy(alpha = 0.25f),
                                    NebulaBlue.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .border(1.dp, NebulaIndigo.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .clickable {
                            coroutineScope.launch {
                                exportedAuditJson = onExportAuditLog()
                                showExportDialog = true
                            }
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(NebulaIndigo.copy(alpha = 0.3f))
                                    .border(1.dp, NebulaIndigo, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Export Encrypted Audit Log",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = "Generate zero-knowledge encrypted backup record",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = SecurityGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search audit logs by event, actor, or text...", color = TextMutedSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NebulaIndigo)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMutedSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NebulaIndigo,
                        unfocusedBorderColor = FrostedGlassBorder,
                        focusedContainerColor = FrostedGlassSurface,
                        unfocusedContainerColor = FrostedGlassSurface,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Severity Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedSeverityFilter == null,
                            onClick = { selectedSeverityFilter = null },
                            label = { Text("ALL (${auditEvents.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NebulaIndigo,
                                selectedLabelColor = Color.White,
                                containerColor = FrostedGlassSurface,
                                labelColor = TextMutedSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedSeverityFilter == null,
                                borderColor = FrostedGlassBorder,
                                selectedBorderColor = NebulaIndigo
                            )
                        )
                    }
                    items(AuditSeverity.entries.toTypedArray()) { severity ->
                        val count = auditEvents.count { it.severity == severity }
                        FilterChip(
                            selected = selectedSeverityFilter == severity,
                            onClick = {
                                selectedSeverityFilter = if (selectedSeverityFilter == severity) null else severity
                            },
                            label = { Text("${severity.name} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(severity.colorHex).copy(alpha = 0.25f),
                                selectedLabelColor = Color(severity.colorHex),
                                containerColor = FrostedGlassSurface,
                                labelColor = TextMutedSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedSeverityFilter == severity,
                                borderColor = FrostedGlassBorder,
                                selectedBorderColor = Color(severity.colorHex)
                            )
                        )
                    }
                }
            }

            // Audit Events List
            if (filteredEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = TextMutedSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching security events found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    AuditEventItemCard(
                        event = event,
                        onClick = { selectedEventForDetail = event }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun AuditMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = TextMutedSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = accentColor
            )
        }
    }
}

@Composable
private fun AuditEventItemCard(
    event: SecurityAuditEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(event.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(event.timestamp))
    }
    val severityColor = Color(event.severity.colorHex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, severityColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Severity icon pill
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(severityColor.copy(alpha = 0.15f))
                    .border(1.dp, severityColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getSeverityIcon(event.severity),
                    contentDescription = null,
                    tint = severityColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhitePrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.details,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = TextMutedSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(severityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.eventType.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = severityColor
                        )
                    }

                    Text(
                        text = event.contextInfo,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextIndigoSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMutedSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextWhitePrimary
        )
    }
}

private fun getSeverityIcon(severity: AuditSeverity): ImageVector {
    return when (severity) {
        AuditSeverity.CRITICAL -> Icons.Default.Warning
        AuditSeverity.WARNING -> Icons.Default.Warning
        AuditSeverity.SUCCESS -> Icons.Default.CheckCircle
        AuditSeverity.INFO -> Icons.Default.Info
    }
}
