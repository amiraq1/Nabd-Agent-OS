package com.nabd.ai.local.core.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationManager {
    
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Future schema migrations for memory/knowledge databases
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
             // Future schema migrations for memory/knowledge databases
        }
    }
    
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}
