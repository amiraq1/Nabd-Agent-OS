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

class EditFileTool(
    private val workspaceManager: WorkspaceManager,
    private val historyManager: WorkspaceHistoryManager,
    private val observer: WorkspaceObserver
) : ToolDefinition {
    override val name = "edit_file"
    override val description = "Edit an existing file by replacing a specific string."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "path": { "type": "string", "description": "Relative path to the file." },
            "old_string": { "type": "string", "description": "The exact literal text to replace." },
            "new_string": { "type": "string", "description": "The exact literal text to insert." }
          },
          "required": ["path", "old_string", "new_string"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.MODERATE
    override val requiresHumanApproval = true

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val path = json.getString("path")
            val oldString = json.getString("old_string")
            val newString = json.getString("new_string")
            
            val file = workspaceManager.resolvePath(path)
            if (!file.exists()) {
                return Result.success("Error: File not found at $path")
            }
            
            val currentContent = file.readText()
            if (!currentContent.contains(oldString)) {
                return Result.success("Error: The exact old_string was not found in the file.")
            }
            
            val occurrences = currentContent.split(oldString).size - 1
            if (occurrences > 1) {
                return Result.success("Error: Ambiguous replacement. old_string occurs $occurrences times.")
            }

            // Snapshot for rollback
            historyManager.createSnapshot(path)

            val updatedContent = currentContent.replace(oldString, newString)
            file.writeText(updatedContent)
            
            observer.notifyEvent(WorkspaceEventType.MODIFIED, path)
            
            Result.success("File edited successfully at $path")
        } catch (e: SecurityException) {
            Result.success("Error: ${e.message}")
        } catch (e: Exception) {
            Result.success("Error: Failed to edit file. ${e.message}")
        }
    }
}
