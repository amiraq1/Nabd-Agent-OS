package com.nabd.ai.local.mtp_engine.domain.generation

import com.nabd.ai.local.mtp_engine.api.LlamaChatEngine
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch

/**
 * GenerationManager: Modular Task Processor (MTP) Engine domain implementation.
 * Intentional Minimalism - JNI Stream Orchestration.
 */
class GenerationManager(
    private val llamaEngine: LlamaChatEngine,
    private val contextAssembler: ContextAssembler
) {
    /**
     * executeGeneration: Unidirectional flow from native engine to Nabd UI.
     */
    suspend fun executeGeneration(
        activePath: List<ChatMessage>,
        config: InferenceConfig = InferenceConfig(),
        systemPrompt: String = NabdPersona.NUCLEAR_PROMPT
    ): Flow<GenerationChunk> = flow<GenerationChunk> {
        
        // 1. Assemble context window (Off-main thread)
        val safeContext = contextAssembler.assembleContext(activePath, systemPrompt)
        
        // 2. Apply Chat Template via JNI
        val formattedPrompt = llamaEngine.applyChatTemplate(safeContext)
        
        // 3. Tactical Token Streaming with sampling config
        llamaEngine.streamTokens(formattedPrompt).collect { token ->
            emit(GenerationChunk.Text(token))
        }
        
    }.catch { e ->
        emit(GenerationChunk.Error(GenerationError.EngineFault(e.message ?: "Native Core Exception")))
    }

    suspend fun halt() = llamaEngine.cancel()
}

sealed interface GenerationChunk {
    data class Text(val content: String) : GenerationChunk
    data class Error(val error: GenerationError) : GenerationChunk
}
