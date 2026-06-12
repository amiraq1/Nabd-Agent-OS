package com.nabd.ai.local.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Configuration for different LLM providers.
 */
sealed interface ProviderConfig {
    val providerId: String
    val modelId: String

    data class Local(
        override val providerId: String,
        override val modelId: String,
        val modelPath: String
    ) : ProviderConfig

    data class Cloud(
        override val providerId: String,
        override val modelId: String
    ) : ProviderConfig
}

/**
 * Request structure for text generation.
 */
data class GenerationRequest(
    val prompt: String,
    val modelId: String = "default",
    val grammar: String? = null,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048
)

/**
 * Represent a chunk of generated text.
 */
data class GenerationChunk(
    val text: String,
    val isLast: Boolean = false
)

/**
 * LlmProvider: The primary abstraction boundary for LLM inference (Local and Cloud).
 */
interface LlmProvider : EngineStatus {

    val id: String
    val name: String
    val isLocal: Boolean

    /**
     * Initializes the engine with the provided configuration.
     */
    suspend fun initialize(config: ProviderConfig)

    /**
     * Releases all resources and transitions to Released state.
     */
    suspend fun shutdown()

    /**
     * Starts text generation based on the provided request.
     * Returns a [Flow] that emits [GenerationChunk]s.
     */
    suspend fun generateText(request: GenerationRequest): Flow<GenerationChunk>

    /**
     * Non-streaming convenience method for text generation.
     */
    suspend fun generateResponse(request: GenerationRequest): String {
        val builder = StringBuilder()
        generateText(request).collect { chunk ->
            builder.append(chunk.text)
        }
        return builder.toString()
    }
}
