package com.nabd.ai.local.agent.tools.filesystem

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.workspace.WorkspaceManager
import org.json.JSONObject

class ListDirectoryTool(
    private val workspaceManager: WorkspaceManager
) : ToolDefinition {
    override val name = "list_directory"
    override val description = "List files and folders in a workspace directory."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "path": { "type": "string", "description": "Relative path to the directory (use '.' for root)." }
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
            
            val dir = if (path == "." || path.isEmpty()) {
                workspaceManager.workspaceRoot
            } else {
                workspaceManager.resolvePath(path)
            }
            
            if (!dir.exists() || !dir.isDirectory) {
                return Result.success("Error: Directory not found at $path")
            }
            
            val contents = dir.listFiles()?.joinToString("\n") { 
                val type = if (it.isDirectory) "[DIR] " else "[FILE]"
                "$type ${it.name}"
            } ?: "Empty directory"
            
            Result.success(contents)
        } catch (e: SecurityException) {
            Result.success("Error: ${e.message}")
        } catch (e: Exception) {
            Result.success("Error: Failed to list directory. ${e.message}")
        }
    }
}
