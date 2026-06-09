package com.nabd.ai.local.core.migration

import android.content.Context
import com.nabd.ai.local.core.branding.Branding
import java.io.File

class DatabaseMigrationManager(private val context: Context) {
    fun migrate() {
        val legacyDb = context.getDatabasePath(Branding.LEGACY_DATABASE_NAME)
        val legacyWal = context.getDatabasePath("${Branding.LEGACY_DATABASE_NAME}-wal")
        val legacyShm = context.getDatabasePath("${Branding.LEGACY_DATABASE_NAME}-shm")
        
        val newDb = context.getDatabasePath(Branding.DATABASE_NAME)
        val newWal = context.getDatabasePath("${Branding.DATABASE_NAME}-wal")
        val newShm = context.getDatabasePath("${Branding.DATABASE_NAME}-shm")

        if (legacyDb.exists() && !newDb.exists()) {
            legacyDb.renameTo(newDb)
            if (legacyWal.exists()) legacyWal.renameTo(newWal)
            if (legacyShm.exists()) legacyShm.renameTo(newShm)
        }
    }
}
