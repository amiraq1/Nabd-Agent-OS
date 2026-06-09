package com.nabd.ai.local.agent.tools.intelligence

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.intelligence.refactor.RefactoringPlanner
import com.nabd.ai.local.workspace.WorkspaceManager
import com.nabd.ai.local.workspace.WorkspaceObserver
import com.nabd.ai.local.workspace.WorkspaceEventType
import com.nabd.ai.local.workspace.history.WorkspaceHistoryManager
import org.json.JSONObject

class CrossFileRefactorTool(
    private val refactoringPlanner: RefactoringPlanner,
    private val workspaceManager: WorkspaceManager,
    private val historyManager: WorkspaceHistoryManager,
    private val observer: WorkspaceObserver
) : ToolDefinition {
    override val name = "cross_file_refactor"
    override val description = "Plan and execute a multi-file refactor (e.g. rename a class or function across the entire workspace)."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "old_symbol": { "type": "string", "description": "The exact name of the symbol to rename." },
            "new_symbol": { "type": "string", "description": "The new name for the symbol." }
          },
          "required": ["old_symbol", "new_symbol"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.HIGH
    override val requiresHumanApproval = true

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val oldSymbol = json.getString("old_symbol")
            val newSymbol = json.getString("new_symbol")

            val transaction = refactoringPlanner.planRename(oldSymbol, newSymbol)
            
            if (transaction.affectedFiles.isEmpty()) {
                return Result.success("No files were affected. Refactoring aborted.")
            }

            // Snapshot all affected files before modifying
            transaction.affectedFiles.forEach { filePath ->
                historyManager.createSnapshot(filePath)
            }

            // Apply patches
            transaction.patches.forEach { patch ->
                val file = workspaceManager.resolvePath(patch.relativePath)
                // In a real system we apply the diff/patch natively. For this milestone,
                // we assumed RefactoringPlanner does the string replace directly. 
                // Wait, RefactoringPlanner currently just returns patches but we didn't 
                // expose the final string. Let's do a simple regex replace here again since 
                // the planner already confirmed the files.
                val content = file.readText()
                val updatedContent = content.replace(Regex("\\b$oldSymbol\\b"), newSymbol)
                file.writeText(updatedContent)
                observer.notifyEvent(WorkspaceEventType.MODIFIED, patch.relativePath)
            }

            Result.success("Refactored '$oldSymbol' to '$newSymbol' across ${transaction.affectedFiles.size} files successfully.")
        } catch (e: SecurityException) {
            Result.success("Error: ${e.message}")
        } catch (e: Exception) {
            Result.success("Error: Refactoring failed. ${e.message}")
        }
    }

    fun generatePreview(params: String): String {
        return try {
            val json = JSONObject(params)
            val oldSymbol = json.getString("old_symbol")
            val newSymbol = json.getString("new_symbol")
            
            // In a real suspending UI, this preview generation might need to be async.
            // For now, we simulate a synchronous preview string generation.
            "Agent requested to rename '$oldSymbol' to '$newSymbol'. This is a multi-file transaction."
        } catch (e: Exception) {
            "Could not generate preview."
        }
    }
}
