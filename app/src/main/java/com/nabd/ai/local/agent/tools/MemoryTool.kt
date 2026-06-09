package com.nabd.ai.local.agent.tools

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.memory.MemoryManager
import com.nabd.ai.local.memory.MemoryType
import com.nabd.ai.local.memory.SemanticMemoryRetriever
import org.json.JSONObject

/**
 * MemoryTool: Allows the Agent to autonomously remember, search, and forget information.
 */
class MemoryTool(
    private val memoryManager: MemoryManager,
    private val semanticRetriever: SemanticMemoryRetriever? = null
) : ToolDefinition {
    override val name = "memory_management"
    override val description = "Manage long-term memory. Actions: remember, search, semantic_search, forget."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "action": { "type": "string", "enum": ["remember", "search", "semantic_search", "forget"] },
            "content": { "type": "string", "description": "The fact or information to remember or search for." },
            "type": { "type": "string", "enum": ["USER_FACT", "PREFERENCE", "TASK", "SUMMARY", "OBSERVATION", "KNOWLEDGE"] },
            "importance": { "type": "integer", "minimum": 1, "maximum": 10 },
            "id": { "type": "string", "description": "ID of the memory to forget." }
          },
          "required": ["action"]
        }
    """.trimIndent()
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        val json = JSONObject(params)
        val action = json.getString("action")

        return when (action) {
            "remember" -> {
                val content = json.getString("content")
                val typeStr = json.optString("type", "USER_FACT")
                val importance = json.optInt("importance", 5)
                val type = MemoryType.valueOf(typeStr)
                memoryManager.storeMemory(type, content, importance)
                Result.success("Memory stored successfully.")
            }
            "search" -> {
                val content = json.getString("content")
                val results = memoryManager.searchMemories(content)
                val response = results.joinToString("\n") { "[${it.id}] ${it.content}" }
                Result.success(if (response.isNotEmpty()) "Search results:\n$response" else "No memories found.")
            }
            "semantic_search" -> {
                if (semanticRetriever == null) return Result.failure(Exception("Semantic search not enabled."))
                val content = json.getString("content")
                val results = semanticRetriever.retrieveSemanticContext(content)
                Result.success(if (results.isNotEmpty()) "Semantic Search results:\n$results" else "No relevant memories found.")
            }
            "forget" -> {
                val id = json.getString("id")
                memoryManager.deleteMemory(id)
                Result.success("Memory deleted.")
            }
            else -> Result.failure(Exception("Unknown memory action: $action"))
        }
    }
}
