package com.nabd.ai.local.core.crash

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashReporter(private val context: Context) {
    
    fun init() {
        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            handleUncaughtException(thread, exception)
            defaultUncaughtExceptionHandler?.uncaughtException(thread, exception)
        }
    }

    private fun handleUncaughtException(thread: Thread, exception: Throwable) {
        val crashesDir = File(context.filesDir, "crashes")
        if (!crashesDir.exists()) crashesDir.mkdirs()

        val crashFile = File(crashesDir, "crash_${System.currentTimeMillis()}.log")
        
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        
        val report = """
            --- Nabd OS Crash Report ---
            Thread: ${thread.name}
            Timestamp: ${System.currentTimeMillis()}
            
            Exception:
            ${sw.toString()}
        """.trimIndent()
        
        crashFile.writeText(report)
    }
}
