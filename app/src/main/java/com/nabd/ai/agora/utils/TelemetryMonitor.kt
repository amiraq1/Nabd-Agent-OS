package com.nabd.ai.agora.utils

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.roundToInt

data class RamSnapshot(
    val usedGib: Float,
    val totalGib: Float,
    val usagePercent: Int
)

object TelemetryMonitor {

    fun getMemoryMetrics(context: Context): RamSnapshot {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val totalBytes = memInfo.totalMem.toFloat()
        val availBytes = memInfo.availMem.toFloat()
        val usedBytes  = totalBytes - availBytes

        val gibFactor = 1024f * 1024f * 1024f
        val totalGib  = totalBytes / gibFactor
        val usedGib   = usedBytes / gibFactor
        
        val usagePercent = (usedGib / totalGib * 100).roundToInt().coerceIn(0, 100)

        return RamSnapshot(usedGib, totalGib, usagePercent)
    }

    fun memoryTickerFlow(context: Context, intervalMs: Long = 1_000L): Flow<RamSnapshot> {
        return flow {
            while (true) {
                emit(getMemoryMetrics(context))
                delay(intervalMs)
            }
        }.flowOn(Dispatchers.Default)
    }
}
