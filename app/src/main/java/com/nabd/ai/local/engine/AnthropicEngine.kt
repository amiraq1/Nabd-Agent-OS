package com.nabd.ai.local.engine

import com.nabd.ai.local.core.AuthInterceptor
import com.nabd.ai.local.core.SecureKeyManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okhttp3.Response
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import android.util.Log

/**
 * AnthropicEngine: Implementation of [LlmProvider] for Anthropic Claude APIs.
 */
class AnthropicEngine(
    private val secureKeyManager: SecureKeyManager
) : LlmProvider {

    override val id: String = "anthropic"
    override val name: String = "Anthropic"
    override val isLocal: Boolean = false

    private val _state = MutableStateFlow<EngineState>(EngineState.Uninitialized)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private var client: OkHttpClient? = null
    private var modelId: String = "claude-3-5-sonnet-20240620"

    override suspend fun initialize(config: ProviderConfig) {
        if (config !is ProviderConfig.Cloud) {
            _state.value = EngineState.Error(IllegalArgumentException("AnthropicEngine requires ProviderConfig.Cloud"))
            return
        }

        _state.value = EngineState.Initializing
        this.modelId = config.modelId

        client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .addInterceptor(AuthInterceptor(secureKeyManager, id))
            .build()

        _state.value = EngineState.Ready
    }

    override suspend fun shutdown() {
        client = null
        _state.value = EngineState.Released
    }

    override suspend fun generateText(request: GenerationRequest): Flow<GenerationChunk> = callbackFlow {
        val currentClient = client ?: run {
            close(IllegalStateException("Engine not initialized"))
            return@callbackFlow
        }

        _state.value = EngineState.Generating
        
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", request.prompt)
            })
        }
        
        val requestBodyJson = JSONObject().apply {
            put("model", modelId)
            put("messages", messages)
            put("max_tokens", request.maxTokens)
            put("stream", true)
            put("temperature", request.temperature)
            // System prompt could be added here if supported in GenerationRequest
        }

        val httpRequest = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("Accept", "text/event-stream")
            // Note: AuthInterceptor adds x-api-key and anthropic-version headers
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        val factory = EventSources.createFactory(currentClient)
        
        val eventSource = factory.newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    // Anthropic uses different event types: message_start, content_block_delta, message_delta, etc.
                    val json = JSONObject(data)
                    
                    when (json.optString("type")) {
                        "content_block_delta" -> {
                            val delta = json.optJSONObject("delta")
                            if (delta?.optString("type") == "text_delta") {
                                val text = delta.optString("text", "")
                                if (text.isNotEmpty()) {
                                    trySend(GenerationChunk(text))
                                }
                            }
                        }
                        "message_stop" -> {
                            _state.value = EngineState.Ready
                            trySend(GenerationChunk("", isLast = true))
                            close()
                        }
                        "error" -> {
                            val errorObj = json.optJSONObject("error")
                            val msg = errorObj?.optString("message") ?: "Unknown API Error"
                            _state.value = EngineState.Error(RuntimeException(msg))
                            close(RuntimeException(msg))
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse errors on partial/unhandled event types
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val errorMsg = if (response != null) {
                    "HTTP ${response.code}: ${response.message}"
                } else {
                    t?.message ?: "Unknown SSE Error"
                }
                Log.e("AnthropicEngine", "SSE Failure: $errorMsg", t)
                _state.value = EngineState.Error(RuntimeException(errorMsg, t))
                close(t ?: RuntimeException(errorMsg))
            }

            override fun onClosed(eventSource: EventSource) {
                if (_state.value is EngineState.Generating) {
                    _state.value = EngineState.Ready
                    trySend(GenerationChunk("", isLast = true))
                }
                close()
            }
        })

        awaitClose {
            eventSource.cancel()
            if (_state.value is EngineState.Generating) {
                _state.value = EngineState.Ready
            }
        }
    }
}
