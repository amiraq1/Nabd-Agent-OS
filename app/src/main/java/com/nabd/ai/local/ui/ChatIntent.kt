package com.nabd.ai.local.ui

/**
 * ChatIntent: Actions that a user can perform on the Chat Screen.
 */
sealed interface ChatIntent {
    data object LoadModel : ChatIntent
    data object UnloadModel : ChatIntent
    data object StopGeneration : ChatIntent
    data class UpdatePrompt(val prompt: String) : ChatIntent
    data object SendPrompt : ChatIntent
    data object ClearError : ChatIntent
}
