package com.nabd.ai.local.agent.tools.intelligence

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.intelligence.memory.ArchitectureMemory
import org.json.JSONObject

class SummarizeArchitectureTool(
    private val architectureMemory: ArchitectureMemory
) : ToolDefinition {
    override val name = "summarize_architecture"
    override val description = "Get a high-level summary of the project architecture and entry points."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {},
          "required": []
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        return try {
            val snapshot = architectureMemory.load()
            if (snapshot == null) {
                Result.success("Architecture memory is empty or uninitialized.")
            } else {
                val formatted = """
                    Project Architecture Summary:
                    ${snapshot.description}
                    
                    Core Modules:
                    ${snapshot.coreModules.joinToString("\n - ", prefix = " - ")}
                    
                    Entry Points:
                    ${snapshot.entryPoints.joinToString("\n - ", prefix = " - ")}
                """.trimIndent()
                Result.success(formatted)
            }
        } catch (e: Exception) {
            Result.success("Error: ${e.message}")
        }
    }
}
