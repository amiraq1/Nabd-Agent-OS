package com.nabd.ai.local.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.SmartScreen
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.engine.EngineState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onIntent: (ChatIntent) -> Unit
) {
    val listState = rememberLazyListState()

    // Automatic scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Nabd Agent OS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    EngineControls(uiState.engineState, onIntent)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        NabdMessageBubble(message)
                    }
                }

                if (uiState.error != null) {
                    ErrorMessage(uiState.error, onClear = { onIntent(ChatIntent.ClearError) })
                }

                // Input Area at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                            )
                        )
                        .padding(16.dp)
                ) {
                    NabdInputArea(
                        prompt = uiState.currentPrompt,
                        isGenerating = uiState.isGenerating,
                        isModelLoaded = uiState.isModelLoaded,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
fun EngineControls(
    engineState: EngineState,
    onIntent: (ChatIntent) -> Unit
) {
    Row {
        when (engineState) {
            is EngineState.Initialized, is EngineState.Unloaded -> {
                IconButton(onClick = { onIntent(ChatIntent.LoadModel) }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Load Model")
                }
            }
            is EngineState.Loaded -> {
                IconButton(onClick = { onIntent(ChatIntent.UnloadModel) }) {
                    Icon(Icons.Default.Stop, contentDescription = "Unload Model")
                }
            }
            is EngineState.Generating -> {
                IconButton(onClick = { onIntent(ChatIntent.StopGeneration) }) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop Generation", tint = Color.Red)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun NabdMessageBubble(message: ChatMessage) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isUser) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                if (!isUser) {
                    Text(
                        text = "[NABD_OS]",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Thinking View Integration
                if (!isUser && !message.thoughts.isNullOrBlank()) {
                    ThinkingAccordion(
                        thought = message.thoughts,
                        title = "CORE_LOGIC",
                        initialExpanded = message.isPending
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Surface(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 2.dp,
                        topEnd = 2.dp,
                        bottomStart = if (isUser) 2.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 2.dp
                    ),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(
                                topStart = 2.dp,
                                topEnd = 2.dp,
                                bottomStart = if (isUser) 2.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 2.dp
                            )
                        )
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Main Response Text
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (message.isPending) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(1.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Transparent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NabdInputArea(
    prompt: String,
    isGenerating: Boolean,
    isModelLoaded: Boolean,
    onIntent: (ChatIntent) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = prompt,
                onValueChange = { onIntent(ChatIntent.UpdatePrompt(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "INPUT_STREEM >",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ) 
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                enabled = isModelLoaded && !isGenerating,
                maxLines = 5
            )

            IconButton(
                onClick = { onIntent(ChatIntent.SendPrompt) },
                enabled = isModelLoaded && !isGenerating && prompt.isNotBlank(),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Execute",
                    tint = if (isModelLoaded && !isGenerating && prompt.isNotBlank()) 
                           MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ErrorMessage(error: String, onClear: () -> Unit) {
    Snackbar(
        action = {
            TextButton(onClick = onClear) {
                Text("Dismiss", color = Color.White)
            }
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(error)
    }
}
