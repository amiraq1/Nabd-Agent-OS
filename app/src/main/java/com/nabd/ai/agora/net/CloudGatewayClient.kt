package com.nabd.ai.agora.net

import com.nabd.ai.agora.api.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.*

class CloudGatewayClient {
    private val json = Json { ignoreUnknownKeys = true }

    fun streamChatCompletion(
        prompt: String,
        provider: String,
        apiKey: String,
        contextWindow: Int
    ): Flow<String> = flow {
        val endpoint = when (provider.lowercase().trim()) {
            "openai" -> "https://api.openai.com/v1/chat/completions"
            "anthropic" -> "https://api.anthropic.com/v1/messages"
            "deepseek" -> "https://api.deepseek.com/v1/chat/completions"
            "qwen" -> "https://dashscope-intl.aliyuncs.com/compatible-mode/v1/chat/completions"
            "open router", "openrouter" -> "https://openrouter.ai/api/v1/chat/completions"
            "nvidia" -> "https://integrate.api.nvidia.com/v1/chat/completions"
            "ollama" -> "http://localhost:11434/api/chat"
            "google" -> "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:streamGenerateContent?key=$apiKey"
            else -> "https://api.openai.com/v1/chat/completions"
        }

        val headers = mutableMapOf("Content-Type" to "application/json")
        if (provider.lowercase().trim() == "anthropic") {
            headers["x-api-key"] = apiKey
            headers["anthropic-version"] = "2023-06-01"
        } else if (provider.lowercase().trim() != "google") {
            if (apiKey.isNotEmpty()) {
                headers["Authorization"] = "Bearer $apiKey"
            }
        }

        val body = buildJsonObject {
            val model = when (provider.lowercase().trim()) {
                "openai" -> "gpt-4o"
                "anthropic" -> "claude-3-5-sonnet-20240620"
                "deepseek" -> "deepseek-chat"
                "qwen" -> "qwen-max"
                "open router", "openrouter" -> "google/gemini-flash-1.5"
                "nvidia" -> "meta/llama-3.1-405b-instruct"
                "ollama" -> "llama3"
                "google" -> "gemini-pro"
                else -> "gpt-4o"
            }

            if (provider.lowercase().trim() == "google") {
                putJsonArray("contents") {
                    addJsonObject {
                        put("role", "user")
                        putJsonArray("parts") {
                            addJsonObject { put("text", prompt) }
                        }
                    }
                }
            } else if (provider.lowercase().trim() == "anthropic") {
                put("model", model)
                putJsonArray("messages") {
                    addJsonObject {
                        put("role", "user")
                        put("content", prompt)
                    }
                }
                put("max_tokens", 4096)
                put("stream", true)
            } else {
                put("model", model)
                putJsonArray("messages") {
                    addJsonObject {
                        put("role", "user")
                        put("content", prompt)
                    }
                }
                put("stream", true)
            }
        }

        val handle = try {
            HttpClient.streamPost(endpoint, body.toString(), headers)
        } catch (e: Exception) {
            emit("❌ [GATEWAY_ERROR]: Network Failure")
            return@flow
        }

        try {
            if (handle.code == 401 || handle.code == 403) {
                emit("❌ [GATEWAY_ERROR]: Unauthorized Key Configuration")
                return@flow
            }
            if (handle.code != 200) {
                emit("❌ [GATEWAY_ERROR]: Gateway Server Error (${handle.code})")
                return@flow
            }

            val source = handle.source ?: return@flow
            while (true) {
                val line = handle.readLine() ?: break
                if (line.isBlank()) continue
                if (line.startsWith(":")) continue

                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break

                    try {
                        val element = json.parseToJsonElement(data)
                        val token = when (provider.lowercase().trim()) {
                            "anthropic" -> element.jsonObject["delta"]?.jsonObject?.get("text")?.takeIf { it !is JsonNull }?.jsonPrimitive?.content
                            "google" -> element.jsonObject["candidates"]?.jsonArray?.get(0)?.jsonObject?.get("content")?.jsonObject?.get("parts")?.jsonArray?.get(0)?.jsonObject?.get("text")?.takeIf { it !is JsonNull }?.jsonPrimitive?.content
                            else -> element.jsonObject["choices"]?.jsonArray?.get(0)?.jsonObject?.get("delta")?.jsonObject?.get("content")?.takeIf { it !is JsonNull }?.jsonPrimitive?.content
                        }
                        if (token != null) emit(token)
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            emit("❌ [GATEWAY_ERROR]: Streaming Interrupted")
        } finally {
            handle.close()
        }
    }.flowOn(Dispatchers.IO)
}
