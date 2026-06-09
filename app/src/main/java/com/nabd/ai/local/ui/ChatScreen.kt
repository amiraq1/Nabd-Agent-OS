package com.nabd.ai.local.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nabd Agent OS (نبض)") },
                actions = {
                    EngineControls(uiState.engineState, onIntent)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MessageList(
                messages = uiState.messages,
                modifier = Modifier.weight(1f)
            )

            if (uiState.error != null) {
                ErrorMessage(uiState.error, onClear = { onIntent(ChatIntent.ClearError) })
            }

            InputArea(
                prompt = uiState.currentPrompt,
                isGenerating = uiState.isGenerating,
                isModelLoaded = uiState.isModelLoaded,
                onIntent = onIntent
            )
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
fun MessageList(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message)
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (message.isUser) "You" else "Nabd",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (message.isPending) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun InputArea(
    prompt: String,
    isGenerating: Boolean,
    isModelLoaded: Boolean,
    onIntent: (ChatIntent) -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = prompt,
                onValueChange = { onIntent(ChatIntent.UpdatePrompt(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask something...") },
                enabled = isModelLoaded && !isGenerating,
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onIntent(ChatIntent.SendPrompt) },
                enabled = isModelLoaded && !isGenerating && prompt.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
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
