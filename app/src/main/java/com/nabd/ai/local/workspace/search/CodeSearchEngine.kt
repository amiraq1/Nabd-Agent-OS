package com.nabd.ai.local.workspace.search

import com.nabd.ai.local.workspace.WorkspaceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class SearchResult(
    val relativePath: String,
    val lineNumber: Int,
    val snippet: String
)

class CodeSearchEngine(
    private val workspaceManager: WorkspaceManager
) {
    suspend fun searchContent(keyword: String, extensionFilter: String? = null): List<SearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SearchResult>()
        val root = workspaceManager.workspaceRoot

        if (!root.exists() || !root.isDirectory) return@withContext emptyList()

        root.walkTopDown()
            .filter { it.isFile && !it.isHidden }
            .filter { extensionFilter == null || it.name.endsWith(extensionFilter) }
            .forEach { file ->
                try {
                    file.useLines { lines ->
                        lines.forEachIndexed { index, line ->
                            if (line.contains(keyword, ignoreCase = true)) {
                                val relativePath = file.relativeTo(root).path
                                results.add(SearchResult(relativePath, index + 1, line.trim()))
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore files that can't be read as text
                }
            }
        
        results
    }
}
