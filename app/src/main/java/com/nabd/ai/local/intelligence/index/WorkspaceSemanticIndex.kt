package com.nabd.ai.local.intelligence.index

import com.nabd.ai.local.intelligence.parser.SemanticParser
import com.nabd.ai.local.workspace.WorkspaceManager
import com.nabd.ai.local.workspace.WorkspaceObserver
import com.nabd.ai.local.workspace.WorkspaceEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class WorkspaceSemanticIndex(
    private val workspaceManager: WorkspaceManager,
    private val parsers: List<SemanticParser>,
    private val symbolIndex: SymbolIndex,
    private val observer: WorkspaceObserver,
    private val scope: CoroutineScope
) {
    init {
        scope.launch(Dispatchers.IO) {
            observer.events.collect { event ->
                handleWorkspaceEvent(event.type, event.relativePath)
            }
        }
    }

    fun buildFullIndex() {
        val root = workspaceManager.workspaceRoot
        if (!root.exists()) return

        symbolIndex.clear()
        root.walkTopDown()
            .filter { it.isFile && !it.isHidden }
            .forEach { file ->
                indexFile(file.relativeTo(root).path)
            }
    }

    private fun handleWorkspaceEvent(type: WorkspaceEventType, relativePath: String) {
        when (type) {
            WorkspaceEventType.CREATED, WorkspaceEventType.MODIFIED -> indexFile(relativePath)
            WorkspaceEventType.DELETED -> symbolIndex.removeFile(relativePath)
            WorkspaceEventType.RENAMED -> {
                // Renames might be handled differently depending on the OS event mapping,
                // but generally it's a delete + create or explicit path update.
                // Assuming relativePath here is the new path, we might need a two-path event.
            }
        }
    }

    private fun indexFile(relativePath: String) {
        val file = workspaceManager.resolvePath(relativePath)
        if (!file.exists() || !file.isFile) {
            symbolIndex.removeFile(relativePath)
            return
        }

        val extension = file.extension
        val parser = parsers.find { it.supportedExtensions.contains(extension) } ?: return

        try {
            val content = file.readText()
            val symbols = parser.parse(content, relativePath)
            symbolIndex.addSymbols(relativePath, symbols)
        } catch (e: Exception) {
            // Ignore unreadable files or parse errors
        }
    }
}
