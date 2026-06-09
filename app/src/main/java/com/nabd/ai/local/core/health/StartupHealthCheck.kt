package com.nabd.ai.local.core.health

import android.content.Context
import java.io.File

class StartupHealthCheck(private val context: Context) {
    fun runChecks(): HealthReport {
        val issues = mutableListOf<String>()

        // 1. Check workspace directory
        val workspaceDir = File(context.filesDir, "workspace")
        if (!workspaceDir.exists() && !workspaceDir.mkdirs()) {
            issues.add("Failed to create workspace directory.")
        }

        // 2. Database checks (Assuming standard Room location)
        val dbFile = context.getDatabasePath("memory_database")
        if (dbFile.exists() && dbFile.length() == 0L) {
            issues.add("Database file exists but is 0 bytes (corrupted).")
        }

        // 3. Models directory check
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }

        return HealthReport(
            isHealthy = issues.isEmpty(),
            issues = issues
        )
    }
}
