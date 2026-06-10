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
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            if (!isUser) {
                AvatarIcon(isUser = false)
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f, fill = false)) {
                // Thinking View Integration (Agora Style)
                if (!isUser && !message.thoughts.isNullOrBlank()) {
                    ThinkingAccordion(
                        thought = message.thoughts,
                        title = "Agent Logic & Planning",
                        initialExpanded = message.isPending // Expand by default while generating
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Surface(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    ),
                    tonalElevation = if (isUser) 2.dp else 0.dp,
                    modifier = Modifier.animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (!isUser) {
                            Text(
                                text = "NABD AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // Main Response Text
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (message.isPending) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(10.dp))
                AvatarIcon(isUser = true)
            }
        }
    }
}

@Composable
fun AvatarIcon(isUser: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (isUser) MaterialTheme.colorScheme.secondaryContainer 
                else MaterialTheme.colorScheme.tertiaryContainer
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isUser) Icons.Default.Face else Icons.Default.SmartScreen,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isUser) MaterialTheme.colorScheme.onSecondaryContainer 
                   else MaterialTheme.colorScheme.onTertiaryContainer
        )
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
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = prompt,
                onValueChange = { onIntent(ChatIntent.UpdatePrompt(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Ask Nabd to plan or execute...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ) 
                },
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

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = { onIntent(ChatIntent.SendPrompt) },
                enabled = isModelLoaded && !isGenerating && prompt.isNotBlank(),
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(24.dp)
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
