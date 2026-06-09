package com.nabd.ai.local.agent.parser

import org.json.JSONException
import org.json.JSONObject

/**
 * ToolCallParser: Deterministic JSON parser for structured LLM generation.
 * Expects the output to be purely valid JSON conforming to the requested schema,
 * enforced at runtime via llama.cpp GBNF grammar.
 */
class ToolCallParser {

    fun parse(text: String): ToolCallResult {
        // Strip any potential markdown code blocks that the model might incorrectly emit
        // despite grammar, though a strict grammar prevents this.
        val cleanText = text.replace("```json", "").replace("```", "").trim()
        
        return try {
            val json = JSONObject(cleanText)
            
            if (!json.has("tool")) {
                return ToolCallResult.Error("Missing 'tool' field in JSON output.")
            }
            if (!json.has("parameters")) {
                return ToolCallResult.Error("Missing 'parameters' field in JSON output.")
            }
            
            val toolName = json.getString("tool")
            val paramsObj = json.getJSONObject("parameters")
            
            val paramsMap = mutableMapOf<String, Any>()
            val keys = paramsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                paramsMap[key] = paramsObj.get(key)
            }
            
            ToolCallResult.Success(ToolCall(toolName, paramsMap))
        } catch (e: JSONException) {
            ToolCallResult.Error("Failed to parse valid JSON. Reason: ${e.message}. Raw Output: ${cleanText.take(100)}")
        }
    }
}
