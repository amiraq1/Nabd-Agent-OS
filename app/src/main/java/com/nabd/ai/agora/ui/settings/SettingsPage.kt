package com.nabd.ai.agora.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// الألوان التكتيكية (Cyberpunk Matrix Theme)
val NeonGreen = Color(0xFF00FF66)
val TacticalBackground = Color(0xFF0B0E14)
val BentoCardBackground = Color(0xFF161A23)
val MutedText = Color(0xFF8A94A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage() {
    var isLocalEngine by remember { mutableStateOf(true) }
    var threadCount by remember { mutableFloatStateOf(4f) }
    var apiKey by remember { mutableStateOf("") }

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
                icon = Icons.Default.Settings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Execute Model Locally", color = Color.White, fontFamily = FontFamily.Monospace)
                    Switch(
                        checked = isLocalEngine,
                        onCheckedChange = { isLocalEngine = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isLocalEngine) "Mode: llama.cpp GGUF Core" else "Mode: Cloud API Gateway",
                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Bento Box 2 - Resource Allocation (Crucial for Termux/Android stability)
            BentoSettingsCard(
                title = "RESOURCE ALLOCATION",
                icon = Icons.Default.Refresh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CPU Threads: ${threadCount.toInt()}",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = threadCount,
                    onValueChange = { threadCount = it },
                    valueRange = 1f..8f,
                    steps = 7,
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
            BentoSettingsCard(
                title = "SECURITY & ENVIRONMENT",
                icon = Icons.Default.Lock,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("NVIDIA NIM API KEY", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text("env: NVIDIA_NIM_API_KEY", color = MutedText) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = MutedText
                    )
                )
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
