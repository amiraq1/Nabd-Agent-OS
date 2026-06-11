package com.nabd.ai.local.mtp_engine.architecture

import com.nabd.ai.local.mtp_engine.domain.generation.InferenceConfig

/**
 * NabdAction: Precise user actions for the Nabd UI.
 * Follows Strict Unidirectional Data Flow (UDF) within the MTP architecture.
 */
sealed interface NabdAction {
    data class ProcessPrompt(val text: String, val images: List<String> = emptyList()) : NabdAction
    data class EditMessage(val messageId: String, val newText: String) : NabdAction
    data class SwitchBranch(val parentId: String, val childId: String) : NabdAction
    data class RetryGeneration(val messageId: String) : NabdAction
    data object CancelGeneration : NabdAction
    data class UpdateInferenceConfig(val config: InferenceConfig) : NabdAction
}
