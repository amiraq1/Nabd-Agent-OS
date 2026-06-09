package com.nabd.ai.local.agent.tools.filesystem

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.workspace.WorkspaceManager
import com.nabd.ai.local.workspace.WorkspaceObserver
import com.nabd.ai.local.workspace.WorkspaceEventType
import com.nabd.ai.local.workspace.diff.DiffGenerator
import com.nabd.ai.local.workspace.history.WorkspaceHistoryManager
import org.json.JSONObject

class WriteFileTool(
    private val workspaceManager: WorkspaceManager,
    private val historyManager: WorkspaceHistoryManager,
    private val observer: WorkspaceObserver
) : ToolDefinition {
    override val name = "write_file"
    override val description = "Create a new file or completely overwrite an existing one."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "path": { "type": "string", "description": "Relative path to the file." },
            "content": { "type": "string", "description": "The complete new content of the file." }
          },
          "required": ["path", "content"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.MODERATE
    override val requiresHumanApproval = true

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val path = json.getString("path")
            val content = json.getString("content")
            
            val file = workspaceManager.resolvePath(path)
            
            val isModification = file.exists()
            if (isModification) {
                // Snapshot for rollback
                historyManager.createSnapshot(path)
            } else {
                file.parentFile?.mkdirs()
            }
            
            // Atomic write approach could be used here. For now, simple writeText.
            file.writeText(content)
            
            val eventType = if (isModification) WorkspaceEventType.MODIFIED else WorkspaceEventType.CREATED
            observer.notifyEvent(eventType, path)
            
            Result.success("File written successfully at $path")
        } catch (e: SecurityException) {
            Result.success("Error: ${e.message}")
        } catch (e: Exception) {
            Result.success("Error: Failed to write file. ${e.message}")
        }
    }

    // Helper for approval preview
    fun generatePreview(params: String): String {
        return try {
            val json = JSONObject(params)
            val path = json.getString("path")
            val newContent = json.getString("content")
            
            val file = workspaceManager.resolvePath(path)
            val oldContent = if (file.exists()) file.readText() else ""
            
            DiffGenerator.generateDiff(oldContent, newContent, path).unifiedDiff
        } catch (e: Exception) {
            "Could not generate diff preview."
        }
    }
}
