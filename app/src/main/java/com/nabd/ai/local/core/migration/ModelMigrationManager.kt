package com.nabd.ai.local.core.migration

import android.content.Context
import com.nabd.ai.local.core.branding.Branding
import java.io.File

class ModelMigrationManager(private val context: Context) {
    fun migrate() {
        val legacyModels = File(context.filesDir, Branding.LEGACY_MODEL_DIRECTORY_NAME)
        val newModels = File(context.filesDir, Branding.MODEL_DIRECTORY_NAME)

        if (legacyModels.exists() && !newModels.exists()) {
            legacyModels.renameTo(newModels)
        }
    }
}
