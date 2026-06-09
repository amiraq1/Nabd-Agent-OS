package com.nabd.ai.local.agent.tools.filesystem

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.workspace.WorkspaceManager
import com.nabd.ai.local.workspace.WorkspaceObserver
import com.nabd.ai.local.workspace.WorkspaceEventType
import com.nabd.ai.local.workspace.history.WorkspaceHistoryManager
import org.json.JSONObject

class DeleteFileTool(
    private val workspaceManager: WorkspaceManager,
    private val historyManager: WorkspaceHistoryManager,
    private val observer: WorkspaceObserver
) : ToolDefinition {
    override val name = "delete_file"
    override val description = "Delete a file from the workspace."
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
    override val riskLevel = ToolRiskLevel.HIGH
    override val requiresHumanApproval = true

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val path = json.getString("path")
            
            val file = workspaceManager.resolvePath(path)
            if (!file.exists()) {
                return Result.success("Error: File not found at $path")
            }
            if (file.isDirectory) {
                 return Result.success("Error: Cannot delete directory. Must be a file.")
            }
            
            // Snapshot for rollback before deleting
            historyManager.createSnapshot(path)
            
            val deleted = file.delete()
            if (deleted) {
                observer.notifyEvent(WorkspaceEventType.DELETED, path)
                Result.success("File deleted successfully at $path")
            } else {
                Result.success("Error: Could not delete file.")
            }
        } catch (e: SecurityException) {
            Result.success("Error: ${e.message}")
        } catch (e: Exception) {
            Result.success("Error: Failed to delete file. ${e.message}")
        }
    }
}
