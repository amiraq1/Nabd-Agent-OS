package com.nabd.ai.local.autonomy.metrics

import android.util.Log
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * ExecutionMetrics: Captures and reports performance telemetry for autonomous agent execution.
 */
class ExecutionMetrics {
    private val stepDurations = ConcurrentHashMap<UUID, Long>()
    private var planStartTime: Long = 0

    /**
     * Records the start time of an execution plan.
     */
    fun trackPlanStart() {
        planStartTime = System.currentTimeMillis()
        Log.i("NabdAgentOS_Telemetry", "Plan execution started at: $planStartTime")
    }

    /**
     * Records the execution duration of a single step.
     */
    fun trackStepDuration(stepId: UUID, durationMs: Long) {
        stepDurations[stepId] = durationMs
        Log.d("NabdAgentOS_Telemetry", "Step: $stepId completed in: ${durationMs}ms")
    }

    /**
     * Generates a summary report of the execution telemetry.
     */
    fun dumpTelemetryReport(): String {
        val totalDuration = if (planStartTime > 0) System.currentTimeMillis() - planStartTime else 0
        val avgLatency = if (stepDurations.isNotEmpty()) stepDurations.values.average() else 0.0
        
        return """
            === NABD AUTONOMY TELEMETRY ===
            Total Plan Execution Time: ${totalDuration}ms
            Total Steps Processed: ${stepDurations.size}
            Average Step Latency: ${avgLatency}ms
            ================================
        """.trimIndent()
    }
}
