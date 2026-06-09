package com.nabd.ai.local.agent.tools.intelligence

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.intelligence.index.SymbolIndex
import org.json.JSONObject

class FindDefinitionTool(
    private val symbolIndex: SymbolIndex
) : ToolDefinition {
    override val name = "find_definition"
    override val description = "Find the definition and file location of a specific symbol (class, function, variable)."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "symbol": { "type": "string", "description": "The exact name of the symbol." }
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
            
            val definitions = symbolIndex.findDefinition(symbol)
            if (definitions.isEmpty()) {
                Result.success("Symbol '$symbol' not found in the workspace index.")
            } else {
                val formatted = definitions.joinToString("\n") { 
                    "[${it.type}] ${it.name} in ${it.filePath}:${it.startLine}"
                }
                Result.success("Found definitions for '$symbol':\n$formatted")
            }
        } catch (e: Exception) {
            Result.success("Error: ${e.message}")
        }
    }
}
