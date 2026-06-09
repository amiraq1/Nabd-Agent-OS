package com.nabd.ai.local.core.migration

import android.content.Context
import com.nabd.ai.local.core.branding.Branding
import java.io.File

data class MigrationReport(
    val success: Boolean,
    val logs: List<String>
)

class MigrationValidator(private val context: Context) {
    fun validate(): MigrationReport {
        val logs = mutableListOf<String>()
        var success = true

        val newDb = context.getDatabasePath(Branding.DATABASE_NAME)
        if (context.getDatabasePath(Branding.LEGACY_DATABASE_NAME).exists() && !newDb.exists()) {
            success = false
            logs.add("Database migration failed.")
        }

        val newWorkspace = File(context.filesDir, Branding.WORKSPACE_DIR_NAME)
        if (File(context.filesDir, Branding.LEGACY_WORKSPACE_DIR_NAME).exists() && !newWorkspace.exists()) {
            success = false
            logs.add("Workspace migration failed.")
        }

        val newModels = File(context.filesDir, Branding.MODEL_DIRECTORY_NAME)
        if (File(context.filesDir, Branding.LEGACY_MODEL_DIRECTORY_NAME).exists() && !newModels.exists()) {
            success = false
            logs.add("Models migration failed.")
        }
        
        try {
            System.loadLibrary("nabd_engine")
            logs.add("JNI loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            success = false
            logs.add("JNI loading failed: ${e.message}")
        }

        if (success) {
            logs.add("All migrations verified successfully.")
        }

        return MigrationReport(success, logs)
    }
}
