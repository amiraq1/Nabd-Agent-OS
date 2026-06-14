package com.nabd.ai.agora.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nabd.ai.agora.ui.common.SettingsBentoCard
import kotlin.math.roundToInt

private val ACCENT_NEON   = Color(0xFF00FF66)
private val TEXT_MUTED    = Color(0xFF5B6B85)
private val FONT_MONO     = FontFamily.Monospace
private val BORDER_SUBTLE = Color(0xFF8A94A6).copy(alpha = 0.15f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebSearchConfigPage(
    viewModel: WebSearchConfigViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled by viewModel.isWebSearchEnabled.collectAsStateWithLifecycle()
    val key by viewModel.braveSearchApiKey.collectAsStateWithLifecycle()
    val maxR by viewModel.maxSearchResults.collectAsStateWithLifecycle()
    var keyVisible by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            containerColor = Color(0xFF0D1117),
            topBar = {
                TopAppBar(
                    containerColor = Color(0xFF0D1117),
                    title = {
                        Text(
                            text = "Web Search",
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
                        text = "// WEB SEARCH",
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
                            Text("Enable Web Search", fontFamily = FONT_MONO, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Switch(
                                checked = enabled,
                                onCheckedChange = viewModel::toggleWebSearch,
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
                    SettingsBentoCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = TEXT_MUTED, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Search Provider", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp, letterSpacing = 2.sp)
                                Text("Brave Search", fontFamily = FONT_MONO, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("ACTIVE", fontFamily = FONT_MONO, color = ACCENT_NEON, fontSize = 10.sp, letterSpacing = 2.sp)
                        }
                    }
                }

                item {
                    SettingsBentoCard {
                        Text("BRAVE API KEY", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = key,
                            onValueChange = viewModel::saveBraveKey,
                            label = { Text("API Key") },
                            placeholder = { Text("BSA-…") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (keyVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { keyVisible = !keyVisible }) {
                                    Icon(
                                        if (keyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = TEXT_MUTED
                                    )
                                }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text(
                        text = "// ADVANCED",
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
                            Text("Max Results", fontFamily = FONT_MONO, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("$maxR results", fontFamily = FONT_MONO, color = ACCENT_NEON, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Slider(
                            value = maxR.toFloat(),
                            onValueChange = { viewModel.saveMaxResults(it.roundToInt()) },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp)
                            Text("10", fontFamily = FONT_MONO, color = TEXT_MUTED, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
