package com.nabd.ai.local.workspace.history

import java.io.File

data class WorkspaceSnapshot(
    val snapshotId: String,
    val timestamp: Long,
    val filePath: String,
    val backupFile: File
)
