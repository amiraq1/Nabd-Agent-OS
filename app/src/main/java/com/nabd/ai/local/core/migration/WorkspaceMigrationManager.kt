package com.nabd.ai.local.core.migration

import android.content.Context
import com.nabd.ai.local.core.branding.Branding
import java.io.File

class WorkspaceMigrationManager(private val context: Context) {
    fun migrate() {
        val legacyWorkspace = File(context.filesDir, Branding.LEGACY_WORKSPACE_DIR_NAME)
        val newWorkspace = File(context.filesDir, Branding.WORKSPACE_DIR_NAME)

        if (legacyWorkspace.exists() && !newWorkspace.exists()) {
            legacyWorkspace.renameTo(newWorkspace)
        }
        
        val legacyHistory = File(context.filesDir, "${Branding.LEGACY_WORKSPACE_DIR_NAME}_history")
        val newHistory = File(context.filesDir, "${Branding.WORKSPACE_DIR_NAME}_history")
        
        if (legacyHistory.exists() && !newHistory.exists()) {
            legacyHistory.renameTo(newHistory)
        }
    }
}
