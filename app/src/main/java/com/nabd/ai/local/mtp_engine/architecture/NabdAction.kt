package com.nabd.ai.local.mtp_engine.architecture

import com.nabd.ai.local.mtp_engine.domain.generation.InferenceConfig

/**
 * NabdAction: Precise user actions for the Nabd UI.
 * Follows Strict Unidirectional Data Flow (UDF) within the MTP architecture.
 */
sealed interface NabdAction {
    data class ProcessPrompt(val text: String, val attachments: List<SelectedAttachment> = emptyList()) : NabdAction
    data class EditMessage(val messageId: String, val newText: String) : NabdAction
    data class SwitchBranch(val parentId: String, val childId: String) : NabdAction
    data class RetryGeneration(val messageId: String) : NabdAction
    data object CancelGeneration : NabdAction
    data object ResetConversation : NabdAction
    data class UpdateInferenceConfig(val config: InferenceConfig) : NabdAction
    data class ToggleWebSearch(val enabled: Boolean) : NabdAction
    data class ToggleShell(val enabled: Boolean) : NabdAction
    data class ToggleMemory(val enabled: Boolean) : NabdAction
}
