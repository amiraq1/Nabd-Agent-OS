package com.nabd.ai.local.intelligence.refactor

import com.nabd.ai.local.intelligence.index.SymbolIndex
import com.nabd.ai.local.workspace.WorkspaceManager
import com.nabd.ai.local.workspace.diff.DiffGenerator
import com.nabd.ai.local.workspace.diff.FilePatch
import com.nabd.ai.local.workspace.search.CodeSearchEngine
import java.util.UUID

class RefactoringPlanner(
    private val workspaceManager: WorkspaceManager,
    private val searchEngine: CodeSearchEngine,
    private val symbolIndex: SymbolIndex
) {
    suspend fun planRename(oldSymbolName: String, newSymbolName: String): RefactoringTransaction {
        // Find definition
        val definitions = symbolIndex.findDefinition(oldSymbolName)
        val filesToModify = mutableMapOf<String, String>() // filePath -> newContent
        
        // 1. Rename definitions (assuming simple string replace for this milestone)
        for (def in definitions) {
            val file = workspaceManager.resolvePath(def.filePath)
            if (file.exists()) {
                val content = filesToModify[def.filePath] ?: file.readText()
                filesToModify[def.filePath] = content.replaceFirst("class $oldSymbolName", "class $newSymbolName")
                    .replaceFirst("fun $oldSymbolName", "fun $newSymbolName")
                    .replaceFirst("val $oldSymbolName", "val $newSymbolName")
                    .replaceFirst("var $oldSymbolName", "var $newSymbolName")
            }
        }

        // 2. Find and rename usages
        val usages = searchEngine.searchContent("\\b$oldSymbolName\\b")
        for (usage in usages) {
            val file = workspaceManager.resolvePath(usage.relativePath)
            if (file.exists()) {
                val content = filesToModify[usage.relativePath] ?: file.readText()
                // Simple regex replace for whole word boundary
                val updatedContent = content.replace(Regex("\\b$oldSymbolName\\b"), newSymbolName)
                filesToModify[usage.relativePath] = updatedContent
            }
        }

        val patches = mutableListOf<FilePatch>()
        filesToModify.forEach { (filePath, newContent) ->
            val oldContent = workspaceManager.resolvePath(filePath).readText()
            if (oldContent != newContent) {
                patches.add(DiffGenerator.generateDiff(oldContent, newContent, filePath))
            }
        }

        return RefactoringTransaction(
            id = UUID.randomUUID().toString(),
            description = "Rename symbol '$oldSymbolName' to '$newSymbolName'",
            affectedFiles = patches.map { it.relativePath },
            patches = patches
        )
    }
}
