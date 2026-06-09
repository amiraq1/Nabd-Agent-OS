package com.nabd.ai.local.agent.tools.filesystem

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.workspace.search.CodeSearchEngine
import org.json.JSONObject

class SearchFilesTool(
    private val searchEngine: CodeSearchEngine
) : ToolDefinition {
    override val name = "search_code"
    override val description = "Search for a keyword or symbol across all files in the workspace."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "keyword": { "type": "string", "description": "The exact string or symbol to search for." },
            "extension": { "type": "string", "description": "Optional file extension filter (e.g., '.kt')." }
          },
          "required": ["keyword"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val keyword = json.getString("keyword")
            val ext = if (json.has("extension")) json.getString("extension") else null
            
            val results = searchEngine.searchContent(keyword, ext)
            
            if (results.isEmpty()) {
                Result.success("No matches found for '$keyword'")
            } else {
                val formatted = results.take(50).joinToString("\n") { 
                    "${it.relativePath}:${it.lineNumber}: ${it.snippet}"
                }
                val suffix = if (results.size > 50) "\n...and ${results.size - 50} more matches." else ""
                Result.success("Found ${results.size} matches:\n$formatted$suffix")
            }
        } catch (e: Exception) {
            Result.success("Error: Search failed. ${e.message}")
        }
    }
}
