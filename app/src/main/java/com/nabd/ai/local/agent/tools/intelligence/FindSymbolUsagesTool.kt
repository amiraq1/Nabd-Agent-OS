package com.nabd.ai.local.agent.tools.intelligence

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.workspace.search.CodeSearchEngine
import org.json.JSONObject

class FindSymbolUsagesTool(
    private val searchEngine: CodeSearchEngine
) : ToolDefinition {
    override val name = "find_symbol_usages"
    override val description = "Find all usages or references of a specific symbol across the workspace."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "symbol": { "type": "string", "description": "The exact name of the symbol to search for." },
            "extension": { "type": "string", "description": "Optional file extension to filter by (e.g. '.kt')." }
          },
          "required": ["symbol"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val symbol = json.getString("symbol")
            val ext = if (json.has("extension")) json.getString("extension") else null
            
            // Using a simple regex boundary for finding usages. 
            // In a more advanced AST parser, we'd use index usages.
            val regexKeyword = "\\b$symbol\\b"
            val results = searchEngine.searchContent(regexKeyword, ext)
            
            if (results.isEmpty()) {
                Result.success("No usages found for '$symbol'.")
            } else {
                val formatted = results.take(30).joinToString("\n") { 
                    "${it.relativePath}:${it.lineNumber}: ${it.snippet}"
                }
                val suffix = if (results.size > 30) "\n...and ${results.size - 30} more usages." else ""
                Result.success("Found ${results.size} usages of '$symbol':\n$formatted$suffix")
            }
        } catch (e: Exception) {
            Result.success("Error: ${e.message}")
        }
    }
}
