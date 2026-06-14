package com.nabd.ai.agora.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import kotlin.math.roundToInt

// الألوان التكتيكية (Cyberpunk Matrix Theme)
val NeonGreen = Color(0xFF00FF66)
val TacticalBackground = Color(0xFF0B0E14)
val BentoCardBackground = Color(0xFF161A23)
val MutedText = Color(0xFF8A94A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application))
) {
    val isLocalEngine by viewModel.isLocalEngine.collectAsStateWithLifecycle()
    val threadCount by viewModel.threadCount.collectAsStateWithLifecycle()
    val apiKey by viewModel.nvidiaApiKey.collectAsStateWithLifecycle()

    var keyVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "SYSTEM CONFIGURATION",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TacticalBackground)
            )
        },
        containerColor = TacticalBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // GRID LAYOUT: Bento Box 1 - AI Engine
            BentoSettingsCard(
                title = "INFERENCE ENGINE",
                icon = Icons.Outlined.Memory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Execute Model Locally", color = Color.White, fontFamily = FontFamily.Monospace)
                        Text(
                            text = if (isLocalEngine) "Mode: llama.cpp Core" else "Mode: Cloud Gateway",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = isLocalEngine,
                        onCheckedChange = { viewModel.updateEngineMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                    )
                }
            }

            // Bento Box 2 - Resource Allocation (Crucial for Termux/Android stability)
            BentoSettingsCard(
                title = "RESOURCE ALLOCATION",
                icon = Icons.Outlined.Tune,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CPU Threads: ${threadCount}",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ACTIVE",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = threadCount.toFloat(),
                    onValueChange = { viewModel.updateThreadCount(it.roundToInt()) },
                    valueRange = 1f..16f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonGreen,
                        activeTrackColor = NeonGreen
                    )
                )
                Text(
                    text = "Keep at 4 threads maximum on mobile to prevent Kernel thermal throttling.",
                    color = MutedText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Bento Box 3 - Sandbox Security Context
            AnimatedVisibility(
                visible = !isLocalEngine,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                BentoSettingsCard(
                    title = "SECURITY & ENVIRONMENT",
                    icon = Icons.Outlined.Key,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("NVIDIA NIM API KEY", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { viewModel.updateApiKey(it) },
                        placeholder = { Text("nvapi-...", color = MutedText) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { keyVisible = !keyVisible }) {
                                Text(
                                    if (keyVisible) "HIDE" else "SHOW",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = MutedText
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BentoSettingsCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(1.dp, MutedText.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = BentoCardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = NeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
            Divider(color = MutedText.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}
