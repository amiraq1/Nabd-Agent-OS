package com.nabd.ai.local.core.diagnostics

import android.content.Context
import android.os.PowerManager

class ThermalManager(private val context: Context) {

    fun isThermalThrottlingRequired(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        // Android 10+ API for thermal status
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val status = powerManager.currentThermalStatus
            return status >= PowerManager.THERMAL_STATUS_SEVERE
        }
        
        // Fallback for older devices (less accurate)
        return false
    }
}
