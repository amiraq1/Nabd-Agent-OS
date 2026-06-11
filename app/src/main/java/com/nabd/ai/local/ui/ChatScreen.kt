package com.nabd.ai.local.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nabd.ai.local.mtp_engine.ui.chat.NabdMessageItem
import com.nabd.ai.local.mtp_engine.ui.chat.NabdInputArea
import com.nabd.ai.local.mtp_engine.ui.chat.NabdErrorBanner
import com.nabd.ai.local.mtp_engine.architecture.NabdAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onAction: (NabdAction) -> Unit
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
                    if (uiState.isGenerating) {
                        IconButton(onClick = { onAction(NabdAction.CancelGeneration) }) {
                            Icon(Icons.Default.Stop, contentDescription = "Cancel", tint = Color.Red)
                        }
                    }
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        NabdMessageItem(
                            message = message,
                            isStreaming = message.isPending && uiState.isGenerating
                        )
                    }
                }

                if (uiState.errorState != null) {
                    NabdErrorBanner(error = uiState.errorState)
                }

                // Tactical Input Area at the bottom
                NabdInputArea(
                    onAction = onAction,
                    isGenerating = uiState.isGenerating
                )
            }
        }
    }
}
