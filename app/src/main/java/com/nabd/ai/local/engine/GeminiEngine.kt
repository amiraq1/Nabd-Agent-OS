package com.nabd.ai.local.engine

import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeminiEngine(private val apiKey: String) : LlmProvider {

    private val _state = MutableStateFlow<EngineState>(EngineState.Unloaded)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var currentEventSource: EventSource? = null

    override suspend fun loadModel(modelPath: String) {
        _state.value = EngineState.Loading
        _state.value = EngineState.Loaded
    }

    override fun generateText(prompt: String, grammar: String?): Flow<String> = callbackFlow {
        _state.value = EngineState.Generating
        
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        
        // Gemini API structure
        val contents = JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                })
            })
        }
        
        val requestBodyJson = JSONObject().apply {
            put("contents", contents)
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:streamGenerateContent?alt=sse&key=$apiKey")
            .header("Accept", "text/event-stream")
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        val factory = EventSources.createFactory(client)
        
        currentEventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val json = JSONObject(data)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            if (text.isNotEmpty()) {
                                trySend(text)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors on partial chunks
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
