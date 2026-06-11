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
 * LlamaEngine: Implementation of [LlmProvider] and [LlamaChatEngine] using llama.cpp.
 * Uses explicit JNI bindings to interact with the native C++ runtime.
 */
class LlamaEngine : LlmProvider, LlamaChatEngine, java.io.Closeable {

    private val _state = MutableStateFlow<EngineState>(EngineState.Initialized)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private val mutex = Mutex()
    private var nativeContextHandle: Long = 0

    init {
        try {
            System.loadLibrary("nabd_engine")
            nativeContextHandle = nativeInit()
        } catch (e: UnsatisfiedLinkError) {
            Log.e("LlamaEngine", "Failed to load native library", e)
            _state.value = EngineState.Failed("Native library load failed")
        }
    }

    override suspend fun loadModel(modelPath: String) = mutex.withLock {
        if (_state.value !is EngineState.Initialized && _state.value !is EngineState.Unloaded) {
            // Unload existing model if necessary
            if (_state.value is EngineState.Loaded || _state.value is EngineState.Failed) {
                nativeUnloadModel(nativeContextHandle)
            }
        }

        _state.value = EngineState.Loading
        val success = nativeLoadModel(nativeContextHandle, modelPath)
        
        if (success) {
            _state.value = EngineState.Loaded
        } else {
            _state.value = EngineState.Failed("Failed to load model at $modelPath")
        }
    }

    /**
     * applyChatTemplate: Basic implementation of chat templating.
     * In a real implementation, this would call native JNI to use GGUF templates.
     */
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
        // TODO: Call native JNI to compute embeddings. 
        // Returning a dummy vector for now to satisfy the compiler.
        return FloatArray(384) { 0.1f } 
    }

    override fun streamTokens(prompt: String): Flow<String> = generateText(prompt)

    override fun cancel() = stopGeneration()

    override fun generateText(prompt: String, grammar: String?): Flow<String> = callbackFlow {
        if (_state.value !is EngineState.Loaded) {
            throw IllegalStateException("Model must be loaded before generation")
        }

        _state.value = EngineState.Generating

        val callback = object : TokenCallback {
            override fun onToken(token: String) {
                trySend(token)
            }

            override fun onCompletion() {
                _state.value = EngineState.Loaded
                close()
            }

            override fun onError(error: String) {
                _state.value = EngineState.Failed(error)
                close(RuntimeException(error))
            }
        }

        nativeGenerate(nativeContextHandle, prompt, grammar ?: "", callback)

        awaitClose {
            stopGeneration()
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    override fun stopGeneration() {
        synchronized(this) {
            if (_state.value is EngineState.Generating) {
                _state.value = EngineState.Stopping
                nativeStopGeneration(nativeContextHandle)
                _state.value = EngineState.Loaded
            }
        }
    }

    override fun unloadModel() {
        if (nativeContextHandle != 0L) {
            nativeUnloadModel(nativeContextHandle)
            _state.value = EngineState.Unloaded
        }
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
    private external fun nativeStopGeneration(handle: Long)
    private external fun nativeUnloadModel(handle: Long)
    private external fun nativeRelease(handle: Long)

    /**
     * Internal interface for JNI to call back into Kotlin.
     * This is the only boundary where JNI interacts with Kotlin objects.
     */
    interface TokenCallback {
        fun onToken(token: String)
        fun onCompletion()
        fun onError(error: String)
    }
}
