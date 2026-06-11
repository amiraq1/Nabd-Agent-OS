package com.nabd.ai.local.ui

import androidx.compose.runtime.Stable
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.domain.generation.GenerationError
import com.nabd.ai.local.mtp_engine.domain.generation.InferenceConfig

/**
 * ChatUiState: Pure, lightweight, and immutable representation of the UI state.
 * Following strict Unidirectional Data Flow (UDF).
 */
@Stable
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val activeBranchId: String? = null,
    val isGenerating: Boolean = false,
    val selectedModel: String = "local:gguf-model",
    val errorState: GenerationError? = null,
    val inferenceConfig: InferenceConfig = InferenceConfig()
)
