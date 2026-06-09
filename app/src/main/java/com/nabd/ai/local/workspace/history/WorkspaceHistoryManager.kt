package com.nabd.ai.local.workspace.history

import com.nabd.ai.local.workspace.WorkspaceManager
import java.io.File
import java.util.UUID

class WorkspaceHistoryManager(
    private val workspaceManager: WorkspaceManager,
    private val historyDir: File
) {
    init {
        if (!historyDir.exists()) {
            historyDir.mkdirs()
        }
    }

    private val snapshots = mutableListOf<WorkspaceSnapshot>()

    fun createSnapshot(relativePath: String): WorkspaceSnapshot? {
        val targetFile = workspaceManager.resolvePath(relativePath)
        if (!targetFile.exists() || !targetFile.isFile) return null

        val snapshotId = UUID.randomUUID().toString()
        val backupFile = File(historyDir, "${snapshotId}.bak")
        
        targetFile.copyTo(backupFile, overwrite = true)
        
        val snapshot = WorkspaceSnapshot(
            snapshotId = snapshotId,
            timestamp = System.currentTimeMillis(),
            filePath = relativePath,
            backupFile = backupFile
        )
        
        snapshots.add(snapshot)
        return snapshot
    }

    fun rollback(snapshotId: String): Boolean {
        val snapshot = snapshots.find { it.snapshotId == snapshotId } ?: return false
        val targetFile = workspaceManager.resolvePath(snapshot.filePath)
        
        if (snapshot.backupFile.exists()) {
            snapshot.backupFile.copyTo(targetFile, overwrite = true)
            return true
        }
        return false
    }
}
