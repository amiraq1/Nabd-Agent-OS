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

/**
 * GeminiEngine: Implementation of [LlmProvider] for Google Gemini APIs.
 */
class GeminiEngine(
    private val secureKeyManager: SecureKeyManager
) : LlmProvider {

    override val id: String = "gemini"
    override val name: String = "Gemini"
    override val isLocal: Boolean = false

    private val _state = MutableStateFlow<EngineState>(EngineState.Uninitialized)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private var client: OkHttpClient? = null
    private var modelId: String = "gemini-1.5-flash"

    override suspend fun initialize(config: ProviderConfig) {
        if (config !is ProviderConfig.Cloud) {
            _state.value = EngineState.Error(IllegalArgumentException("GeminiEngine requires ProviderConfig.Cloud"))
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
        
        val contents = JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", request.prompt)
                    })
                })
            })
        }
        
        val requestBodyJson = JSONObject().apply {
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", request.temperature)
                put("maxOutputTokens", request.maxTokens)
            })
        }

        val httpRequest = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$modelId:streamGenerateContent?alt=sse")
            .header("Accept", "text/event-stream")
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        val factory = EventSources.createFactory(currentClient)
        
        val eventSource = factory.newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val json = JSONObject(data)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            if (text.isNotEmpty()) {
                                trySend(GenerationChunk(text))
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors on partial chunks
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                _state.value = EngineState.Error(t ?: RuntimeException("Unknown SSE Error"))
                close(t)
            }

            override fun onClosed(eventSource: EventSource) {
                _state.value = EngineState.Ready
                trySend(GenerationChunk("", isLast = true))
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
