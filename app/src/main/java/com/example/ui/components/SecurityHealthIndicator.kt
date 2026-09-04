package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScoreStatus
import com.example.model.SecurityScore
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Canvas 240-degree Security Health Gauge
 */
@Composable
fun SecurityHealthCanvasGauge(
    score: SecurityScore,
    modifier: Modifier = Modifier
) {
    val animatedScoreFraction by animateFloatAsState(
        targetValue = score.score / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )

    val complexityFraction by animateFloatAsState(
        targetValue = score.complexityScore / 100f,
        animationSpec = tween(durationMillis = 900, delayMillis = 100),
        label = "complexity_progress"
    )

    val uniquenessFraction by animateFloatAsState(
        targetValue = score.uniquenessScore / 100f,
        animationSpec = tween(durationMillis = 900, delayMillis = 200),
        label = "uniqueness_progress"
    )

    val twoFactorFraction by animateFloatAsState(
        targetValue = score.twoFactorScore / 100f,
        animationSpec = tween(durationMillis = 900, delayMillis = 300),
        label = "twofactor_progress"
    )

    val statusColor = Color(score.status.colorHex)

    val infiniteTransition = rememberInfiniteTransition(label = "bead_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("security_health_canvas_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 20.dp.toPx()

            val startAngle = 150f
            val totalSweep = 240f

            // 1. Background Outer Track Arc
            drawArc(
                color = Color(0x18FFFFFF),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Radial Tick Marks
            val tickCount = 24
            for (i in 0..tickCount) {
                val fraction = i.toFloat() / tickCount
                val angleDeg = startAngle + (totalSweep * fraction)
                val angleRad = angleDeg * (PI.toFloat() / 180f)

                val isMajorTick = i % 4 == 0
                val tickLength = if (isMajorTick) 10.dp.toPx() else 5.dp.toPx()
                val tickWidth = if (isMajorTick) 2.5.dp.toPx() else 1.2.dp.toPx()
                val tickRadiusStart = radius + 12.dp.toPx()
                val tickRadiusEnd = tickRadiusStart + tickLength

                val startP = Offset(
                    center.x + tickRadiusStart * cos(angleRad),
                    center.y + tickRadiusStart * sin(angleRad)
                )
                val endP = Offset(
                    center.x + tickRadiusEnd * cos(angleRad),
                    center.y + tickRadiusEnd * sin(angleRad)
                )

                val tickActive = fraction <= animatedScoreFraction
                val tickColor = if (tickActive) statusColor.copy(alpha = 0.8f) else Color(0x22FFFFFF)

                drawLine(
                    color = tickColor,
                    start = startP,
                    end = endP,
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. Active Primary Security Gauge Arc (Gradient sweep)
            val activeSweep = totalSweep * animatedScoreFraction
            if (activeSweep > 0f) {
                val gaugeGradient = Brush.sweepGradient(
                    0.0f to SecurityRed,
                    0.4f to SecurityAmber,
                    0.7f to NebulaIndigo,
                    1.0f to SecurityGreen,
                    center = center
                )

                drawArc(
                    brush = gaugeGradient,
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Glowing Head Bead at current progress
                val beadAngleRad = (startAngle + activeSweep) * (PI.toFloat() / 180f)
                val beadCenter = Offset(
                    center.x + radius * cos(beadAngleRad),
                    center.y + radius * sin(beadAngleRad)
                )

                // Outer halo
                drawCircle(
                    color = statusColor.copy(alpha = 0.35f * pulseGlow),
                    radius = 12.dp.toPx(),
                    center = beadCenter
                )
                // Solid core
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = beadCenter
                )
            }

            // 5. Inner Micro-Tracks (Sub-metric Rings)
            // Complexity (Outer inner)
            val r1 = radius - 16.dp.toPx()
            drawArc(
                color = Color(0x10FFFFFF),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - r1, center.y - r1),
                size = Size(r1 * 2, r1 * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = NebulaIndigo.copy(alpha = 0.85f),
                startAngle = startAngle,
                sweepAngle = totalSweep * complexityFraction,
                useCenter = false,
                topLeft = Offset(center.x - r1, center.y - r1),
                size = Size(r1 * 2, r1 * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Uniqueness (Middle inner)
            val r2 = radius - 24.dp.toPx()
            drawArc(
                color = Color(0x10FFFFFF),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - r2, center.y - r2),
                size = Size(r2 * 2, r2 * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = NebulaFuchsia.copy(alpha = 0.85f),
                startAngle = startAngle,
                sweepAngle = totalSweep * uniquenessFraction,
                useCenter = false,
                topLeft = Offset(center.x - r2, center.y - r2),
                size = Size(r2 * 2, r2 * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2FA Protection (Deep inner)
            val r3 = radius - 32.dp.toPx()
            drawArc(
                color = Color(0x10FFFFFF),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - r3, center.y - r3),
                size = Size(r3 * 2, r3 * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = NebulaBlue.copy(alpha = 0.85f),
                startAngle = startAngle,
                sweepAngle = totalSweep * twoFactorFraction,
                useCenter = false,
                topLeft = Offset(center.x - r3, center.y - r3),
                size = Size(r3 * 2, r3 * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center Digital Readout & Tier Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${(score.score * animatedScoreFraction).toInt()}",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    color = TextWhitePrimary
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextMutedSecondary,
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.20f),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
            ) {
                Text(
                    text = score.status.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "SECURITY SCORE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextIndigoSubtle
            )
        }
    }
}

/**
 * Custom Canvas 5-Axis Spider / Radar Security Health Matrix
 */
@Composable
fun SecurityRadarHealthCanvas(
    score: SecurityScore,
    modifier: Modifier = Modifier
) {
    val axisNames = listOf(
        "Complexity",
        "Uniqueness",
        "2FA Coverage",
        "Freshness",
        "Breach Clean"
    )

    val values = listOf(
        score.complexityScore / 100f,
        score.uniquenessScore / 100f,
        score.twoFactorScore / 100f,
        score.freshnessScore / 100f,
        score.breachSafetyScore / 100f
    )

    val animatedValues = values.mapIndexed { index, targetVal ->
        animateFloatAsState(
            targetValue = targetVal,
            animationSpec = tween(durationMillis = 800, delayMillis = index * 100),
            label = "radar_axis_$index"
        ).value
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("security_radar_health_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f - 24.dp.toPx()
            val numAxes = axisNames.size
            val angleStep = (2 * PI / numAxes).toFloat()

            // 1. Draw Concentric Radar Grid Rings (20%, 40%, 60%, 80%, 100%)
            val gridLevels = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
            for (level in gridLevels) {
                val currentRadius = maxRadius * level
                val gridPath = Path()

                for (i in 0 until numAxes) {
                    val angle = -PI.toFloat() / 2f + i * angleStep
                    val px = center.x + currentRadius * cos(angle)
                    val py = center.y + currentRadius * sin(angle)
                    if (i == 0) gridPath.moveTo(px, py) else gridPath.lineTo(px, py)
                }
                gridPath.close()

                drawPath(
                    path = gridPath,
                    color = Color(0x18FFFFFF),
                    style = Stroke(width = if (level == 1.0f) 1.5.dp.toPx() else 1.dp.toPx())
                )
            }

            // 2. Draw Spokes / Axis Lines
            for (i in 0 until numAxes) {
                val angle = -PI.toFloat() / 2f + i * angleStep
                val endX = center.x + maxRadius * cos(angle)
                val endY = center.y + maxRadius * sin(angle)

                drawLine(
                    color = Color(0x28FFFFFF),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Draw Data Polygon
            val dataPath = Path()
            val pointList = mutableListOf<Offset>()

            for (i in 0 until numAxes) {
                val angle = -PI.toFloat() / 2f + i * angleStep
                val currentVal = animatedValues[i]
                val pRadius = maxRadius * currentVal
                val px = center.x + pRadius * cos(angle)
                val py = center.y + pRadius * sin(angle)
                val pt = Offset(px, py)
                pointList.add(pt)

                if (i == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
            }
            dataPath.close()

            // Fill Data Area with Gradient
            drawPath(
                path = dataPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        NebulaIndigo.copy(alpha = 0.5f),
                        NebulaFuchsia.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                ),
                style = Fill
            )

            // Data Outline Stroke
            drawPath(
                path = dataPath,
                color = NebulaIndigo,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Glowing Corner Vertices
            for (pt in pointList) {
                drawCircle(
                    color = NebulaFuchsia.copy(alpha = 0.4f),
                    radius = 7.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = pt
                )
            }
        }

        // Radar Center Status Pill
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(FrostedGlassSurfaceVariant)
                .border(1.dp, FrostedGlassBorderLight, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${score.score}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(score.status.colorHex)
            )
        }
    }
}

/**
 * Full Interactive Security Health Dashboard Card with View Mode Switcher
 */
@Composable
fun SecurityHealthInteractiveCard(
    score: SecurityScore,
    onWatchtowerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChartMode by remember { mutableIntStateOf(0) } // 0 = Gauge, 1 = Radar Matrix
    val statusColor = Color(score.status.colorHex)

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
                        statusColor.copy(alpha = 0.45f),
                        FrostedGlassBorder
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .testTag("security_health_interactive_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Title + Mode Toggle Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NebulaIndigo.copy(alpha = 0.2f))
                            .border(1.dp, NebulaIndigo.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
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
                    Column {
                        Text(
                            text = "SECURITY HEALTH",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = TextIndigoSubtle
                        )
                        Text(
                            text = "Defense Posture & Fortification",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedSecondary
                        )
                    }
                }

                // Mode switcher (Speed Gauge vs Radar Matrix)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedGlassSurfaceVariant)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedChartMode == 0) NebulaIndigo.copy(alpha = 0.35f) else Color.Transparent)
                            .clickable { selectedChartMode = 0 }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Gauge View",
                            tint = if (selectedChartMode == 0) Color.White else TextMutedSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedChartMode == 1) NebulaIndigo.copy(alpha = 0.35f) else Color.Transparent)
                            .clickable { selectedChartMode = 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Radar Matrix View",
                            tint = if (selectedChartMode == 1) Color.White else TextMutedSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Canvas Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedChartMode == 0) {
                    SecurityHealthCanvasGauge(score = score)
                } else {
                    SecurityRadarHealthCanvas(score = score)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Sub-Metric Interactive Health Bars
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SubMetricProgressBar(
                    title = "Password Complexity",
                    score = score.complexityScore,
                    subtitle = if (score.weakCount > 0) "${score.weakCount} weak passwords" else "High entropy passwords",
                    color = NebulaIndigo,
                    icon = Icons.Default.Key
                )

                SubMetricProgressBar(
                    title = "Password Uniqueness",
                    score = score.uniquenessScore,
                    subtitle = if (score.reusedCount > 0) "${score.reusedCount} reused credentials" else "0 reused secrets",
                    color = NebulaFuchsia,
                    icon = Icons.Default.Lock
                )

                SubMetricProgressBar(
                    title = "2FA & MFA Protection",
                    score = score.twoFactorScore,
                    subtitle = if (score.missing2FaCount > 0) "${score.missing2FaCount} sensitive accounts without 2FA tag" else "Financial & sensitive accounts fortified",
                    color = NebulaBlue,
                    icon = Icons.Default.Fingerprint
                )

                SubMetricProgressBar(
                    title = "Password Freshness",
                    score = score.freshnessScore,
                    subtitle = if (score.oldPasswordCount > 0) "${score.oldPasswordCount} passwords over 90 days old" else "All passwords rotated within 90 days",
                    color = SecurityAmber,
                    icon = Icons.Default.Schedule
                )

                SubMetricProgressBar(
                    title = "Breach Immunity",
                    score = score.breachSafetyScore,
                    subtitle = "0 exposed hashes in HIBP",
                    color = SecurityGreen,
                    icon = Icons.Default.CheckCircle
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Action Button to Watchtower / Fix
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(NebulaIndigo, NebulaFuchsia))
                    )
                    .border(1.dp, FrostedGlassBorderLight, RoundedCornerShape(16.dp))
                    .clickable(onClick = onWatchtowerClick)
                    .padding(vertical = 12.dp)
                    .testTag("open_watchtower_interactive_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Review & Fortify in Watchtower",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SubMetricProgressBar(
    title: String,
    score: Int,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "sub_metric_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FrostedGlassSurfaceVariant)
            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextWhitePrimary
                    )
                }

                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Bar Track & Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x20FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(color.copy(alpha = 0.7f), color)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMutedSecondary
            )
        }
    }
}
