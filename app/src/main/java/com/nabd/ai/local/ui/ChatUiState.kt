package com.nabd.ai.local.ui

import com.nabd.ai.local.engine.EngineState
import java.util.UUID

/**
 * ChatUiState: Immutable representation of the Chat Screen's state.
 */
data class ChatUiState(
    val engineState: EngineState = EngineState.Initialized,
    val currentPrompt: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val isModelLoaded: Boolean = false,
    val error: String? = null
)

/**
 * ChatMessage: Represents a single message in the conversation.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isPending: Boolean = false,
    val thoughts: String? = null
)
