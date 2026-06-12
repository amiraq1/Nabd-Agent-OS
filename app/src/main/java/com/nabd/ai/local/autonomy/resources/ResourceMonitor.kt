package com.nabd.ai.local.autonomy.resources

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.app.ActivityManager

/**
 * ResourceMonitor: Tracks system resources and provides safeguards for LLM execution.
 */
class ResourceMonitor(private val context: Context) {
    
    fun isBatteryCriticallyLow(): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        if (level == -1 || scale == -1) return false
        
        val batteryPct = level * 100 / scale.toFloat()
        return batteryPct < 15.0f
    }

    fun isMemoryCriticallyLow(): Boolean {
        val memoryInfo = getMemoryInfo()
        return memoryInfo.lowMemory
    }

    fun getMemoryInfo(): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    fun getRamUsageBytes(): Long {
        val info = getMemoryInfo()
        return info.totalMem - info.availMem
    }

    fun shouldPauseExecution(): Boolean {
        return isBatteryCriticallyLow() || isMemoryCriticallyLow()
    }
}
