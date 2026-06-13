package com.nabd.ai.local.autonomy.resources

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.app.ActivityManager
import android.os.PowerManager

/**
 * ResourceMonitor: Tracks system resources and provides safeguards for LLM execution.
 * Monitors battery, memory, thermal state, and storage.
 */
class ResourceMonitor(private val context: Context) {

    /**
     * Structured report of current system resources.
     */
    data class ResourceReport(
        val batteryPercent: Float,
        val isBatteryLow: Boolean,
        val availableRamMB: Long,
        val isMemoryLow: Boolean,
        val isThermalThrottling: Boolean,
        val availableStorageMB: Long,
        val isStorageLow: Boolean
    )
    
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

    fun getBatteryPercent(): Float {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level == -1 || scale == -1) return -1f
        return level * 100 / scale.toFloat()
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

    /**
     * Checks if the device is thermal throttling (API 29+).
     * Returns false on older devices where the API is unavailable.
     */
    fun isThermalThrottling(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val thermalStatus = powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE
            // THERMAL_STATUS_SEVERE (3) or higher means active throttling
            return thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE
        }
        return false
    }

    /**
     * Checks if internal storage is critically low (< 100MB free).
     */
    fun isStorageCriticallyLow(): Boolean {
        return getAvailableStorageMB() < 100
    }

    /**
     * Returns available internal storage in MB.
     */
    fun getAvailableStorageMB(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        return stat.availableBytes / (1024 * 1024)
    }

    /**
     * Comprehensive check: should the execution engine pause?
     */
    fun shouldPauseExecution(): Boolean {
        return isBatteryCriticallyLow() || isMemoryCriticallyLow() || isThermalThrottling()
    }

    /**
     * Generates a structured report of all monitored resources.
     */
    fun getResourceReport(): ResourceReport {
        val memInfo = getMemoryInfo()
        return ResourceReport(
            batteryPercent = getBatteryPercent(),
            isBatteryLow = isBatteryCriticallyLow(),
            availableRamMB = memInfo.availMem / (1024 * 1024),
            isMemoryLow = memInfo.lowMemory,
            isThermalThrottling = isThermalThrottling(),
            availableStorageMB = getAvailableStorageMB(),
            isStorageLow = isStorageCriticallyLow()
        )
    }
}

