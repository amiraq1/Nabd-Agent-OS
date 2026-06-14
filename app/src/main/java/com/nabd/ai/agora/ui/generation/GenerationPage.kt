package com.nabd.ai.agora.ui.generation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nabd.ai.agora.ui.common.SettingsBentoCard
import kotlin.math.roundToInt

private val ACCENT_NEON     = Color(0xFF00FF66)
private val TEXT_MUTED      = Color(0xFF5B6B85)
private val FONT_MONO       = FontFamily.Monospace
private val BORDER_SUBTLE   = Color(0xFF8A94A6).copy(alpha = 0.15f)
private val SEGMENTED_ACTIVE_BORDER = Color(0xFF00FF66).copy(alpha = 0.4f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerationPage(
    viewModel: GenerationViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contextSize by viewModel.contextWindowSize.collectAsStateWithLifecycle()
    val visualize by viewModel.visualizeContext.collectAsStateWithLifecycle()
    val thinkingOn by viewModel.isThinkingEnabled.collectAsStateWithLifecycle()
    val level by viewModel.thinkingLevel.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            containerColor = Color(0xFF0D1117),
            topBar = {
                TopAppBar(
                    containerColor = Color(0xFF0D1117),
                    title = {
                        Text(
                            text = "Generation",
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
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                item {
                    Text(
                        text = "// DEFAULT CONTEXT WINDOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = TEXT_MUTED,
                        fontFamily = FONT_MONO,
                        fontSize = 14.sp
                    )
                }

                item {
                    SettingsBentoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Context Size", fontFamily = FONT_MONO, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("$contextSize msg", fontFamily = FONT_MONO, color = ACCENT_NEON, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Slider(
                            value = contextSize.toFloat(),
                            onValueChange = { viewModel.saveContextWindowSize(it.roundToInt()) },
                            valueRange = 1f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp)
                            Text("100", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp)
                        }
                    }
                }

                item {
                    SettingsBentoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Visualize Context Roll-Out", fontFamily = FONT_MONO, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("Dim messages outside the context window", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp)
                            }
                            Switch(
                                checked = visualize,
                                onCheckedChange = viewModel::saveVisualizeContext,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = ACCENT_NEON,
                                    uncheckedTrackColor = BORDER_SUBTLE
                                )
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text(
                        text = "// DEFAULT THINKING",
                        style = MaterialTheme.typography.labelSmall,
                        color = TEXT_MUTED,
                        fontFamily = FONT_MONO,
                        fontSize = 14.sp
                    )
                }

                item {
                    SettingsBentoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Thinking", fontFamily = FONT_MONO, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("Enable model reasoning capability", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp)
                            }
                            Switch(
                                checked = thinkingOn,
                                onCheckedChange = viewModel::saveThinkingEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = ACCENT_NEON,
                                    uncheckedTrackColor = BORDER_SUBTLE
                                )
                            )
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = thinkingOn,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        SettingsBentoCard {
                            Text("Thinking Level", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            ThinkingLevelSelector(selected = level, onSelect = viewModel::saveThinkingLevel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingLevelSelector(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("High", "Medium", "Low")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            OutlinedButton(
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) SEGMENTED_ACTIVE_BORDER else BORDER_SUBTLE
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) ACCENT_NEON.copy(alpha = 0.08f)
                    else Color.Transparent
                )
            ) {
                Text(
                    text = option,
                    fontFamily = FONT_MONO,
                    color = if (isSelected) ACCENT_NEON else TEXT_MUTED,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
