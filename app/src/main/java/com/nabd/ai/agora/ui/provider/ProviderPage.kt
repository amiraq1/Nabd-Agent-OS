package com.nabd.ai.agora.ui.provider

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.flow.StateFlow

private val BG_CARD        = Color(0xFF161A23)
private val BORDER_SUBTLE  = Color(0xFF8A94A6).copy(alpha = 0.15f)
private val ACCENT_NEON    = Color(0xFF00FF66)
private val TEXT_MUTED     = Color(0xFF5B6B85)
private val CORNER_CARD    = RoundedCornerShape(14.dp)
private val FONT_MONO      = FontFamily.Monospace

private data class ProviderEntry(
    val name: String,
    val isConfigured: StateFlow<Boolean>,
    val onSave: (String) -> Unit,
    val currentKey: StateFlow<String>,
    val keyHint: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPage(
    onBack: () -> Unit,
    viewModel: ProviderViewModel
) {
    var selectedProvider by remember { mutableStateOf<ProviderEntry?>(null) }

    val entries = listOf(
        ProviderEntry("Google",     viewModel.googleConfigured,     viewModel::saveGoogleKey,    viewModel.currentGoogleKey,     "AIza..."),
        ProviderEntry("OpenAI",     viewModel.openAIConfigured,     viewModel::saveOpenAiKey,    viewModel.currentOpenAiKey,     "sk-..."),
        ProviderEntry("Anthropic",  viewModel.anthropicConfigured,  viewModel::saveAnthropicKey, viewModel.currentAnthropicKey,  "sk-ant-..."),
        ProviderEntry("DeepSeek",   viewModel.deepSeekConfigured,   viewModel::saveDeepSeekKey,  viewModel.currentDeepSeekKey,   "sk-..."),
        ProviderEntry("Qwen",       viewModel.qwenConfigured,       viewModel::saveQwenKey,      viewModel.currentQwenKey,       ""),
        ProviderEntry("OpenRouter", viewModel.openRouterConfigured, viewModel::saveOpenRouterKey, viewModel.currentOpenRouterKey, ""),
        ProviderEntry("NVIDIA",     viewModel.nvidiaKeyConfigured,   viewModel::saveNvidiaKey,    viewModel.currentNvidiaKey,     "nvapi-...")
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "AI Providers",
                            fontFamily = FONT_MONO,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
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
            containerColor = Color(0xFF0B0E14)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(
                        text = "// CLOUD PROVIDERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TEXT_MUTED,
                        fontFamily = FONT_MONO,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(entries) { entry ->
                    val isConfigured by entry.isConfigured.collectAsStateWithLifecycle()
                    ProviderBentoRow(
                        name = entry.name,
                        isConfigured = isConfigured,
                        onClick = { selectedProvider = entry }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "// LOCAL RUNTIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = TEXT_MUTED,
                        fontFamily = FONT_MONO,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                item {
                    ProviderBentoRow(
                        name = "Local llama.cpp Core",
                        isConfigured = true,
                        onClick = { /* No sheet triggered for local */ }
                    )
                }
            }
        }

        selectedProvider?.let { entry ->
            val currentKey by entry.currentKey.collectAsStateWithLifecycle()
            ProviderConfigSheet(
                providerName = entry.name,
                currentKey   = currentKey,
                onSave       = entry.onSave,
                onDismiss    = { selectedProvider = null }
            )
        }
    }
}

@Composable
fun ProviderBentoRow(
    name: String,
    isConfigured: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BORDER_SUBTLE, CORNER_CARD)
            .background(BG_CARD, CORNER_CARD)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontFamily = FONT_MONO,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            AnimatedContent(
                targetState = isConfigured,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(150))
                },
                label = "StatusTransition"
            ) { configured ->
                Text(
                    text = if (configured) "● Configured" else "○ Not configured",
                    fontFamily = FONT_MONO,
                    fontSize = 11.sp,
                    color = if (configured) ACCENT_NEON else TEXT_MUTED
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TEXT_MUTED,
            modifier = Modifier.size(16.dp)
        )
    }
}
