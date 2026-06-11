package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nabd.ai.local.ui.ChatStateOrchestrator
import com.nabd.ai.local.ui.ChatScreen
import com.nabd.ai.ui.theme.NabdTheme

/**
 * AvantGardeChatShell: The top-level entry point for the MTP Chat experience.
 * Orchestrates the transition from boot to active conversation.
 */
@Composable
fun AvantGardeChatShell(
    engineState: ChatStateOrchestrator
) {
    val uiState by engineState.state.collectAsState()

    NabdTheme {
        ChatScreen(
            uiState = uiState,
            onAction = { action -> engineState.dispatch(action) }
        )
    }
}
