package com.nabd.ai.local.core.recovery

import android.content.Context
import java.io.File

class RecoveryManager(private val context: Context) {

    fun executeDatabaseRecovery() {
        val dbFile = context.getDatabasePath("memory_database")
        val walFile = context.getDatabasePath("memory_database-wal")
        val shmFile = context.getDatabasePath("memory_database-shm")
        
        dbFile.delete()
        walFile.delete()
        shmFile.delete()
    }

    fun executeWorkspaceRecovery() {
        val workspaceDir = File(context.filesDir, "workspace")
        if (workspaceDir.exists()) {
            workspaceDir.deleteRecursively()
        }
        workspaceDir.mkdirs()
    }

    fun executeCacheClear() {
        context.cacheDir.deleteRecursively()
    }
}
