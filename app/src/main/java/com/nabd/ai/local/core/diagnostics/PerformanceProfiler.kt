package com.nabd.ai.local.core.diagnostics

import android.os.SystemClock

class PerformanceProfiler {
    private var inferenceStartTime: Long = 0
    private var tokenCount: Int = 0

    fun startInference() {
        inferenceStartTime = SystemClock.elapsedRealtime()
        tokenCount = 0
    }

    fun recordToken() {
        tokenCount++
    }

    fun stopInference(): Double {
        val durationMs = SystemClock.elapsedRealtime() - inferenceStartTime
        if (durationMs == 0L) return 0.0
        val tokensPerSecond = (tokenCount.toDouble() / durationMs) * 1000.0
        return tokensPerSecond
    }
}
