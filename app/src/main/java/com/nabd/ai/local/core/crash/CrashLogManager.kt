package com.nabd.ai.local.core.crash

import android.content.Context
import java.io.File

class CrashLogManager(private val context: Context) {
    
    fun getCrashLogs(): List<File> {
        val crashesDir = File(context.filesDir, "crashes")
        if (!crashesDir.exists()) return emptyList()
        return crashesDir.listFiles()?.toList() ?: emptyList()
    }
    
    fun clearCrashLogs() {
        val crashesDir = File(context.filesDir, "crashes")
        if (crashesDir.exists()) {
            crashesDir.deleteRecursively()
        }
    }
}
