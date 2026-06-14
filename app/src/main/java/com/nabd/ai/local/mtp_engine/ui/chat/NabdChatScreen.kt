package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import com.nabd.ai.local.ui.ChatViewModel
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant
import com.nabd.ai.local.mtp_engine.architecture.NabdAction
import com.nabd.ai.local.mtp_engine.ui.components.RecomposeSafeMarkdown
import com.nabd.ai.local.mtp_engine.ui.components.IntelligenceFlowIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NabdChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onProviderHubClick: () -> Unit = {},
    onToolsHubClick: () -> Unit = {},
    onTelemetryHubClick: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // تفعيل التمرير التلقائي التكتيكي عند وصول رسائل جديدة (Auto-Scroll)
    // Optimization: Use derivedStateOf or remember keys to avoid unnecessary triggers
    LaunchedEffect(uiState.messages.size, uiState.isGenerating) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                // Focus on the end of the stream, including the flow indicator if generating
                listState.animateScrollToItem(uiState.messages.size - (if (uiState.isGenerating) 0 else 1))
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { 
            NabdTopAppBar(
                currentModel = uiState.selectedModel,
                onNewChat = { viewModel.dispatch(NabdAction.ResetConversation) },
                onMemoryClick = { /* Future: Semantic Memory View */ },
                onSettingsClick = onSettingsClick,
                onProviderHubClick = onProviderHubClick,
                onToolsHubClick = onToolsHubClick,
                onTelemetryHubClick = onTelemetryHubClick
            ) 
        },
        bottomBar = {
            // Integration of the refactored Avant-Garde input area
            NabdInputArea(
                onSendMessage = { text, attachments ->
                    if (text.isNotBlank() || attachments.isNotEmpty()) {
                        viewModel.dispatch(NabdAction.ProcessPrompt(text, attachments))
                        true
                    } else false
                },
                onStopGeneration = { viewModel.dispatch(NabdAction.CancelGeneration) },
                isLoading = uiState.isGenerating,
                enabledModels = emptySet(),
                selectedModel = uiState.selectedModel,
                webSearchEnabled = uiState.inferenceConfig.webSearchEnabled,
                shellEnabled = uiState.inferenceConfig.shellEnabled,
                memoryEnabled = uiState.inferenceConfig.memoryEnabled,
                onWebSearchToggle = { viewModel.dispatch(NabdAction.ToggleWebSearch(it)) },
                onShellToggle = { viewModel.dispatch(NabdAction.ToggleShell(it)) },
                onMemoryToggle = { viewModel.dispatch(NabdAction.ToggleMemory(it)) },
                onModelSelect = { /* Navigation to model picker */ },
                modifier = Modifier.padding(16.dp) // Maintain the "floating" padding
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.messages.isEmpty()) {
                // الواجهة المركزية الأنيقة المستوحاة من الشاشة الفارغة لـ Agora
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Nabd",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Local Autonomous AI Agent OS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // قائمة الرسائل المتدفقة بدقة وبدون وميض (Anti-Flash)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        val isLastMessage = uiState.messages.lastOrNull()?.id == message.id
                        val isStreaming = isLastMessage && uiState.isGenerating && message.participant != Participant.USER
                        
                        NabdMessageBubble(
                            message = message,
                            isGenerating = isStreaming
                        )
                    }

                    if (uiState.isGenerating) {
                        item {
                            IntelligenceFlowIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NabdTopAppBar(
    currentModel: String?,
    onNewChat: () -> Unit,
    onMemoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProviderHubClick: () -> Unit,
    onToolsHubClick: () -> Unit,
    onTelemetryHubClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // الأزرار اليسارية مدمجة داخل كبسولة ملساء كبنية Agora
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNewChat) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onProviderHubClick) {
                Icon(Icons.Default.Memory, contentDescription = "Provider Hub", tint = Color(0xFF00FF66)) // Neon Green microchip
            }
            IconButton(onClick = onToolsHubClick) {
                Icon(Icons.Default.Build, contentDescription = "Tools Hub", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onTelemetryHubClick) {
                Icon(Icons.Default.Analytics, contentDescription = "Telemetry Hub", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        // كبسولة العنوان وشعار نبض جهة اليمين مع زر الإعدادات التكتيكي
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "نبض",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Settings, 
                    contentDescription = "System Configuration", 
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NabdMessageBubble(
    message: ChatMessage,
    isGenerating: Boolean = false
) {
    val isUser = message.participant == Participant.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (isUser) {
                Text(
                    text = message.text,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            } else {
                RecomposeSafeMarkdown(
                    content = message.text,
                    modifier = Modifier.fillMaxWidth(),
                    isGenerating = isGenerating
                )
            }
        }
    }
}

