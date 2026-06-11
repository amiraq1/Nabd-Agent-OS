package com.nabd.ai.local.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OpenAIEngine(private val apiKey: String) : LlmProvider {

    private val _state = MutableStateFlow<EngineState>(EngineState.Unloaded)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Important for SSE
        .build()

    private var currentEventSource: EventSource? = null

    override suspend fun loadModel(modelPath: String) {
        _state.value = EngineState.Loading
        // No local model to load, just simulating readiness
        _state.value = EngineState.Loaded
    }

    override fun generateText(prompt: String, grammar: String?): Flow<String> = callbackFlow {
        _state.value = EngineState.Generating
        
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }
        
        val requestBodyJson = JSONObject().apply {
            put("model", "gpt-4o-mini") // Or configurable
            put("messages", messages)
            put("stream", true)
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "text/event-stream")
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        val factory = EventSources.createFactory(client)
        
        currentEventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    _state.value = EngineState.Loaded
                    close()
                    return
                }
                try {
                    val json = JSONObject(data)
                    val choices = json.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                        val content = delta?.optString("content", "") ?: ""
                        if (content.isNotEmpty()) {
                            trySend(content)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse errors on partial chunks
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                _state.value = EngineState.Failed(t?.message ?: "Unknown SSE Error")
                close(t)
            }

            override fun onClosed(eventSource: EventSource) {
                _state.value = EngineState.Loaded
                close()
            }
        })

        awaitClose {
            currentEventSource?.cancel()
            _state.value = EngineState.Loaded
        }
    }

    override fun stopGeneration() {
        _state.value = EngineState.Stopping
        currentEventSource?.cancel()
        _state.value = EngineState.Loaded
    }

    override fun unloadModel() {
        _state.value = EngineState.Unloaded
    }
}
