package com.nabd.ai.agora.ui.telemetry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val BG_CARD       = Color(0xFF161A23)
private val BORDER_SUBTLE = Color(0xFF8A94A6).copy(alpha = 0.15f)
private val ACCENT_NEON   = Color(0xFF00FF66)
private val ACCENT_WARN   = Color(0xFFFF3366)
private val TEXT_MUTED    = Color(0xFF5B6B85)
private val CORNER_CARD    = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
private val FONT_MONO      = FontFamily.Monospace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelemetryPage(
    viewModel: TelemetryViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val ram by viewModel.ramMetrics.collectAsStateWithLifecycle()
    val tokens by viewModel.contextTokens.collectAsStateWithLifecycle()
    val speed by viewModel.inferenceSpeed.collectAsStateWithLifecycle()

    LaunchedEffect(ram.usagePercent > 85) {
        if (ram.usagePercent > 85) {
            snackbarHostState.showSnackbar("⚠ RAM usage critical: ${ram.usagePercent}%")
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Telemetry & Metrics",
                            fontFamily = FONT_MONO,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117))
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFF0B0E14)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "// SYSTEM PERFORMANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TEXT_MUTED,
                        fontFamily = FONT_MONO,
                        fontSize = 14.sp
                    )
                }

                item {
                    MetricBentoCard {
                        Text("RAM USAGE", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("${"%.1f".format(ram.usedGib)} GiB", fontFamily = FONT_MONO, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("/ ${"%.1f".format(ram.totalGib)} GiB", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { ram.usagePercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (ram.usagePercent > 85) ACCENT_WARN else ACCENT_NEON,
                            trackColor = BORDER_SUBTLE,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${ram.usagePercent}% utilized",
                            fontFamily = FONT_MONO,
                            color = if (ram.usagePercent > 85) ACCENT_WARN else TEXT_MUTED,
                            fontSize = 11.sp
                        )
                    }
                }

                item {
                    MetricBentoCard {
                        Text("CONTEXT WINDOW", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(tokens.label, fontFamily = FONT_MONO, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { tokens.percent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = ACCENT_NEON,
                            trackColor = BORDER_SUBTLE,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${tokens.percent}% window filled", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp)
                    }
                }

                item {
                    MetricBentoCard {
                        Text("INFERENCE SPEED", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (speed > 0f) "%.1f".format(speed) else "—",
                                fontFamily = FONT_MONO,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("tok/s", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 14.sp)
                        }
                        if (speed == 0f) {
                            Text("Awaiting inference session…", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBentoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = CORNER_CARD,
        colors   = CardDefaults.cardColors(containerColor = BG_CARD),
        border   = BorderStroke(1.dp, BORDER_SUBTLE)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
