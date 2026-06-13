package com.nabd.ai.local.engine

import com.nabd.ai.local.mtp_engine.api.LlamaChatEngine
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.util.Log

/**
 * LlamaEngine: Implementation of [LlmProvider] using llama.cpp native runtime.
 */
class LlamaEngine : LlmProvider, LlamaChatEngine, java.io.Closeable {

    override val id: String = "local_llama"
    override val name: String = "Local Llama"
    override val isLocal: Boolean = true

    private val _state = MutableStateFlow<EngineState>(EngineState.Uninitialized)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private val mutex = Mutex()
    private var nativeContextHandle: Long = 0

    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("nabd_engine")
            nativeContextHandle = nativeInit()
            isNativeLoaded = true
            _state.value = EngineState.Uninitialized
        } catch (e: UnsatisfiedLinkError) {
            Log.e("LlamaEngine", "Failed to load native library", e)
            _state.value = EngineState.Error(e)
            isNativeLoaded = false
        }
    }

    override suspend fun initialize(config: ProviderConfig) = mutex.withLock {
        if (!isNativeLoaded) {
            _state.value = EngineState.Error(IllegalStateException("Native library not loaded. Local models are unavailable."))
            return@withLock
        }

        if (config !is ProviderConfig.Local) {
            _state.value = EngineState.Error(IllegalArgumentException("LlamaEngine requires ProviderConfig.Local"))
            return@withLock
        }

        if (_state.value is EngineState.Ready) {
            nativeUnloadModel(nativeContextHandle)
        }

        _state.value = EngineState.Initializing
        val success = nativeLoadModel(nativeContextHandle, config.modelPath)
        
        if (success) {
            _state.value = EngineState.Ready
        } else {
            _state.value = EngineState.Error(RuntimeException("Failed to load model at ${config.modelPath}"))
        }
    }

    override suspend fun loadModel(path: String) {
        initialize(ProviderConfig.Local(id, "default", path))
    }

    override suspend fun shutdown() = mutex.withLock {
        if (nativeContextHandle != 0L) {
            nativeUnloadModel(nativeContextHandle)
            _state.value = EngineState.Released
        }
    }

    override suspend fun generateText(request: GenerationRequest): Flow<GenerationChunk> = callbackFlow {
        if (_state.value !is EngineState.Ready) {
            close(IllegalStateException("Engine must be Ready before generation"))
            return@callbackFlow
        }

        _state.value = EngineState.Generating

        val callback = object : TokenCallback {
            override fun onToken(token: String) {
                trySend(GenerationChunk(token))
            }

            override fun onCompletion() {
                _state.value = EngineState.Ready
                trySend(GenerationChunk("", isLast = true))
                close()
            }

            override fun onError(error: String) {
                _state.value = EngineState.Error(RuntimeException(error))
                close(RuntimeException(error))
            }
        }

        nativeGenerate(nativeContextHandle, request.prompt, request.grammar ?: "", callback)

        awaitClose {
            nativeAbortGeneration(nativeContextHandle)
            if (_state.value is EngineState.Generating) {
                _state.value = EngineState.Ready
            }
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // --- Backward Compatibility for LlamaChatEngine ---

    override suspend fun applyChatTemplate(messages: List<ChatMessage>): String {
        return messages.joinToString("\n") { 
            when (it.participant) {
                Participant.SYSTEM -> "System: ${it.text}"
                Participant.USER -> "User: ${it.text}"
                Participant.ASSISTANT -> "Assistant: ${it.text}"
                Participant.ERROR -> "Error: ${it.text}"
            }
        } + "\nAssistant: "
    }

    override suspend fun computeEmbedding(text: String): FloatArray {
        return FloatArray(384) { 0.1f } 
    }

    override fun streamTokens(prompt: String): Flow<String> = flow {
        generateText(GenerationRequest(prompt)).collect { chunk ->
            emit(chunk.text)
        }
    }

    override fun cancel() {
        nativeAbortGeneration(nativeContextHandle)
    }

    override fun close() {
        if (nativeContextHandle != 0L) {
            nativeRelease(nativeContextHandle)
            nativeContextHandle = 0L
        }
    }

    // --- Native JNI Methods ---

    private external fun nativeInit(): Long
    private external fun nativeLoadModel(handle: Long, path: String): Boolean
    private external fun nativeGenerate(handle: Long, prompt: String, grammar: String, callback: TokenCallback)
    private external fun nativeAbortGeneration(handle: Long)
    private external fun nativeUnloadModel(handle: Long)
    private external fun nativeRelease(handle: Long)

    interface TokenCallback {
        fun onToken(token: String)
        fun onCompletion()
        fun onError(error: String)
    }
}
