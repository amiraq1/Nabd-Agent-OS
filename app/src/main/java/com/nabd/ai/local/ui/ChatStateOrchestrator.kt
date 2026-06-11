package com.nabd.ai.local.ui

import kotlinx.coroutines.flow.StateFlow
import com.nabd.ai.local.mtp_engine.architecture.NabdAction

/**
 * ChatStateOrchestrator: The new control node (alternative to ChatViewModel).
 * It delegates actions to specialized managers (ConversationManager, GenerationManager).
 */
interface ChatStateOrchestrator {
    val state: StateFlow<ChatUiState>
    fun dispatch(action: NabdAction)
}
