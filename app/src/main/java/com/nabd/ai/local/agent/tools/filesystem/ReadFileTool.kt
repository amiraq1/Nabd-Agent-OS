package com.nabd.ai.local.agent.tools.filesystem

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.workspace.WorkspaceManager
import org.json.JSONObject

class ReadFileTool(
    private val workspaceManager: WorkspaceManager
) : ToolDefinition {
    override val name = "read_file"
    override val description = "Read the contents of a file in the workspace."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "path": { "type": "string", "description": "Relative path to the file." }
          },
          "required": ["path"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val path = json.getString("path")
            
            val file = workspaceManager.resolvePath(path)
            if (!file.exists() || !file.isFile) {
                return Result.success("Error: File not found at $path")
            }
            
            Result.success(file.readText())
        } catch (e: SecurityException) {
            Result.success("Error: ${e.message}") // Return to LLM so it knows it failed securely
        } catch (e: Exception) {
            Result.success("Error: Failed to read file. ${e.message}")
        }
    }
}
