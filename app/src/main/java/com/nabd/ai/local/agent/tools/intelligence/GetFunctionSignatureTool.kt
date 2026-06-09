package com.nabd.ai.local.agent.tools.intelligence

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.intelligence.index.SymbolIndex
import org.json.JSONObject

class GetFunctionSignatureTool(
    private val symbolIndex: SymbolIndex
) : ToolDefinition {
    override val name = "get_function_signature"
    override val description = "Retrieve the full signature/snippet of a function or class definition."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "name": { "type": "string", "description": "The exact name of the function or class." }
          },
          "required": ["name"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val name = json.getString("name")
            
            val definitions = symbolIndex.findDefinition(name)
            if (definitions.isEmpty()) {
                Result.success("Symbol '$name' not found.")
            } else {
                val formatted = definitions.joinToString("\n\n") { 
                    "Location: ${it.filePath}:${it.startLine}\nSignature: ${it.snippet}"
                }
                Result.success(formatted)
            }
        } catch (e: Exception) {
            Result.success("Error: ${e.message}")
        }
    }
}
