package com.nabd.ai.local.mtp_engine.api

import com.nabd.ai.local.engine.LlamaEngine
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * LlamaChatEngine: Specialized JNI Bridge for MTP-based chat generation.
 */
interface LlamaChatEngine {
    /**
     * Loads the native model from disk.
     */
    suspend fun loadModel(path: String)

    /**
     * Applies the model-specific chat template (e.g., ChatML, Llama3) to the message list.
     * This is typically handled by the native llama.cpp templates.
     */
    suspend fun applyChatTemplate(messages: List<ChatMessage>): String

    /**
     * Computes the semantic embedding vector for the given text.
     */
    suspend fun computeEmbedding(text: String): FloatArray

    /**
     * Streams tokens from the native inference engine.
     */
    fun streamTokens(prompt: String): Flow<String>

    /**
     * Aborts current generation and cleans up native state.
     */
    fun cancel()

    companion object {
        @Volatile
        private var instance: LlamaChatEngine? = null

        fun getInstance(): LlamaChatEngine {
            return instance ?: synchronized(this) {
                instance ?: LlamaEngine().also { instance = it }
            }
        }
    }
}
